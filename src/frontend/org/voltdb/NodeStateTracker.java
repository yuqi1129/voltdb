/* This file is part of VoltDB.
 * Copyright (C) 2008-2020 VoltDB Inc.
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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.voltcore.logging.VoltLogger;
import org.voltdb.common.NodeState;

import com.google_voltpatches.common.base.Supplier;

/**
 * Class that aids in the tracking of a VoltDB node state.
 *
 * As well as the actual node state, there is a set-once
 * flag that marks the point at which VoltDB considers
 * startup to be complete; this is for the convenience
 * of status reporting. The node state will be UP; however,
 * we have an explicit startup-complete indication because
 * in some cases we need to defer the indication until
 * log replay is complete. The flag offers a single point
 * of truth as to server startup.
 */
public class NodeStateTracker {

    private static final VoltLogger logger = new VoltLogger("NODESTATE");

    private final AtomicReference<NodeState> nodeState =
        new AtomicReference<>(NodeState.INITIALIZING);

    private final AtomicBoolean startupComplete =
        new AtomicBoolean(false);

    public NodeState set(NodeState newState) {
        NodeState prevState = nodeState.getAndSet(newState);
        logger.info(String.format("State change, %s => %s", prevState, newState));
        return prevState;
    }

    public NodeState get() {
        return nodeState.get();
    }

    public Supplier<NodeState> getSupplier() {
        return nodeState::get;
    }

    public void setStartupComplete() {
        boolean prev = startupComplete.getAndSet(true);
        if (!prev) {
            logger.info("Server is now fully started");
        }
    }

    public boolean getStartupComplete() {
        return startupComplete.get();
    }
}
