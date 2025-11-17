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
import net.raphimc.thingl.gl.resource.image.ImageStorage2D;
import net.raphimc.thingl.gl.resource.image.texture.SampledTexture;
import net.raphimc.thingl.image.io.impl.stb.StbImageIO;
import net.raphimc.thingl.resource.image.Image;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL45C;

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
        return fromImage(StbImageIO.INSTANCE.readByteImage2D(imageBytes));
    }

    public static Texture2D fromImage(final int internalFormat, final byte[] imageBytes) {
        return fromImage(internalFormat, StbImageIO.INSTANCE.readByteImage2D(imageBytes));
    }

    public static Texture2D fromImage(final Image image) {
        return fromImage(GL11C.GL_RGBA8, image);
    }

    public static Texture2D fromImage(final int internalFormat, final Image image) {
        return fromImage(internalFormat, image, true);
    }

    public static Texture2D fromImage(final int internalFormat, final Image image, final boolean freeImage) {
        final Texture2D texture = new Texture2D(internalFormat, image.getWidth(), image.getHeight());
        texture.uploadImage(0, 0, image);
        if (freeImage) {
            image.free();
        }
        return texture;
    }

    public void uploadImage(final int x, final int y, final Image image) {
        this.uploadImage(0, x, y, image);
    }

    public void uploadImage(final int level, final int x, final int y, final Image image) {
        this.uploadImage(level, x, y, 0, image);
    }

    public Image downloadImage(final int x, final int y, final int width, final int height, final int pixelFormat) {
        return this.downloadImage(x, y, width, height, pixelFormat, GL11C.GL_UNSIGNED_BYTE);
    }

    public Image downloadImage(final int x, final int y, final int width, final int height, final int pixelFormat, final int pixelDataType) {
        return this.downloadImage(0, x, y, width, height, pixelFormat, pixelDataType);
    }

    public Image downloadImage(final int level, final int x, final int y, final int width, final int height, final int pixelFormat, final int pixelDataType) {
        return this.downloadImage(level, x, y, 0, width, height, 1, pixelFormat, pixelDataType);
    }

    public void clear(final int x, final int y, final int width, final int height, final Color color) {
        this.clear(0, x, y, width, height, color);
    }

    public void clear(final int level, final int x, final int y, final int width, final int height, final Color color) {
        this.clear(level, x, y, 0, width, height, 1, color);
    }

}
