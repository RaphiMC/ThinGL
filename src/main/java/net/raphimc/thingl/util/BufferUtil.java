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

import net.raphimc.thingl.resource.buffer.Buffer;
import net.raphimc.thingl.resource.buffer.ImmutableBuffer;
import net.raphimc.thingl.resource.buffer.MutableBuffer;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class BufferUtil {

    public static final long DEFAULT_BUFFER_SIZE = 256 * 1024L;

    public static Buffer uploadResizing(Buffer buffer, final ByteBuffer dataBuffer) {
        buffer = ensureSize(buffer, dataBuffer.remaining());
        buffer.upload(dataBuffer);
        return buffer;
    }

    public static Buffer resize(final Buffer buffer, final long size) {
        if (buffer.getSize() >= size) {
            return buffer;
        }

        if (buffer instanceof ImmutableBuffer immutableBuffer) {
            final ImmutableBuffer newBuffer = new ImmutableBuffer(size, immutableBuffer.getFlags());
            newBuffer.setDebugName(immutableBuffer.getDebugName());
            immutableBuffer.copyTo(newBuffer, 0, 0, immutableBuffer.getSize());
            immutableBuffer.free();
            return newBuffer;
        } else if (buffer instanceof MutableBuffer mutableBuffer) {
            final MutableBuffer newBuffer = new MutableBuffer(size, mutableBuffer.getUsage());
            newBuffer.setDebugName(mutableBuffer.getDebugName());
            mutableBuffer.copyTo(newBuffer, 0, 0, mutableBuffer.getSize());
            mutableBuffer.free();
            return newBuffer;
        } else {
            throw new IllegalArgumentException("Unsupported buffer type " + buffer.getClass().getSimpleName());
        }
    }

    private static Buffer ensureSize(final Buffer buffer, final long size) {
        if (buffer instanceof ImmutableBuffer immutableBuffer) {
            if (immutableBuffer.getSize() < size) {
                final String debugName = immutableBuffer.getDebugName();
                final int flags = immutableBuffer.getFlags();
                immutableBuffer.free();
                final ImmutableBuffer newBuffer = new ImmutableBuffer(size, flags);
                newBuffer.setDebugName(debugName);
                return newBuffer;
            }
            return immutableBuffer;
        } else if (buffer instanceof MutableBuffer mutableBuffer) {
            mutableBuffer.ensureSize(size);
            return mutableBuffer;
        } else {
            throw new IllegalArgumentException("Unsupported buffer type " + buffer.getClass().getSimpleName());
        }
    }

    /**
     * Wrapper method to call {@link MemoryUtil#memFree}. Needed for LWJGL 3.3.3 support.
     *
     * @param buffer The buffer to free
     */
    public static void memFree(final java.nio.Buffer buffer) {
        MemoryUtil.memFree(buffer);
    }

    public static ByteBuffer memAlloc(final int size, final boolean direct) {
        if (direct) {
            return MemoryUtil.memAlloc(size);
        } else {
            return ByteBuffer.allocate(size);
        }
    }

    public static ByteBuffer memCopy(final ByteBuffer source) {
        return memCopy(source, source.isDirect());
    }

    public static ByteBuffer memCopy(final ByteBuffer source, final boolean direct) {
        if (source == null) {
            return null;
        }
        final ByteBuffer copy = memAlloc(source.remaining(), direct);
        if (source.isDirect() && direct) {
            MemoryUtil.memCopy(source, copy);
        } else {
            final int position = source.position();
            copy.put(source);
            source.position(position);
            copy.flip();
        }
        return copy;
    }

}
