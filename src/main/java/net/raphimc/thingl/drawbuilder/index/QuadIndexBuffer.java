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

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.drawbuilder.builder.BufferBuilder;
import net.raphimc.thingl.drawbuilder.databuilder.holder.IndexDataHolder;
import net.raphimc.thingl.resource.buffer.AbstractBuffer;
import net.raphimc.thingl.resource.buffer.Buffer;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class QuadIndexBuffer {

    private static final int QUAD_INDEX_COUNT = 6;
    private static final int INT_SIZE = 4;
    private static final Buffer INT_BUFFER = new Buffer(0L, GL15C.GL_STATIC_DRAW);
    private static ByteBuffer BYTE_BUFFER = null;

    static {
        ensureSize(4096);
        INT_BUFFER.setDebugName("Quad Index Buffer");
    }

    public static void ensureSize(final int quadCount) {
        ThinGL.assertOnRenderThread();
        if (INT_BUFFER.getSize() / QUAD_INDEX_COUNT / INT_SIZE < quadCount) {
            if (BYTE_BUFFER != null) {
                MemoryUtil.memFree(BYTE_BUFFER);
            }
            BYTE_BUFFER = createIndexBuffer(quadCount);
            INT_BUFFER.setSize(BYTE_BUFFER.remaining());
            INT_BUFFER.upload(0, BYTE_BUFFER);
        }
    }

    public static ByteBuffer createIndexBuffer(final int quadCount) {
        final BufferBuilder bufferBuilder = new BufferBuilder(quadCount * QUAD_INDEX_COUNT * INT_SIZE);
        final IndexDataHolder indexDataHolder = new IndexDataHolder(bufferBuilder);
        for (int i = 0; i < quadCount; i++) {
            indexDataHolder.quad();
        }
        return bufferBuilder.finish();
    }

    public static AbstractBuffer getSharedGlBuffer() {
        return INT_BUFFER;
    }

    public static ByteBuffer getSharedByteBuffer() {
        return BYTE_BUFFER;
    }

}
