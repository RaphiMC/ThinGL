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
package net.raphimc.thingl.gl.resource.image.texture;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.resource.image.ImageStorage;
import net.raphimc.thingl.gl.resource.image.ImageStorage1D;
import net.raphimc.thingl.gl.resource.image.ImageStorage2D;
import net.raphimc.thingl.gl.resource.image.ImageStorage3D;
import net.raphimc.thingl.resource.image.Image;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GL44C;
import org.lwjgl.opengl.GL45C;

import java.nio.ByteBuffer;
import java.util.Arrays;

public abstract class ImageTexture extends Texture implements ImageStorage {

    protected final Int2ObjectMap<Object> parameters = new Int2ObjectOpenHashMap<>();

    public ImageTexture(final int target) {
        super(target);
        this.parameters.put(GL45C.GL_TEXTURE_TARGET, (Integer) target);
    }

    protected ImageTexture(final int glId, final Object unused) {
        super(glId, unused);
    }

    protected void uploadImage(final int level, final int x, final int y, final int z, final Image image) {
        ThinGL.glStateStack().pushPixelStore();
        ThinGL.glStateStack().pixelStore(GL11C.GL_UNPACK_ALIGNMENT, image.getRowAlignment());
        ThinGL.glStateStack().pixelStore(GL11C.GL_UNPACK_SKIP_PIXELS, 0);
        ThinGL.glStateStack().pixelStore(GL11C.GL_UNPACK_SKIP_ROWS, 0);
        ThinGL.glStateStack().pixelStore(GL11C.GL_UNPACK_ROW_LENGTH, 0);
        if (this instanceof ImageStorage2D) {
            if (z != 0 || image.getDepth() != 1) {
                throw new IllegalArgumentException("z and depth must be 0 and 1 for 2D textures");
            }
            GL45C.nglTextureSubImage2D(this.getGlId(), level, x, y, image.getWidth(), image.getHeight(), image.getPixelFormat(), image.getPixelDataType(), image.getPixels().getAddress());
        } else if (this instanceof ImageStorage3D) {
            GL45C.nglTextureSubImage3D(this.getGlId(), level, x, y, z, image.getWidth(), image.getHeight(), image.getDepth(), image.getPixelFormat(), image.getPixelDataType(), image.getPixels().getAddress());
        } else if (this instanceof ImageStorage1D) {
            if (y != 0 || z != 0 || image.getHeight() != 1 || image.getDepth() != 1) {
                throw new IllegalArgumentException("y, z, height, and depth must be 0, 0, 1, and 1 for 1D textures");
            }
            GL45C.nglTextureSubImage1D(this.getGlId(), level, x, image.getWidth(), image.getPixelFormat(), image.getPixelDataType(), image.getPixels().getAddress());
        } else {
            throw new IllegalArgumentException("Unsupported texture class: " + this.getClass().getName());
        }
        ThinGL.glStateStack().popPixelStore();
    }

    protected Image downloadImage(final int level, final int x, final int y, final int z, final int width, final int height, final int depth, final int pixelFormat, final int pixelDataType) {
        final Image image = new Image(width, height, depth, pixelFormat, pixelDataType);
        ThinGL.glStateStack().pushPixelStore();
        ThinGL.glStateStack().pixelStore(GL11C.GL_PACK_ALIGNMENT, image.getRowAlignment());
        ThinGL.glStateStack().pixelStore(GL11C.GL_PACK_SKIP_PIXELS, 0);
        ThinGL.glStateStack().pixelStore(GL11C.GL_PACK_SKIP_ROWS, 0);
        ThinGL.glStateStack().pixelStore(GL11C.GL_PACK_ROW_LENGTH, 0);
        GL45C.nglGetTextureSubImage(this.getGlId(), level, x, y, z, image.getWidth(), image.getHeight(), image.getDepth(), image.getPixelFormat(), image.getPixelDataType(), image.getPixels().getSizeAsInt(), image.getPixels().getAddress());
        ThinGL.glStateStack().popPixelStore();
        return image;
    }

    public void clear(final Color color) {
        this.clear(0, color);
    }

    public void clear(final int level, final Color color) {
        if (!color.equals(Color.TRANSPARENT)) {
            GL44C.glClearTexImage(this.getGlId(), level, GL11C.GL_RGBA, GL11C.GL_FLOAT, color.toRGBAF());
        } else {
            GL44C.glClearTexImage(this.getGlId(), level, GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        }
    }

    protected void clear(final int level, final int x, final int y, final int z, final int width, final int height, final int depth, final Color color) {
        if (!color.equals(Color.TRANSPARENT)) {
            GL44C.glClearTexSubImage(this.getGlId(), level, x, y, z, width, height, depth, GL11C.GL_RGBA, GL11C.GL_FLOAT, color.toRGBAF());
        } else {
            GL44C.glClearTexSubImage(this.getGlId(), level, x, y, z, width, height, depth, GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        }
    }

    @Override
    public void copyTo(final ImageStorage target, final int srcLevel, final int srcX, final int srcY, final int srcZ, final int dstLevel, final int dstX, final int dstY, final int dstZ, final int width, final int height, final int depth) {
        GL43C.glCopyImageSubData(this.getGlId(), this.getTarget(), srcLevel, srcX, srcY, srcZ, target.getGlId(), target.getTarget(), dstLevel, dstX, dstY, dstZ, width, height, depth);
    }

    public int getParameterInt(final int parameter) {
        Object value = this.parameters.get(parameter);
        if (!(value instanceof Integer)) {
            if (parameter == GL45C.GL_TEXTURE_TARGET && ThinGL.workarounds().isGetTextureParameterTextureTargetBroken()) {
                value = getTextureTarget(this.getGlId());
            } else {
                value = GL45C.glGetTextureParameteri(this.getGlId(), parameter);
            }
            this.parameters.put(parameter, value);
        }
        return (int) value;
    }

    public void setParameterInt(final int parameter, final int value) {
        if (this.getParameterInt(parameter) != value) {
            this.parameters.put(parameter, (Integer) value);
            GL45C.glTextureParameteri(this.getGlId(), parameter, value);
        }
    }

    public float getParameterFloat(final int parameter) {
        Object value = this.parameters.get(parameter);
        if (!(value instanceof Float)) {
            value = GL45C.glGetTextureParameterf(this.getGlId(), parameter);
            this.parameters.put(parameter, value);
        }
        return (float) value;
    }

    public void setParameterFloat(final int parameter, final float value) {
        if (this.getParameterFloat(parameter) != value) {
            this.parameters.put(parameter, (Float) value);
            GL45C.glTextureParameterf(this.getGlId(), parameter, value);
        }
    }

    public int[] getParameterIntArray(final int parameter, final int length) {
        Object value = this.parameters.get(parameter);
        if (!(value instanceof int[] && ((int[]) value).length == length)) {
            value = new int[length];
            GL45C.glGetTextureParameteriv(this.getGlId(), parameter, (int[]) value);
            this.parameters.put(parameter, value);
        }
        return (int[]) value;
    }

    public void setParameterIntArray(final int parameter, final int[] value) {
        if (!Arrays.equals(this.getParameterIntArray(parameter, value.length), value)) {
            this.parameters.put(parameter, value.clone());
            GL45C.glTextureParameteriv(this.getGlId(), parameter, value);
        }
    }

    public float[] getParameterFloatArray(final int parameter, final int length) {
        Object value = this.parameters.get(parameter);
        if (!(value instanceof float[] && ((float[]) value).length == length)) {
            value = new float[length];
            GL45C.glGetTextureParameterfv(this.getGlId(), parameter, (float[]) value);
            this.parameters.put(parameter, value);
        }
        return (float[]) value;
    }

    public void setParameterFloatArray(final int parameter, final float[] value) {
        if (!Arrays.equals(this.getParameterFloatArray(parameter, value.length), value)) {
            this.parameters.put(parameter, value.clone());
            GL45C.glTextureParameterfv(this.getGlId(), parameter, value);
        }
    }

    @Override
    public int getTarget() {
        return this.getParameterInt(GL45C.GL_TEXTURE_TARGET);
    }

}
