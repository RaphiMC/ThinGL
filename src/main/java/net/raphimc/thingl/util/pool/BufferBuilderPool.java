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
import net.raphimc.thingl.drawbuilder.builder.BufferBuilder;

public class BufferBuilderPool {

    private final ReferenceList<BufferBuilder> free = new ReferenceArrayList<>();
    private final ReferenceList<BufferBuilder> inUse = new ReferenceArrayList<>();
    private final Reference2LongMap<BufferBuilder> bufferBuilderAccessTime = new Reference2LongOpenHashMap<>();

    public BufferBuilderPool() {
        ThinGL.get().addFinishFrameCallback(() -> {
            if (!this.inUse.isEmpty()) {
                ThinGL.LOGGER.warn(this.inUse.size() + " BufferBuilder(s) were not returned to the pool. Forcibly reclaiming them.");
                for (BufferBuilder bufferBuilder : this.inUse) {
                    bufferBuilder.reset();
                }
                this.free.addAll(this.inUse);
                this.inUse.clear();
            }
            this.bufferBuilderAccessTime.reference2LongEntrySet().removeIf(entry -> {
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

    public BufferBuilder borrowBufferBuilder() {
        ThinGL.get().assertOnRenderThread();
        final BufferBuilder bufferBuilder;
        if (this.free.isEmpty()) {
            bufferBuilder = new BufferBuilder();
        } else {
            bufferBuilder = this.free.remove(0);
        }
        this.inUse.add(bufferBuilder);
        this.bufferBuilderAccessTime.put(bufferBuilder, System.nanoTime());
        return bufferBuilder;
    }

    public void returnBufferBuilder(final BufferBuilder bufferBuilder) {
        ThinGL.get().assertOnRenderThread();
        if (!this.inUse.remove(bufferBuilder)) {
            throw new IllegalStateException("BufferBuilder is not part of the pool");
        }
        bufferBuilder.reset();
        this.free.add(bufferBuilder);
    }

    public int getSize() {
        return this.free.size() + this.inUse.size();
    }

    public void free() {
        for (BufferBuilder bufferBuilder : this.free) {
            bufferBuilder.free();
        }
        for (BufferBuilder bufferBuilder : this.inUse) {
            bufferBuilder.free();
        }
    }

}
