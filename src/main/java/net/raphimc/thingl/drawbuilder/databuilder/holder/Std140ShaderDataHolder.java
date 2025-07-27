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
package net.raphimc.thingl.drawbuilder.databuilder.holder;

import net.lenni0451.commons.math.MathUtils;
import net.raphimc.thingl.drawbuilder.builder.BufferBuilder;
import org.joml.Matrix4f;

import java.util.function.Function;

public class Std140ShaderDataHolder extends ShaderDataHolder {

    public static final Function<BufferBuilder, Std140ShaderDataHolder> SUPPLIER = Std140ShaderDataHolder::new;

    private static final int VEC4_BYTES = Float.BYTES * 4;

    public Std140ShaderDataHolder(final BufferBuilder bufferBuilder) {
        super(bufferBuilder);
    }

    @Override
    public ShaderDataHolder putInt(final int i) {
        this.checkArrayAlignment();
        return super.putInt(i);
    }

    @Override
    public ShaderDataHolder putFloat(final float f) {
        this.checkArrayAlignment();
        return super.putFloat(f);
    }

    @Override
    public ShaderDataHolder putDouble(final double d) {
        this.checkArrayAlignment();
        return super.putDouble(d);
    }

    @Override
    public ShaderDataHolder putVector2i(final int x, final int y) {
        this.checkArrayAlignment();
        return super.putVector2i(x, y);
    }

    @Override
    public ShaderDataHolder putVector3i(final int x, final int y, final int z) {
        this.checkArrayAlignment();
        return super.putVector3i(x, y, z);
    }

    @Override
    public ShaderDataHolder putVector4i(final int x, final int y, final int z, final int w) {
        this.checkArrayAlignment();
        return super.putVector4i(x, y, z, w);
    }

    @Override
    public ShaderDataHolder putVector2f(final float x, final float y) {
        this.checkArrayAlignment();
        return super.putVector2f(x, y);
    }

    @Override
    public ShaderDataHolder putVector3f(final float x, final float y, final float z) {
        this.checkArrayAlignment();
        return super.putVector3f(x, y, z);
    }

    @Override
    public ShaderDataHolder putVector4f(final float x, final float y, final float z, final float w) {
        this.checkArrayAlignment();
        return super.putVector4f(x, y, z, w);
    }

    @Override
    public ShaderDataHolder putVector2d(final double x, final double y) {
        this.checkArrayAlignment();
        return super.putVector2d(x, y);
    }

    @Override
    public ShaderDataHolder putVector3d(final double x, final double y, final double z) {
        this.checkArrayAlignment();
        return super.putVector3d(x, y, z);
    }

    @Override
    public ShaderDataHolder putVector4d(final double x, final double y, final double z, final double w) {
        this.checkArrayAlignment();
        return super.putVector4d(x, y, z, w);
    }

    @Override
    public ShaderDataHolder putMatrix4f(final Matrix4f matrix) {
        this.checkArrayAlignment();
        return super.putMatrix4f(matrix);
    }

    @Override
    public ShaderDataHolder endArray() {
        this.checkArrayAlignment();
        return super.endArray();
    }

    @Override
    protected int getStructAlignment(final int maxMemberAlignment) {
        return MathUtils.align(maxMemberAlignment, VEC4_BYTES);
    }

    private void checkArrayAlignment() {
        if (!this.inArrayStack.isEmpty() && this.inArrayStack.topBoolean()) {
            this.bufferBuilder.align(VEC4_BYTES);
        }
    }

}
