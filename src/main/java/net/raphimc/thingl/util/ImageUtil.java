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

import net.lenni0451.commons.math.MathUtils;

import java.nio.ByteBuffer;

public class ImageUtil {

    public static ByteBuffer packTightly(final ByteBuffer sourcePixelBuffer, final int width, final int height, final int bytesPerPixel, final int sourceRowPitch) {
        return packTightly(sourcePixelBuffer, width, height, bytesPerPixel, sourceRowPitch, true);
    }

    public static ByteBuffer packTightly(final ByteBuffer sourcePixelBuffer, final int width, final int height, final int bytesPerPixel, final int sourceRowPitch, final boolean freeSource) {
        return packTightly(sourcePixelBuffer, width, height, bytesPerPixel, sourceRowPitch, freeSource, sourcePixelBuffer.isDirect());
    }

    public static ByteBuffer packTightly(final ByteBuffer sourcePixelBuffer, final int width, final int height, final int bytesPerPixel, final int sourceRowPitch, final boolean freeSource, final boolean allocateDirect) {
        final ByteBuffer destinationPixelBuffer = BufferUtil.memAlloc(width * height * bytesPerPixel, allocateDirect);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int b = 0; b < bytesPerPixel; b++) {
                    destinationPixelBuffer.put((y * width + x) * bytesPerPixel + b, sourcePixelBuffer.get(y * sourceRowPitch + x * bytesPerPixel + b));
                }
            }
        }
        if (freeSource && sourcePixelBuffer.isDirect()) {
            BufferUtil.memFree(sourcePixelBuffer);
        }
        return destinationPixelBuffer;
    }

    public static ByteBuffer packMonochromeTightly(final ByteBuffer sourcePixelBuffer, final int width, final int height, final int sourceRowPitch) {
        return packMonochromeTightly(sourcePixelBuffer, width, height, sourceRowPitch, true);
    }

    public static ByteBuffer packMonochromeTightly(final ByteBuffer sourcePixelBuffer, final int width, final int height, final int sourceRowPitch, final boolean freeSource) {
        return packMonochromeTightly(sourcePixelBuffer, width, height, sourceRowPitch, freeSource, sourcePixelBuffer.isDirect());
    }

    public static ByteBuffer packMonochromeTightly(final ByteBuffer sourcePixelBuffer, final int width, final int height, final int sourceRowPitch, final boolean freeSource, final boolean allocateDirect) {
        return packTightly(sourcePixelBuffer, (width + 7) / 8, height, 1, sourceRowPitch, freeSource, allocateDirect);
    }

    public static ByteBuffer convertMonochromeToGrayscale(final ByteBuffer sourcePixelBuffer, final int width, final int height) {
        return convertMonochromeToGrayscale(sourcePixelBuffer, width, height, true);
    }

    public static ByteBuffer convertMonochromeToGrayscale(final ByteBuffer sourcePixelBuffer, final int width, final int height, final boolean freeSource) {
        return convertMonochromeToGrayscale(sourcePixelBuffer, width, height, freeSource, sourcePixelBuffer.isDirect());
    }

    public static ByteBuffer convertMonochromeToGrayscale(final ByteBuffer sourcePixelBuffer, final int width, final int height, final boolean freeSource, final boolean allocateDirect) {
        final int bytesPerRow = (width + 7) / 8;
        final ByteBuffer destinationPixelBuffer = BufferUtil.memAlloc(width * height, allocateDirect);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final int byteIndex = x / 8;
                final int bitIndex = 7 - (x & 7);
                final byte value = sourcePixelBuffer.get(y * bytesPerRow + byteIndex);
                final byte grayValue = (byte) (((value >> bitIndex) & 1) * 255);
                destinationPixelBuffer.put(y * width + x, grayValue);
            }
        }
        if (freeSource && sourcePixelBuffer.isDirect()) {
            BufferUtil.memFree(sourcePixelBuffer);
        }
        return destinationPixelBuffer;
    }

    public static ByteBuffer convertGrayscaleToColor(final ByteBuffer sourcePixelBuffer, final int width, final int height, final int destinationChannel) {
        return convertGrayscaleToColor(sourcePixelBuffer, width, height, destinationChannel, true);
    }

    public static ByteBuffer convertGrayscaleToColor(final ByteBuffer sourcePixelBuffer, final int width, final int height, final int destinationChannel, final boolean freeSource) {
        return convertGrayscaleToColor(sourcePixelBuffer, width, height, destinationChannel, freeSource, sourcePixelBuffer.isDirect());
    }

    public static ByteBuffer convertGrayscaleToColor(final ByteBuffer sourcePixelBuffer, final int width, final int height, final int destinationChannel, final boolean freeSource, final boolean allocateDirect) {
        if (destinationChannel < 0 || destinationChannel > 3) {
            throw new IllegalArgumentException("Destination channel must be between 0 and 3 (inclusive)");
        }
        final ByteBuffer destinationPixelBuffer = BufferUtil.memAlloc(width * height * Integer.BYTES, allocateDirect);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final int index = y * width + x;
                destinationPixelBuffer.putInt(index * Integer.BYTES, 0xFFFFFFFF);
                destinationPixelBuffer.put(index * Integer.BYTES + destinationChannel, sourcePixelBuffer.get(index));
            }
        }
        if (freeSource && sourcePixelBuffer.isDirect()) {
            BufferUtil.memFree(sourcePixelBuffer);
        }
        return destinationPixelBuffer;
    }

    public static ByteBuffer convertColorToGrayscale(final ByteBuffer sourcePixelBuffer, final int width, final int height, final int sourceChannel) {
        return convertColorToGrayscale(sourcePixelBuffer, width, height, sourceChannel, true);
    }

    public static ByteBuffer convertColorToGrayscale(final ByteBuffer sourcePixelBuffer, final int width, final int height, final int sourceChannel, final boolean freeSource) {
        return convertColorToGrayscale(sourcePixelBuffer, width, height, sourceChannel, freeSource, sourcePixelBuffer.isDirect());
    }

    public static ByteBuffer convertColorToGrayscale(final ByteBuffer sourcePixelBuffer, final int width, final int height, final int sourceChannel, final boolean freeSource, final boolean allocateDirect) {
        if (sourceChannel < 0 || sourceChannel > 3) {
            throw new IllegalArgumentException("Source channel must be between 0 and 3 (inclusive)");
        }
        final ByteBuffer destinationPixelBuffer = BufferUtil.memAlloc(width * height, allocateDirect);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final int index = y * width + x;
                destinationPixelBuffer.put(index, sourcePixelBuffer.get(index * Integer.BYTES + sourceChannel));
            }
        }
        if (freeSource && sourcePixelBuffer.isDirect()) {
            BufferUtil.memFree(sourcePixelBuffer);
        }
        return destinationPixelBuffer;
    }

    public static ByteBuffer revertPreMultipliedAlphaBGRA(final ByteBuffer sourcePixelBuffer) {
        return revertPreMultipliedAlphaBGRA(sourcePixelBuffer, true);
    }

    public static ByteBuffer revertPreMultipliedAlphaBGRA(final ByteBuffer sourcePixelBuffer, final boolean freeSource) {
        return revertPreMultipliedAlphaBGRA(sourcePixelBuffer, freeSource, sourcePixelBuffer.isDirect());
    }

    public static ByteBuffer revertPreMultipliedAlphaBGRA(final ByteBuffer sourcePixelBuffer, final boolean freeSource, final boolean allocateDirect) {
        if (sourcePixelBuffer.limit() % Integer.BYTES != 0) {
            throw new IllegalArgumentException("Source pixel buffer size must be a multiple of 4");
        }
        final int pixelCount = sourcePixelBuffer.limit() / Integer.BYTES;
        final ByteBuffer destinationPixelBuffer = BufferUtil.memAlloc(sourcePixelBuffer.limit(), allocateDirect);
        for (int i = 0; i < pixelCount; i++) {
            final int index = i * Integer.BYTES;
            final int b = sourcePixelBuffer.get(index) & 0xFF;
            final int g = sourcePixelBuffer.get(index + 1) & 0xFF;
            final int r = sourcePixelBuffer.get(index + 2) & 0xFF;
            final int a = sourcePixelBuffer.get(index + 3) & 0xFF;
            if (a == 0) {
                destinationPixelBuffer.putInt(index, 0);
            } else {
                destinationPixelBuffer.put(index, (byte) MathUtils.clamp((b * 255) / a, 0, 255));
                destinationPixelBuffer.put(index + 1, (byte) MathUtils.clamp((g * 255) / a, 0, 255));
                destinationPixelBuffer.put(index + 2, (byte) MathUtils.clamp((r * 255) / a, 0, 255));
                destinationPixelBuffer.put(index + 3, (byte) a);
            }
        }
        if (freeSource && sourcePixelBuffer.isDirect()) {
            BufferUtil.memFree(sourcePixelBuffer);
        }
        return destinationPixelBuffer;
    }

    public static ByteBuffer thresholdGrayscale(final ByteBuffer sourcePixelBuffer, final int alphaThreshold) {
        return thresholdGrayscale(sourcePixelBuffer, alphaThreshold, true);
    }

    public static ByteBuffer thresholdGrayscale(final ByteBuffer sourcePixelBuffer, final int alphaThreshold, final boolean freeSource) {
        return thresholdGrayscale(sourcePixelBuffer, alphaThreshold, freeSource, sourcePixelBuffer.isDirect());
    }

    public static ByteBuffer thresholdGrayscale(final ByteBuffer sourcePixelBuffer, final int alphaThreshold, final boolean freeSource, final boolean allocateDirect) {
        final ByteBuffer destinationPixelBuffer = BufferUtil.memAlloc(sourcePixelBuffer.limit(), allocateDirect);
        for (int i = 0; i < sourcePixelBuffer.limit(); i++) {
            final int grayValue = sourcePixelBuffer.get(i) & 0xFF;
            final byte newGrayValue = (byte) (grayValue >= alphaThreshold ? 255 : 0);
            destinationPixelBuffer.put(i, newGrayValue);
        }
        if (freeSource && sourcePixelBuffer.isDirect()) {
            BufferUtil.memFree(sourcePixelBuffer);
        }
        return destinationPixelBuffer;
    }

}
