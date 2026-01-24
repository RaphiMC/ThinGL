/*
 * This file is part of ThinGL - https://github.com/RaphiMC/ThinGL
 * Copyright (C) 2024-2026 RK_01/RaphiMC and contributors
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
package net.raphimc.thingl.resource.image.impl;

import net.raphimc.thingl.resource.memory.Memory;
import org.lwjgl.opengl.GL12C;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class AwtByteImage2D extends ByteImage2D {

    public AwtByteImage2D(final byte[] imageBytes) throws IOException {
        final BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (image == null) {
            throw new IllegalArgumentException("Failed to read image");
        }
        this(image);
    }

    public AwtByteImage2D(final BufferedImage image) {
        super(image.getWidth(), image.getHeight(), GL12C.GL_BGRA);
        final int[] awtPixels = new int[this.getWidth() * this.getHeight()];
        image.getRGB(0, 0, this.getWidth(), this.getHeight(), awtPixels, 0, this.getWidth());
        final Memory pixels = this.getPixels();
        for (int i = 0; i < awtPixels.length; i++) {
            pixels.putInt((long) i * Integer.BYTES, awtPixels[i]);
        }
    }

}
