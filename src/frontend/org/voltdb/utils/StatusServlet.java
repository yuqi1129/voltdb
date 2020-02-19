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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.voltdb.VoltDB;
import org.voltdb.VoltDBInterface;

/**
 * Servlet to report status of the instance in which
 * it is executing.
 */
public class StatusServlet extends HttpServlet {

    private static final String CONTENT_TYPE = "text/plain";
    private static final String ENCODING = "utf-8";
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        response.setHeader("Host", StatusListener.instance().getHostHeader());
        response.setContentType(CONTENT_TYPE);
        response.setCharacterEncoding(ENCODING);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().print(theStatus());
    }

    private String theStatus() {
        VoltDBInterface instance = VoltDB.instance();
        return String.format("pid: %d\r\nnodeState: %s\r\noperMode: %s\r\n",
                             instance.getVoltPid(),
                             instance.getNodeState(),
                             instance.getMode());
    }
}
