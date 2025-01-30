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

import net.raphimc.thingl.wrapper.GLStateTracker;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.stb.STBIWriteCallback;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.system.MemoryUtil;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Texture2D extends AbstractTexture {

    private final int width;
    private final int height;
    private final int mipMapLevels;
    private int minificationFilter;
    private int magnificationFilter;
    private int wrapS;
    private int wrapT;

    public Texture2D(final InternalFormat internalFormat, final int width, final int height) {
        this(internalFormat, width, height, 1);
    }

    public Texture2D(final InternalFormat internalFormat, final int width, final int height, final int mipMapLevels) {
        super(Type.TEX_2D, internalFormat);
        this.width = width;
        this.height = height;
        this.mipMapLevels = mipMapLevels;
        GL45C.glTextureStorage2D(this.getGlId(), mipMapLevels, internalFormat.getGlFormat(), width, height);
        this.setWrap(GL12C.GL_CLAMP_TO_EDGE);
        this.setFilter(GL11C.GL_LINEAR);
    }

    public Texture2D(final InternalFormat internalFormat, final byte[] imageData) {
        super(Type.TEX_2D, internalFormat);
        try {
            final ByteBuffer imageBuffer = MemoryUtil.memAlloc(imageData.length).put(imageData).flip();
            try {
                final int[] width = new int[1];
                final int[] height = new int[1];
                if (!STBImage.stbi_info_from_memory(imageBuffer, width, height, new int[1])) {
                    throw new RuntimeException("Failed to read image: " + STBImage.stbi_failure_reason());
                }
                this.width = width[0];
                this.height = height[0];
                this.mipMapLevels = 1;
                GL45C.glTextureStorage2D(this.getGlId(), this.mipMapLevels, internalFormat.getGlFormat(), this.width, this.height);
                this.setWrap(GL12C.GL_CLAMP_TO_EDGE);
                this.setFilter(GL11C.GL_LINEAR);
                this.uploadImage(0, 0, this.width, this.height, PixelFormat.RGBA, imageBuffer);
            } finally {
                MemoryUtil.memFree(imageBuffer);
            }
        } catch (Throwable e) {
            this.delete();
            throw e;
        }
    }

    protected Texture2D(final int glId) {
        super(glId);
        this.width = GL45C.glGetTextureLevelParameteri(glId, 0, GL11C.GL_TEXTURE_WIDTH);
        this.height = GL45C.glGetTextureLevelParameteri(glId, 0, GL11C.GL_TEXTURE_HEIGHT);
        this.mipMapLevels = GL45C.glGetTextureParameteri(glId, GL43C.GL_TEXTURE_IMMUTABLE_LEVELS);
        this.refreshCachedData();
    }

    @Override
    public void refreshCachedData() {
        this.minificationFilter = GL45C.glGetTextureParameteri(this.getGlId(), GL11C.GL_TEXTURE_MIN_FILTER);
        this.magnificationFilter = GL45C.glGetTextureParameteri(this.getGlId(), GL11C.GL_TEXTURE_MAG_FILTER);
        this.wrapS = GL45C.glGetTextureParameteri(this.getGlId(), GL11C.GL_TEXTURE_WRAP_S);
        this.wrapT = GL45C.glGetTextureParameteri(this.getGlId(), GL11C.GL_TEXTURE_WRAP_T);
    }

    public void uploadImage(final int x, final int y, final int width, final int height, final PixelFormat pixelFormat, final byte[] imageData) {
        final ByteBuffer imageBuffer = MemoryUtil.memAlloc(imageData.length).put(imageData).flip();
        try {
            this.uploadImage(x, y, width, height, pixelFormat, imageBuffer);
        } finally {
            MemoryUtil.memFree(imageBuffer);
        }
    }

    public void uploadImage(final int x, final int y, final int width, final int height, final PixelFormat pixelFormat, final ByteBuffer imageBuffer) {
        final int[] imgWidth = new int[1];
        final int[] imgHeight = new int[1];
        final ByteBuffer pixelBuffer = STBImage.stbi_load_from_memory(imageBuffer, imgWidth, imgHeight, new int[1], pixelFormat.getChannelCount());
        if (pixelBuffer == null) {
            throw new IllegalArgumentException("Failed to read image: " + STBImage.stbi_failure_reason());
        }
        try {
            this.uploadPixels(x, y, width, height, pixelFormat, pixelBuffer);
        } finally {
            STBImage.stbi_image_free(pixelBuffer);
        }
    }

    public void uploadPixels(final int x, final int y, final int width, final int height, final PixelFormat pixelFormat, final int[] pixelData) {
        this.uploadPixels(x, y, width, height, pixelFormat, pixelData, true);
    }

    public void uploadPixels(final int x, final int y, final int width, final int height, final PixelFormat pixelFormat, final int[] pixelData, final boolean bigEndian) {
        final ByteBuffer pixelBuffer = MemoryUtil.memAlloc(pixelData.length * 4);
        pixelBuffer.order(bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).asIntBuffer().put(pixelData).flip();
        try {
            this.uploadPixels(x, y, width, height, pixelFormat, pixelBuffer);
        } finally {
            MemoryUtil.memFree(pixelBuffer);
        }
    }

    public void uploadPixels(final int x, final int y, final int width, final int height, final PixelFormat pixelFormat, final byte[] pixelData) {
        final ByteBuffer pixelBuffer = MemoryUtil.memAlloc(pixelData.length).put(pixelData).flip();
        try {
            this.uploadPixels(x, y, width, height, pixelFormat, pixelBuffer);
        } finally {
            MemoryUtil.memFree(pixelBuffer);
        }
    }

    public void uploadPixels(final int x, final int y, final int width, final int height, final PixelFormat pixelFormat, final ByteBuffer pixelBuffer) {
        if (x < 0 || y < 0 || width < 0 || height < 0 || x + width > this.width || y + height > this.height) {
            throw new IllegalArgumentException("Specified dimensions are out of bounds");
        }
        if (pixelBuffer.remaining() != width * height * pixelFormat.getChannelCount()) {
            throw new IllegalArgumentException("Pixel buffer size does not match the specified dimensions");
        }

        GLStateTracker.pushPixelStore();
        GLStateTracker.pixelStore(GL11C.GL_UNPACK_ALIGNMENT, pixelFormat.getAlignment());
        GLStateTracker.pixelStore(GL11C.GL_UNPACK_SKIP_PIXELS, 0);
        GLStateTracker.pixelStore(GL11C.GL_UNPACK_SKIP_ROWS, 0);
        GLStateTracker.pixelStore(GL11C.GL_UNPACK_ROW_LENGTH, 0);
        GL45C.glTextureSubImage2D(this.getGlId(), 0, x, y, width, height, pixelFormat.getGlFormat(), GL11C.GL_UNSIGNED_BYTE, pixelBuffer);
        GLStateTracker.popPixelStore();
    }

    public byte[] downloadPixelData(final int x, final int y, final int width, final int height, final PixelFormat pixelFormat) {
        final ByteBuffer pixelBuffer = this.downloadPixelBuffer(x, y, width, height, pixelFormat);
        try {
            final byte[] pixelData = new byte[pixelBuffer.remaining()];
            pixelBuffer.get(pixelData);
            return pixelData;
        } finally {
            MemoryUtil.memFree(pixelBuffer);
        }
    }

    public ByteBuffer downloadPixelBuffer(final int x, final int y, final int width, final int height, final PixelFormat pixelFormat) {
        if (x < 0 || y < 0 || width < 0 || height < 0 || x + width > this.width || y + height > this.height) {
            throw new IllegalArgumentException("Specified dimensions are out of bounds");
        }

        final ByteBuffer pixelBuffer = MemoryUtil.memAlloc(width * height * pixelFormat.getChannelCount());
        GLStateTracker.pushPixelStore();
        GLStateTracker.pixelStore(GL11C.GL_PACK_ALIGNMENT, pixelFormat.getAlignment());
        GLStateTracker.pixelStore(GL11C.GL_PACK_SKIP_PIXELS, 0);
        GLStateTracker.pixelStore(GL11C.GL_PACK_SKIP_ROWS, 0);
        GLStateTracker.pixelStore(GL11C.GL_PACK_ROW_LENGTH, 0);
        GL45C.glGetTextureSubImage(this.getGlId(), 0, x, y, 0, width, height, 1, pixelFormat.getGlFormat(), GL11C.GL_UNSIGNED_BYTE, pixelBuffer);
        GLStateTracker.popPixelStore();
        return pixelBuffer;
    }

    public byte[] downloadPngImageData(final int x, final int y, final int width, final int height, final PixelFormat pixelFormat) {
        final ByteBuffer pixelBuffer = this.downloadPixelBuffer(x, y, width, height, pixelFormat);

        final BufferedSTBIWriteCallback writeCallback = new BufferedSTBIWriteCallback();
        try {
            if (!STBImageWrite.stbi_write_png_to_func(writeCallback, 0, width, height, pixelFormat.getChannelCount(), pixelBuffer, 0)) {
                throw new RuntimeException("Failed to write image: " + STBImage.stbi_failure_reason());
            }
            return writeCallback.getImageData();
        } finally {
            writeCallback.free();
            MemoryUtil.memFree(pixelBuffer);
        }
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getMipMapLevels() {
        return this.mipMapLevels;
    }

    public int getMinificationFilter() {
        return this.minificationFilter;
    }

    public void setMinificationFilter(final int minificationFilter) {
        this.minificationFilter = minificationFilter;
        GL45C.glTextureParameteri(this.getGlId(), GL11C.GL_TEXTURE_MIN_FILTER, minificationFilter);
    }

    public int getMagnificationFilter() {
        return this.magnificationFilter;
    }

    public void setMagnificationFilter(final int magnificationFilter) {
        this.magnificationFilter = magnificationFilter;
        GL45C.glTextureParameteri(this.getGlId(), GL11C.GL_TEXTURE_MAG_FILTER, magnificationFilter);
    }

    public void setFilter(final int filter) {
        this.setMinificationFilter(filter);
        this.setMagnificationFilter(filter);
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

    private static class BufferedSTBIWriteCallback extends STBIWriteCallback {

        private final ByteArrayOutputStream imageData = new ByteArrayOutputStream();

        @Override
        public void invoke(final long context, final long data, final int size) {
            final ByteBuffer dataBuffer = getData(data, size);
            final byte[] dataBytes = new byte[size];
            dataBuffer.get(dataBytes);
            this.imageData.writeBytes(dataBytes);
        }

        private byte[] getImageData() {
            return this.imageData.toByteArray();
        }

    }

}
