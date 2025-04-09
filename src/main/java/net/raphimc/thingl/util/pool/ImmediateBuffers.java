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

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.drawbuilder.builder.command.DrawCommand;
import net.raphimc.thingl.resource.buffer.Buffer;
import net.raphimc.thingl.resource.vertexarray.VertexArray;
import net.raphimc.thingl.util.BufferUtil;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.opengl.GL15C;

public class ImmediateBuffers {

    private final Buffer indexBuffer;
    private final Buffer instanceBuffer;
    private final Buffer[] ssboBuffers = new Buffer[3];
    private final Buffer commandBuffer;
    private final VertexArray postProcessingVao;

    @ApiStatus.Internal
    public ImmediateBuffers(final ThinGL thinGL) {
        this.indexBuffer = new Buffer(BufferUtil.DEFAULT_BUFFER_SIZE, GL15C.GL_STREAM_DRAW);
        this.indexBuffer.setDebugName("Immediate Index Buffer");
        this.instanceBuffer = new Buffer(BufferUtil.DEFAULT_BUFFER_SIZE, GL15C.GL_STREAM_DRAW);
        this.instanceBuffer.setDebugName("Immediate Instance Buffer");
        for (int i = 0; i < this.ssboBuffers.length; i++) {
            this.ssboBuffers[i] = new Buffer(BufferUtil.DEFAULT_BUFFER_SIZE, GL15C.GL_STREAM_DRAW);
            this.ssboBuffers[i].setDebugName("Immediate SSBO Buffer " + i);
        }
        this.commandBuffer = new Buffer(DrawCommand.SIZE * 512L, GL15C.GL_STREAM_DRAW);
        this.commandBuffer.setDebugName("Immediate Command Buffer");
        this.postProcessingVao = new VertexArray();
        this.postProcessingVao.setDebugName("Post-Processing VAO");
    }

    public Buffer getIndexBuffer() {
        return this.indexBuffer;
    }

    public Buffer getInstanceBuffer() {
        return this.instanceBuffer;
    }

    public Buffer getSSBOBuffer(final int index) {
        if (index < 0 || index >= this.ssboBuffers.length) {
            throw new IndexOutOfBoundsException("Index must be between 0 and " + (this.ssboBuffers.length - 1));
        }
        return this.ssboBuffers[index];
    }

    public Buffer getCommandBuffer() {
        return this.commandBuffer;
    }

    public VertexArray getPostProcessingVao() {
        return this.postProcessingVao;
    }

    @ApiStatus.Internal
    public void free() {
        this.indexBuffer.free();
        this.instanceBuffer.free();
        for (Buffer ssboBuffer : this.ssboBuffers) {
            ssboBuffer.free();
        }
        this.commandBuffer.free();
        this.postProcessingVao.free();
    }

}
