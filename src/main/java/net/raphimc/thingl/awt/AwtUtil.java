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
package net.raphimc.thingl.awt;

import net.raphimc.thingl.resource.image.texture.Texture2D;
import net.raphimc.thingl.resource.image.texture.Texture2DArray;
import org.lwjgl.opengl.GL12C;

import java.awt.image.BufferedImage;

public class AwtUtil {

    public static void uploadBufferedImageToTexture2D(final Texture2D texture, final int x, final int y, final BufferedImage image) {
        final int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        texture.uploadPixels(x, y, image.getWidth(), image.getHeight(), GL12C.GL_BGRA, pixels, false);
    }

    public static void uploadBufferedImageToTexture2DArray(final Texture2DArray texture, final int x, final int y, final int layer, final BufferedImage image) {
        final int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        texture.uploadPixels(x, y, layer, image.getWidth(), image.getHeight(), GL12C.GL_BGRA, pixels, false);
    }

}
