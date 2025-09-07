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

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class ImageUtil {

    public static ByteBuffer convertMonochromeToGrayscale(final ByteBuffer sourcePixelBuffer, final int width, final int height) {
        return convertMonochromeToGrayscale(sourcePixelBuffer, width, height, (width + 7) / 8);
    }

    public static ByteBuffer convertMonochromeToGrayscale(final ByteBuffer sourcePixelBuffer, final int width, final int height, final int sourceRowPitch) {
        final ByteBuffer destinationPixelBuffer = MemoryUtil.memAlloc(width * height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final int byteIndex = x / 8;
                final int bitIndex = 7 - (x & 7);
                final byte value = sourcePixelBuffer.get(y * sourceRowPitch + byteIndex);
                final byte grayValue = (byte) (((value >> bitIndex) & 1) * 255);
                destinationPixelBuffer.put(y * width + x, grayValue);
            }
        }
        return destinationPixelBuffer;
    }

    public static ByteBuffer convertGrayscaleToARGB(final ByteBuffer sourcePixelBuffer, final int width, final int height) {
        return convertGrayscaleToARGB(sourcePixelBuffer, width, height, width);
    }

    public static ByteBuffer convertGrayscaleToARGB(final ByteBuffer sourcePixelBuffer, final int width, final int height, final int sourceRowPitch) {
        final ByteBuffer destinationPixelBuffer = MemoryUtil.memAlloc(width * height * 4);
        final IntBuffer destinationIntPixelBuffer = destinationPixelBuffer.asIntBuffer();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final byte grayValue = sourcePixelBuffer.get(y * sourceRowPitch + x);
                final int rgbaValue = 0x00FFFFFF | ((grayValue & 0xFF) << 24);
                destinationIntPixelBuffer.put(y * width + x, rgbaValue);
            }
        }
        return destinationPixelBuffer;
    }

}
