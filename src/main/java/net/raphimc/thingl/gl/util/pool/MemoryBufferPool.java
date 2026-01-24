/*
 * This file is part of ThinGL - https://github.com/RaphiMC/ThinGL
 * Copyright (C) 2024-2026 RK_01/RaphiMC and contributors
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
package net.raphimc.thingl.gl.util.pool;

import it.unimi.dsi.fastutil.objects.Reference2LongMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.memory.MemoryBuffer;

public class MemoryBufferPool {

    private final ReferenceList<MemoryBuffer> free = new ReferenceArrayList<>();
    private final ReferenceList<MemoryBuffer> inUse = new ReferenceArrayList<>();
    private final Reference2LongMap<MemoryBuffer> memoryBufferAccessTime = new Reference2LongOpenHashMap<>();

    public MemoryBufferPool() {
        ThinGL.get().addFrameFinishedCallback(() -> {
            if (!this.inUse.isEmpty()) {
                ThinGL.LOGGER.warn(this.inUse.size() + " MemoryBuffer(s) were not returned to the pool. Forcibly reclaiming them.");
                for (MemoryBuffer memoryBuffer : this.inUse) {
                    memoryBuffer.reset();
                }
                this.free.addAll(this.inUse);
                this.inUse.clear();
            }
            this.memoryBufferAccessTime.reference2LongEntrySet().removeIf(entry -> {
                if (System.nanoTime() - entry.getLongValue() > 60_000_000_000L) {
                    if (this.free.contains(entry.getKey())) {
                        this.free.remove(entry.getKey());
                        entry.getKey().free();
                    }
                    return true;
                }
                return false;
            });
        });
    }

    public MemoryBuffer borrowMemoryBuffer() {
        ThinGL.get().assertOnRenderThread();
        final MemoryBuffer memoryBuffer;
        if (this.free.isEmpty()) {
            memoryBuffer = new MemoryBuffer();
        } else {
            memoryBuffer = this.free.removeFirst();
        }
        this.inUse.add(memoryBuffer);
        this.memoryBufferAccessTime.put(memoryBuffer, System.nanoTime());
        return memoryBuffer;
    }

    public void returnMemoryBuffer(final MemoryBuffer memoryBuffer) {
        ThinGL.get().assertOnRenderThread();
        if (!this.inUse.remove(memoryBuffer)) {
            throw new IllegalStateException("MemoryBuffer is not part of the pool");
        }
        memoryBuffer.reset();
        this.free.add(memoryBuffer);
    }

    public int getSize() {
        return this.free.size() + this.inUse.size();
    }

    public void free() {
        for (MemoryBuffer memoryBuffer : this.free) {
            memoryBuffer.free();
        }
        for (MemoryBuffer memoryBuffer : this.inUse) {
            memoryBuffer.free();
        }
    }

}
