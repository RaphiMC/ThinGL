/*
 * This file is part of ThinGL - https://github.com/RaphiMC/ThinGL
 * Copyright (C) 2024-2025 RK_01/RaphiMC and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.raphimc.thingl.util;

import it.unimi.dsi.fastutil.Pair;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.resource.query.Query;
import net.raphimc.thingl.resource.sync.FenceSync;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SyncManager {

    private final List<Pair<FenceSync, Consumer<FenceSync>>> pendingFenceSyncs = new ArrayList<>();
    private final List<Pair<Query, Consumer<Query>>> pendingQueries = new ArrayList<>();

    public SyncManager() {
        ThinGL.get().addFinishFrameCallback(() -> {
            for (int i = 0; i < this.pendingFenceSyncs.size(); i++) {
                final Pair<FenceSync, Consumer<FenceSync>> pair = this.pendingFenceSyncs.get(i);
                if (pair.key().isSignaled()) {
                    pair.value().accept(pair.key());
                    this.pendingFenceSyncs.remove(i);
                    i--;
                }
            }
            for (int i = 0; i < this.pendingQueries.size(); i++) {
                final Pair<Query, Consumer<Query>> pair = this.pendingQueries.get(i);
                if (pair.key().isResultAvailable()) {
                    pair.value().accept(pair.key());
                    this.pendingQueries.remove(i);
                    i--;
                }
            }
        });
    }

    public void runWhenFenceSyncSignaled(final FenceSync fenceSync, final Consumer<FenceSync> callback) {
        ThinGL.get().assertOnRenderThread();
        this.pendingFenceSyncs.add(Pair.of(fenceSync, callback));
    }

    public void runWhenQueryResultAvailable(final Query query, final Consumer<Query> callback) {
        ThinGL.get().assertOnRenderThread();
        this.pendingQueries.add(Pair.of(query, callback));
    }

    public int getPendingFenceSyncsCount() {
        return this.pendingFenceSyncs.size();
    }

    public int getPendingQueriesCount() {
        return this.pendingQueries.size();
    }

}
