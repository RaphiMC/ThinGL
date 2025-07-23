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
package net.raphimc.thingl.resource.image.texture;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.resource.image.ImageStorage;
import net.raphimc.thingl.resource.image.ImageStorage1D;
import net.raphimc.thingl.resource.image.ImageStorage2D;
import net.raphimc.thingl.resource.image.ImageStorage3D;
import net.raphimc.thingl.util.BufferUtil;
import net.raphimc.thingl.util.BufferedSTBWriteCallback;
import org.lwjgl.opengl.*;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

    protected void uploadImage(final int level, final int x, final int y, final int z, final int pixelFormat, final byte[] imageBytes) {
        final ByteBuffer imageBuffer = MemoryUtil.memAlloc(imageBytes.length).put(imageBytes).flip();
        try {
            this.uploadImage(level, x, y, z, pixelFormat, imageBuffer);
        } finally {
            BufferUtil.memFree(imageBuffer);
        }
    }

    protected void uploadImage(final int level, final int x, final int y, final int z, final int pixelFormat, final ByteBuffer imageBuffer) {
        if (!imageBuffer.isDirect()) {
            throw new IllegalArgumentException("Image buffer must be a direct ByteBuffer");
        }

        final int[] imgWidth = new int[1];
        final int[] imgHeight = new int[1];
        final ByteBuffer pixelBuffer = STBImage.stbi_load_from_memory(imageBuffer, imgWidth, imgHeight, new int[1], getPixelChannelCount(pixelFormat));
        if (pixelBuffer == null) {
            throw new IllegalArgumentException("Failed to read image: " + STBImage.stbi_failure_reason());
        }
        try {
            this.uploadPixels(level, x, y, z, imgWidth[0], imgHeight[0], 1, pixelFormat, pixelBuffer);
        } finally {
            STBImage.stbi_image_free(pixelBuffer);
        }
    }

    protected void uploadPixels(final int level, final int x, final int y, final int z, final int width, final int height, final int depth, final int pixelFormat, final int[] pixels) {
        this.uploadPixels(level, x, y, z, width, height, depth, pixelFormat, pixels, true);
    }

    protected void uploadPixels(final int level, final int x, final int y, final int z, final int width, final int height, final int depth, final int pixelFormat, final int[] pixels, final boolean bigEndian) {
        final ByteBuffer pixelBuffer = MemoryUtil.memAlloc(pixels.length * Integer.BYTES);
        pixelBuffer.order(bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).asIntBuffer().put(pixels).flip();
        try {
            this.uploadPixels(level, x, y, z, width, height, depth, pixelFormat, pixelBuffer);
        } finally {
            BufferUtil.memFree(pixelBuffer);
        }
    }

    protected void uploadPixels(final int level, final int x, final int y, final int z, final int width, final int height, final int depth, final int pixelFormat, final byte[] pixelBytes) {
        final ByteBuffer pixelBuffer = MemoryUtil.memAlloc(pixelBytes.length).put(pixelBytes).flip();
        try {
            this.uploadPixels(level, x, y, z, width, height, depth, pixelFormat, pixelBuffer);
        } finally {
            BufferUtil.memFree(pixelBuffer);
        }
    }

    protected void uploadPixels(final int level, final int x, final int y, final int z, final int width, final int height, final int depth, final int pixelFormat, final ByteBuffer pixelBuffer) {
        if (!pixelBuffer.isDirect()) {
            throw new IllegalArgumentException("Pixel buffer must be a direct ByteBuffer");
        }
        if (pixelBuffer.remaining() != width * height * depth * getPixelChannelCount(pixelFormat)) {
            throw new IllegalArgumentException("Pixel buffer size does not match the specified dimensions");
        }

        ThinGL.glStateStack().pushPixelStore();
        ThinGL.glStateStack().pixelStore(GL11C.GL_UNPACK_ALIGNMENT, calculateRowAlignment(width, pixelFormat, Byte.BYTES));
        ThinGL.glStateStack().pixelStore(GL11C.GL_UNPACK_SKIP_PIXELS, 0);
        ThinGL.glStateStack().pixelStore(GL11C.GL_UNPACK_SKIP_ROWS, 0);
        ThinGL.glStateStack().pixelStore(GL11C.GL_UNPACK_ROW_LENGTH, 0);
        if (this instanceof ImageStorage2D) {
            if (z != 0 || depth != 1) {
                throw new IllegalArgumentException("z and depth must be 0 and 1 for 2D textures");
            }
            GL45C.glTextureSubImage2D(this.getGlId(), level, x, y, width, height, pixelFormat, GL11C.GL_UNSIGNED_BYTE, pixelBuffer);
        } else if (this instanceof ImageStorage3D) {
            GL45C.glTextureSubImage3D(this.getGlId(), level, x, y, z, width, height, depth, pixelFormat, GL11C.GL_UNSIGNED_BYTE, pixelBuffer);
        } else if (this instanceof ImageStorage1D) {
            if (y != 0 || z != 0 || height != 1 || depth != 1) {
                throw new IllegalArgumentException("y, z, height, and depth must be 0, 0, 1, and 1 for 1D textures");
            }
            GL45C.glTextureSubImage1D(this.getGlId(), level, x, width, pixelFormat, GL11C.GL_UNSIGNED_BYTE, pixelBuffer);
        } else {
            throw new IllegalArgumentException("Unsupported texture class: " + this.getClass().getName());
        }
        ThinGL.glStateStack().popPixelStore();
    }

    protected byte[] downloadPngImageBytes(final int level, final int x, final int y, final int z, final int width, final int height, final int pixelFormat) {
        final ByteBuffer pixelBuffer = this.downloadPixelBuffer(level, x, y, z, width, height, 1, pixelFormat);
        final BufferedSTBWriteCallback writeCallback = new BufferedSTBWriteCallback();
        try {
            if (!STBImageWrite.stbi_write_png_to_func(writeCallback, 0, width, height, getPixelChannelCount(pixelFormat), pixelBuffer, 0)) {
                throw new RuntimeException("Failed to write image: " + STBImage.stbi_failure_reason());
            }
            return writeCallback.getImageBytes();
        } finally {
            writeCallback.free();
            BufferUtil.memFree(pixelBuffer);
        }
    }

    protected byte[] downloadPixelBytes(final int level, final int x, final int y, final int z, final int width, final int height, final int depth, final int pixelFormat) {
        final ByteBuffer pixelBuffer = this.downloadPixelBuffer(level, x, y, z, width, height, depth, pixelFormat);
        try {
            final byte[] pixelBytes = new byte[pixelBuffer.remaining()];
            pixelBuffer.get(pixelBytes);
            return pixelBytes;
        } finally {
            BufferUtil.memFree(pixelBuffer);
        }
    }

    protected ByteBuffer downloadPixelBuffer(final int level, final int x, final int y, final int z, final int width, final int height, final int depth, final int pixelFormat) {
        final ByteBuffer pixelBuffer = MemoryUtil.memAlloc(width * height * depth * getPixelChannelCount(pixelFormat));
        ThinGL.glStateStack().pushPixelStore();
        ThinGL.glStateStack().pixelStore(GL11C.GL_PACK_ALIGNMENT, calculateRowAlignment(width, pixelFormat, Byte.BYTES));
        ThinGL.glStateStack().pixelStore(GL11C.GL_PACK_SKIP_PIXELS, 0);
        ThinGL.glStateStack().pixelStore(GL11C.GL_PACK_SKIP_ROWS, 0);
        ThinGL.glStateStack().pixelStore(GL11C.GL_PACK_ROW_LENGTH, 0);
        GL45C.glGetTextureSubImage(this.getGlId(), level, x, y, z, width, height, depth, pixelFormat, GL11C.GL_UNSIGNED_BYTE, pixelBuffer);
        ThinGL.glStateStack().popPixelStore();
        return pixelBuffer;
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


    private static int getPixelChannelCount(final int pixelFormat) {
        return switch (pixelFormat) {
            case GL11C.GL_RGBA, GL12C.GL_BGRA -> 4;
            case GL11C.GL_RGB, GL12C.GL_BGR -> 3;
            case GL30C.GL_RG, GL30C.GL_DEPTH_STENCIL -> 2;
            case GL11C.GL_RED, GL11C.GL_GREEN, GL11C.GL_BLUE, GL11C.GL_ALPHA, GL11C.GL_DEPTH_COMPONENT, GL11C.GL_STENCIL_INDEX -> 1;
            default -> throw new IllegalArgumentException("Unsupported pixel format: " + pixelFormat);
        };
    }

    private static int calculateRowAlignment(final int width, final int pixelFormat, final int componentBytes) {
        final int rowBytes = width * getPixelChannelCount(pixelFormat) * componentBytes;
        int alignment = 8;
        while (alignment > 1 && (rowBytes % alignment != 0)) {
            alignment /= 2;
        }
        return alignment;
    }

}
