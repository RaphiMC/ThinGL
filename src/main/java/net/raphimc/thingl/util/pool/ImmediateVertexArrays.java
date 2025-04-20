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
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.drawbuilder.vertex.VertexDataLayout;
import net.raphimc.thingl.resource.buffer.Buffer;
import net.raphimc.thingl.resource.vertexarray.VertexArray;
import net.raphimc.thingl.util.BufferUtil;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.opengl.GL15C;

public class ImmediateVertexArrays {

    private final Reference2ObjectMap<VertexDataLayout, VertexArray> vertexArrayCache = new Reference2ObjectOpenHashMap<>();
    private final Reference2LongMap<VertexArray> vertexArrayAccessTime = new Reference2LongOpenHashMap<>();
    private final VertexArray postProcessingVao;

    @ApiStatus.Internal
    public ImmediateVertexArrays(final ThinGL thinGL) {
        thinGL.addEndFrameCallback(() -> {
            if (this.vertexArrayCache.size() > 64) {
                ThinGL.LOGGER.warn("ImmediateVertexArrays cache has grown to " + this.vertexArrayCache.size() + " entries. Clearing the cache to prevent memory starvation.");
                this.vertexArrayCache.values().forEach(VertexArray::freeFully);
                this.vertexArrayCache.clear();
                this.vertexArrayAccessTime.clear();
            }
            this.vertexArrayAccessTime.reference2LongEntrySet().removeIf(entry -> {
                if (System.nanoTime() - entry.getLongValue() > 60_000_000_000L) {
                    if (this.vertexArrayCache.containsValue(entry.getKey())) {
                        this.vertexArrayCache.values().remove(entry.getKey());
                        entry.getKey().freeFully();
                    }
                    return true;
                }
                return false;
            });
        });
        this.postProcessingVao = new VertexArray();
        this.postProcessingVao.setDebugName("Post-Processing VAO");
    }

    public VertexArray getVertexArray(final VertexDataLayout vertexDataLayout) {
        ThinGL.get().assertOnRenderThread();
        final VertexArray vertexArray = this.vertexArrayCache.computeIfAbsent(vertexDataLayout, this::createVertexArray);
        this.vertexArrayAccessTime.put(vertexArray, System.nanoTime());
        return vertexArray;
    }

    public int getSize() {
        return this.vertexArrayCache.size();
    }

    public VertexArray getPostProcessingVao() {
        return this.postProcessingVao;
    }

    @ApiStatus.Internal
    public void free() {
        for (VertexArray vertexArray : this.vertexArrayCache.values()) {
            vertexArray.freeFully();
        }
        this.postProcessingVao.free();
    }

    private VertexArray createVertexArray(final VertexDataLayout vertexDataLayout) {
        final Buffer vertexBuffer = new Buffer(BufferUtil.DEFAULT_BUFFER_SIZE, GL15C.GL_DYNAMIC_DRAW);
        vertexBuffer.setDebugName("Immediate Vertex Buffer " + vertexBuffer.getGlId());
        final VertexArray vertexArray = new VertexArray();
        vertexArray.setDebugName("Immediate Vertex Array " + vertexArray.getGlId() + " (" + vertexDataLayout.getElements().length + " elements)");
        vertexArray.setVertexBuffer(0, vertexBuffer, 0, vertexDataLayout.getSize());
        vertexArray.configureVertexDataLayout(0, 0, vertexDataLayout, 0);
        return vertexArray;
    }

}
