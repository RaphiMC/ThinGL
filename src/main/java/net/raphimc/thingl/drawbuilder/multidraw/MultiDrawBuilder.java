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

package net.raphimc.thingl.drawbuilder.multidraw;

import it.unimi.dsi.fastutil.ints.*;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.drawbuilder.DrawBatch;
import net.raphimc.thingl.drawbuilder.builder.BufferBuilder;
import net.raphimc.thingl.drawbuilder.builder.BuiltBuffer;
import net.raphimc.thingl.drawbuilder.builder.command.DrawCommand;
import net.raphimc.thingl.drawbuilder.builder.command.DrawElementsCommand;
import net.raphimc.thingl.drawbuilder.index.IndexType;
import net.raphimc.thingl.resource.buffer.AbstractBuffer;
import net.raphimc.thingl.resource.buffer.Buffer;
import net.raphimc.thingl.resource.buffer.ImmutableBuffer;
import net.raphimc.thingl.resource.vertexarray.VertexArray;
import net.raphimc.thingl.util.ArenaMemoryAllocator;
import net.raphimc.thingl.util.BufferUtil;
import net.raphimc.thingl.util.MathUtil;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL42C;
import org.lwjgl.opengl.GL45C;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiDrawBuilder {

    private static final long MAX_BUFFER_SIZE = 1024 * 1024 * 1024;
    private static final long MIN_RESIZE_AMOUNT = 10 * 1024 * 1024;

    private final DrawBatch drawBatch;
    private final ArenaMemoryAllocator vertexAllocator;
    private final ArenaMemoryAllocator indexAllocator;
    private AbstractBuffer indexBuffer;
    private final Buffer commandBuffer;
    private final VertexArray vertexArray;
    private final AtomicInteger idCounter = new AtomicInteger();
    private final Int2LongMap storedVertexBuffers = new Int2LongOpenHashMap(); // id -> vertex address
    private final Int2LongMap storedIndexBuffers = new Int2LongOpenHashMap(); // id -> index address
    private final Int2ObjectMap<List<DrawCommand>> bufferDrawCommands = new Int2ObjectOpenHashMap<>(); // id -> draw commands
    private final IntSet renderBuffers = new IntLinkedOpenHashSet();
    private BuiltBuffer builtBuffer;

    public MultiDrawBuilder(final DrawBatch drawBatch) {
        this.drawBatch = drawBatch;
        this.vertexAllocator = new ArenaMemoryAllocator(0, MAX_BUFFER_SIZE);
        this.indexAllocator = new ArenaMemoryAllocator(0, MAX_BUFFER_SIZE);
        this.indexBuffer = new ImmutableBuffer(BufferUtil.DEFAULT_BUFFER_SIZE, 0);
        this.commandBuffer = new Buffer(DrawCommand.SIZE * 512L, GL15C.GL_DYNAMIC_DRAW);
        this.vertexArray = new VertexArray();
        this.vertexArray.setVertexBuffer(0, new ImmutableBuffer(BufferUtil.DEFAULT_BUFFER_SIZE, 0), 0, drawBatch.vertexDataLayout().getSize());
        this.vertexArray.configureVertexDataLayout(0, 0, drawBatch.vertexDataLayout(), 0);
        this.rebuildCommandBuffer();
    }

    public int uploadBuffer(final BuiltBuffer builtBuffer) {
        final List<DrawCommand> drawCommands = new ArrayList<>(builtBuffer.drawCommands());
        if (builtBuffer.drawBatch() != this.drawBatch) {
            throw new IllegalArgumentException("BuiltBuffer is not compatible");
        }
        final VertexArray vertexArray = builtBuffer.vertexArray();
        if (vertexArray.getVertexBuffers().size() != 1 || !vertexArray.getVertexBuffers().containsKey(0)) {
            throw new IllegalArgumentException("BuiltBuffer has more than one vertex buffer");
        }
        final AbstractBuffer vertexBuffer = vertexArray.getVertexBuffers().get(0);
        if (!builtBuffer.shaderDataBuffers().isEmpty()) {
            throw new IllegalArgumentException("BuiltBuffer has shader data buffers");
        }

        final int id = this.idCounter.getAndIncrement();

        final AbstractBuffer indexBuffer = vertexArray.getIndexBuffer();
        if (indexBuffer != null) {
            if (vertexArray.getIndexType() != IndexType.UNSIGNED_INT) {
                throw new IllegalArgumentException("BuiltBuffer has unsupported index type");
            }

            long indexBufferSize = indexBuffer.getSize();
            if (indexBuffer == ThinGL.quadIndexBuffer().getSharedGlBuffer()) {
                final DrawElementsCommand drawCommand = (DrawElementsCommand) drawCommands.get(0);
                indexBufferSize = (long) drawCommand.vertexCount() * Integer.BYTES;
            }

            final long alignedSize = MathUtil.align(indexBufferSize, Integer.BYTES);
            final long address = this.indexAllocator.alloc(alignedSize);
            if (address == -1) {
                throw new OutOfMemoryError("Failed to allocate memory for index buffer");
            }
            final int indexAddress = (int) (address / Integer.BYTES);
            if (address % Integer.BYTES != 0) {
                throw new IllegalStateException("Index data is not aligned");
            }
            final long requiredSize = MathUtil.align(address + indexBufferSize, MIN_RESIZE_AMOUNT);
            if (this.indexBuffer.getSize() < requiredSize) {
                this.indexBuffer = BufferUtil.resize(this.indexBuffer, requiredSize);
            }
            GL45C.glCopyNamedBufferSubData(indexBuffer.getGlId(), this.indexBuffer.getGlId(), 0, address, indexBufferSize);
            drawCommands.replaceAll(drawCommand -> ((DrawElementsCommand) drawCommand).withIndexOffset(indexAddress));
            this.storedIndexBuffers.put(id, address);
        }

        final long alignedSize = MathUtil.align(vertexBuffer.getSize(), this.drawBatch.vertexDataLayout().getSize());
        final long address = this.vertexAllocator.alloc(alignedSize);
        if (address == -1) {
            throw new OutOfMemoryError("Failed to allocate memory for vertex buffer");
        }
        final int vertexAddress = (int) (address / this.drawBatch.vertexDataLayout().getSize());
        if (address % this.drawBatch.vertexDataLayout().getSize() != 0) {
            throw new IllegalStateException("Vertex data is not aligned");
        }
        final long requiredSize = MathUtil.align(address + vertexBuffer.getSize(), MIN_RESIZE_AMOUNT);
        if (this.vertexArray.getVertexBuffers().get(0).getSize() < requiredSize) {
            this.vertexArray.setVertexBuffer(0, BufferUtil.resize(this.vertexArray.getVertexBuffers().get(0), requiredSize), 0, this.drawBatch.vertexDataLayout().getSize());
        }
        GL45C.glCopyNamedBufferSubData(vertexBuffer.getGlId(), this.vertexArray.getVertexBuffers().get(0).getGlId(), 0, address, vertexBuffer.getSize());
        drawCommands.replaceAll(drawCommand -> drawCommand.withVertexOffset(vertexAddress));
        this.storedVertexBuffers.put(id, address);

        this.bufferDrawCommands.put(id, drawCommands);
        GL42C.glMemoryBarrier(GL42C.GL_BUFFER_UPDATE_BARRIER_BIT);
        return id;
    }

    public void removeBuffer(final int id) {
        if (!this.storedVertexBuffers.containsKey(id)) {
            throw new IllegalArgumentException("BuiltBuffer is not uploaded");
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
        this.idCounter.set(0);
    }

    public void addToRenderList(final int id) {
        if (!this.storedVertexBuffers.containsKey(id)) {
            throw new IllegalArgumentException("BuiltBuffer is not uploaded");
        }
        this.renderBuffers.add(id);
    }

    public void removeFromRenderList(final int id) {
        if (!this.storedVertexBuffers.containsKey(id)) {
            throw new IllegalArgumentException("BuiltBuffer is not uploaded");
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
        final BufferBuilder commandBufferBuilder = ThinGL.bufferBuilderPool().borrowBufferBuilder();
        commandBufferBuilder.ensureHasEnoughSpace(drawCommands.size() * DrawCommand.SIZE);
        this.vertexArray.setIndexBuffer(null, null);
        for (DrawCommand drawCommand : drawCommands) {
            if (drawCommand instanceof DrawElementsCommand && this.vertexArray.getIndexBuffer() == null) {
                this.vertexArray.setIndexBuffer(IndexType.UNSIGNED_INT, this.indexBuffer);
            }

            drawCommand.write(commandBufferBuilder);
        }
        final ByteBuffer commandData = commandBufferBuilder.finish();
        if (this.commandBuffer.getSize() < commandData.remaining()) {
            this.commandBuffer.setSize(commandData.remaining());
        }
        this.commandBuffer.upload(0, commandData);
        ThinGL.bufferBuilderPool().returnBufferBuilder(commandBufferBuilder);
        this.builtBuffer = new BuiltBuffer(this.drawBatch, this.vertexArray, new HashMap<>(), this.commandBuffer, drawCommands);
    }

    public void free() {
        this.builtBuffer.free();
        this.indexBuffer.free();
    }

    public ArenaMemoryAllocator getVertexAllocator() {
        return this.vertexAllocator;
    }

    public ArenaMemoryAllocator getIndexAllocator() {
        return this.indexAllocator;
    }

    public BuiltBuffer getBuiltBuffer() {
        return this.builtBuffer;
    }

}
