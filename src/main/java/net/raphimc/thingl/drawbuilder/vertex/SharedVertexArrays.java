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

package net.raphimc.thingl.drawbuilder.vertex;

import it.unimi.dsi.fastutil.objects.Reference2LongMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.util.BufferUtil;
import net.raphimc.thingl.resource.buffer.AbstractBuffer;
import net.raphimc.thingl.resource.buffer.ImmutableBuffer;
import net.raphimc.thingl.resource.vertexarray.VertexArray;
import org.lwjgl.opengl.GL44C;

public class SharedVertexArrays {

    private static final Reference2ObjectMap<VertexDataLayout, VertexArray> VERTEX_ARRAY_CACHE = new Reference2ObjectOpenHashMap<>();
    private static final Reference2LongMap<VertexArray> VERTEX_ARRAY_ACCESS_TIME = new Reference2LongOpenHashMap<>();

    static {
        ThinGL.registerEndFrameCallback(() -> {
            if (VERTEX_ARRAY_CACHE.size() > 64) {
                ThinGL.LOGGER.warn("SharedVertexArray cache has grown to " + VERTEX_ARRAY_CACHE.size() + " entries. Clearing the cache to prevent memory starvation.");
                VERTEX_ARRAY_CACHE.values().forEach(SharedVertexArrays::deleteVertexArray);
                VERTEX_ARRAY_CACHE.clear();
            }
            VERTEX_ARRAY_ACCESS_TIME.reference2LongEntrySet().removeIf(entry -> {
                if (System.currentTimeMillis() - entry.getLongValue() > 60 * 1000) {
                    if (VERTEX_ARRAY_CACHE.containsValue(entry.getKey())) {
                        VERTEX_ARRAY_CACHE.values().remove(entry.getKey());
                        deleteVertexArray(entry.getKey());
                    }
                    return true;
                }
                return false;
            });
        });
    }

    public static VertexArray getVertexArray(final VertexDataLayout vertexDataLayout) {
        ThinGL.assertOnRenderThread();
        final VertexArray vertexArray = VERTEX_ARRAY_CACHE.computeIfAbsent(vertexDataLayout, SharedVertexArrays::createVertexArray);
        VERTEX_ARRAY_ACCESS_TIME.put(vertexArray, System.currentTimeMillis());
        return vertexArray;
    }

    private static VertexArray createVertexArray(final VertexDataLayout vertexDataLayout) {
        final VertexArray vertexArray = new VertexArray();
        vertexArray.setDebugName("SharedVertexArray (" + vertexDataLayout.getElements().length + " elements)");
        vertexArray.setVertexBuffer(0, new ImmutableBuffer(BufferUtil.DEFAULT_BUFFER_SIZE, GL44C.GL_DYNAMIC_STORAGE_BIT), 0, vertexDataLayout.getSize());
        vertexArray.configureVertexDataLayout(0, 0, vertexDataLayout, 0);
        return vertexArray;
    }

    private static void deleteVertexArray(final VertexArray vertexArray) {
        for (AbstractBuffer buffer : vertexArray.getVertexBuffers().values()) {
            buffer.delete();
        }
        vertexArray.delete();
    }

    public static int getSize() {
        return VERTEX_ARRAY_CACHE.size();
    }

}
