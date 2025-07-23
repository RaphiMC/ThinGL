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
package net.raphimc.thingl.util.pool;

import it.unimi.dsi.fastutil.objects.Reference2LongMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.resource.buffer.MutableBuffer;
import net.raphimc.thingl.util.BufferUtil;
import org.lwjgl.opengl.GL15C;

public class GpuBufferPool {

    private final ReferenceList<MutableBuffer> free = new ReferenceArrayList<>();
    private final ReferenceList<MutableBuffer> inUse = new ReferenceArrayList<>();
    private final Reference2LongMap<MutableBuffer> bufferAccessTime = new Reference2LongOpenHashMap<>();

    public GpuBufferPool() {
        ThinGL.get().addFinishFrameCallback(() -> {
            if (!this.inUse.isEmpty()) {
                ThinGL.LOGGER.warn(this.inUse.size() + " GPU Buffer(s) were not returned to the pool. Forcibly reclaiming them.");
                this.free.addAll(this.inUse);
                this.inUse.clear();
            }
            this.bufferAccessTime.reference2LongEntrySet().removeIf(entry -> {
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

    public MutableBuffer borrowBuffer() {
        ThinGL.get().assertOnRenderThread();
        final MutableBuffer buffer;
        if (this.free.isEmpty()) {
            buffer = new MutableBuffer(BufferUtil.DEFAULT_BUFFER_SIZE, GL15C.GL_DYNAMIC_DRAW);
            buffer.setDebugName("Buffer Pool Buffer " + this.getSize());
        } else {
            buffer = this.free.remove(0);
        }
        this.inUse.add(buffer);
        this.bufferAccessTime.put(buffer, System.nanoTime());
        return buffer;
    }

    public void returnBuffer(final MutableBuffer buffer) {
        ThinGL.get().assertOnRenderThread();
        if (!this.inUse.remove(buffer)) {
            throw new IllegalStateException("Buffer is not part of the pool");
        }
        this.free.add(buffer);
    }

    public int getSize() {
        return this.free.size() + this.inUse.size();
    }

    public void free() {
        for (MutableBuffer buffer : this.free) {
            buffer.free();
        }
        for (MutableBuffer buffer : this.inUse) {
            buffer.free();
        }
    }

}
