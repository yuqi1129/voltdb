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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.ServletException;
import javax.servlet.SessionTrackingMode;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google_voltpatches.common.net.HostAndPort;
import org.apache.http.entity.ContentType;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.json_voltpatches.JSONArray;
import org.json_voltpatches.JSONObject;
import org.voltcore.logging.Level;
import org.voltcore.logging.VoltLogger;
import org.voltcore.utils.CoreUtils;
import org.voltdb.VoltDB;

/**
 * This listener handles requests for server status.
 * It is similar in a broad sense to HTTPAdminListener,
 * but is separate since it needs to be started early
 * in server initialization.
 */
public class StatusListener {

    private static final VoltLogger m_log = new VoltLogger("HOST");

    private static final String JSON_CONTENT_TYPE = ContentType.APPLICATION_JSON.toString();
    private static final String HTML_CONTENT_TYPE = "text/html;charset=utf-8";

    private final int m_port;
    private final String m_resolvedIntf;
    private final String m_publicIntf;
    private final boolean m_mustListen;
    private final Server m_server;
    private final DefaultSessionIdManager m_idmanager;
    private final SessionHandler m_sessionHandler;

    private static StatusListener singleton; // xxx fix this
    private String m_hostHeader;

    public StatusListener(String intf, String publicIntf, int port, boolean mustListen) {

	final int poolsize = Integer.getInteger("STATUS_POOL_SIZE", 4);
	final int queueLim = poolsize + 4;
	final int timeout = Integer.getInteger("STATUS_REQUEST_TIMEOUT_SECONDS", 15);
	final int idleTime = Integer.getInteger("STATUS_SESSION_TIMEOUT_SECONDS", 30);
	final int maxQuery = 1024;
	final int maxKeys = 16;

	m_port = (port > 0 ? port : 8989);
	m_resolvedIntf = resolveInterface(intf, "", m_port);
	m_publicIntf = resolveInterface(publicIntf, m_resolvedIntf, m_port);
	m_mustListen = mustListen;

	QueuedThreadPool qtp = new QueuedThreadPool(poolsize, 1, timeout * 1000,
						    new LinkedBlockingQueue<>(queueLim));
	m_server = new Server(qtp);
	m_server.setAttribute("org.eclipse.jetty.server.Request.maxFormContentSize",
			      Integer.valueOf(maxQuery));

	m_sessionHandler= null;
	/**
	m_sessionHandler = new SessionHandler();
	m_sessionHandler.setMaxInactiveInterval(idleTime);
	m_sessionHandler.setServer(m_server);
	**/

	m_idmanager = null;
	/**
	m_idmanager = new DefaultSessionIdManager(m_server);
	m_idmanager.setWorkerName("status-worker");
	m_server.setSessionIdManager(m_idmanager);
	m_sessionHandler.setSessionIdManager(m_idmanager);
	**/

	ServerConnector connector = null;
	try {
	    connector = new ServerConnector(m_server);
	    if (!m_resolvedIntf.isEmpty()) {
		connector.setHost(m_resolvedIntf);
	    }
	    connector.setPort(m_port);
	    connector.setName("status-connector");
	    connector.open();
	    m_server.addConnector(connector);

	    ServletHandler handy = new ServletHandler();
	    m_server.setHandler(handy);
	    handy.addServletWithMapping(StatusServlet.class, "/status");
	    /***
	    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
	    context.setContextPath("/");
	    context.setMaxFormContentSize(maxQuery);
	    context.setMaxFormKeys(maxKeys);
	    context.getSessionHandler().getSessionCookieConfig().setHttpOnly(true);
	    context.addServlet(StatusServlet.class, "/status");
	    context.addServlet(DefaultServlet.class, "/");
	    // xxx async supported?

	    Set<SessionTrackingMode> trackModes = new HashSet<>();  // setOf ? xxx
	    trackModes.add(SessionTrackingMode.COOKIE);
	    context.getSessionHandler().setSessionTrackingModes(trackModes);
	    **/

	    singleton = this; // xxxx

	} catch (Exception ex) {
	    logWarning("Unexpected exception in init: %s", ex);
	    try { connector.close(); } catch (Exception e2) { }
	    try { m_server.destroy(); } catch (Exception e2) { }
	    throw new RuntimeException("Failed to initialize status listener", ex);
	}
    }

    public static StatusListener instance() {
	return singleton;
    }

    public void start() {
	try {
	    m_server.start();
	    logInfo("Listening for status requests on %s", m_publicIntf);
	} catch (Exception ex) {
	    logWarning("Unexpected exception in start: %s", ex);
	    try { m_server.stop(); } catch (Exception e2) { }
	    try { m_server.destroy(); } catch (Exception e2) { }
	    if (m_mustListen) {
		throw new RuntimeException("Failed to start status listener", ex);
	    }
	}
    }

    public void stop() {
	logInfo("Shutting down status listener on %s", m_publicIntf);
	try {
	    m_server.stop();
	    m_server.join();
	} catch (Exception ex) {
	    logWarning("Unexpected exception in stop: %s", ex);
	}
	try {
	    m_server.destroy();
	} catch (Exception ex) {
	    logWarning("Unexpected exception in stop: %s", ex);
	}
    }

    protected String getHostHeader() {
	if (m_hostHeader != null) {
	    return m_hostHeader;
	}
	if (!m_publicIntf.isEmpty()) {
	    m_hostHeader = m_publicIntf;
	    return m_hostHeader;
	}
	InetAddress addr = null;
	try {
	    JSONObject jsObj = new JSONObject(VoltDB.instance().getLocalMetadata());
	    JSONArray interfaces = jsObj.getJSONArray("interfaces");
	    addr = InetAddress.getByName(interfaces.getString(0)); // external interface
	} catch (Exception ex) {
	    logWarning("Failed to get HTTP interface information: %s", ex);
	}
	if (addr == null) {
	    addr = CoreUtils.getLocalAddress();
	}
	m_hostHeader = addr.getHostAddress() + ":" + m_port;
	return m_hostHeader;
    }

    private static String resolveInterface(String intf, String defIntf, int defPort) {
	String temp = (intf == null ? "" : intf.trim());
	return temp.isEmpty() ? defIntf
	    : HostAndPort.fromHost(temp).withDefaultPort(defPort).toString();
    }

    private static void doLog(Level level, String str, Object[] args) {
	if (args.length != 0) {
	    str = String.format(str, args);
	}
	m_log.log(level, str, null);
    }

    private static void logError(String str, Object... args) {
	doLog(Level.ERROR, str, args);
    }

    private static void logWarning(String str, Object... args) {
	doLog(Level.WARN, str, args);
    }

    private static void logInfo(String str, Object... args) {
	doLog(Level.INFO, str, args);
    }

    private static void logDebug(String str, Object... args) {
	// TODO: check if debug enabled
	doLog(Level.INFO, str, args); // xxx debug
    }
}
