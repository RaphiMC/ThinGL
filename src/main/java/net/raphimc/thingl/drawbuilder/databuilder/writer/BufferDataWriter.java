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
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

public abstract class BufferDataWriter<T extends BufferDataWriter<T>> extends BufferWriter<T> {

    public BufferDataWriter(final BufferBuilder bufferBuilder) {
        super(bufferBuilder);
    }

    public T putVector3f(final Matrix4f positionMatrix, final float x, final float y, final float z) {
        if ((positionMatrix.properties() & Matrix4fc.PROPERTY_IDENTITY) != 0) {
            return this.putVector3f(x, y, z);
        } else {
            final Vector3f vector3f = positionMatrix.transformPosition(new Vector3f(x, y, z));
            return this.putVector3f(vector3f.x, vector3f.y, vector3f.z);
        }
    }

    public T putVector3f(final float x, final float y, final float z) {
        this.bufferBuilder.putVector3f(x, y, z);
        return (T) this;
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

}
