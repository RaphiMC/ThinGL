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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.raphimc.thingl.memory.MemoryBuffer;
import net.raphimc.thingl.rendering.bufferbuilder.DataBufferBuilder;
import net.raphimc.thingl.util.MathUtil;

public class VertexBufferBuilder extends DataBufferBuilder<VertexBufferBuilder> {

    private int vertexCount;
    private IntList connectedPrimitiveIndices;

    public VertexBufferBuilder(final MemoryBuffer memoryBuffer) {
        super(memoryBuffer);
    }

    public VertexBufferBuilder writeByte(final byte b) {
        this.memoryBuffer.writeByte(b);
        return this;
    }

    public VertexBufferBuilder writeShort(final short s) {
        this.memoryBuffer.writeShort(s);
        return this;
    }

    public VertexBufferBuilder writeHalfFloat(final float f) {
        return this.writeShort(MathUtil.encodeHalfFloat(f));
    }

    public VertexBufferBuilder writeTextureCoord(final float u, final float v) {
        this.memoryBuffer.writeFloat(u).writeFloat(v);
        return this;
    }

    public int endVertex() {
        return this.vertexCount++;
    }

    public void endConnectedPrimitive() {
        if (this.vertexCount == 0) {
            throw new IllegalStateException("Cannot end connected primitive without writing any vertices");
        }
        if (this.connectedPrimitiveIndices == null) {
            this.connectedPrimitiveIndices = new IntArrayList();
            this.connectedPrimitiveIndices.add(0);
        }
        if (this.connectedPrimitiveIndices.getInt(this.connectedPrimitiveIndices.size() - 1) != this.vertexCount) {
            this.connectedPrimitiveIndices.add(this.vertexCount);
        }
    }

    public int getVertexCount() {
        return this.vertexCount;
    }

    public IntList getConnectedPrimitiveIndices() {
        if (this.connectedPrimitiveIndices != null) {
            this.endConnectedPrimitive(); // End the last connected primitive if it wasn't ended yet
        }
        return this.connectedPrimitiveIndices;
    }

}
