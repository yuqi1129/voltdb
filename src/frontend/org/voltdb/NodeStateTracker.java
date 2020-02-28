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
 * As well as the overall state, there is a set-once
 * flag that marks the point at which VoltDB considers
 * intiialization to be complete; this is for convenience
 * of status reporting. The node state is now UP; however,
 * we have an explicit initialization-complete indication
 * because there are places in the code that momentarily
 * set the state to UP during initalization.
 */
public class NodeStateTracker {

    private static final VoltLogger logger = new VoltLogger("NODESTATE");

    private final AtomicReference<NodeState> nodeState =
        new AtomicReference<>(NodeState.INITIALIZING);

    private final AtomicBoolean initComplete =
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

    public void setInitComplete() {
        initComplete.set(true);
    }

    public boolean getInitComplete() {
        return initComplete.get();
    }
}
