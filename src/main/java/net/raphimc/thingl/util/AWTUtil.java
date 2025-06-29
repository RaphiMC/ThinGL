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
package net.raphimc.thingl.util;

import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.resource.texture.AbstractTexture;
import net.raphimc.thingl.resource.texture.Texture2D;

import java.awt.image.BufferedImage;

public class AWTUtil {

    public static Color convertColor(final java.awt.Color color) {
        return Color.fromARGB(color.getRGB());
    }

    public static Texture2D createTextureFromBufferedImage(final BufferedImage bufferedImage) {
        return createTextureFromBufferedImage(AbstractTexture.InternalFormat.RGBA8, bufferedImage);
    }

    public static Texture2D createTextureFromBufferedImage(final AbstractTexture.InternalFormat internalFormat, final BufferedImage bufferedImage) {
        final int[] pixels = new int[bufferedImage.getWidth() * bufferedImage.getHeight()];
        bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), pixels, 0, bufferedImage.getWidth());
        final Texture2D texture = new Texture2D(internalFormat, bufferedImage.getWidth(), bufferedImage.getHeight());
        texture.uploadPixels(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), AbstractTexture.PixelFormat.BGRA, pixels, false);
        return texture;
    }

}
