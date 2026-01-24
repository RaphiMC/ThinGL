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
package net.raphimc.thingl.resource.memory.impl;

import net.raphimc.thingl.memory.allocator.impl.UnsafeMemoryAllocator;
import net.raphimc.thingl.resource.memory.Memory;

@SuppressWarnings("removal")
public class UnsafeMemory extends Memory {

    public UnsafeMemory(final long address, final long size) {
        super(address, size);
    }

    @Override
    public byte getByte(final long offset) {
        return UnsafeMemoryAllocator.UNSAFE.getByte(this.address + offset);
    }

    @Override
    public Memory putByte(final long offset, final byte b) {
        UnsafeMemoryAllocator.UNSAFE.putByte(this.address + offset, b);
        return this;
    }

    @Override
    public short getShort(final long offset) {
        return UnsafeMemoryAllocator.UNSAFE.getShort(this.address + offset);
    }

    @Override
    public Memory putShort(final long offset, final short s) {
        UnsafeMemoryAllocator.UNSAFE.putShort(this.address + offset, s);
        return this;
    }

    @Override
    public int getInt(final long offset) {
        return UnsafeMemoryAllocator.UNSAFE.getInt(this.address + offset);
    }

    @Override
    public Memory putInt(final long offset, final int i) {
        UnsafeMemoryAllocator.UNSAFE.putInt(this.address + offset, i);
        return this;
    }

    @Override
    public long getLong(final long offset) {
        return UnsafeMemoryAllocator.UNSAFE.getLong(this.address + offset);
    }

    @Override
    public Memory putLong(final long offset, final long l) {
        UnsafeMemoryAllocator.UNSAFE.putLong(this.address + offset, l);
        return this;
    }

    @Override
    public float getFloat(final long offset) {
        return UnsafeMemoryAllocator.UNSAFE.getFloat(this.address + offset);
    }

    @Override
    public Memory putFloat(final long offset, final float f) {
        UnsafeMemoryAllocator.UNSAFE.putFloat(this.address + offset, f);
        return this;
    }

    @Override
    public double getDouble(final long offset) {
        return UnsafeMemoryAllocator.UNSAFE.getDouble(this.address + offset);
    }

    @Override
    public Memory putDouble(final long offset, final double d) {
        UnsafeMemoryAllocator.UNSAFE.putDouble(this.address + offset, d);
        return this;
    }

    @Override
    public byte[] getBytes(final long offset, final int length) {
        if (UnsafeMemoryAllocator.BYTE_ARRAY_BASE_OFFSET != null) {
            final byte[] bytes = new byte[length];
            UnsafeMemoryAllocator.UNSAFE.copyMemory(null, this.address + offset, bytes, UnsafeMemoryAllocator.BYTE_ARRAY_BASE_OFFSET, length);
            return bytes;
        } else {
            return super.getBytes(offset, length);
        }
    }

    @Override
    public Memory putBytes(final long offset, final byte... bytes) {
        if (UnsafeMemoryAllocator.BYTE_ARRAY_BASE_OFFSET != null) {
            UnsafeMemoryAllocator.UNSAFE.copyMemory(bytes, UnsafeMemoryAllocator.BYTE_ARRAY_BASE_OFFSET, null, this.address + offset, bytes.length);
            return this;
        } else {
            return super.putBytes(offset, bytes);
        }
    }

    @Override
    public Memory clear(final byte value) {
        UnsafeMemoryAllocator.UNSAFE.setMemory(this.address, this.size, value);
        return this;
    }

    @Override
    public Memory copyTo(final Memory target, final long sourceOffset, final long targetOffset, final long length) {
        UnsafeMemoryAllocator.UNSAFE.copyMemory(this.address + sourceOffset, target.getAddress() + targetOffset, length);
        return this;
    }

    @Override
    public Memory slice(final long offset, final long size) {
        return new UnsafeMemory(this.address + offset, size);
    }

    @Override
    protected void free0() {
        UnsafeMemoryAllocator.INSTANCE.free(this);
    }

}
