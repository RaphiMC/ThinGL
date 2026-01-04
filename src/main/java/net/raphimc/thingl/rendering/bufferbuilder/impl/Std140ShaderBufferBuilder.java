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
package net.raphimc.thingl.rendering.bufferbuilder.impl;

import net.lenni0451.commons.math.MathUtils;
import net.raphimc.thingl.memory.MemoryBuffer;
import net.raphimc.thingl.rendering.bufferbuilder.ShaderBufferBuilder;
import net.raphimc.thingl.rendering.bufferbuilder.StdShaderBufferBuilder;
import org.joml.Matrix4f;

import java.util.function.Function;

public class Std140ShaderBufferBuilder extends StdShaderBufferBuilder {

    public static final Function<MemoryBuffer, Std140ShaderBufferBuilder> SUPPLIER = Std140ShaderBufferBuilder::new;

    private static final int VEC4_BYTES = Float.BYTES * 4;

    public Std140ShaderBufferBuilder(final MemoryBuffer memoryBuffer) {
        super(memoryBuffer);
    }

    @Override
    public ShaderBufferBuilder writeInt(final int i) {
        this.checkArrayAlignment();
        return super.writeInt(i);
    }

    @Override
    public ShaderBufferBuilder writeFloat(final float f) {
        this.checkArrayAlignment();
        return super.writeFloat(f);
    }

    @Override
    public ShaderBufferBuilder writeDouble(final double d) {
        this.checkArrayAlignment();
        return super.writeDouble(d);
    }

    @Override
    public ShaderBufferBuilder writeVector2i(final int x, final int y) {
        this.checkArrayAlignment();
        return super.writeVector2i(x, y);
    }

    @Override
    public ShaderBufferBuilder writeVector3i(final int x, final int y, final int z) {
        this.checkArrayAlignment();
        return super.writeVector3i(x, y, z);
    }

    @Override
    public ShaderBufferBuilder writeVector4i(final int x, final int y, final int z, final int w) {
        this.checkArrayAlignment();
        return super.writeVector4i(x, y, z, w);
    }

    @Override
    public ShaderBufferBuilder writeVector2f(final float x, final float y) {
        this.checkArrayAlignment();
        return super.writeVector2f(x, y);
    }

    @Override
    public ShaderBufferBuilder writeVector3f(final float x, final float y, final float z) {
        this.checkArrayAlignment();
        return super.writeVector3f(x, y, z);
    }

    @Override
    public ShaderBufferBuilder writeVector4f(final float x, final float y, final float z, final float w) {
        this.checkArrayAlignment();
        return super.writeVector4f(x, y, z, w);
    }

    @Override
    public ShaderBufferBuilder writeVector2d(final double x, final double y) {
        this.checkArrayAlignment();
        return super.writeVector2d(x, y);
    }

    @Override
    public ShaderBufferBuilder writeVector3d(final double x, final double y, final double z) {
        this.checkArrayAlignment();
        return super.writeVector3d(x, y, z);
    }

    @Override
    public ShaderBufferBuilder writeVector4d(final double x, final double y, final double z, final double w) {
        this.checkArrayAlignment();
        return super.writeVector4d(x, y, z, w);
    }

    @Override
    public ShaderBufferBuilder writeMatrix4f(final Matrix4f matrix) {
        this.checkArrayAlignment();
        return super.writeMatrix4f(matrix);
    }

    @Override
    public ShaderBufferBuilder endArray() {
        this.checkArrayAlignment();
        return super.endArray();
    }

    @Override
    protected int getStructAlignment(final int maxMemberAlignment) {
        return MathUtils.align(maxMemberAlignment, VEC4_BYTES);
    }

    private void checkArrayAlignment() {
        if (!this.inArrayStack.isEmpty() && this.inArrayStack.topBoolean()) {
            this.memoryBuffer.alignWritePosition(VEC4_BYTES);
        }
    }

}
