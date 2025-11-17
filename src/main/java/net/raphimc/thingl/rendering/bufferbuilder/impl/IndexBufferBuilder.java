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
package net.raphimc.thingl.rendering.bufferbuilder.impl;

import net.raphimc.thingl.gl.util.QuadIndexBuffer;
import net.raphimc.thingl.memory.MemoryBuffer;
import net.raphimc.thingl.rendering.bufferbuilder.BufferBuilder;

public class IndexBufferBuilder extends BufferBuilder<IndexBufferBuilder> {

    private int indexCount;
    private int vertexOffset;

    public IndexBufferBuilder(final MemoryBuffer memoryBuffer) {
        super(memoryBuffer);
    }

    public void applyVertexOffset(final VertexBufferBuilder vertexBufferBuilder) {
        this.vertexOffset = vertexBufferBuilder.getVertexCount();
    }

    public IndexBufferBuilder writeRelativeIndex(final int i) {
        this.memoryBuffer.writeInt(this.vertexOffset + i);
        this.indexCount++;
        return this;
    }

    public IndexBufferBuilder writeAbsoluteIndex(final int i) {
        this.memoryBuffer.writeInt(i);
        this.indexCount++;
        return this;
    }

    public IndexBufferBuilder writeQuad() {
        return this.writeQuad(0, 1, 2, 3);
    }

    public IndexBufferBuilder writeQuad(final int i1, final int i2, final int i3, final int i4) {
        this.memoryBuffer.writeInt(this.vertexOffset + i1).writeInt(this.vertexOffset + i2).writeInt(this.vertexOffset + i3).writeInt(this.vertexOffset + i3).writeInt(this.vertexOffset + i4).writeInt(this.vertexOffset + i1);
        this.indexCount += QuadIndexBuffer.QUAD_INDEX_COUNT;
        this.vertexOffset += QuadIndexBuffer.QUAD_VERTEX_COUNT;
        return this;
    }

    public int getIndexCount() {
        return this.indexCount;
    }

}
