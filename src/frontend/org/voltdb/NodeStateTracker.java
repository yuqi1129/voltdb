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

import java.util.concurrent.atomic.AtomicReference;

import org.voltcore.logging.VoltLogger;
import org.voltdb.common.NodeState;

import com.google_voltpatches.common.base.Supplier;

/**
 * Class that aids in the tracking of a VoltDB node state.
 */
public class NodeStateTracker {

    private static final VoltLogger logger = new VoltLogger("NODE");

    private final AtomicReference<NodeState> nodeState =
        new AtomicReference<>(NodeState.INITIALIZING);

    public void set(NodeState newState) {
        NodeState prevState = nodeState.getAndSet(newState);
        // TODO - make this debug?
        logger.info(String.format("State change, %s => %s", prevState, newState));
    }

    public NodeState get() {
        return nodeState.get();
    }

    public Supplier<NodeState> getSupplier() {
        return nodeState::get;
    }
}
