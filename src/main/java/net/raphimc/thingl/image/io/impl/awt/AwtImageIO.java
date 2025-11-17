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
package net.raphimc.thingl.image.io.impl.awt;

import net.raphimc.thingl.image.io.ByteImage2DReader;
import net.raphimc.thingl.resource.image.impl.ByteImage2D;
import net.raphimc.thingl.resource.memory.Memory;
import org.lwjgl.opengl.GL12C;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

public class AwtImageIO implements ByteImage2DReader {

    public static final AwtImageIO INSTANCE = new AwtImageIO();

    @Override
    public ByteImage2D readByteImage2D(final Memory imageData, final boolean forceColor, final boolean freeImageData) {
        final byte[] imageBytes = imageData.getBytes(0, imageData.getSizeAsInt());
        if (freeImageData) {
            imageData.free();
        }
        return this.readByteImage2D(imageBytes, forceColor);
    }

    @Override
    public ByteImage2D readByteImage2D(final byte[] imageBytes, final boolean forceColor) {
        final BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (bufferedImage == null) {
                throw new IllegalArgumentException("Failed to read image");
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read image", e);
        }
        return this.createByteImage2D(bufferedImage, forceColor);
    }

    public ByteImage2D createByteImage2D(final BufferedImage bufferedImage) {
        return this.createByteImage2D(bufferedImage, true);
    }

    public ByteImage2D createByteImage2D(final BufferedImage bufferedImage, final boolean forceColor) {
        final int[] awtPixels = new int[bufferedImage.getWidth() * bufferedImage.getHeight()];
        bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), awtPixels, 0, bufferedImage.getWidth());
        final ByteImage2D image = new ByteImage2D(bufferedImage.getWidth(), bufferedImage.getHeight(), GL12C.GL_BGRA);
        final Memory pixels = image.getPixels();
        for (int i = 0; i < awtPixels.length; i++) {
            pixels.putInt((long) i * Integer.BYTES, awtPixels[i]);
        }
        return image;
    }

}
