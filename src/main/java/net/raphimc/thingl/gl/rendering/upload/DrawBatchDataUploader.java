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
package net.raphimc.thingl.gl.rendering.upload;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.resource.buffer.Buffer;
import net.raphimc.thingl.gl.resource.buffer.impl.ImmutableBuffer;
import net.raphimc.thingl.gl.resource.buffer.impl.MutableBuffer;
import net.raphimc.thingl.gl.resource.vertexarray.VertexArray;
import net.raphimc.thingl.memory.MemoryBuffer;
import net.raphimc.thingl.rendering.DrawBatch;
import net.raphimc.thingl.rendering.command.DrawCommand;
import net.raphimc.thingl.rendering.preparation.PreparedDrawBatchData;
import net.raphimc.thingl.resource.memory.Memory;

import java.util.Map;

public class DrawBatchDataUploader {

    public static UploadedDrawBatchData uploadTemporary(final PreparedDrawBatchData preparedDrawBatchData) {
        final DrawBatch drawBatch = preparedDrawBatchData.drawBatch();
        final VertexArray vertexArray = ThinGL.immediateVertexArrays().getVertexArray(drawBatch.vertexDataLayout(), drawBatch.instanceVertexDataLayout());

        if (preparedDrawBatchData.indexBuffer() != null) {
            final Memory indexData = preparedDrawBatchData.indexBuffer().buffer();
            if (indexData == ThinGL.quadIndexBuffer().getSharedData()) {
                vertexArray.setIndexBuffer(preparedDrawBatchData.indexBuffer().type(), ThinGL.quadIndexBuffer().getSharedBuffer());
            } else {
                final MutableBuffer indexBuffer = ThinGL.gpuBufferPool().borrowBuffer();
                indexBuffer.ensureSize(indexData.getSize());
                indexBuffer.upload(indexData);
                vertexArray.setIndexBuffer(preparedDrawBatchData.indexBuffer().type(), indexBuffer);
            }
        }

        final Memory vertexData = preparedDrawBatchData.vertexBuffer();
        final MutableBuffer vertexBuffer = (MutableBuffer) vertexArray.getVertexBuffers().get(0);
        vertexBuffer.ensureSize(vertexData.getSize());
        vertexBuffer.upload(vertexData);

        final Memory instanceVertexData = preparedDrawBatchData.instanceVertexBuffer();
        if (instanceVertexData != null) {
            final MutableBuffer instanceVertexBuffer = (MutableBuffer) vertexArray.getVertexBuffers().get(1);
            instanceVertexBuffer.ensureSize(instanceVertexData.getSize());
            instanceVertexBuffer.upload(instanceVertexData);
        }

        final Object2ObjectMap<String, Buffer> uniformBuffers = new Object2ObjectOpenHashMap<>(preparedDrawBatchData.uniformBuffers().size());
        for (Map.Entry<String, Memory> entry : preparedDrawBatchData.uniformBuffers().entrySet()) {
            final Memory uniformData = entry.getValue();
            final MutableBuffer uniformBuffer = ThinGL.gpuBufferPool().borrowBuffer();
            uniformBuffer.ensureSize(uniformData.getSize());
            uniformBuffer.upload(uniformData);
            uniformBuffers.put(entry.getKey(), uniformBuffer);
        }

        final Object2ObjectMap<String, Buffer> shaderStorageBuffers = new Object2ObjectOpenHashMap<>(preparedDrawBatchData.shaderStorageBuffers().size());
        for (Map.Entry<String, Memory> entry : preparedDrawBatchData.shaderStorageBuffers().entrySet()) {
            final Memory shaderStorageData = entry.getValue();
            final MutableBuffer shaderStorageBuffer = ThinGL.gpuBufferPool().borrowBuffer();
            shaderStorageBuffer.ensureSize(shaderStorageData.getSize());
            shaderStorageBuffer.upload(shaderStorageData);
            shaderStorageBuffers.put(entry.getKey(), shaderStorageBuffer);
        }

        MutableBuffer commandBuffer = null;
        if (preparedDrawBatchData.drawCommands().size() > 1) {
            final MemoryBuffer commandMemoryBuffer = ThinGL.memoryBufferPool().borrowMemoryBuffer();
            commandMemoryBuffer.ensureCanWrite((long) preparedDrawBatchData.drawCommands().size() * DrawCommand.BYTES);
            for (DrawCommand drawCommand : preparedDrawBatchData.drawCommands()) {
                drawCommand.write(commandMemoryBuffer);
            }
            final Memory commandData = commandMemoryBuffer.finish();
            commandBuffer = ThinGL.gpuBufferPool().borrowBuffer();
            commandBuffer.ensureSize(commandData.getSize());
            commandBuffer.upload(commandData);
            ThinGL.memoryBufferPool().returnMemoryBuffer(commandMemoryBuffer);
        }

        return new UploadedDrawBatchData(preparedDrawBatchData.drawBatch(), vertexArray, uniformBuffers, shaderStorageBuffers, commandBuffer, preparedDrawBatchData.drawCommands());
    }

    public static UploadedDrawBatchData uploadPersistent(final PreparedDrawBatchData preparedDrawBatchData) {
        final DrawBatch drawBatch = preparedDrawBatchData.drawBatch();
        final VertexArray vertexArray = new VertexArray();

        if (preparedDrawBatchData.indexBuffer() != null) {
            final Memory indexData = preparedDrawBatchData.indexBuffer().buffer();
            if (indexData == ThinGL.quadIndexBuffer().getSharedData()) {
                vertexArray.setIndexBuffer(preparedDrawBatchData.indexBuffer().type(), ThinGL.quadIndexBuffer().getSharedBuffer());
            } else {
                final Buffer indexBuffer = new ImmutableBuffer(indexData, 0);
                vertexArray.setIndexBuffer(preparedDrawBatchData.indexBuffer().type(), indexBuffer);
            }
        }

        final Buffer vertexBuffer = new ImmutableBuffer(preparedDrawBatchData.vertexBuffer(), 0);
        vertexArray.setVertexBuffer(0, vertexBuffer, 0, drawBatch.vertexDataLayout().getSize());
        vertexArray.configureVertexDataLayout(0, 0, drawBatch.vertexDataLayout(), 0);

        if (preparedDrawBatchData.instanceVertexBuffer() != null) {
            final Buffer instanceVertexBuffer = new ImmutableBuffer(preparedDrawBatchData.instanceVertexBuffer(), 0);
            vertexArray.setVertexBuffer(1, instanceVertexBuffer, 0, drawBatch.instanceVertexDataLayout().getSize());
            vertexArray.configureVertexDataLayout(1, drawBatch.vertexDataLayout().getElements().length, drawBatch.instanceVertexDataLayout(), 1);
        }

        final Object2ObjectMap<String, Buffer> uniformBuffers = new Object2ObjectOpenHashMap<>(preparedDrawBatchData.uniformBuffers().size());
        for (Map.Entry<String, Memory> entry : preparedDrawBatchData.uniformBuffers().entrySet()) {
            uniformBuffers.put(entry.getKey(), new ImmutableBuffer(entry.getValue(), 0));
        }

        final Object2ObjectMap<String, Buffer> shaderStorageBuffers = new Object2ObjectOpenHashMap<>(preparedDrawBatchData.shaderStorageBuffers().size());
        for (Map.Entry<String, Memory> entry : preparedDrawBatchData.shaderStorageBuffers().entrySet()) {
            shaderStorageBuffers.put(entry.getKey(), new ImmutableBuffer(entry.getValue(), 0));
        }

        Buffer commandBuffer = null;
        if (preparedDrawBatchData.drawCommands().size() > 1) {
            final MemoryBuffer commandMemoryBuffer = ThinGL.memoryBufferPool().borrowMemoryBuffer();
            commandMemoryBuffer.ensureCanWrite((long) preparedDrawBatchData.drawCommands().size() * DrawCommand.BYTES);
            for (DrawCommand drawCommand : preparedDrawBatchData.drawCommands()) {
                drawCommand.write(commandMemoryBuffer);
            }
            commandBuffer = new ImmutableBuffer(commandMemoryBuffer.finish(), 0);
            ThinGL.memoryBufferPool().returnMemoryBuffer(commandMemoryBuffer);
        }

        return new UploadedDrawBatchData(preparedDrawBatchData.drawBatch(), vertexArray, uniformBuffers, shaderStorageBuffers, commandBuffer, preparedDrawBatchData.drawCommands());
    }

    public static void freeTemporaryData(final UploadedDrawBatchData uploadedDrawBatchData) {
        final VertexArray vertexArray = uploadedDrawBatchData.vertexArray();
        if (vertexArray.getIndexBuffer() != null) {
            if (vertexArray.getIndexBuffer() != ThinGL.quadIndexBuffer().getSharedBuffer()) {
                ThinGL.gpuBufferPool().returnBuffer((MutableBuffer) vertexArray.getIndexBuffer());
            }
            vertexArray.setIndexBuffer(0, null);
        }
        for (Buffer buffer : uploadedDrawBatchData.uniformBuffers().values()) {
            ThinGL.gpuBufferPool().returnBuffer((MutableBuffer) buffer);
        }
        for (Buffer buffer : uploadedDrawBatchData.shaderStorageBuffers().values()) {
            ThinGL.gpuBufferPool().returnBuffer((MutableBuffer) buffer);
        }
        if (uploadedDrawBatchData.commandBuffer() != null) {
            ThinGL.gpuBufferPool().returnBuffer((MutableBuffer) uploadedDrawBatchData.commandBuffer());
        }
    }

    public static void freePersistentData(final UploadedDrawBatchData uploadedDrawBatchData) {
        uploadedDrawBatchData.vertexArray().freeFully();
        for (Buffer buffer : uploadedDrawBatchData.uniformBuffers().values()) {
            buffer.free();
        }
        for (Buffer buffer : uploadedDrawBatchData.shaderStorageBuffers().values()) {
            buffer.free();
        }
        if (uploadedDrawBatchData.commandBuffer() != null) {
            uploadedDrawBatchData.commandBuffer().free();
        }
    }

}
