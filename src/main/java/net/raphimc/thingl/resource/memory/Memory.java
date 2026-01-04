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
package net.raphimc.thingl.resource.memory;

import net.raphimc.thingl.resource.Resource;
import org.joml.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

@SuppressWarnings("PointlessArithmeticExpression")
public abstract class Memory extends Resource {

    public static final int BYTE_SIZE = Byte.BYTES;
    public static final int SHORT_SIZE = Short.BYTES;
    public static final int INT_SIZE = Integer.BYTES;
    public static final int LONG_SIZE = Long.BYTES;
    public static final int FLOAT_SIZE = Float.BYTES;
    public static final int DOUBLE_SIZE = Double.BYTES;
    public static final int VECTOR2I_SIZE = Integer.BYTES * 2;
    public static final int VECTOR3I_SIZE = Integer.BYTES * 3;
    public static final int VECTOR4I_SIZE = Integer.BYTES * 4;
    public static final int VECTOR2F_SIZE = Float.BYTES * 2;
    public static final int VECTOR3F_SIZE = Float.BYTES * 3;
    public static final int VECTOR4F_SIZE = Float.BYTES * 4;
    public static final int VECTOR2D_SIZE = Double.BYTES * 2;
    public static final int VECTOR3D_SIZE = Double.BYTES * 3;
    public static final int VECTOR4D_SIZE = Double.BYTES * 4;
    public static final int MATRIX3F_SIZE = Float.BYTES * 3 * 3;
    public static final int MATRIX4F_SIZE = Float.BYTES * 4 * 4;
    public static final int MATRIX3D_SIZE = Double.BYTES * 3 * 3;
    public static final int MATRIX4D_SIZE = Double.BYTES * 4 * 4;

    protected final long address;
    protected final long size;

    public Memory(final long address, final long size) {
        this.address = address;
        this.size = size;
    }

    public abstract byte getByte(final long offset);

    public abstract Memory putByte(final long offset, final byte b);

    public abstract short getShort(final long offset);

    public abstract Memory putShort(final long offset, final short s);

    public abstract int getInt(final long offset);

    public abstract Memory putInt(final long offset, final int i);

    public abstract long getLong(final long offset);

    public abstract Memory putLong(final long offset, final long l);

    public abstract float getFloat(final long offset);

    public abstract Memory putFloat(final long offset, final float f);

    public abstract double getDouble(final long offset);

    public abstract Memory putDouble(final long offset, final double d);

    public byte[] getBytes(final long offset, final int length) {
        final byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = this.getByte(offset + i);
        }
        return bytes;
    }

    public Memory putBytes(final long offset, final byte... bytes) {
        for (int i = 0; i < bytes.length; i++) {
            this.putByte(offset + i, bytes[i]);
        }
        return this;
    }

    public Vector2i getVector2i(final long offset) {
        return new Vector2i(
                this.getInt(offset + Integer.BYTES * 0),
                this.getInt(offset + Integer.BYTES * 1)
        );
    }

    public Memory putVector2i(final long offset, final Vector2i vector) {
        return this.putVector2i(offset, vector.x, vector.y);
    }

    public Memory putVector2i(final long offset, final int x, final int y) {
        this.putInt(offset + Integer.BYTES * 0, x);
        this.putInt(offset + Integer.BYTES * 1, y);
        return this;
    }

    public Vector3i getVector3i(final long offset) {
        return new Vector3i(
                this.getInt(offset + Integer.BYTES * 0),
                this.getInt(offset + Integer.BYTES * 1),
                this.getInt(offset + Integer.BYTES * 2)
        );
    }

    public Memory putVector3i(final long offset, final Vector3i vector) {
        return this.putVector3i(offset, vector.x, vector.y, vector.z);
    }

    public Memory putVector3i(final long offset, final int x, final int y, final int z) {
        this.putInt(offset + Integer.BYTES * 0, x);
        this.putInt(offset + Integer.BYTES * 1, y);
        this.putInt(offset + Integer.BYTES * 2, z);
        return this;
    }

    public Vector4i getVector4i(final long offset) {
        return new Vector4i(
                this.getInt(offset + Integer.BYTES * 0),
                this.getInt(offset + Integer.BYTES * 1),
                this.getInt(offset + Integer.BYTES * 2),
                this.getInt(offset + Integer.BYTES * 3)
        );
    }

    public Memory putVector4i(final long offset, final Vector4i vector) {
        return this.putVector4i(offset, vector.x, vector.y, vector.z, vector.w);
    }

    public Memory putVector4i(final long offset, final int x, final int y, final int z, final int w) {
        this.putInt(offset + Integer.BYTES * 0, x);
        this.putInt(offset + Integer.BYTES * 1, y);
        this.putInt(offset + Integer.BYTES * 2, z);
        this.putInt(offset + Integer.BYTES * 3, w);
        return this;
    }

    public Vector2f getVector2f(final long offset) {
        return new Vector2f(
                this.getFloat(offset + Float.BYTES * 0),
                this.getFloat(offset + Float.BYTES * 1)
        );
    }

    public Memory putVector2f(final long offset, final Vector2f vector) {
        return this.putVector2f(offset, vector.x, vector.y);
    }

    public Memory putVector2f(final long offset, final float x, final float y) {
        this.putFloat(offset + Float.BYTES * 0, x);
        this.putFloat(offset + Float.BYTES * 1, y);
        return this;
    }

    public Vector3f getVector3f(final long offset) {
        return new Vector3f(
                this.getFloat(offset + Float.BYTES * 0),
                this.getFloat(offset + Float.BYTES * 1),
                this.getFloat(offset + Float.BYTES * 2)
        );
    }

    public Memory putVector3f(final long offset, final Vector3f vector) {
        return this.putVector3f(offset, vector.x, vector.y, vector.z);
    }

    public Memory putVector3f(final long offset, final float x, final float y, final float z) {
        this.putFloat(offset + Float.BYTES * 0, x);
        this.putFloat(offset + Float.BYTES * 1, y);
        this.putFloat(offset + Float.BYTES * 2, z);
        return this;
    }

    public Vector4f getVector4f(final long offset) {
        return new Vector4f(
                this.getFloat(offset + Float.BYTES * 0),
                this.getFloat(offset + Float.BYTES * 1),
                this.getFloat(offset + Float.BYTES * 2),
                this.getFloat(offset + Float.BYTES * 3)
        );
    }

    public Memory putVector4f(final long offset, final Vector4f vector) {
        return this.putVector4f(offset, vector.x, vector.y, vector.z, vector.w);
    }

    public Memory putVector4f(final long offset, final float x, final float y, final float z, final float w) {
        this.putFloat(offset + Float.BYTES * 0, x);
        this.putFloat(offset + Float.BYTES * 1, y);
        this.putFloat(offset + Float.BYTES * 2, z);
        this.putFloat(offset + Float.BYTES * 3, w);
        return this;
    }

    public Vector2d getVector2d(final long offset) {
        return new Vector2d(
                this.getDouble(offset + Double.BYTES * 0),
                this.getDouble(offset + Double.BYTES * 1)
        );
    }

    public Memory putVector2d(final long offset, final Vector2d vector) {
        return this.putVector2d(offset, vector.x, vector.y);
    }

    public Memory putVector2d(final long offset, final double x, final double y) {
        this.putDouble(offset + Double.BYTES * 0, x);
        this.putDouble(offset + Double.BYTES * 1, y);
        return this;
    }

    public Vector3d getVector3d(final long offset) {
        return new Vector3d(
                this.getDouble(offset + Double.BYTES * 0),
                this.getDouble(offset + Double.BYTES * 1),
                this.getDouble(offset + Double.BYTES * 2)
        );
    }

    public Memory putVector3d(final long offset, final Vector3d vector) {
        return this.putVector3d(offset, vector.x, vector.y, vector.z);
    }

    public Memory putVector3d(final long offset, final double x, final double y, final double z) {
        this.putDouble(offset + Double.BYTES * 0, x);
        this.putDouble(offset + Double.BYTES * 1, y);
        this.putDouble(offset + Double.BYTES * 2, z);
        return this;
    }

    public Vector4d getVector4d(final long offset) {
        return new Vector4d(
                this.getDouble(offset + Double.BYTES * 0),
                this.getDouble(offset + Double.BYTES * 1),
                this.getDouble(offset + Double.BYTES * 2),
                this.getDouble(offset + Double.BYTES * 3)
        );
    }

    public Memory putVector4d(final long offset, final Vector4d vector) {
        return this.putVector4d(offset, vector.x, vector.y, vector.z, vector.w);
    }

    public Memory putVector4d(final long offset, final double x, final double y, final double z, final double w) {
        this.putDouble(offset + Double.BYTES * 0, x);
        this.putDouble(offset + Double.BYTES * 1, y);
        this.putDouble(offset + Double.BYTES * 2, z);
        this.putDouble(offset + Double.BYTES * 3, w);
        return this;
    }

    public Matrix3f getMatrix3f(final long offset) {
        return new Matrix3f(
                this.getFloat(offset + Float.BYTES * 0),
                this.getFloat(offset + Float.BYTES * 1),
                this.getFloat(offset + Float.BYTES * 2),
                this.getFloat(offset + Float.BYTES * 3),
                this.getFloat(offset + Float.BYTES * 4),
                this.getFloat(offset + Float.BYTES * 5),
                this.getFloat(offset + Float.BYTES * 6),
                this.getFloat(offset + Float.BYTES * 7),
                this.getFloat(offset + Float.BYTES * 8)
        );
    }

    public Memory putMatrix3f(final long offset, final Matrix3f matrix) {
        this.putFloat(offset + Float.BYTES * 0, matrix.m00);
        this.putFloat(offset + Float.BYTES * 1, matrix.m01);
        this.putFloat(offset + Float.BYTES * 2, matrix.m02);
        this.putFloat(offset + Float.BYTES * 3, matrix.m10);
        this.putFloat(offset + Float.BYTES * 4, matrix.m11);
        this.putFloat(offset + Float.BYTES * 5, matrix.m12);
        this.putFloat(offset + Float.BYTES * 6, matrix.m20);
        this.putFloat(offset + Float.BYTES * 7, matrix.m21);
        this.putFloat(offset + Float.BYTES * 8, matrix.m22);
        return this;
    }

    public Matrix4f getMatrix4f(final long offset) {
        return new Matrix4f(
                this.getFloat(offset + Float.BYTES * 0),
                this.getFloat(offset + Float.BYTES * 1),
                this.getFloat(offset + Float.BYTES * 2),
                this.getFloat(offset + Float.BYTES * 3),
                this.getFloat(offset + Float.BYTES * 4),
                this.getFloat(offset + Float.BYTES * 5),
                this.getFloat(offset + Float.BYTES * 6),
                this.getFloat(offset + Float.BYTES * 7),
                this.getFloat(offset + Float.BYTES * 8),
                this.getFloat(offset + Float.BYTES * 9),
                this.getFloat(offset + Float.BYTES * 10),
                this.getFloat(offset + Float.BYTES * 11),
                this.getFloat(offset + Float.BYTES * 12),
                this.getFloat(offset + Float.BYTES * 13),
                this.getFloat(offset + Float.BYTES * 14),
                this.getFloat(offset + Float.BYTES * 15)
        );
    }

    public Memory putMatrix4f(final long offset, final Matrix4f matrix) {
        this.putFloat(offset + Float.BYTES * 0, matrix.m00());
        this.putFloat(offset + Float.BYTES * 1, matrix.m01());
        this.putFloat(offset + Float.BYTES * 2, matrix.m02());
        this.putFloat(offset + Float.BYTES * 3, matrix.m03());
        this.putFloat(offset + Float.BYTES * 4, matrix.m10());
        this.putFloat(offset + Float.BYTES * 5, matrix.m11());
        this.putFloat(offset + Float.BYTES * 6, matrix.m12());
        this.putFloat(offset + Float.BYTES * 7, matrix.m13());
        this.putFloat(offset + Float.BYTES * 8, matrix.m20());
        this.putFloat(offset + Float.BYTES * 9, matrix.m21());
        this.putFloat(offset + Float.BYTES * 10, matrix.m22());
        this.putFloat(offset + Float.BYTES * 11, matrix.m23());
        this.putFloat(offset + Float.BYTES * 12, matrix.m30());
        this.putFloat(offset + Float.BYTES * 13, matrix.m31());
        this.putFloat(offset + Float.BYTES * 14, matrix.m32());
        this.putFloat(offset + Float.BYTES * 15, matrix.m33());
        return this;
    }

    public Matrix3d getMatrix3d(final long offset) {
        return new Matrix3d(
                this.getDouble(offset + Double.BYTES * 0),
                this.getDouble(offset + Double.BYTES * 1),
                this.getDouble(offset + Double.BYTES * 2),
                this.getDouble(offset + Double.BYTES * 3),
                this.getDouble(offset + Double.BYTES * 4),
                this.getDouble(offset + Double.BYTES * 5),
                this.getDouble(offset + Double.BYTES * 6),
                this.getDouble(offset + Double.BYTES * 7),
                this.getDouble(offset + Double.BYTES * 8)
        );
    }

    public Memory putMatrix3d(final long offset, final Matrix3d matrix) {
        this.putDouble(offset + Double.BYTES * 0, matrix.m00);
        this.putDouble(offset + Double.BYTES * 1, matrix.m01);
        this.putDouble(offset + Double.BYTES * 2, matrix.m02);
        this.putDouble(offset + Double.BYTES * 3, matrix.m10);
        this.putDouble(offset + Double.BYTES * 4, matrix.m11);
        this.putDouble(offset + Double.BYTES * 5, matrix.m12);
        this.putDouble(offset + Double.BYTES * 6, matrix.m20);
        this.putDouble(offset + Double.BYTES * 7, matrix.m21);
        this.putDouble(offset + Double.BYTES * 8, matrix.m22);
        return this;
    }

    public Matrix4d getMatrix4d(final long offset) {
        return new Matrix4d(
                this.getDouble(offset + Double.BYTES * 0),
                this.getDouble(offset + Double.BYTES * 1),
                this.getDouble(offset + Double.BYTES * 2),
                this.getDouble(offset + Double.BYTES * 3),
                this.getDouble(offset + Double.BYTES * 4),
                this.getDouble(offset + Double.BYTES * 5),
                this.getDouble(offset + Double.BYTES * 6),
                this.getDouble(offset + Double.BYTES * 7),
                this.getDouble(offset + Double.BYTES * 8),
                this.getDouble(offset + Double.BYTES * 9),
                this.getDouble(offset + Double.BYTES * 10),
                this.getDouble(offset + Double.BYTES * 11),
                this.getDouble(offset + Double.BYTES * 12),
                this.getDouble(offset + Double.BYTES * 13),
                this.getDouble(offset + Double.BYTES * 14),
                this.getDouble(offset + Double.BYTES * 15)
        );
    }

    public Memory putMatrix4d(final long offset, final Matrix4d matrix) {
        this.putDouble(offset + Double.BYTES * 0, matrix.m00());
        this.putDouble(offset + Double.BYTES * 1, matrix.m01());
        this.putDouble(offset + Double.BYTES * 2, matrix.m02());
        this.putDouble(offset + Double.BYTES * 3, matrix.m03());
        this.putDouble(offset + Double.BYTES * 4, matrix.m10());
        this.putDouble(offset + Double.BYTES * 5, matrix.m11());
        this.putDouble(offset + Double.BYTES * 6, matrix.m12());
        this.putDouble(offset + Double.BYTES * 7, matrix.m13());
        this.putDouble(offset + Double.BYTES * 8, matrix.m20());
        this.putDouble(offset + Double.BYTES * 9, matrix.m21());
        this.putDouble(offset + Double.BYTES * 10, matrix.m22());
        this.putDouble(offset + Double.BYTES * 11, matrix.m23());
        this.putDouble(offset + Double.BYTES * 12, matrix.m30());
        this.putDouble(offset + Double.BYTES * 13, matrix.m31());
        this.putDouble(offset + Double.BYTES * 14, matrix.m32());
        this.putDouble(offset + Double.BYTES * 15, matrix.m33());
        return this;
    }

    public Memory clear() {
        return this.clear((byte) 0);
    }

    public Memory clear(final byte value) {
        for (long i = 0; i < this.size; i++) {
            this.putByte(i, value);
        }
        return this;
    }

    public Memory copyTo(final Memory target) {
        if (this.size > target.size) {
            throw new IllegalArgumentException("Target memory is smaller than source memory");
        }
        return this.copyTo(target, 0L, 0L, this.size);
    }

    public Memory copyTo(final Memory target, final long sourceOffset, final long targetOffset, final long length) {
        for (long i = 0; i < length; i++) {
            target.putByte(targetOffset + i, this.getByte(sourceOffset + i));
        }
        return this;
    }

    public abstract Memory slice(final long offset, final long size);

    public ByteBuffer asByteBuffer() {
        return MemoryUtil.memByteBuffer(this.address, this.getSizeAsInt());
    }

    public int getSizeAsInt() {
        if (this.size <= Integer.MAX_VALUE) {
            return (int) this.size;
        } else {
            throw new IllegalStateException("Memory is too large to fit into an int");
        }
    }

    public long getAddress() {
        return this.address;
    }

    public long getSize() {
        return this.size;
    }

}
