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

package net.raphimc.thingl.drawbuilder.builder;

import net.raphimc.thingl.util.MathUtil;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class BufferBuilder implements AutoCloseable {

    private static final int DEFAULT_INITIAL_SIZE = 64 * 1024;

    private long baseAddress;
    private long cursorAddress;
    private int size;

    public BufferBuilder() {
        this(DEFAULT_INITIAL_SIZE);
    }

    public BufferBuilder(final int initialSize) {
        this.baseAddress = MemoryUtil.nmemAlloc(initialSize);
        if (this.baseAddress == 0) {
            throw new OutOfMemoryError("Failed to allocate memory of size: " + initialSize);
        }
        this.cursorAddress = this.baseAddress;
        this.size = initialSize;
    }

    public final BufferBuilder putByte(final byte b) {
        this.ensureHasEnoughSpace(1);
        MemoryUtil.memPutByte(this.cursorAddress, b);
        this.cursorAddress++;
        return this;
    }

    public final BufferBuilder putShort(final short s) {
        this.ensureHasEnoughSpace(2);
        MemoryUtil.memPutShort(this.cursorAddress, s);
        this.cursorAddress += 2;
        return this;
    }

    public final BufferBuilder putInt(final int i) {
        this.ensureHasEnoughSpace(4);
        MemoryUtil.memPutInt(this.cursorAddress, i);
        this.cursorAddress += 4;
        return this;
    }

    public final BufferBuilder putFloat(final float f) {
        this.ensureHasEnoughSpace(4);
        MemoryUtil.memPutFloat(this.cursorAddress, f);
        this.cursorAddress += 4;
        return this;
    }

    public final BufferBuilder putDouble(final double d) {
        this.ensureHasEnoughSpace(8);
        MemoryUtil.memPutDouble(this.cursorAddress, d);
        this.cursorAddress += 8;
        return this;
    }

    public final BufferBuilder putBytes(final byte... b) {
        this.ensureHasEnoughSpace(b.length);
        for (byte v : b) {
            MemoryUtil.memPutByte(this.cursorAddress, v);
            this.cursorAddress++;
        }
        return this;
    }

    public final BufferBuilder putShorts(final short... s) {
        this.ensureHasEnoughSpace(s.length * 2);
        for (short v : s) {
            MemoryUtil.memPutShort(this.cursorAddress, v);
            this.cursorAddress += 2;
        }
        return this;
    }

    public final BufferBuilder putInts(final int... i) {
        this.ensureHasEnoughSpace(i.length * 4);
        for (int v : i) {
            MemoryUtil.memPutInt(this.cursorAddress, v);
            this.cursorAddress += 4;
        }
        return this;
    }

    public final BufferBuilder putFloats(final float... f) {
        this.ensureHasEnoughSpace(f.length * 4);
        for (float v : f) {
            MemoryUtil.memPutFloat(this.cursorAddress, v);
            this.cursorAddress += 4;
        }
        return this;
    }

    public final BufferBuilder putDoubles(final double... d) {
        this.ensureHasEnoughSpace(d.length * 8);
        for (double v : d) {
            MemoryUtil.memPutDouble(this.cursorAddress, v);
            this.cursorAddress += 8;
        }
        return this;
    }

    public final BufferBuilder putHalfFloat(final float f) {
        return this.putShort(MathUtil.encodeHalfFloat(f));
    }

    public final BufferBuilder putVec2f(final Vector2f vec2f) {
        return this.putVec2f(vec2f.x, vec2f.y);
    }

    public final BufferBuilder putVec2f(final float x, final float y) {
        this.ensureHasEnoughSpace(8);
        MemoryUtil.memPutFloat(this.cursorAddress, x);
        MemoryUtil.memPutFloat(this.cursorAddress + 4, y);
        this.cursorAddress += 8;
        return this;
    }

    public final BufferBuilder putVec3f(final Vector3f vec3f) {
        return this.putVec3f(vec3f.x, vec3f.y, vec3f.z);
    }

    public final BufferBuilder putVec3f(final float x, final float y, final float z) {
        this.ensureHasEnoughSpace(12);
        MemoryUtil.memPutFloat(this.cursorAddress, x);
        MemoryUtil.memPutFloat(this.cursorAddress + 4, y);
        MemoryUtil.memPutFloat(this.cursorAddress + 8, z);
        this.cursorAddress += 12;
        return this;
    }

    public final BufferBuilder align(final int alignment) {
        final int position = this.getPosition();
        final int alignedPosition = MathUtil.align(position, alignment);
        final int paddingLength = alignedPosition - position;
        this.ensureHasEnoughSpace(paddingLength);
        this.cursorAddress += paddingLength;
        return this;
    }

    public final BufferBuilder skip(final int bytes) {
        this.ensureHasEnoughSpace(bytes);
        this.cursorAddress += bytes;
        return this;
    }

    public ByteBuffer finish() {
        final int position = this.getPosition();
        final ByteBuffer byteBuffer = MemoryUtil.memByteBuffer(this.baseAddress, position);
        this.reset();
        return byteBuffer;
    }

    public void reset() {
        this.cursorAddress = this.baseAddress;
    }

    @Override
    public void close() {
        MemoryUtil.nmemFree(this.baseAddress);
        this.baseAddress = 0;
        this.cursorAddress = 0;
        this.size = 0;
    }

    public void ensureHasEnoughSpace(final int size) {
        if (this.baseAddress + this.size < this.cursorAddress + size) {
            this.resize(MathUtil.align(this.size + Math.max(size, 512 * 1024), 8 * 1024));
        }
    }

    public long getBaseAddress() {
        return this.baseAddress;
    }

    public void setCursorAddress(final long cursorAddress) {
        if (cursorAddress < this.baseAddress || cursorAddress > this.baseAddress + this.size) {
            throw new IllegalArgumentException("Cursor address is out of bounds");
        }

        this.cursorAddress = cursorAddress;
    }

    public long getCursorAddress() {
        return this.cursorAddress;
    }

    public int getSize() {
        return this.size;
    }

    public int getPosition() {
        return (int) (this.cursorAddress - this.baseAddress);
    }

    private void resize(final int newSize) {
        final int position = this.getPosition();
        final long newAddress = MemoryUtil.nmemRealloc(this.baseAddress, newSize);
        if (newAddress == 0) {
            throw new OutOfMemoryError("Failed to allocate memory of size: " + newSize);
        }
        this.baseAddress = newAddress;
        this.cursorAddress = this.baseAddress + position;
        this.size = newSize;
    }

}
