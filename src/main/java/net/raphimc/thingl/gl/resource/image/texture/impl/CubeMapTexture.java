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
package net.raphimc.thingl.gl.resource.image.texture.impl;

import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.resource.image.ImageStorage3D;
import net.raphimc.thingl.gl.resource.image.texture.SampledTexture;
import net.raphimc.thingl.resource.image.Image;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL13C;

public class CubeMapTexture extends SampledTexture implements ImageStorage3D {

    // Cube map textures have a depth of 1 according to OpenGL specifications, but they are conceptually a 2D texture array with 6 layers.
    private static final int CUBE_MAP_DEPTH = 6;

    public CubeMapTexture(final int internalFormat, final int size) {
        this(internalFormat, size, 1);
    }

    public CubeMapTexture(final int internalFormat, final int size, final int mipMapLevels) {
        super(GL13C.GL_TEXTURE_CUBE_MAP);
        ThinGL.glBackend().textureStorage2D(this.getGlId(), mipMapLevels, internalFormat, size, size);
    }

    protected CubeMapTexture(final int glId) {
        super(glId, null);
    }

    public static CubeMapTexture fromGlIdUnsafe(final int glId) {
        return new CubeMapTexture(glId);
    }

    public void uploadImage(final int x, final int y, final int z, final Image image) {
        this.uploadImage(0, x, y, z, image);
    }

    @Override
    public void uploadImage(final int level, final int x, final int y, final int z, final Image image) {
        super.uploadImage(level, x, y, z, image);
    }

    public Image downloadImage(final int x, final int y, final int z, final int width, final int height, final int depth, final int pixelFormat) {
        return this.downloadImage(x, y, z, width, height, depth, pixelFormat, GL11C.GL_UNSIGNED_BYTE);
    }

    public Image downloadImage(final int x, final int y, final int z, final int width, final int height, final int depth, final int pixelFormat, final int pixelDataType) {
        return this.downloadImage(0, x, y, z, width, height, depth, pixelFormat, pixelDataType);
    }

    @Override
    public Image downloadImage(final int level, final int x, final int y, final int z, final int width, final int height, final int depth, final int pixelFormat, final int pixelDataType) {
        return super.downloadImage(level, x, y, z, width, height, depth, pixelFormat, pixelDataType);
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
