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
package net.raphimc.thingl.awt.texture;

import net.raphimc.thingl.resource.image.texture.Texture2D;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;

import java.awt.image.BufferedImage;

public class AwtTexture2D extends Texture2D {

    public AwtTexture2D(final BufferedImage image) {
        this(GL11C.GL_RGBA8, image);
    }

    public AwtTexture2D(final int internalFormat, final BufferedImage image) {
        super(internalFormat, image.getWidth(), image.getHeight());
        this.uploadImage(0, 0, image);
    }

    public void uploadImage(final int x, final int y, final BufferedImage image) {
        final int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        this.uploadPixels(x, y, image.getWidth(), image.getHeight(), GL12C.GL_BGRA, pixels, false);
    }

    public void uploadImage(final int level, final int x, final int y, final BufferedImage image) {
        final int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        this.uploadPixels(level, x, y, image.getWidth(), image.getHeight(), GL12C.GL_BGRA, pixels, false);
    }

}
