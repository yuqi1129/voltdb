/* This file is part of VoltDB.
 * Copyright (C) 2008-2019 VoltDB Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package genqa.procedures;

import org.voltdb.SQLStmt;
import org.voltdb.VoltProcedure;
import org.voltdb.DeprecatedProcedureAPIAccess;

public class JiggleSkinnyExportSinglePartition extends VoltProcedure {
    public final SQLStmt export_kafka = new SQLStmt( "INSERT INTO export_skinny_partitioned_table_kafka (rowid, txnid) VALUES (?,?)");
    public final SQLStmt export_rabbit = new SQLStmt( "INSERT INTO export_skinny_partitioned_table_rabbit (rowid, txnid) VALUES (?,?)");
    public final SQLStmt export_jdbc = new SQLStmt( "INSERT INTO export_skinny_partitioned_table_jdbc (rowid, txnid) VALUES (?,?)");
    public final SQLStmt export_file = new SQLStmt( "INSERT INTO export_skinny_partitioned_table_file (rowid, txnid) VALUES (?,?)");

    public long run(long rowid, int reversed) {
        @SuppressWarnings("deprecation")
        long txnid = DeprecatedProcedureAPIAccess.getVoltPrivateRealTransactionId(this);

        voltQueueSQL(export_kafka, rowid, txnid);
        voltQueueSQL(export_rabbit, rowid, txnid);
        voltQueueSQL(export_file, rowid, txnid);
        voltQueueSQL(export_jdbc, rowid, txnid);

        // Execute last statement batch
        voltExecuteSQL(true);

        // Return to caller
        return txnid;
    }
}
