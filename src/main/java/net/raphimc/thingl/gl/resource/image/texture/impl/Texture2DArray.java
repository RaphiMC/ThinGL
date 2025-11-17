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
import net.raphimc.thingl.gl.resource.image.ImageStorage3D;
import net.raphimc.thingl.gl.resource.image.texture.SampledTexture;
import net.raphimc.thingl.resource.image.Image;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL45C;

public class Texture2DArray extends SampledTexture implements ImageStorage3D {

    public Texture2DArray(final int internalFormat, final int width, final int height, final int layers) {
        this(internalFormat, width, height, layers, 1);
    }

    public Texture2DArray(final int internalFormat, final int width, final int height, final int layers, final int mipMapLevels) {
        super(GL30C.GL_TEXTURE_2D_ARRAY);
        GL45C.glTextureStorage3D(this.getGlId(), mipMapLevels, internalFormat, width, height, layers);
    }

    protected Texture2DArray(final int glId) {
        super(glId, null);
    }

    public static Texture2DArray fromGlIdUnsafe(final int glId) {
        return new Texture2DArray(glId);
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

}
