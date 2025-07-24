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

import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.resource.image.ImageStorage2D;
import net.raphimc.thingl.util.BufferUtil;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class Texture2D extends SampledTexture implements ImageStorage2D {

    public Texture2D(final int internalFormat, final int width, final int height) {
        this(internalFormat, width, height, 1);
    }

    public Texture2D(final int internalFormat, final int width, final int height, final int mipMapLevels) {
        super(GL11C.GL_TEXTURE_2D);
        GL45C.glTextureStorage2D(this.getGlId(), mipMapLevels, internalFormat, width, height);
    }

    protected Texture2D(final int glId) {
        super(glId, null);
    }

    public static Texture2D fromGlIdUnsafe(final int glId) {
        return new Texture2D(glId);
    }

    public static Texture2D fromImage(final byte[] imageBytes) {
        return fromImage(GL11C.GL_RGBA8, imageBytes);
    }

    public static Texture2D fromImage(final int internalFormat, final byte[] imageBytes) {
        final ByteBuffer imageBuffer = MemoryUtil.memAlloc(imageBytes.length).put(imageBytes).flip();
        try {
            return fromImage(internalFormat, imageBuffer);
        } finally {
            BufferUtil.memFree(imageBuffer);
        }
    }

    public static Texture2D fromImage(final ByteBuffer imageBuffer) {
        return fromImage(GL11C.GL_RGBA8, imageBuffer);
    }

    public static Texture2D fromImage(final int internalFormat, final ByteBuffer imageBuffer) {
        if (!imageBuffer.isDirect()) {
            throw new IllegalArgumentException("Image buffer must be a direct ByteBuffer");
        }

        final int[] width = new int[1];
        final int[] height = new int[1];
        if (!STBImage.stbi_info_from_memory(imageBuffer, width, height, new int[1])) {
            throw new IllegalArgumentException("Failed to read image: " + STBImage.stbi_failure_reason());
        }
        final Texture2D texture = new Texture2D(internalFormat, width[0], height[0]);
        texture.uploadImage(0, 0, GL11C.GL_RGBA, imageBuffer);
        return texture;
    }

    public void uploadImage(final int x, final int y, final int pixelFormat, final byte[] imageBytes) {
        this.uploadImage(0, x, y, pixelFormat, imageBytes);
    }

    public void uploadImage(final int level, final int x, final int y, final int pixelFormat, final byte[] imageBytes) {
        this.uploadImage(level, x, y, 0, pixelFormat, imageBytes);
    }

    public void uploadImage(final int x, final int y, final int pixelFormat, final ByteBuffer imageBuffer) {
        this.uploadImage(0, x, y, pixelFormat, imageBuffer);
    }

    public void uploadImage(final int level, final int x, final int y, final int pixelFormat, final ByteBuffer imageBuffer) {
        this.uploadImage(level, x, y, 0, pixelFormat, imageBuffer);
    }

    public void uploadPixels(final int x, final int y, final int width, final int height, final int pixelFormat, final int[] pixels) {
        this.uploadPixels(0, x, y, width, height, pixelFormat, pixels);
    }

    public void uploadPixels(final int level, final int x, final int y, final int width, final int height, final int pixelFormat, final int[] pixels) {
        this.uploadPixels(level, x, y, 0, width, height, 1, pixelFormat, pixels);
    }

    public void uploadPixels(final int x, final int y, final int width, final int height, final int pixelFormat, final int[] pixels, final boolean bigEndian) {
        this.uploadPixels(0, x, y, width, height, pixelFormat, pixels, bigEndian);
    }

    public void uploadPixels(final int level, final int x, final int y, final int width, final int height, final int pixelFormat, final int[] pixels, final boolean bigEndian) {
        this.uploadPixels(level, x, y, 0, width, height, 1, pixelFormat, pixels, bigEndian);
    }

    public void uploadPixels(final int x, final int y, final int width, final int height, final int pixelFormat, final byte[] pixelBytes) {
        this.uploadPixels(0, x, y, width, height, pixelFormat, pixelBytes);
    }

    public void uploadPixels(final int level, final int x, final int y, final int width, final int height, final int pixelFormat, final byte[] pixelBytes) {
        this.uploadPixels(level, x, y, 0, width, height, 1, pixelFormat, pixelBytes);
    }

    public void uploadPixels(final int x, final int y, final int width, final int height, final int pixelFormat, final ByteBuffer pixelBuffer) {
        this.uploadPixels(0, x, y, width, height, pixelFormat, pixelBuffer);
    }

    public void uploadPixels(final int level, final int x, final int y, final int width, final int height, final int pixelFormat, final ByteBuffer pixelBuffer) {
        this.uploadPixels(level, x, y, 0, width, height, 1, pixelFormat, pixelBuffer);
    }

    public byte[] downloadPngImageBytes(final int x, final int y, final int width, final int height, final int pixelFormat) {
        return this.downloadPngImageBytes(0, x, y, width, height, pixelFormat);
    }

    public byte[] downloadPngImageBytes(final int level, final int x, final int y, final int width, final int height, final int pixelFormat) {
        return this.downloadPngImageBytes(level, x, y, 0, width, height, pixelFormat);
    }

    public byte[] downloadPixelBytes(final int x, final int y, final int width, final int height, final int pixelFormat) {
        return this.downloadPixelBytes(0, x, y, width, height, pixelFormat);
    }

    public byte[] downloadPixelBytes(final int level, final int x, final int y, final int width, final int height, final int pixelFormat) {
        return this.downloadPixelBytes(level, x, y, 0, width, height, 1, pixelFormat);
    }

    public ByteBuffer downloadPixelBuffer(final int x, final int y, final int width, final int height, final int pixelFormat) {
        return this.downloadPixelBuffer(0, x, y, width, height, pixelFormat);
    }

    public ByteBuffer downloadPixelBuffer(final int level, final int x, final int y, final int width, final int height, final int pixelFormat) {
        return this.downloadPixelBuffer(level, x, y, 0, width, height, 1, pixelFormat);
    }

    public void clear(final int x, final int y, final int width, final int height, final Color color) {
        this.clear(0, x, y, width, height, color);
    }

    public void clear(final int level, final int x, final int y, final int width, final int height, final Color color) {
        this.clear(level, x, y, 0, width, height, 1, color);
    }

}
