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
package net.raphimc.thingl.rendering.preparation;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.implementation.Capabilities;
import net.raphimc.thingl.memory.MemoryBuffer;
import net.raphimc.thingl.memory.allocator.MemoryAllocator;
import net.raphimc.thingl.rendering.DrawBatch;
import net.raphimc.thingl.rendering.command.DrawCommand;
import net.raphimc.thingl.rendering.command.impl.DrawArraysCommand;
import net.raphimc.thingl.rendering.command.impl.DrawElementsCommand;
import net.raphimc.thingl.resource.memory.Memory;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.util.meshoptimizer.MeshOptimizer;

import java.util.List;

public class DrawBatchDataOptimizer {

    static {
        Capabilities.assertMeshOptimizerAvailable();
    }

    public static PreparedDrawBatchData optimize(final PreparedDrawBatchData preparedDrawBatchData) {
        final DrawBatch drawBatch = preparedDrawBatchData.drawBatch();
        final List<DrawCommand> drawCommands = preparedDrawBatchData.drawCommands();
        if (drawBatch.drawMode().getGlMode() != GL11C.GL_TRIANGLES) {
            return preparedDrawBatchData;
        }
        if (drawCommands.size() != 1) {
            return preparedDrawBatchData;
        }
        if (preparedDrawBatchData.indexBuffer() != null && preparedDrawBatchData.indexBuffer().type() != GL11C.GL_UNSIGNED_INT) {
            return preparedDrawBatchData;
        }

        final DrawCommand drawCommand = drawCommands.getFirst();
        final int vertexSize = drawBatch.vertexDataLayout().getSize();
        Memory vertexBuffer = preparedDrawBatchData.vertexBuffer();
        Memory indexBuffer = preparedDrawBatchData.indexBuffer() != null ? preparedDrawBatchData.indexBuffer().buffer() : null;

        long originalVertexCount = vertexBuffer.getSize() / vertexSize;
        if (drawCommand instanceof DrawArraysCommand) {
            if (drawCommand.vertexCount() > originalVertexCount) {
                throw new IllegalStateException("Draw command vertex count exceeds vertex buffer size");
            }
            originalVertexCount = drawCommand.vertexCount();
        }
        long indexCount = indexBuffer != null ? indexBuffer.getSize() / Integer.BYTES : originalVertexCount;
        if (drawCommand instanceof DrawElementsCommand) {
            if (drawCommand.vertexCount() > indexCount) {
                throw new IllegalStateException("Draw command index count exceeds index buffer size");
            }
            indexCount = drawCommand.vertexCount();
        }

        final Memory remapTable = MemoryAllocator.allocateMemory(originalVertexCount * Integer.BYTES);
        final long uniqueVertexCount = MeshOptimizer.nmeshopt_generateVertexRemap(remapTable.getAddress(), indexBuffer != null ? indexBuffer.getAddress() : 0L, indexCount, vertexBuffer.getAddress(), originalVertexCount, vertexSize);
        final Memory newVertexBuffer = MemoryAllocator.allocateMemory(uniqueVertexCount * vertexSize);
        MeshOptimizer.nmeshopt_remapVertexBuffer(newVertexBuffer.getAddress(), vertexBuffer.getAddress(), originalVertexCount, vertexSize, remapTable.getAddress());
        final Memory newIndexBuffer = MemoryAllocator.allocateMemory(indexCount * Integer.BYTES);
        MeshOptimizer.nmeshopt_remapIndexBuffer(newIndexBuffer.getAddress(), indexBuffer != null ? indexBuffer.getAddress() : 0L, indexCount, remapTable.getAddress());
        remapTable.free();

        MeshOptimizer.nmeshopt_optimizeVertexCache(newIndexBuffer.getAddress(), newIndexBuffer.getAddress(), indexCount, uniqueVertexCount);
        final long usedVertexCount = MeshOptimizer.nmeshopt_optimizeVertexFetch(newVertexBuffer.getAddress(), newIndexBuffer.getAddress(), indexCount, newVertexBuffer.getAddress(), uniqueVertexCount, vertexSize);
        if (usedVertexCount != uniqueVertexCount) {
            throw new IllegalStateException("Mesh contains unused vertices");
        }

        newVertexBuffer.copyTo(vertexBuffer);
        vertexBuffer = vertexBuffer.slice(0L, newVertexBuffer.getSize());
        newVertexBuffer.free();

        if (indexBuffer != null && indexBuffer != ThinGL.quadIndexBuffer().getSharedData()) {
            newIndexBuffer.copyTo(indexBuffer);
            indexBuffer = indexBuffer.slice(0L, newIndexBuffer.getSize());
        } else {
            final MemoryBuffer indexBufferBuilder = preparedDrawBatchData.drawBatchDataHolder().getIndexBufferBuilder().getMemoryBuffer();
            indexBufferBuilder.reset();
            indexBufferBuilder.writeMemory(newIndexBuffer);
            indexBuffer = indexBufferBuilder.finish();
        }
        newIndexBuffer.free();

        if (drawCommand instanceof DrawArraysCommand drawArraysCommand) {
            drawCommands.clear();
            drawCommands.add(new DrawElementsCommand(drawCommand.vertexCount(), drawCommand.instanceCount(), 0, drawArraysCommand.firstVertex(), drawCommand.baseInstance()));
        }

        return new PreparedDrawBatchData(preparedDrawBatchData.drawBatchDataHolder(), drawBatch, vertexBuffer, preparedDrawBatchData.instanceVertexBuffer(), new IndexBuffer(GL11C.GL_UNSIGNED_INT, indexBuffer), preparedDrawBatchData.uniformBuffers(), preparedDrawBatchData.shaderStorageBuffers(), drawCommands);
    }

}
