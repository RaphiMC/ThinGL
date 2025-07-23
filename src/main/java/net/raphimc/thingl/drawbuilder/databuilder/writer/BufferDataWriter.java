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
package net.raphimc.thingl.drawbuilder.databuilder.writer;

import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.drawbuilder.builder.BufferBuilder;
import org.joml.*;
import org.joml.Math;

public abstract class BufferDataWriter<T extends BufferDataWriter<T>> extends BufferWriter<T> {

    public BufferDataWriter(final BufferBuilder bufferBuilder) {
        super(bufferBuilder);
    }

    public T putInt(final int i) {
        this.bufferBuilder.putInt(i);
        return (T) this;
    }

    public T putFloat(final float f) {
        this.bufferBuilder.putFloat(f);
        return (T) this;
    }

    public T putDouble(final double d) {
        this.bufferBuilder.putDouble(d);
        return (T) this;
    }

    public T putVector2i(final Vector2i vector) {
        return this.putVector2i(vector.x, vector.y);
    }

    public T putVector2i(final int x, final int y) {
        this.bufferBuilder.putVector2i(x, y);
        return (T) this;
    }

    public T putVector3i(final Vector3i vector) {
        return this.putVector3i(vector.x, vector.y, vector.z);
    }

    public T putVector3i(final int x, final int y, final int z) {
        this.bufferBuilder.putVector3i(x, y, z);
        return (T) this;
    }

    public T putVector4i(final Vector4i vector) {
        return this.putVector4i(vector.x, vector.y, vector.z, vector.w);
    }

    public T putVector4i(final int x, final int y, final int z, final int w) {
        this.bufferBuilder.putVector4i(x, y, z, w);
        return (T) this;
    }

    public T putVector2f(final Vector2f vector) {
        return this.putVector2f(vector.x, vector.y);
    }

    public T putVector2f(final float x, final float y) {
        this.bufferBuilder.putVector2f(x, y);
        return (T) this;
    }

    public T putVector3f(final Vector3f vector) {
        return this.putVector3f(vector.x, vector.y, vector.z);
    }

    public T putVector3f(final float x, final float y, final float z) {
        this.bufferBuilder.putVector3f(x, y, z);
        return (T) this;
    }

    public T putVector4f(final Vector4f vector) {
        return this.putVector4f(vector.x, vector.y, vector.z, vector.w);
    }

    public T putVector4f(final float x, final float y, final float z, final float w) {
        this.bufferBuilder.putVector4f(x, y, z, w);
        return (T) this;
    }

    public T putVector2d(final Vector2d vector) {
        return this.putVector2d(vector.x, vector.y);
    }

    public T putVector2d(final double x, final double y) {
        this.bufferBuilder.putVector2d(x, y);
        return (T) this;
    }

    public T putVector3d(final Vector3d vector) {
        return this.putVector3d(vector.x, vector.y, vector.z);
    }

    public T putVector3d(final double x, final double y, final double z) {
        this.bufferBuilder.putVector3d(x, y, z);
        return (T) this;
    }

    public T putVector4d(final Vector4d vector) {
        return this.putVector4d(vector.x, vector.y, vector.z, vector.w);
    }

    public T putVector4d(final double x, final double y, final double z, final double w) {
        this.bufferBuilder.putVector4d(x, y, z, w);
        return (T) this;
    }

    public T putVector3f(final Matrix4f positionMatrix, final Vector3f vector) {
        return this.putVector3f(positionMatrix, vector.x, vector.y, vector.z);
    }

    public T putVector3f(final Matrix4f positionMatrix, final float x, final float y, final float z) {
        // Code from Vector3f#mulPosition
        if ((positionMatrix.properties() & Matrix4fc.PROPERTY_IDENTITY) != 0) {
            return this.putVector3f(x, y, z);
        } else if ((positionMatrix.properties() & Matrix4fc.PROPERTY_TRANSLATION) != 0) {
            return this.putVector3f(x + positionMatrix.m30(), y + positionMatrix.m31(), z + positionMatrix.m32());
        } else {
            return this.putVector3f(
                    Math.fma(positionMatrix.m00(), x, Math.fma(positionMatrix.m10(), y, Math.fma(positionMatrix.m20(), z, positionMatrix.m30()))),
                    Math.fma(positionMatrix.m01(), x, Math.fma(positionMatrix.m11(), y, Math.fma(positionMatrix.m21(), z, positionMatrix.m31()))),
                    Math.fma(positionMatrix.m02(), x, Math.fma(positionMatrix.m12(), y, Math.fma(positionMatrix.m22(), z, positionMatrix.m32())))
            );
        }
    }

    public T putColor(final int r, final int g, final int b, final int a) {
        return this.putColor(a << 24 | b << 16 | g << 8 | r);
    }

    public T putColor(final Color color) {
        return this.putColor(color.toABGR());
    }

    public T putColor(final int abgrColor) {
        return this.putInt(abgrColor);
    }

}
