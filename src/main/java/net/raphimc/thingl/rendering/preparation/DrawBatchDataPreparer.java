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
package net.raphimc.thingl.rendering.preparation;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.util.QuadIndexBuffer;
import net.raphimc.thingl.memory.MemoryBuffer;
import net.raphimc.thingl.rendering.DrawBatch;
import net.raphimc.thingl.rendering.DrawMode;
import net.raphimc.thingl.rendering.bufferbuilder.ShaderBufferBuilder;
import net.raphimc.thingl.rendering.bufferbuilder.impl.IndexBufferBuilder;
import net.raphimc.thingl.rendering.bufferbuilder.impl.VertexBufferBuilder;
import net.raphimc.thingl.rendering.command.DrawCommand;
import net.raphimc.thingl.rendering.command.impl.DrawArraysCommand;
import net.raphimc.thingl.rendering.command.impl.DrawElementsCommand;
import net.raphimc.thingl.rendering.dataholder.DrawBatchDataHolder;
import net.raphimc.thingl.resource.memory.Memory;
import org.lwjgl.opengl.GL11C;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DrawBatchDataPreparer {

    public static PreparedDrawBatchData prepareDrawBatchData(final DrawBatchDataHolder drawBatchDataHolder) {
        final DrawBatch drawBatch = drawBatchDataHolder.getDrawBatch();
        final VertexBufferBuilder vertexBufferBuilder = drawBatchDataHolder.getVertexBufferBuilder();
        final MemoryBuffer vertexMemoryBuffer = vertexBufferBuilder.getMemoryBuffer();
        if (vertexBufferBuilder.getVertexCount() == 0 && vertexMemoryBuffer.getWritePosition() != 0) {
            throw new IllegalStateException("Trying to build a buffer with no vertices but the buffer builder is not empty. Did you forget to call endVertex()?");
        }

        int totalVertexCount = vertexBufferBuilder.getVertexCount();
        IndexBuffer indexBuffer = null;
        if (drawBatch.drawMode().isIndexed()) {
            if (drawBatchDataHolder.hasIndexData()) {
                final IndexBufferBuilder indexBufferBuilder = drawBatchDataHolder.getIndexBufferBuilder();
                final MemoryBuffer indexMemoryBuffer = indexBufferBuilder.getMemoryBuffer();
                if (indexBufferBuilder.getIndexCount() == 0 && indexMemoryBuffer.getWritePosition() != 0) {
                    throw new IllegalStateException("Trying to build a buffer with no indices but the buffer builder is not empty");
                }

                indexBuffer = new IndexBuffer(GL11C.GL_UNSIGNED_INT, indexMemoryBuffer.finish());
                totalVertexCount = indexBufferBuilder.getIndexCount();
            } else if (drawBatch.drawMode() == DrawMode.QUADS) {
                final int quadCount = vertexBufferBuilder.getVertexCount() / QuadIndexBuffer.QUAD_VERTEX_COUNT;
                ThinGL.quadIndexBuffer().ensureSize(quadCount);
                indexBuffer = new IndexBuffer(GL11C.GL_UNSIGNED_INT, ThinGL.quadIndexBuffer().getSharedData());
                totalVertexCount = quadCount * QuadIndexBuffer.QUAD_INDEX_COUNT;
            } else {
                throw new IllegalStateException("Draw mode uses indexed drawing but no index data was provided");
            }
        } else if (drawBatchDataHolder.hasIndexData()) {
            throw new IllegalStateException("Draw mode does not use indexed drawing but index data was provided");
        }

        int instanceCount = 1;
        Memory instanceVertexBuffer = null;
        if (drawBatch.instanceVertexDataLayout() != null) {
            if (drawBatchDataHolder.hasInstanceVertexData()) {
                final VertexBufferBuilder instanceVertexBufferBuilder = drawBatchDataHolder.getInstanceVertexBufferBuilder();
                final MemoryBuffer instanceVertexMemoryBuffer = instanceVertexBufferBuilder.getMemoryBuffer();
                if (instanceVertexBufferBuilder.getVertexCount() == 0 && instanceVertexMemoryBuffer.getWritePosition() != 0) {
                    throw new IllegalStateException("Trying to build a buffer with no instances but the buffer builder is not empty. Did you forget to call endVertex()?");
                }

                instanceVertexBuffer = instanceVertexMemoryBuffer.finish();
                instanceCount = instanceVertexBufferBuilder.getVertexCount();
            } else {
                throw new IllegalStateException("Draw mode uses instancing but no instance data was provided");
            }
        } else if (drawBatchDataHolder.hasInstanceVertexData()) {
            throw new IllegalStateException("Draw batch does not use instancing but instance data was provided");
        }

        final Object2ObjectMap<String, Memory> uniformBuffers = new Object2ObjectOpenHashMap<>(drawBatchDataHolder.getUniformBufferBuilders().size());
        for (Map.Entry<String, ShaderBufferBuilder> entry : drawBatchDataHolder.getUniformBufferBuilders().entrySet()) {
            uniformBuffers.put(entry.getKey(), entry.getValue().getMemoryBuffer().finish());
        }

        final Object2ObjectMap<String, Memory> shaderStorageBuffers = new Object2ObjectOpenHashMap<>(drawBatchDataHolder.getShaderStorageBufferBuilders().size());
        for (Map.Entry<String, ShaderBufferBuilder> entry : drawBatchDataHolder.getShaderStorageBufferBuilders().entrySet()) {
            shaderStorageBuffers.put(entry.getKey(), entry.getValue().getMemoryBuffer().finish());
        }

        final IntList connectedPrimitiveIndices = vertexBufferBuilder.getConnectedPrimitiveIndices();
        final List<DrawCommand> drawCommands = new ArrayList<>(1);
        if (indexBuffer == null) {
            if (drawBatch.drawMode().usesConnectedPrimitives()) {
                if (connectedPrimitiveIndices == null) {
                    throw new IllegalStateException("Draw mode uses connected primitives but no connected primitive indices were provided");
                }
                final int[] startIndices = new int[connectedPrimitiveIndices.size() - 1];
                final int[] vertexCounts = new int[startIndices.length];
                for (int i = 0; i < startIndices.length; i++) {
                    final int vertexIndex = connectedPrimitiveIndices.getInt(i);
                    startIndices[i] = vertexIndex;
                    vertexCounts[i] = connectedPrimitiveIndices.getInt(i + 1) - vertexIndex;
                }
                for (int i = 0; i < startIndices.length; i++) {
                    drawCommands.add(new DrawArraysCommand(vertexCounts[i], instanceCount, startIndices[i], 0));
                }
            } else {
                if (connectedPrimitiveIndices != null) {
                    throw new IllegalStateException("Draw mode does not use connected primitives but connected primitive indices were provided");
                }
                drawCommands.add(new DrawArraysCommand(totalVertexCount, instanceCount));
            }
        } else {
            if (drawBatch.drawMode().usesConnectedPrimitives()) {
                throw new IllegalStateException("Draw mode uses connected primitives but connected primitives are not supported with indexed drawing");
            } else {
                if (connectedPrimitiveIndices != null) {
                    throw new IllegalStateException("Draw mode does not use connected primitives but connected primitive indices were provided");
                }
                drawCommands.add(new DrawElementsCommand(totalVertexCount, instanceCount));
            }
        }

        return new PreparedDrawBatchData(drawBatchDataHolder, drawBatch, vertexMemoryBuffer.finish(), instanceVertexBuffer, indexBuffer, uniformBuffers, shaderStorageBuffers, drawCommands);
    }

    public static void freePreparedDrawBatchData(final PreparedDrawBatchData preparedDrawBatchData) {
        preparedDrawBatchData.drawBatchDataHolder().free();
    }

}
