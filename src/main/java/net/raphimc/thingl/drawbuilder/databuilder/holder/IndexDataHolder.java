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
package net.raphimc.thingl.drawbuilder.databuilder.holder;

import net.raphimc.thingl.drawbuilder.builder.BufferBuilder;
import net.raphimc.thingl.drawbuilder.databuilder.writer.BufferWriter;
import net.raphimc.thingl.drawbuilder.index.QuadIndexBuffer;

public class IndexDataHolder extends BufferWriter<IndexDataHolder> {

    private int indexCount;
    private int vertexOffset;

    public IndexDataHolder(final BufferBuilder bufferBuilder) {
        super(bufferBuilder);
    }

    public void applyVertexOffset(final VertexDataHolder vertexDataHolder) {
        this.vertexOffset = vertexDataHolder.getVertexCount();
    }

    public IndexDataHolder putRelativeIndex(final int i) {
        this.bufferBuilder.putInt(this.vertexOffset + i);
        this.indexCount++;
        return this;
    }

    public IndexDataHolder putAbsoluteIndex(final int i) {
        this.bufferBuilder.putInt(i);
        this.indexCount++;
        return this;
    }

    public IndexDataHolder putQuad() {
        return this.putQuad(0, 1, 2, 3);
    }

    public IndexDataHolder putQuad(final int i1, final int i2, final int i3, final int i4) {
        this.bufferBuilder.putInt(this.vertexOffset + i1).putInt(this.vertexOffset + i2).putInt(this.vertexOffset + i3).putInt(this.vertexOffset + i3).putInt(this.vertexOffset + i4).putInt(this.vertexOffset + i1);
        this.indexCount += QuadIndexBuffer.QUAD_INDEX_COUNT;
        this.vertexOffset += QuadIndexBuffer.QUAD_VERTEX_COUNT;
        return this;
    }

    public int getIndexCount() {
        return this.indexCount;
    }

    @Deprecated(forRemoval = true)
    public IndexDataHolder putIndex(final int i) {
        return this.putRelativeIndex(i);
    }

}
