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
package net.raphimc.thingl.drawbuilder.index;

import net.raphimc.thingl.drawbuilder.builder.BufferBuilder;
import net.raphimc.thingl.drawbuilder.databuilder.holder.IndexDataHolder;
import net.raphimc.thingl.resource.buffer.Buffer;
import net.raphimc.thingl.resource.buffer.MutableBuffer;
import net.raphimc.thingl.util.BufferUtil;
import org.lwjgl.opengl.GL15C;

import java.nio.ByteBuffer;

public class QuadIndexBuffer {

    public static final int QUAD_VERTEX_COUNT = 4;
    public static final int QUAD_INDEX_COUNT = 6;

    private final MutableBuffer indexBuffer = new MutableBuffer(0L, GL15C.GL_DYNAMIC_DRAW);
    private ByteBuffer indexData = null;

    public QuadIndexBuffer() {
        this.ensureSize(4096);
        this.indexBuffer.setDebugName("Quad Index Buffer");
    }

    public void ensureSize(final int quadCount) {
        if (this.indexBuffer.getSize() / QUAD_INDEX_COUNT / Integer.BYTES < quadCount) {
            if (this.indexData != null) {
                BufferUtil.memFree(this.indexData);
            }
            this.indexData = this.createIndexData(quadCount);
            this.indexBuffer.setSize(this.indexData.remaining());
            this.indexBuffer.upload(this.indexData);
        }
    }

    public ByteBuffer createIndexData(final int quadCount) {
        final BufferBuilder bufferBuilder = new BufferBuilder(quadCount * QUAD_INDEX_COUNT * Integer.BYTES);
        final IndexDataHolder indexDataHolder = new IndexDataHolder(bufferBuilder);
        for (int i = 0; i < quadCount; i++) {
            indexDataHolder.putQuad();
        }
        return bufferBuilder.finish();
    }

    public Buffer getSharedBuffer() {
        return this.indexBuffer;
    }

    public ByteBuffer getSharedData() {
        return this.indexData;
    }

    public void free() {
        this.indexBuffer.free();
        BufferUtil.memFree(this.indexData);
    }

}
