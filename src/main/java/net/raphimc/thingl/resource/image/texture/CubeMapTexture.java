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
import net.raphimc.thingl.resource.image.ImageStorage3D;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL45C;

import java.nio.ByteBuffer;

public class CubeMapTexture extends SampledTexture implements ImageStorage3D {

    // Cube map textures have a depth of 1 according to OpenGL specifications, but they are conceptually a 2D texture array with 6 layers.
    private static final int CUBE_MAP_DEPTH = 6;

    public CubeMapTexture(final int internalFormat, final int size) {
        this(internalFormat, size, 1);
    }

    public CubeMapTexture(final int internalFormat, final int size, final int mipMapLevels) {
        super(GL13C.GL_TEXTURE_CUBE_MAP);
        GL45C.glTextureStorage2D(this.getGlId(), mipMapLevels, internalFormat, size, size);
    }

    protected CubeMapTexture(final int glId) {
        super(glId, null);
    }

    public static CubeMapTexture fromGlIdUnsafe(final int glId) {
        return new CubeMapTexture(glId);
    }

    public void uploadImage(final int x, final int y, final int z, final int pixelFormat, final byte[] imageBytes) {
        this.uploadImage(0, x, y, z, pixelFormat, imageBytes);
    }

    @Override
    public void uploadImage(final int level, final int x, final int y, final int z, final int pixelFormat, final byte[] imageBytes) {
        super.uploadImage(level, x, y, z, pixelFormat, imageBytes);
    }

    public void uploadImage(final int x, final int y, final int z, final int pixelFormat, final ByteBuffer imageBuffer) {
        this.uploadImage(0, x, y, z, pixelFormat, imageBuffer);
    }

    @Override
    public void uploadImage(final int level, final int x, final int y, final int z, final int pixelFormat, final ByteBuffer imageBuffer) {
        super.uploadImage(level, x, y, z, pixelFormat, imageBuffer);
    }

    public void uploadPixels(final int x, final int y, final int z, final int width, final int height, final int pixelFormat, final int[] pixels) {
        this.uploadPixels(0, x, y, z, width, height, pixelFormat, pixels);
    }

    public void uploadPixels(final int level, final int x, final int y, final int z, final int width, final int height, final int pixelFormat, final int[] pixels) {
        this.uploadPixels(level, x, y, z, width, height, 1, pixelFormat, pixels);
    }

    public void uploadPixels(final int x, final int y, final int z, final int width, final int height, final int pixelFormat, final int[] pixels, final boolean bigEndian) {
        this.uploadPixels(0, x, y, z, width, height, pixelFormat, pixels, bigEndian);
    }

    public void uploadPixels(final int level, final int x, final int y, final int z, final int width, final int height, final int pixelFormat, final int[] pixels, final boolean bigEndian) {
        this.uploadPixels(level, x, y, z, width, height, 1, pixelFormat, pixels, bigEndian);
    }

    public void uploadPixels(final int x, final int y, final int z, final int width, final int height, final int pixelFormat, final byte[] pixelBytes) {
        this.uploadPixels(0, x, y, z, width, height, pixelFormat, pixelBytes);
    }

    public void uploadPixels(final int level, final int x, final int y, final int z, final int width, final int height, final int pixelFormat, final byte[] pixelBytes) {
        this.uploadPixels(level, x, y, z, width, height, 1, pixelFormat, pixelBytes);
    }

    public void uploadPixels(final int x, final int y, final int z, final int width, final int height, final int pixelFormat, final ByteBuffer pixelBuffer) {
        this.uploadPixels(0, x, y, z, width, height, pixelFormat, pixelBuffer);
    }

    public void uploadPixels(final int level, final int x, final int y, final int z, final int width, final int height, final int pixelFormat, final ByteBuffer pixelBuffer) {
        this.uploadPixels(level, x, y, z, width, height, 1, pixelFormat, pixelBuffer);
    }

    public byte[] downloadPngImageBytes(final int x, final int y, final int z, final int width, final int height, final int pixelFormat) {
        return this.downloadPngImageBytes(0, x, y, z, width, height, pixelFormat);
    }

    @Override
    public byte[] downloadPngImageBytes(final int level, final int x, final int y, final int z, final int width, final int height, final int pixelFormat) {
        return super.downloadPngImageBytes(level, x, y, z, width, height, pixelFormat);
    }

    public byte[] downloadPixelBytes(final int x, final int y, final int z, final int width, final int height, final int pixelFormat) {
        return this.downloadPixelBytes(0, x, y, z, width, height, pixelFormat);
    }

    public byte[] downloadPixelBytes(final int level, final int x, final int y, final int z, final int width, final int height, final int pixelFormat) {
        return this.downloadPixelBytes(level, x, y, z, width, height, 1, pixelFormat);
    }

    public ByteBuffer downloadPixelBuffer(final int x, final int y, final int z, final int width, final int height, final int pixelFormat) {
        return this.downloadPixelBuffer(0, x, y, z, width, height, pixelFormat);
    }

    public ByteBuffer downloadPixelBuffer(final int level, final int x, final int y, final int z, final int width, final int height, final int pixelFormat) {
        return this.downloadPixelBuffer(level, x, y, z, width, height, 1, pixelFormat);
    }

    public void clear(final int x, final int y, final int z, final int width, final int height, final int depth, final Color color) {
        this.clear(0, x, y, z, width, height, depth, color);
    }

    @Override
    public void clear(final int level, final int x, final int y, final int z, final int width, final int height, final int depth, final Color color) {
        super.clear(level, x, y, z, width, height, depth, color);
    }

    @Override
    public int getLevelParameterInt(final int level, final int parameter) {
        if (parameter == GL12C.GL_TEXTURE_DEPTH) {
            return CUBE_MAP_DEPTH;
        } else {
            return super.getLevelParameterInt(level, parameter);
        }
    }

}
