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
package net.raphimc.thingl.gl.util;

import net.raphimc.thingl.gl.resource.buffer.Buffer;
import net.raphimc.thingl.gl.resource.buffer.impl.MutableBuffer;
import net.raphimc.thingl.memory.MemoryBuffer;
import net.raphimc.thingl.rendering.bufferbuilder.impl.IndexBufferBuilder;
import net.raphimc.thingl.resource.memory.Memory;
import org.lwjgl.opengl.GL15C;

public class QuadIndexBuffer {

    public static final int QUAD_VERTEX_COUNT = 4;
    public static final int QUAD_INDEX_COUNT = 6;

    private final MutableBuffer indexBuffer = new MutableBuffer(0L, GL15C.GL_DYNAMIC_DRAW);
    private Memory indexData = null;

    public QuadIndexBuffer() {
        this.ensureSize(4096);
        this.indexBuffer.setDebugName("Quad Index Buffer");
    }

    public void ensureSize(final int quadCount) {
        if (this.indexBuffer.getSize() / QUAD_INDEX_COUNT / Integer.BYTES < quadCount) {
            if (this.indexData != null) {
                this.indexData.free();
            }
            this.indexData = this.createIndexData(quadCount);
            this.indexBuffer.initialize(this.indexData, this.indexBuffer.getUsage());
        }
    }

    public Memory createIndexData(final int quadCount) {
        final MemoryBuffer memoryBuffer = new MemoryBuffer((long) quadCount * QUAD_INDEX_COUNT * Integer.BYTES);
        final IndexBufferBuilder indexBufferBuilder = new IndexBufferBuilder(memoryBuffer);
        for (int i = 0; i < quadCount; i++) {
            indexBufferBuilder.writeQuad();
        }
        return memoryBuffer.finish();
    }

    public Buffer getSharedBuffer() {
        return this.indexBuffer;
    }

    public Memory getSharedData() {
        return this.indexData;
    }

    public void free() {
        this.indexBuffer.free();
        this.indexData.free();
    }

}
