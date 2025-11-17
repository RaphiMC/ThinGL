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
package net.raphimc.thingl.gl.rendering.multidraw;

import it.unimi.dsi.fastutil.ints.*;
import net.lenni0451.commons.math.MathUtils;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.rendering.upload.DrawBatchDataUploader;
import net.raphimc.thingl.gl.rendering.upload.UploadedDrawBatchData;
import net.raphimc.thingl.gl.resource.buffer.Buffer;
import net.raphimc.thingl.gl.resource.buffer.impl.ImmutableBuffer;
import net.raphimc.thingl.gl.resource.buffer.impl.MutableBuffer;
import net.raphimc.thingl.gl.resource.vertexarray.VertexArray;
import net.raphimc.thingl.gl.util.BufferUtil;
import net.raphimc.thingl.memory.MemoryBuffer;
import net.raphimc.thingl.memory.util.ArenaMemoryAllocator;
import net.raphimc.thingl.rendering.DrawBatch;
import net.raphimc.thingl.rendering.command.DrawCommand;
import net.raphimc.thingl.rendering.command.impl.DrawElementsCommand;
import net.raphimc.thingl.resource.memory.Memory;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiDrawBuilder {

    private static final long MAX_BUFFER_SIZE = 1024 * 1024 * 1024;
    private static final long MIN_RESIZE_AMOUNT = 10 * 1024 * 1024;

    private final DrawBatch drawBatch;
    private final ArenaMemoryAllocator vertexAllocator;
    private Buffer vertexBuffer;
    private final ArenaMemoryAllocator indexAllocator;
    private Buffer indexBuffer;
    private final MutableBuffer commandBuffer;
    private final VertexArray vertexArray;
    private final AtomicInteger idGenerator = new AtomicInteger();
    private final Int2LongMap storedVertexBuffers = new Int2LongOpenHashMap(); // id -> vertex address
    private final Int2LongMap storedIndexBuffers = new Int2LongOpenHashMap(); // id -> index address
    private final Int2ObjectMap<List<DrawCommand>> bufferDrawCommands = new Int2ObjectOpenHashMap<>(); // id -> draw commands
    private final IntSet renderBuffers = new IntLinkedOpenHashSet();
    private UploadedDrawBatchData uploadedDrawBatchData;

    public MultiDrawBuilder(final DrawBatch drawBatch) {
        this.drawBatch = drawBatch;
        this.vertexAllocator = new ArenaMemoryAllocator(0, MAX_BUFFER_SIZE);
        this.vertexBuffer = new ImmutableBuffer(BufferUtil.DEFAULT_BUFFER_SIZE, 0);
        if (drawBatch.drawMode().isIndexed()) {
            this.indexAllocator = new ArenaMemoryAllocator(0, MAX_BUFFER_SIZE);
            this.indexBuffer = new ImmutableBuffer(BufferUtil.DEFAULT_BUFFER_SIZE, 0);
        } else {
            this.indexAllocator = null;
        }
        this.commandBuffer = new MutableBuffer(DrawCommand.BYTES * 512L, GL15C.GL_DYNAMIC_DRAW);
        this.vertexArray = new VertexArray();
        this.vertexArray.setVertexBuffer(0, this.vertexBuffer, 0, drawBatch.vertexDataLayout().getSize());
        this.vertexArray.configureVertexDataLayout(0, 0, drawBatch.vertexDataLayout(), 0);
        if (this.indexBuffer != null) {
            this.vertexArray.setIndexBuffer(GL11C.GL_UNSIGNED_INT, this.indexBuffer);
        }
        this.rebuildCommandBuffer();
    }

    public int uploadBuffer(final UploadedDrawBatchData uploadedDrawBatchData) {
        final List<DrawCommand> drawCommands = new ArrayList<>(uploadedDrawBatchData.drawCommands());
        if (uploadedDrawBatchData.drawBatch() != this.drawBatch) {
            throw new IllegalArgumentException("DrawBatchData is not compatible");
        }
        final VertexArray vertexArray = uploadedDrawBatchData.vertexArray();
        if (vertexArray.getVertexBuffers().size() != 1 || !vertexArray.getVertexBuffers().containsKey(0)) {
            throw new IllegalArgumentException("DrawBatchData has more than one vertex buffer");
        }
        final Buffer vertexBuffer = vertexArray.getVertexBuffers().get(0);
        if (!uploadedDrawBatchData.uniformBuffers().isEmpty()) {
            throw new IllegalArgumentException("DrawBatchData has uniform buffers");
        }
        if (!uploadedDrawBatchData.shaderStorageBuffers().isEmpty()) {
            throw new IllegalArgumentException("DrawBatchData has shader storage buffers");
        }
        for (DrawCommand drawCommand : drawCommands) {
            if (drawCommand.instanceCount() != 1 || drawCommand.baseInstance() != 0) {
                throw new IllegalArgumentException("DrawBatchData has instanced draw commands");
            }
        }

        final int id = this.idGenerator.getAndIncrement();

        final Buffer indexBuffer = vertexArray.getIndexBuffer();
        if (indexBuffer != null) {
            if (vertexArray.getIndexType() != GL11C.GL_UNSIGNED_INT) {
                throw new IllegalArgumentException("DrawBatchData has unsupported index type");
            }

            long indexBufferSize = indexBuffer.getSize();
            if (indexBuffer == ThinGL.quadIndexBuffer().getSharedBuffer()) {
                final DrawElementsCommand drawCommand = (DrawElementsCommand) drawCommands.get(0);
                indexBufferSize = (long) drawCommand.vertexCount() * Integer.BYTES;
            }

            final long alignedSize = MathUtils.align(indexBufferSize, Integer.BYTES);
            final long address = this.indexAllocator.alloc(alignedSize);
            if (address == -1) {
                throw new OutOfMemoryError("Failed to allocate memory for index buffer");
            }
            final int indexAddress = (int) (address / Integer.BYTES);
            if (address % Integer.BYTES != 0) {
                throw new IllegalStateException("Index data is not aligned");
            }
            final long requiredSize = MathUtils.align(address + indexBufferSize, MIN_RESIZE_AMOUNT);
            if (this.indexBuffer.getSize() < requiredSize) {
                this.indexBuffer = BufferUtil.resize(this.indexBuffer, requiredSize);
                this.vertexArray.setIndexBuffer(GL11C.GL_UNSIGNED_INT, this.indexBuffer);
            }
            indexBuffer.copyTo(this.indexBuffer, 0L, address, indexBufferSize);
            drawCommands.replaceAll(drawCommand -> ((DrawElementsCommand) drawCommand).withIndexOffset(indexAddress));
            this.storedIndexBuffers.put(id, address);
        }

        final long alignedSize = MathUtils.align(vertexBuffer.getSize(), this.drawBatch.vertexDataLayout().getSize());
        final long address = this.vertexAllocator.alloc(alignedSize);
        if (address == -1) {
            throw new OutOfMemoryError("Failed to allocate memory for vertex buffer");
        }
        final int vertexAddress = (int) (address / this.drawBatch.vertexDataLayout().getSize());
        if (address % this.drawBatch.vertexDataLayout().getSize() != 0) {
            throw new IllegalStateException("Vertex data is not aligned");
        }
        final long requiredSize = MathUtils.align(address + vertexBuffer.getSize(), MIN_RESIZE_AMOUNT);
        if (this.vertexBuffer.getSize() < requiredSize) {
            this.vertexBuffer = BufferUtil.resize(this.vertexBuffer, requiredSize);
            this.vertexArray.setVertexBuffer(0, this.vertexBuffer, 0, this.drawBatch.vertexDataLayout().getSize());
        }
        vertexBuffer.copyTo(this.vertexBuffer, 0L, address, vertexBuffer.getSize());
        drawCommands.replaceAll(drawCommand -> drawCommand.withVertexOffset(vertexAddress));
        this.storedVertexBuffers.put(id, address);

        this.bufferDrawCommands.put(id, drawCommands);
        return id;
    }

    public void removeBuffer(final int id) {
        if (!this.storedVertexBuffers.containsKey(id)) {
            throw new IllegalArgumentException("DrawBatchData is not uploaded");
        }
        this.removeFromRenderList(id);
        final long vertexAddress = this.storedVertexBuffers.remove(id);
        this.vertexAllocator.free(vertexAddress);
        if (this.storedIndexBuffers.containsKey(id)) {
            final long indexAddress = this.storedIndexBuffers.remove(id);
            this.indexAllocator.free(indexAddress);
        }
        this.bufferDrawCommands.remove(id);
    }

    public void clearBuffers() {
        for (int id : this.storedVertexBuffers.keySet().toIntArray()) {
            this.removeBuffer(id);
        }
        this.idGenerator.set(0);
    }

    public void addToRenderList(final int id) {
        if (!this.storedVertexBuffers.containsKey(id)) {
            throw new IllegalArgumentException("DrawBatchData is not uploaded");
        }
        this.renderBuffers.add(id);
    }

    public void removeFromRenderList(final int id) {
        if (!this.storedVertexBuffers.containsKey(id)) {
            throw new IllegalArgumentException("DrawBatchData is not uploaded");
        }
        this.renderBuffers.remove(id);
    }

    public void clearRenderList() {
        this.renderBuffers.clear();
    }

    public void rebuildCommandBuffer() {
        final List<DrawCommand> drawCommands = new ArrayList<>(this.renderBuffers.size());
        for (int id : this.renderBuffers) {
            drawCommands.addAll(this.bufferDrawCommands.get(id));
        }
        final MemoryBuffer commandMemoryBuffer = ThinGL.memoryBufferPool().borrowMemoryBuffer();
        commandMemoryBuffer.ensureCanWrite((long) drawCommands.size() * DrawCommand.BYTES);
        for (DrawCommand drawCommand : drawCommands) {
            drawCommand.write(commandMemoryBuffer);
        }
        final Memory commandData = commandMemoryBuffer.finish();
        this.commandBuffer.ensureSize(commandData.getSize());
        this.commandBuffer.upload(commandData);
        ThinGL.memoryBufferPool().returnMemoryBuffer(commandMemoryBuffer);
        this.uploadedDrawBatchData = new UploadedDrawBatchData(this.drawBatch, this.vertexArray, new HashMap<>(), new HashMap<>(), this.commandBuffer, drawCommands);
    }

    public void free() {
        DrawBatchDataUploader.freePersistentData(this.uploadedDrawBatchData);
    }

    public ArenaMemoryAllocator getVertexAllocator() {
        return this.vertexAllocator;
    }

    public ArenaMemoryAllocator getIndexAllocator() {
        return this.indexAllocator;
    }

    public UploadedDrawBatchData getUploadedDrawBatchData() {
        return this.uploadedDrawBatchData;
    }

}
