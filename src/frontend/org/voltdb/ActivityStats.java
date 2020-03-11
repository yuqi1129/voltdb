/* This file is part of VoltDB.
 * Copyright (C) 2020 VoltDB Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with VoltDB.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.voltdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.voltdb.VoltTable.ColumnInfo;
import org.voltdb.importer.ImportManager;
import org.voltdb.export.ExportManagerInterface;
import org.voltcore.logging.VoltLogger;
import org.voltcore.utils.Pair;

/**
 * The ActivityStats statistics provide a summary of current
 * cluster activity that can be used to determine when cluster
 * shutdown can safely proceed.
 *
 * The intended use is that an admin client executes the
 * two pre-shutdown procedures @PrepareShutdown and @Quiesce,
 * and then monitors '@Statistics activity-summary' until all
 * work is drained.
 *
 * The result of the statistics request is a table with one
 * row per host, and a summary of activity in various categories.
 * Activity is summarized in the 'ACTIVE' column; if desired,
 * then other columns can be used to determine what the activity
 * relates to, and perhaps whether forward progress is being made.
 */
public class ActivityStats extends StatsSource
{
    private static final VoltLogger logger = new VoltLogger("HOST");

    private enum ColumnName {
        ACTIVE, // 0 if all other gauges 0, else 1
        CLIENT_TXNS, CLIENT_REQ_BYTES, CLIENT_RESP_MSGS,
        CMDLOG_TXNS, CMDLOG_BYTES,
        IMPORTS_PENDING, EXPORTS_PENDING,
        DRPROD_SEGS, DRPROD_BYTES, DRCONS_PARTS,
    };

    public ActivityStats() {
        super(false);
    }

    /*
     * Constructs a description of the columns we'll return
     * in our stats table.
     */
    @Override
    protected void populateColumnSchema(ArrayList<ColumnInfo> columns) {
        super.populateColumnSchema(columns);
        for (ColumnName col : ColumnName.values()) {
            columns.add(new ColumnInfo(col.name(), VoltType.INTEGER));
        }
    }

    /*
     * Iterator through the rows of stats we make available. In fact
     * we have a single row, so we fake out the iterator.
     */
    @Override
    protected Iterator<Object> getStatsRowKeyIterator(boolean interval)
    {
        return new DummyIterator();
    }

    private class DummyIterator implements Iterator<Object> {
        private boolean hasNext = true;
        @Override
        public boolean hasNext() {
            return hasNext;
        }
        @Override
        public Object next() {
            Object obj = null;
            if (hasNext) {
                hasNext = false;
                obj = "THE_ROW";
            }
            return obj;
        }
    }

    /*
     * Main stats collection. We return a single row, and
     * the key is irrelevant.
     */
    @Override
    protected void updateStatsRow(Object rowKey, Object[] rowValues) {
        boolean active = false;
        try {
            active |= checkClients(rowValues);
            active |= checkCommandLog(rowValues);
            active |= checkImporter(rowValues);
            active |= checkExporter(rowValues);
            active |= checkDrProducer(rowValues);
            active |= checkDrConsumer(rowValues);
        }
        catch (Exception ex) {
            logger.warn("Unexpected exception in ActivityStats: " + ex);
        }
        setValue(rowValues, ColumnName.ACTIVE, active ? 1 : 0);
        super.updateStatsRow(rowKey, rowValues);
    }

    /*
     * Client interface.
     * Check for outstanding requests, responses, transactions.
     * Counters are all zero if nothing outstanding.
     */
    private boolean checkClients(Object[] out) {
        int reqBytes = 0, respMsgs = 0, txns = 0;
        ClientInterface ci = VoltDB.instance().getClientInterface();
        if (ci != null) {
            for (Pair<String,long[]> ent : ci.getLiveClientStats().values()) {
                long[] val = ent.getSecond(); // order of values is assumed
                reqBytes += val[1];
                respMsgs += val[2];
                txns += val[3];
            }
            logResult("client interface: outstanding txns %d, request bytes %d, responses %d",
                      txns, reqBytes, respMsgs);
        }
        setValue(out, ColumnName.CLIENT_TXNS, txns);
        setValue(out, ColumnName.CLIENT_REQ_BYTES, reqBytes);
        setValue(out, ColumnName.CLIENT_RESP_MSGS, respMsgs);
        return txns != 0 || reqBytes != 0 || respMsgs != 0;
    }

    /*
     * Command log.
     * Check for outstanding logging: bytes and transactions.
     * Counters are zero if nothing outstanding.
     */
    private boolean checkCommandLog(Object[] out) {
        int bytes = 0, txns = 0;
        CommandLog cl = VoltDB.instance().getCommandLog();
        if (cl != null) {
            long[] temp = new long[2];
            temp[0] = temp[1] = 0;
            cl.getCommandLogOutstanding(temp);
            bytes = clip(temp[0]);
            txns = clip(temp[1]);
            logResult("command log: outstanding txns %d, bytes %d", txns, bytes);
        }
        setValue(out, ColumnName.CMDLOG_TXNS, txns);
        setValue(out, ColumnName.CMDLOG_BYTES, bytes);
        return txns != 0 || bytes != 0;
    }

    /*
     * Importer.
     * Check for unprocessed import request.
     * Count is of outstanding requests across all importers,
     * and is zero if there's nothing outstanding.
     */
    private boolean checkImporter(Object[] out) {
        int pend = 0;
        ImportManager im = ImportManager.instance();
        if (im != null) {
            pend = clip(im.statsCollector().getTotalPendingCount());
            logResult("importer: %d pending", pend);
        }
        setValue(out, ColumnName.IMPORTS_PENDING, pend);
        return pend != 0;
    }

    /*
     * Exporter.
     * Check for unprocessed exports.
     * Count is of outstanding tuples across all exporters,
     * and is zero if there's nothing outstanding.
     */
    private boolean checkExporter(Object[] out) {
        int pend = 0;
        ExportManagerInterface em = ExportManagerInterface.instance();
        if (em != null) {
            pend = clip(em.getTotalPendingCount());
            logResult("exporter: %d pending", pend);
        }
        setValue(out, ColumnName.EXPORTS_PENDING, pend);
        return pend != 0;
    }

    /*
     * DR producer.
     * Returns details of outstanding data, expressed in bytes and message
     * segments, summed across all partitions. Zero when none outstanding.
     */
    private boolean checkDrProducer(Object[] out) {
        int bytesPend = 0, segsPend = 0;
        StatsAgent sa = VoltDB.instance().getStatsAgent();
        if (sa != null) {
            Set<StatsSource> sss = sa.lookupStatsSource(StatsSelector.DRPRODUCERPARTITION, 0);
            if (sss != null && !sss.isEmpty()) {
                assert sss.size() == 1;
                StatsSource ss = sss.iterator().next();
                int ix_totalBytes = getIndex(ss, DRProducerStatsBase.Columns.TOTAL_BYTES);
                int ix_lastQueued = getIndex(ss, DRProducerStatsBase.Columns.LAST_QUEUED_DRID);
                int ix_lastAcked = getIndex(ss, DRProducerStatsBase.Columns.LAST_ACK_DRID);
                long lBytesPend = 0, lSegsPend = 0;
                for (Object[] row : ss.getStatsRows(false, System.currentTimeMillis())) {
                    long totalBytes = asLong(row[ix_totalBytes]);
                    long lastQueuedDrId = asLong(row[ix_lastQueued]);
                    long lastAckedDrId = asLong(row[ix_lastAcked]);
                    lBytesPend += totalBytes;
                    if (lastQueuedDrId > lastAckedDrId) {
                        lSegsPend += lastQueuedDrId - lastAckedDrId;
                    }
                }
                bytesPend = clip(lBytesPend);
                segsPend = clip(lSegsPend);
                logResult("DR producer: outstanding segments %d, bytes %d", segsPend, bytesPend);
            }
        }
        setValue(out, ColumnName.DRPROD_SEGS, segsPend);
        setValue(out, ColumnName.DRPROD_BYTES, bytesPend);
        return segsPend != 0 || bytesPend != 0;
    }

    /*
     * DR consumer.
     * Returns count of partitions for which there is data not
     * yet successfully applied. Zero when nothing outstanding.
     */
    private boolean checkDrConsumer(Object[] out) {
        int pend = 0;
        StatsAgent sa = VoltDB.instance().getStatsAgent();
        if (sa != null) {
            Set<StatsSource> sss = sa.lookupStatsSource(StatsSelector.DRCONSUMERPARTITION, 0);
            if (sss != null && !sss.isEmpty()) {
                assert sss.size() == 1;
                StatsSource ss = sss.iterator().next();
                int ix_timeRcvd = getIndex(ss, DRConsumerStatsBase.Columns.LAST_RECEIVED_TIMESTAMP);
                int ix_timeAppl = getIndex(ss, DRConsumerStatsBase.Columns.LAST_APPLIED_TIMESTAMP);
                for (Object[] row : ss.getStatsRows(false, System.currentTimeMillis())) {
                    long timeLastRcvd = asLong(row[ix_timeRcvd]);
                    long timeLastApplied = asLong(row[ix_timeAppl]);
                    if (timeLastRcvd != timeLastApplied) {
                        pend++;
                    }
                }
                logResult("DR consumer: %d partitions with pending data", pend);
            }
        }
        setValue(out, ColumnName.DRCONS_PARTS, pend);
        return pend != 0;
    }

    /*
     * Utility to set a value in a row.
     */
    private void setValue(Object[] row, ColumnName col, int val) {
        row[columnNameToIndex.get(col.name())] = val;
    }

    /*
     * Convert object in stats row to long integer
     */
    private static long asLong(Object obj) {
        return ((Long)obj).longValue();
    }

    /*
     * Get index for a column in someone else's stats table.
     */
    private static int getIndex(StatsSource ss, String name) {
        return ss.columnNameToIndex.get(name);
    }

    /*
     * Clip long value to int. If we have more than 2 billion
     * outstanding operations, we don't really need to be exact.
     */
    private static int clip(long val) {
        return (int) Math.min(val, Integer.MAX_VALUE);
    }

    /*
     * Utility log routine with formatting.
     */
    private static void logResult(String str, Object... args) {
        logger.info("ActivityStats, " + String.format(str, args));
    }
}
