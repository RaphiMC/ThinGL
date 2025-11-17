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
package net.raphimc.thingl.memory.allocator;

import net.raphimc.thingl.memory.allocator.impl.UnsafeMemoryAllocator;
import net.raphimc.thingl.resource.memory.Memory;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public abstract class MemoryAllocator {

    public static final MemoryAllocator INSTANCE = UnsafeMemoryAllocator.INSTANCE;

    public static Memory wrapMemory(final ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            return null;
        }
        if (!byteBuffer.isDirect()) {
            throw new IllegalArgumentException("ByteBuffer must be direct");
        }

        return wrapMemory(MemoryUtil.memAddress(byteBuffer), byteBuffer.remaining());
    }

    public static Memory wrapMemory(final long address, final long size) {
        if (address != 0L) {
            return INSTANCE.wrap(address, size);
        } else {
            return null;
        }
    }

    public static Memory allocateMemory(final byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        final Memory memory = allocateMemory(bytes.length);
        memory.putBytes(0L, bytes);
        return memory;
    }

    public static Memory allocateMemory(final long size) {
        return INSTANCE.allocate(size);
    }

    public static Memory reallocateMemory(final Memory memory, final long newSize) {
        return INSTANCE.reallocate(memory, newSize);
    }

    public static Memory copyMemory(final Memory memory) {
        return INSTANCE.copy(memory);
    }

    public static void freeMemory(final Memory memory) {
        INSTANCE.free(memory);
    }


    public abstract Memory wrap(final long address, final long size);

    public abstract Memory allocate(final long size);

    public abstract Memory reallocate(final Memory memory, final long newSize);

    public Memory copy(final Memory memory) {
        if (memory == null) {
            return null;
        }
        final Memory newMemory = this.allocate(memory.getSize());
        memory.copyTo(newMemory);
        return newMemory;
    }

    public abstract void free(final Memory memory);

}
