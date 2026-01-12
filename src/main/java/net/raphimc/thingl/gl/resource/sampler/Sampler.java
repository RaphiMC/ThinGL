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
package net.raphimc.thingl.gl.resource.sampler;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.resource.GLObject;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL43C;

import java.util.Arrays;

public class Sampler extends GLObject {

    private final Int2ObjectMap<Object> parameters = new Int2ObjectOpenHashMap<>();

    public Sampler() {
        super(ThinGL.glBackend().createSampler());
    }

    protected Sampler(final int glId) {
        super(glId);
    }

    public static Sampler fromGlId(final int glId) {
        if (!ThinGL.glBackend().isSampler(glId)) {
            throw new IllegalArgumentException("Not a sampler object");
        }
        return fromGlIdUnsafe(glId);
    }

    public static Sampler fromGlIdUnsafe(final int glId) {
        return new Sampler(glId);
    }

    public int getParameterInt(final int parameter) {
        Object value = this.parameters.get(parameter);
        if (!(value instanceof Integer)) {
            value = ThinGL.glBackend().getSamplerParameteri(this.getGlId(), parameter);
            this.parameters.put(parameter, value);
        }
        return (int) value;
    }

    public void setParameterInt(final int parameter, final int value) {
        if (this.getParameterInt(parameter) != value) {
            this.parameters.put(parameter, (Integer) value);
            ThinGL.glBackend().samplerParameteri(this.getGlId(), parameter, value);
        }
    }

    public float getParameterFloat(final int parameter) {
        Object value = this.parameters.get(parameter);
        if (!(value instanceof Float)) {
            value = ThinGL.glBackend().getSamplerParameterf(this.getGlId(), parameter);
            this.parameters.put(parameter, value);
        }
        return (float) value;
    }

    public void setParameterFloat(final int parameter, final float value) {
        if (this.getParameterFloat(parameter) != value) {
            this.parameters.put(parameter, (Float) value);
            ThinGL.glBackend().samplerParameterf(this.getGlId(), parameter, value);
        }
    }

    public int[] getParameterIntArray(final int parameter, final int length) {
        Object value = this.parameters.get(parameter);
        if (!(value instanceof int[] && ((int[]) value).length == length)) {
            value = new int[length];
            ThinGL.glBackend().getSamplerParameteriv(this.getGlId(), parameter, (int[]) value);
            this.parameters.put(parameter, value);
        }
        return (int[]) value;
    }

    public void setParameterIntArray(final int parameter, final int[] value) {
        if (!Arrays.equals(this.getParameterIntArray(parameter, value.length), value)) {
            this.parameters.put(parameter, value.clone());
            ThinGL.glBackend().samplerParameteriv(this.getGlId(), parameter, value);
        }
    }

    public float[] getParameterFloatArray(final int parameter, final int length) {
        Object value = this.parameters.get(parameter);
        if (!(value instanceof float[] && ((float[]) value).length == length)) {
            value = new float[length];
            ThinGL.glBackend().getSamplerParameterfv(this.getGlId(), parameter, (float[]) value);
            this.parameters.put(parameter, value);
        }
        return (float[]) value;
    }

    public void setParameterFloatArray(final int parameter, final float[] value) {
        if (!Arrays.equals(this.getParameterFloatArray(parameter, value.length), value)) {
            this.parameters.put(parameter, value.clone());
            ThinGL.glBackend().samplerParameterfv(this.getGlId(), parameter, value);
        }
    }

    public void setFilter(final int filter) {
        this.setMinificationFilter(filter);
        this.setMagnificationFilter(filter);
    }

    public int getMinificationFilter() {
        return this.getParameterInt(GL11C.GL_TEXTURE_MIN_FILTER);
    }

    public void setMinificationFilter(final int minificationFilter) {
        this.setParameterInt(GL11C.GL_TEXTURE_MIN_FILTER, minificationFilter);
    }

    public int getMagnificationFilter() {
        return this.getParameterInt(GL11C.GL_TEXTURE_MAG_FILTER);
    }

    public void setMagnificationFilter(final int magnificationFilter) {
        this.setParameterInt(GL11C.GL_TEXTURE_MAG_FILTER, magnificationFilter);
    }

    public void setWrap(final int wrap) {
        this.setWrapS(wrap);
        this.setWrapT(wrap);
        this.setWrapR(wrap);
    }

    public int getWrapS() {
        return this.getParameterInt(GL11C.GL_TEXTURE_WRAP_S);
    }

    public void setWrapS(final int wrapS) {
        this.setParameterInt(GL11C.GL_TEXTURE_WRAP_S, wrapS);
    }

    public int getWrapT() {
        return this.getParameterInt(GL11C.GL_TEXTURE_WRAP_T);
    }

    public void setWrapT(final int wrapT) {
        this.setParameterInt(GL11C.GL_TEXTURE_WRAP_T, wrapT);
    }

    public int getWrapR() {
        return this.getParameterInt(GL12C.GL_TEXTURE_WRAP_R);
    }

    public void setWrapR(final int wrapR) {
        this.setParameterInt(GL12C.GL_TEXTURE_WRAP_R, wrapR);
    }

    @Override
    protected void free0() {
        ThinGL.glBackend().deleteSampler(this.getGlId());
    }

    @Override
    public int getGlType() {
        return GL43C.GL_SAMPLER;
    }

}
