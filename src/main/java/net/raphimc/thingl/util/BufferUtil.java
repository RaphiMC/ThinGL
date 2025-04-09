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

package net.raphimc.thingl.util;

import net.raphimc.thingl.resource.buffer.AbstractBuffer;
import net.raphimc.thingl.resource.buffer.Buffer;
import net.raphimc.thingl.resource.buffer.ImmutableBuffer;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class BufferUtil {

    public static final long DEFAULT_BUFFER_SIZE = 512 * 1024L;

    public static AbstractBuffer uploadResizing(AbstractBuffer abstractBuffer, final ByteBuffer data) {
        abstractBuffer = ensureSize(abstractBuffer, data.remaining());
        abstractBuffer.upload(0, data);
        return abstractBuffer;
    }

    public static AbstractBuffer resize(AbstractBuffer abstractBuffer, final long size) {
        if (abstractBuffer.getSize() >= size) {
            return abstractBuffer;
        }

        if (abstractBuffer instanceof ImmutableBuffer buffer) {
            final ImmutableBuffer newBuffer = new ImmutableBuffer(size, buffer.getFlags());
            newBuffer.setDebugName(buffer.getDebugName());
            GL45C.glCopyNamedBufferSubData(buffer.getGlId(), newBuffer.getGlId(), 0, 0, buffer.getSize());
            buffer.free();
            return newBuffer;
        } else if (abstractBuffer instanceof Buffer buffer) {
            final Buffer newBuffer = new Buffer(size, buffer.getUsage());
            newBuffer.setDebugName(buffer.getDebugName());
            GL45C.glCopyNamedBufferSubData(buffer.getGlId(), newBuffer.getGlId(), 0, 0, buffer.getSize());
            buffer.free();
            return newBuffer;
        } else {
            throw new IllegalArgumentException("Unsupported buffer type " + abstractBuffer.getClass().getSimpleName());
        }
    }

    private static AbstractBuffer ensureSize(final AbstractBuffer abstractBuffer, final long size) {
        if (abstractBuffer instanceof ImmutableBuffer buffer) {
            if (buffer.getSize() < size) {
                final String debugName = buffer.getDebugName();
                buffer.free();
                final ImmutableBuffer newBuffer = new ImmutableBuffer(size, buffer.getFlags());
                newBuffer.setDebugName(debugName);
                return newBuffer;
            }
            return buffer;
        } else if (abstractBuffer instanceof Buffer buffer) {
            if (buffer.getSize() < size) {
                buffer.setSize(size);
            }
            return buffer;
        } else {
            throw new IllegalArgumentException("Unsupported buffer type " + abstractBuffer.getClass().getSimpleName());
        }
    }

    /**
     * Wrapper method to call {@link MemoryUtil#memFree}. Needed for LWJGL 3.3.3 support.
     * @param buffer The buffer to free
     */
    public static void memFree(final java.nio.Buffer buffer) {
        MemoryUtil.memFree(buffer);
    }

}
