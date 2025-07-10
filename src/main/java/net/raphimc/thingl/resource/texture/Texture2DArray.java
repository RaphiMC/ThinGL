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
package net.raphimc.thingl.resource.texture;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.util.BufferUtil;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Texture2DArray extends SampledTexture {

    private final int width;
    private final int height;
    private final int layers;
    private int wrapS;
    private int wrapT;

    public Texture2DArray(final InternalFormat internalFormat, final int width, final int height, final int layers) {
        this(internalFormat, width, height, layers, 1);
    }

    public Texture2DArray(final InternalFormat internalFormat, final int width, final int height, final int layers, final int mipMapLevels) {
        super(Type.TEX_2D_ARRAY, internalFormat, mipMapLevels);
        this.width = width;
        this.height = height;
        this.layers = layers;
        GL45C.glTextureStorage3D(this.getGlId(), mipMapLevels, internalFormat.getGlFormat(), width, height, layers);
        this.setFilter(GL11C.GL_LINEAR);
        this.setWrap(GL12C.GL_CLAMP_TO_EDGE);
    }

    protected Texture2DArray(final int glId) {
        super(glId, Type.TEX_2D_ARRAY);
        this.width = GL45C.glGetTextureLevelParameteri(glId, 0, GL11C.GL_TEXTURE_WIDTH);
        this.height = GL45C.glGetTextureLevelParameteri(glId, 0, GL11C.GL_TEXTURE_HEIGHT);
        this.layers = GL45C.glGetTextureParameteri(glId, GL12C.GL_TEXTURE_DEPTH);
        this.refreshCachedData();
    }

    @Override
    public void refreshCachedData() {
        super.refreshCachedData();
        this.wrapS = GL45C.glGetTextureParameteri(this.getGlId(), GL11C.GL_TEXTURE_WRAP_S);
        this.wrapT = GL45C.glGetTextureParameteri(this.getGlId(), GL11C.GL_TEXTURE_WRAP_T);
    }

    public void uploadImage(final int x, final int y, final int layer, final PixelFormat pixelFormat, final byte[] imageData) {
        final ByteBuffer imageBuffer = MemoryUtil.memAlloc(imageData.length).put(imageData).flip();
        try {
            this.uploadImage(x, y, layer, pixelFormat, imageBuffer);
        } finally {
            BufferUtil.memFree(imageBuffer);
        }
    }

    public void uploadImage(final int x, final int y, final int layer, final PixelFormat pixelFormat, final ByteBuffer imageBuffer) {
        final int[] imgWidth = new int[1];
        final int[] imgHeight = new int[1];
        final ByteBuffer pixelBuffer = STBImage.stbi_load_from_memory(imageBuffer, imgWidth, imgHeight, new int[1], pixelFormat.getChannelCount());
        if (pixelBuffer == null) {
            throw new IllegalArgumentException("Failed to read image: " + STBImage.stbi_failure_reason());
        }
        try {
            this.uploadPixels(x, y, layer, imgWidth[0], imgHeight[0], pixelFormat, pixelBuffer);
        } finally {
            STBImage.stbi_image_free(pixelBuffer);
        }
    }

    public void uploadPixels(final int x, final int y, final int layer, final int width, final int height, final PixelFormat pixelFormat, final int[] pixelData) {
        this.uploadPixels(x, y, layer, width, height, pixelFormat, pixelData, true);
    }

    public void uploadPixels(final int x, final int y, final int layer, final int width, final int height, final PixelFormat pixelFormat, final int[] pixelData, final boolean bigEndian) {
        final ByteBuffer pixelBuffer = MemoryUtil.memAlloc(pixelData.length * Integer.BYTES);
        pixelBuffer.order(bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).asIntBuffer().put(pixelData).flip();
        try {
            this.uploadPixels(x, y, layer, width, height, pixelFormat, pixelBuffer);
        } finally {
            BufferUtil.memFree(pixelBuffer);
        }
    }

    public void uploadPixels(final int x, final int y, final int layer, final int width, final int height, final PixelFormat pixelFormat, final byte[] pixelData) {
        final ByteBuffer pixelBuffer = MemoryUtil.memAlloc(pixelData.length).put(pixelData).flip();
        try {
            this.uploadPixels(x, y, layer, width, height, pixelFormat, pixelBuffer);
        } finally {
            BufferUtil.memFree(pixelBuffer);
        }
    }

    public void uploadPixels(final int x, final int y, final int layer, final int width, final int height, final PixelFormat pixelFormat, final ByteBuffer pixelBuffer) {
        if (x < 0 || y < 0 || layer < 0 || width < 0 || height < 0 || x + width > this.width || y + height > this.height || layer >= this.layers) {
            throw new IllegalArgumentException("Specified dimensions are out of bounds");
        }
        if (pixelBuffer.remaining() != width * height * pixelFormat.getChannelCount()) {
            throw new IllegalArgumentException("Pixel buffer size does not match the specified dimensions");
        }

        ThinGL.glStateStack().pushPixelStore();
        ThinGL.glStateStack().pixelStore(GL11C.GL_UNPACK_ALIGNMENT, pixelFormat.getAlignment());
        ThinGL.glStateStack().pixelStore(GL11C.GL_UNPACK_SKIP_PIXELS, 0);
        ThinGL.glStateStack().pixelStore(GL11C.GL_UNPACK_SKIP_ROWS, 0);
        ThinGL.glStateStack().pixelStore(GL11C.GL_UNPACK_ROW_LENGTH, 0);
        GL45C.glTextureSubImage3D(this.getGlId(), 0, x, y, layer, width, height, 1, pixelFormat.getGlFormat(), GL11C.GL_UNSIGNED_BYTE, pixelBuffer);
        ThinGL.glStateStack().popPixelStore();
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getLayers() {
        return this.layers;
    }

    public int getWrapS() {
        return this.wrapS;
    }

    public void setWrapS(final int wrapS) {
        this.wrapS = wrapS;
        GL45C.glTextureParameteri(this.getGlId(), GL11C.GL_TEXTURE_WRAP_S, wrapS);
    }

    public int getWrapT() {
        return this.wrapT;
    }

    public void setWrapT(final int wrapT) {
        this.wrapT = wrapT;
        GL45C.glTextureParameteri(this.getGlId(), GL11C.GL_TEXTURE_WRAP_T, wrapT);
    }

    public void setWrap(final int wrap) {
        this.setWrapS(wrap);
        this.setWrapT(wrap);
    }

}
