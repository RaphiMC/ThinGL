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
package net.raphimc.thingl.resource.image.impl;

import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.resource.image.Image;
import net.raphimc.thingl.resource.memory.Memory;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL30C;

public class ByteImage2D extends Image {

    public ByteImage2D(final int width, final int height, final int pixelFormat) {
        super(width, height, 1, pixelFormat, GL11C.GL_UNSIGNED_BYTE);
    }

    public ByteImage2D(final int width, final int height, final int pixelFormat, final Memory pixels) {
        super(width, height, 1, pixelFormat, GL11C.GL_UNSIGNED_BYTE, pixels);
    }

    public ByteImage2D(final int width, final int height, final int pixelFormat, final Memory pixels, final boolean freePixels) {
        super(width, height, 1, pixelFormat, GL11C.GL_UNSIGNED_BYTE, pixels, freePixels);
    }

    public ByteImage2D(final Image image) {
        super(image);
        try {
            if (image.getDepth() != 1) {
                throw new IllegalArgumentException("Image depth must be 1");
            }
            if (image.getPixelDataType() != GL11C.GL_UNSIGNED_BYTE) {
                throw new IllegalArgumentException("Image pixel data type must be GL_UNSIGNED_BYTE");
            }
        } catch (Throwable e) {
            this.free();
            throw e;
        }
    }

    public ByteImage2D flipX() {
        final int width = this.getWidth();
        final int height = this.getHeight();
        final int channels = this.getChannels();
        final Memory pixels = this.getPixels();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width / 2; x++) {
                final long leftIndex = ((long) y * width + x) * channels;
                final long rightIndex = ((long) y * width + (width - 1 - x)) * channels;
                for (int c = 0; c < channels; c++) {
                    final byte leftVal = pixels.getByte(leftIndex + c);
                    final byte rightVal = pixels.getByte(rightIndex + c);
                    pixels.putByte(leftIndex + c, rightVal);
                    pixels.putByte(rightIndex + c, leftVal);
                }
            }
        }
        return this;
    }

    public ByteImage2D flipY() {
        final int width = this.getWidth();
        final int height = this.getHeight();
        final int channels = this.getChannels();
        final Memory pixels = this.getPixels();
        for (int y = 0; y < height / 2; y++) {
            for (int x = 0; x < width; x++) {
                final long topIndex = ((long) y * width + x) * channels;
                final long bottomIndex = ((long) (height - 1 - y) * width + x) * channels;
                for (int c = 0; c < channels; c++) {
                    final byte topVal = pixels.getByte(topIndex + c);
                    final byte bottomVal = pixels.getByte(bottomIndex + c);
                    pixels.putByte(topIndex + c, bottomVal);
                    pixels.putByte(bottomIndex + c, topVal);
                }
            }
        }
        return this;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public ByteImage2D rotate90() {
        final int width = this.getWidth();
        final int height = this.getHeight();
        final int channels = this.getChannels();
        final Memory pixels = this.getPixels();
        final ByteImage2D rotatedImage = new ByteImage2D(height, width, this.getPixelFormat());
        final Memory rotatedPixels = rotatedImage.getPixels();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final long srcIndex = ((long) y * width + x) * channels;
                final long dstIndex = ((long) x * height + (height - 1 - y)) * channels;
                for (int c = 0; c < channels; c++) {
                    rotatedPixels.putByte(dstIndex + c, pixels.getByte(srcIndex + c));
                }
            }
        }
        this.free();
        return rotatedImage;
    }

    public ByteImage2D rotate180() {
        final int channels = this.getChannels();
        final Memory pixels = this.getPixels();
        final long totalPixels = pixels.getSize() / channels;
        for (long i = 0; i < totalPixels / 2; i++) {
            final long indexA = i * channels;
            final long indexB = (totalPixels - 1 - i) * channels;
            for (int c = 0; c < channels; c++) {
                final byte val = pixels.getByte(indexA + c);
                pixels.putByte(indexA + c, pixels.getByte(indexB + c));
                pixels.putByte(indexB + c, val);
            }
        }
        return this;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public ByteImage2D rotate270() {
        final int width = this.getWidth();
        final int height = this.getHeight();
        final int channels = this.getChannels();
        final Memory pixels = this.getPixels();
        final ByteImage2D rotatedImage = new ByteImage2D(height, width, this.getPixelFormat());
        final Memory rotatedPixels = rotatedImage.getPixels();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final long srcIndex = ((long) y * width + x) * channels;
                final long dstIndex = ((long) (width - 1 - x) * height + y) * channels;
                for (int c = 0; c < channels; c++) {
                    rotatedPixels.putByte(dstIndex + c, pixels.getByte(srcIndex + c));
                }
            }
        }
        this.free();
        return rotatedImage;
    }

    public ByteImage2D clear() {
        return this.clear(Color.TRANSPARENT);
    }

    public ByteImage2D clear(final Color color) {
        if (color.equals(Color.TRANSPARENT)) {
            this.getPixels().clear();
            return this;
        } else {
            return this.clear(0, 0, this.getWidth(), this.getHeight());
        }
    }

    public ByteImage2D clear(final int x, final int y, final int width, final int height) {
        return this.clear(x, y, width, height, Color.TRANSPARENT);
    }

    public ByteImage2D clear(final int x, final int y, final int width, final int height, final Color color) {
        if (x < 0 || y < 0 || x + width > this.getWidth() || y + height > this.getHeight()) {
            throw new IllegalArgumentException("Clear area is out of bounds");
        }

        final Memory pixels = this.getPixels();
        switch (this.getPixelFormat()) {
            case GL11C.GL_RGB -> {
                for (int yOffset = 0; yOffset < height; yOffset++) {
                    for (int xOffset = 0; xOffset < width; xOffset++) {
                        final int index = ((y + yOffset) * this.getWidth() + (x + xOffset)) * 3;
                        pixels.putByte(index, (byte) color.getRed());
                        pixels.putByte(index + 1, (byte) color.getGreen());
                        pixels.putByte(index + 2, (byte) color.getBlue());
                    }
                }
            }
            case GL12C.GL_BGR -> {
                for (int yOffset = 0; yOffset < height; yOffset++) {
                    for (int xOffset = 0; xOffset < width; xOffset++) {
                        final int index = ((y + yOffset) * this.getWidth() + (x + xOffset)) * 3;
                        pixels.putByte(index, (byte) color.getBlue());
                        pixels.putByte(index + 1, (byte) color.getGreen());
                        pixels.putByte(index + 2, (byte) color.getRed());
                    }
                }
            }
            case GL11C.GL_RGBA -> {
                final int colorValue = color.toABGR();
                for (int yOffset = 0; yOffset < height; yOffset++) {
                    for (int xOffset = 0; xOffset < width; xOffset++) {
                        final int index = ((y + yOffset) * this.getWidth() + (x + xOffset)) * 4;
                        pixels.putInt(index, colorValue);
                    }
                }
            }
            case GL12C.GL_BGRA -> {
                final int colorValue = color.toARGB();
                for (int yOffset = 0; yOffset < height; yOffset++) {
                    for (int xOffset = 0; xOffset < width; xOffset++) {
                        final int index = ((y + yOffset) * this.getWidth() + (x + xOffset)) * 4;
                        pixels.putInt(index, colorValue);
                    }
                }
            }
            default -> throw new IllegalStateException("Unsupported pixel format: " + this.getPixelFormat());
        }
        return this;
    }

    public ByteImage2D invertColors() {
        final Memory pixels = this.getPixels();
        if (this.getPixelFormat() == GL11C.GL_RGBA || this.getPixelFormat() == GL12C.GL_BGRA) {
            for (long i = 0; i < pixels.getSize(); i++) {
                if ((i + 1) % 4 == 0) {
                    continue; // Skip alpha channel
                }
                pixels.putByte(i, (byte) (~pixels.getByte(i) & 0xFF));
            }
        } else if (this.getPixelFormat() == GL11C.GL_RGB || this.getPixelFormat() == GL12C.GL_BGR) {
            for (long i = 0; i < pixels.getSize(); i++) {
                pixels.putByte(i, (byte) (~pixels.getByte(i) & 0xFF));
            }
        } else {
            throw new IllegalStateException("Image is not color");
        }
        return this;
    }

    public ByteImage2D unpremultiplyAlpha() {
        final Memory pixels = this.getPixels();
        if (this.getPixelFormat() == GL11C.GL_RGBA || this.getPixelFormat() == GL12C.GL_BGRA) {
            for (long i = 0; i < pixels.getSize(); i += 4) {
                final int a = pixels.getByte(i + 3) & 0xFF;
                if (a == 0) continue;
                final int c1 = pixels.getByte(i) & 0xFF;
                final int c2 = pixels.getByte(i + 1) & 0xFF;
                final int c3 = pixels.getByte(i + 2) & 0xFF;
                pixels.putByte(i, (byte) Math.min((c1 * 255) / a, 255));
                pixels.putByte(i + 1, (byte) Math.min((c2 * 255) / a, 255));
                pixels.putByte(i + 2, (byte) Math.min((c3 * 255) / a, 255));
            }
        } else {
            throw new IllegalStateException("Image is not RGBA or BGRA");
        }
        return this;
    }

    public ByteImage2D thresholdGrayscale(final int threshold) {
        if (this.getChannels() != 1) {
            throw new IllegalStateException("Image is not grayscale");
        }
        final Memory pixels = this.getPixels();
        for (long i = 0; i < pixels.getSize(); i++) {
            pixels.putByte(i, (byte) ((pixels.getByte(i) & 0xFF) >= threshold ? 255 : 0));
        }
        return this;
    }

    public ByteImage2D convertColorToGrayscale() {
        return this.convertColorToGrayscale(GrayscaleConversionMode.REC709);
    }

    public ByteImage2D convertColorToGrayscale(final GrayscaleConversionMode mode) {
        final int width = this.getWidth();
        final int height = this.getHeight();
        final Memory colorPixels = this.getPixels();
        if (this.getChannels() == 3) {
            final ByteImage2D grayImage = new ByteImage2D(width, height, GL11C.GL_RED);
            final Memory grayPixels = grayImage.getPixels();
            if (this.getPixelFormat() == GL11C.GL_RGB) {
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        final long index = ((long) y * width + x);
                        final int r = colorPixels.getByte(index * 3) & 0xFF;
                        final int g = colorPixels.getByte(index * 3 + 1) & 0xFF;
                        final int b = colorPixels.getByte(index * 3 + 2) & 0xFF;
                        final int gray = mode.convert(r, g, b);
                        grayPixels.putByte(index, (byte) gray);
                    }
                }
            } else if (this.getPixelFormat() == GL12C.GL_BGR) {
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        final long index = ((long) y * width + x);
                        final int b = colorPixels.getByte(index * 3) & 0xFF;
                        final int g = colorPixels.getByte(index * 3 + 1) & 0xFF;
                        final int r = colorPixels.getByte(index * 3 + 2) & 0xFF;
                        final int gray = mode.convert(r, g, b);
                        grayPixels.putByte(index, (byte) gray);
                    }
                }
            } else {
                throw new IllegalStateException("Image pixel format must be RGB or BGR");
            }
            this.free();
            return grayImage;
        } else if (this.getChannels() == 4) {
            final ByteImage2D grayImage = new ByteImage2D(width, height, GL30C.GL_RG);
            final Memory grayPixels = grayImage.getPixels();
            if (this.getPixelFormat() == GL11C.GL_RGBA) {
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        final long index = ((long) y * width + x);
                        final int r = colorPixels.getByte(index * 4) & 0xFF;
                        final int g = colorPixels.getByte(index * 4 + 1) & 0xFF;
                        final int b = colorPixels.getByte(index * 4 + 2) & 0xFF;
                        final int a = colorPixels.getByte(index * 4 + 3) & 0xFF;
                        final int gray = mode.convert(r, g, b);
                        grayPixels.putByte(index * 2, (byte) gray);
                        grayPixels.putByte(index * 2 + 1, (byte) a);
                    }
                }
            } else if (this.getPixelFormat() == GL12C.GL_BGRA) {
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        final long index = ((long) y * width + x);
                        final int b = colorPixels.getByte(index * 4) & 0xFF;
                        final int g = colorPixels.getByte(index * 4 + 1) & 0xFF;
                        final int r = colorPixels.getByte(index * 4 + 2) & 0xFF;
                        final int a = colorPixels.getByte(index * 4 + 3) & 0xFF;
                        final int gray = mode.convert(r, g, b);
                        grayPixels.putByte(index * 2, (byte) gray);
                        grayPixels.putByte(index * 2 + 1, (byte) a);
                    }
                }
            } else {
                throw new IllegalStateException("Image pixel format must be RGBA or BGRA");
            }
            this.free();
            return grayImage;
        } else {
            throw new IllegalStateException("Image is not color");
        }
    }

    public ByteImage2D convertGrayscaleToColor() {
        final Memory grayPixels = this.getPixels();
        if (this.getChannels() == 1) {
            if (this.getPixelFormat() != GL11C.GL_ALPHA) {
                final ByteImage2D colorImage = new ByteImage2D(this.getWidth(), this.getHeight(), GL11C.GL_RGB);
                final Memory colorPixels = colorImage.getPixels();
                for (long i = 0; i < grayPixels.getSize(); i++) {
                    final byte gray = grayPixels.getByte(i);
                    colorPixels.putByte(i * 3, gray);
                    colorPixels.putByte(i * 3 + 1, gray);
                    colorPixels.putByte(i * 3 + 2, gray);
                }
                this.free();
                return colorImage;
            } else {
                final ByteImage2D colorImage = new ByteImage2D(this.getWidth(), this.getHeight(), GL11C.GL_RGBA);
                final Memory colorPixels = colorImage.getPixels();
                for (long i = 0; i < grayPixels.getSize(); i++) {
                    final byte gray = grayPixels.getByte(i);
                    colorPixels.putInt(i * 4, 0xFFFFFFFF);
                    colorPixels.putByte(i * 4 + 3, gray);
                }
                this.free();
                return colorImage;
            }
        } else if (this.getChannels() == 2) {
            final ByteImage2D colorImage = new ByteImage2D(this.getWidth(), this.getHeight(), GL11C.GL_RGBA);
            final Memory colorPixels = colorImage.getPixels();
            for (long i = 0; i < grayPixels.getSize(); i += 2) {
                final byte gray = grayPixels.getByte(i);
                final byte alpha = grayPixels.getByte(i + 1);
                colorPixels.putByte(i * 2, gray);
                colorPixels.putByte(i * 2 + 1, gray);
                colorPixels.putByte(i * 2 + 2, gray);
                colorPixels.putByte(i * 2 + 3, alpha);
            }
            this.free();
            return colorImage;
        } else {
            throw new IllegalStateException("Image is not grayscale");
        }
    }

    public ByteImage2D convertToSingleChannel(final int channel) {
        if (channel < 0 || channel > this.getChannels()) {
            throw new IllegalArgumentException("Channel must be between 0 and " + (this.getChannels() - 1));
        }
        final int channels = this.getChannels();
        final Memory pixels = this.getPixels();
        final ByteImage2D grayImage = new ByteImage2D(this.getWidth(), this.getHeight(), GL11C.GL_RED);
        final Memory grayPixels = grayImage.getPixels();
        for (long i = 0; i < grayPixels.getSize(); i++) {
            grayPixels.putByte(i, pixels.getByte(i * channels + channel));
        }
        this.free();
        return grayImage;
    }

    @Override
    public ByteImage2D withPixelFormat(final int pixelFormat) {
        return new ByteImage2D(super.withPixelFormat(pixelFormat));
    }

    @Override
    public ByteImage2D copy() {
        return new ByteImage2D(super.copy());
    }

    public enum GrayscaleConversionMode {

        AVERAGE {
            @Override
            protected int convert(final int r, final int g, final int b) {
                return (r + g + b) / 3;
            }
        },
        REC601 {
            @Override
            protected int convert(final int r, final int g, final int b) {
                return (int) (0.299F * r + 0.587F * g + 0.114F * b);
            }
        },
        REC709 {
            @Override
            protected int convert(final int r, final int g, final int b) {
                return (int) (0.2126F * r + 0.7152F * g + 0.0722F * b);
            }
        },
        ONLY_RED {
            @Override
            protected int convert(final int r, final int g, final int b) {
                return r;
            }
        },
        ONLY_GREEN {
            @Override
            protected int convert(final int r, final int g, final int b) {
                return g;
            }
        },
        ONLY_BLUE {
            @Override
            protected int convert(final int r, final int g, final int b) {
                return b;
            }
        },
        ;

        protected abstract int convert(final int r, final int g, final int b);

    }

}
