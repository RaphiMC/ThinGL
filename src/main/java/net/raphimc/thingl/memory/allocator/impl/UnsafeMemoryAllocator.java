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
package net.raphimc.thingl.memory.allocator.impl;

import net.raphimc.thingl.memory.allocator.MemoryAllocator;
import net.raphimc.thingl.resource.memory.Memory;
import net.raphimc.thingl.resource.memory.impl.UnsafeMemory;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class UnsafeMemoryAllocator extends MemoryAllocator {

    public static final UnsafeMemoryAllocator INSTANCE = new UnsafeMemoryAllocator();
    public static final Unsafe UNSAFE;
    public static final Integer BYTE_ARRAY_BASE_OFFSET;

    static {
        Unsafe unsafeInstance = null;
        Integer byteArrayBaseOffset = null;
        try {
            for (Field field : Unsafe.class.getDeclaredFields()) {
                if (field.getType() == Unsafe.class && Modifier.isStatic(field.getModifiers())) {
                    field.setAccessible(true);
                    unsafeInstance = (Unsafe) field.get(null);
                    break;
                }
            }
            if (unsafeInstance == null) {
                throw new IllegalStateException("Failed to obtain Unsafe instance");
            }
            if (unsafeInstance.arrayIndexScale(byte[].class) == 1) {
                byteArrayBaseOffset = unsafeInstance.arrayBaseOffset(byte[].class);
            }
        } catch (Throwable ignored) {
        }
        UNSAFE = unsafeInstance;
        BYTE_ARRAY_BASE_OFFSET = byteArrayBaseOffset;
    }

    @Override
    public Memory wrap(final long address, final long size) {
        if (address != 0L) {
            return new UnsafeMemory(address, size);
        } else {
            return null;
        }
    }

    @Override
    public Memory allocate(final long size) {
        final long address = UNSAFE.allocateMemory(size);
        if (address == 0L) {
            throw new OutOfMemoryError("Failed to allocate memory of size: " + size);
        }
        return new UnsafeMemory(address, size);
    }

    @Override
    public UnsafeMemory reallocate(final Memory memory, final long newSize) {
        final UnsafeMemory unsafeMemory = (UnsafeMemory) memory;
        final long newAddress = UNSAFE.reallocateMemory(unsafeMemory.getAddress(), newSize);
        if (newAddress == 0L) {
            throw new OutOfMemoryError("Failed to reallocate memory to size: " + newSize);
        }
        return new UnsafeMemory(newAddress, newSize);
    }

    @Override
    public void free(final Memory memory) {
        final UnsafeMemory unsafeMemory = (UnsafeMemory) memory;
        UNSAFE.freeMemory(unsafeMemory.getAddress());
    }

}
