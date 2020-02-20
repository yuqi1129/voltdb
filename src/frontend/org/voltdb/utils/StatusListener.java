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

package org.voltdb.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.servlet.ServletContextHandler;

import org.json_voltpatches.JSONArray;
import org.json_voltpatches.JSONObject;
import org.voltcore.logging.Level;
import org.voltcore.logging.VoltLogger;
import org.voltcore.utils.CoreUtils;
import org.voltdb.VoltDB;

/**
 * This listener handles requests for server status. It is similar
 * in a broad sense to HTTPAdminListener, but is separate since
 * it needs to be started early in server initialization. It is
 * also somewhat simpler.
 *
 * The listener is based on Jetty 9.4; the API documentation is at
 * https://www.eclipse.org/jetty/documentation/current/embedding-jetty.html
 */
public class StatusListener {

    private static final VoltLogger m_log = new VoltLogger("HOST");

    private static final int POOLSIZE = Integer.getInteger("STATUS_POOL_SIZE", 4);
    private static final int QUEUELIM = POOLSIZE + 4;
    private static final int CONNTMO = Integer.getInteger("STATUS_CONNECTION_TIMEOUT_SECONDS", 30) * 1000;
    private static final int REQTMO = Integer.getInteger("STATUS_REQUEST_TIMEOUT_SECONDS", 15) * 1000;
    private static final int MAXQUERY = 256;
    private static final int MAXKEYS = 2;

    private final int m_port;
    private final String m_resolvedIntf;
    private final String m_publicIntf;

    private Server m_server;
    private String m_hostHeader;
    private static StatusListener singleton;
    private static final Object lock = new Object();

    /**
     * Status listener:
     * @param intf  address of interface on which to listen for connections
     * @param port  TCP port number
     * @param publicIntf  address to be returned in 'Host' header (if different)
     */
    public StatusListener(String intf, int port, String publicIntf) {

        assert port > 0 && port <= 65535;
        m_port = port;
        m_resolvedIntf = resolveInterface(intf, "");
        m_publicIntf = resolveInterface(publicIntf, m_resolvedIntf);

        QueuedThreadPool qtp = new QueuedThreadPool(POOLSIZE, 1, REQTMO,
                                                    new LinkedBlockingQueue<>(QUEUELIM));
        Server server = new Server(qtp);
        ServerConnector connector = null;

        try {
            connector = new ServerConnector(server);
            connector.setHost(m_resolvedIntf);
            connector.setPort(m_port);
            connector.setName("status-connector");
            connector.setIdleTimeout(CONNTMO);
            server.addConnector(connector);

            ServletContextHandler ctxHandler = new ServletContextHandler();
            ctxHandler.setContextPath("/");
            ctxHandler.setMaxFormContentSize(MAXQUERY);
            ctxHandler.setMaxFormKeys(MAXKEYS);
            ctxHandler.addServlet(StatusServlet.class, "/status").setAsyncSupported(true);
            server.setHandler(ctxHandler);

            connector.open();
        }

        catch (Exception ex) {
            logWarning("StatusListener: unexpected exception in init: %s", ex);
            try { connector.close(); } catch (Exception e2) { }
            try { m_server.destroy(); } catch (Exception e2) { }
            m_server = null;
            throw new RuntimeException("Failed to initialize status listener", ex);
        }

        m_server = server;
        singleton = this;
    }

    public String displayInterface() {
        return (m_resolvedIntf.isEmpty() ? "ANY" : m_resolvedIntf) + ':' + m_port;
    }

    public static StatusListener instance() {
        return singleton;
    }

    public void start() {
        synchronized (lock) {
            if (m_server != null) { // ignore race with stop
                try {
                    m_server.start();
                    logInfo("Listening for status requests on %s", displayInterface());
                } catch (Exception ex) {
                    logWarning("StatusListener: unexpected exception from start: %s", ex);
                    safeStop();
                    throw new RuntimeException("Failed to start status listener", ex);
                }
            }
        }
    }

    public static void shutdown() {
        synchronized (lock) {
            if (singleton != null) {
                logInfo("Shutting down status listener");
                singleton.safeStop();
                singleton = null;
            }
        }
    }

    private void safeStop() { // caller is holding lock
        if (m_server != null) {
            try {
                m_server.stop();
                m_server.join();
            } catch (Exception ex) {
                logWarning("StatusListener: unexpected exception from stop/join: %s", ex);
            }
            try {
                m_server.destroy();
            } catch (Exception ex) {
                logWarning("StatusListener: unexpected exception from destroy: %s", ex);
            }
            m_server = null;
        }
    }

    protected String getHostHeader() {
        if (m_hostHeader == null) {
            String intf;
            if (!m_publicIntf.isEmpty()) {
                intf = m_publicIntf;
            } else {
                intf = getLocalAddress().getHostAddress();
                logInfo("Using %s for host header", intf);
            }
            m_hostHeader = intf + ':' + m_port;
        }
        return m_hostHeader;
    }

    private static InetAddress getLocalAddress() {
        InetAddress addr = null;
        try {
            String meta = VoltDB.instance().getLocalMetadata();
            if (meta != null && !meta.isEmpty()) {
                JSONObject jsObj = new JSONObject(meta);
                JSONArray ifList = jsObj.getJSONArray("interfaces");
                addr = InetAddress.getByName(ifList.getString(0)); // external interface
            }
        }
        catch (Exception ex) {
            logWarning("Failed to get HTTP interface information: %s", ex);
        }
        if (addr == null) {
            addr = CoreUtils.getLocalAddress();
        }
        return addr;
    }

    private static String resolveInterface(String intf, String defIntf) {
        String temp = (intf == null ? "" : intf.trim());
        return temp.isEmpty() ? defIntf : temp;
    }

    private static void doLog(Level level, String str, Object[] args) {
        if (args.length != 0) {
            str = String.format(str, args);
        }
        m_log.log(level, str, null);
    }

    private static void logWarning(String str, Object... args) {
        doLog(Level.WARN, str, args);
    }

    private static void logInfo(String str, Object... args) {
        doLog(Level.INFO, str, args);
    }
}
