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
import net.raphimc.thingl.gl.resource.image.ImageStorage1D;
import net.raphimc.thingl.gl.resource.image.texture.SampledTexture;
import net.raphimc.thingl.resource.image.Image;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL45C;

public class Texture1D extends SampledTexture implements ImageStorage1D {

    public Texture1D(final int internalFormat, final int width) {
        this(internalFormat, width, 1);
    }

    public Texture1D(final int internalFormat, final int width, final int mipMapLevels) {
        super(GL11C.GL_TEXTURE_1D);
        GL45C.glTextureStorage1D(this.getGlId(), mipMapLevels, internalFormat, width);
    }

    protected Texture1D(final int glId) {
        super(glId, null);
    }

    public static Texture1D fromGlIdUnsafe(final int glId) {
        return new Texture1D(glId);
    }

    public void uploadImage(final int x, final Image image) {
        this.uploadImage(0, x, image);
    }

    public void uploadImage(final int level, final int x, final Image image) {
        this.uploadImage(level, x, 0, 0, image);
    }

    public Image downloadImage(final int x, final int width, final int pixelFormat) {
        return this.downloadImage(x, width,  pixelFormat, GL11C.GL_UNSIGNED_BYTE);
    }

    public Image downloadImage(final int x, final int width, final int pixelFormat, final int pixelDataType) {
        return this.downloadImage(0, x, width, pixelFormat, pixelDataType);
    }

    public Image downloadImage(final int level, final int x, final int width, final int pixelFormat, final int pixelDataType) {
        return this.downloadImage(level, x, 0, 0, width, 1, 1, pixelFormat, pixelDataType);
    }

    public void clear(final int x, final int width, final Color color) {
        this.clear(0, x, width, color);
    }

    public void clear(final int level, final int x, final int width, final Color color) {
        this.clear(level, x, 0, 0, width, 1, 1, color);
    }

}
