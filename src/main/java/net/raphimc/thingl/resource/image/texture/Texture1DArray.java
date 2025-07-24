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
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL45C;

import java.nio.ByteBuffer;

public class Texture1DArray extends SampledTexture implements ImageStorage2D {

    public Texture1DArray(final int internalFormat, final int width, final int layers) {
        this(internalFormat, width, layers, 1);
    }

    public Texture1DArray(final int internalFormat, final int width, final int layers, final int mipMapLevels) {
        super(GL30C.GL_TEXTURE_1D_ARRAY);
        GL45C.glTextureStorage2D(this.getGlId(), mipMapLevels, internalFormat, width, layers);
    }

    protected Texture1DArray(final int glId) {
        super(glId, null);
    }

    public static Texture1DArray fromGlIdUnsafe(final int glId) {
        return new Texture1DArray(glId);
    }

    public void uploadPixels(final int x, final int y, final int width, final int pixelFormat, final int[] pixels) {
        this.uploadPixels(0, x, y, width, pixelFormat, pixels);
    }

    public void uploadPixels(final int level, final int x, final int y, final int width, final int pixelFormat, final int[] pixels) {
        this.uploadPixels(level, x, y, 0, width, 1, 1, pixelFormat, pixels);
    }

    public void uploadPixels(final int x, final int y, final int width, final int pixelFormat, final int[] pixels, final boolean bigEndian) {
        this.uploadPixels(0, x, y, width, pixelFormat, pixels, bigEndian);
    }

    public void uploadPixels(final int level, final int x, final int y, final int width, final int pixelFormat, final int[] pixels, final boolean bigEndian) {
        this.uploadPixels(level, x, y, 0, width, 1, 1, pixelFormat, pixels, bigEndian);
    }

    public void uploadPixels(final int x, final int y, final int width, final int pixelFormat, final byte[] pixelBytes) {
        this.uploadPixels(0, x, y, width, pixelFormat, pixelBytes);
    }

    public void uploadPixels(final int level, final int x, final int y, final int width, final int pixelFormat, final byte[] pixelBytes) {
        this.uploadPixels(level, x, y, 0, width, 1, 1, pixelFormat, pixelBytes);
    }

    public void uploadPixels(final int x, final int y, final int width, final int pixelFormat, final ByteBuffer pixelBuffer) {
        this.uploadPixels(0, x, y, width, pixelFormat, pixelBuffer);
    }

    public void uploadPixels(final int level, final int x, final int y, final int width, final int pixelFormat, final ByteBuffer pixelBuffer) {
        this.uploadPixels(level, x, y, 0, width, 1, 1, pixelFormat, pixelBuffer);
    }

    public byte[] downloadPixelBytes(final int x, final int y, final int width, final int pixelFormat) {
        return this.downloadPixelBytes(0, x, y, width, pixelFormat);
    }

    public byte[] downloadPixelBytes(final int level, final int x, final int y, final int width, final int pixelFormat) {
        return this.downloadPixelBytes(level, x, y, 0, width, 1, 1, pixelFormat);
    }

    public ByteBuffer downloadPixelBuffer(final int x, final int y, final int width, final int pixelFormat) {
        return this.downloadPixelBuffer(0, x, y, width, pixelFormat);
    }

    public ByteBuffer downloadPixelBuffer(final int level, final int x, final int y, final int width, final int pixelFormat) {
        return this.downloadPixelBuffer(level, x, y, 0, width, 1, 1, pixelFormat);
    }

    public void clear(final int x, final int y, final int width, final int height, final Color color) {
        this.clear(0, x, y, width, height, color);
    }

    public void clear(final int level, final int x, final int y, final int width, final int height, final Color color) {
        this.clear(level, x, y, 0, width, height, 1, color);
    }

}
