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
package net.raphimc.thingl.resource.image;

import net.raphimc.thingl.memory.allocator.MemoryAllocator;
import net.raphimc.thingl.resource.Resource;
import net.raphimc.thingl.resource.memory.Memory;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL30C;

public class Image extends Resource {

    private final int width;
    private final int height;
    private final int depth;
    private final int pixelFormat;
    private final int pixelDataType;
    private final Memory pixels;
    private final boolean freePixels;
    private final int channels;
    private final int bytesPerPixel;

    public Image(final int width, final int height, final int depth, final int pixelFormat, final int pixelDataType) {
        this(width, height, depth, pixelFormat, pixelDataType, MemoryAllocator.allocateMemory((long) width * height * depth * getBytesPerPixelFromPixelFormat(pixelFormat, pixelDataType)));
    }

    public Image(final int width, final int height, final int depth, final int pixelFormat, final int pixelDataType, final Memory pixels) {
        this(width, height, depth, pixelFormat, pixelDataType, pixels, true);
    }

    public Image(final int width, final int height, final int depth, final int pixelFormat, final int pixelDataType, final Memory pixels, final boolean freePixels) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.pixelFormat = pixelFormat;
        this.pixelDataType = pixelDataType;
        this.pixels = pixels;
        this.freePixels = freePixels;
        try {
            if (width <= 0 || height <= 0 || depth <= 0) {
                throw new IllegalArgumentException("Width, height and depth must be greater than 0");
            }
            this.channels = getChannelsFromPixelFormat(pixelFormat);
            this.bytesPerPixel = getBytesPerPixelFromPixelFormat(pixelFormat, pixelDataType);
            if (pixels.getSize() < (long) width * height * depth * this.bytesPerPixel) {
                throw new IllegalArgumentException("Pixel data is too small for the given width, height, depth and pixel format");
            }
        } catch (Throwable e) {
            this.free();
            throw e;
        }
    }

    protected Image(final Image image) {
        this(image.getWidth(), image.getHeight(), image.getDepth(), image.getPixelFormat(), image.getPixelDataType(), image.getPixels(), image.freePixels);
    }

    public Image withPixelFormat(final int pixelFormat) {
        if (this.channels != getChannelsFromPixelFormat(pixelFormat)) {
            throw new IllegalArgumentException("New pixel format must have the same number of channels as the current pixel format");
        }
        return new Image(this.width, this.height, this.depth, pixelFormat, this.pixelDataType, this.pixels, this.freePixels);
    }

    public Image copy() {
        return new Image(this.width, this.height, this.depth, this.pixelFormat, this.pixelDataType, MemoryAllocator.copyMemory(this.pixels));
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getDepth() {
        return this.depth;
    }

    public int getPixelFormat() {
        return this.pixelFormat;
    }

    public int getPixelDataType() {
        return this.pixelDataType;
    }

    public Memory getPixels() {
        return this.pixels;
    }

    public int getChannels() {
        return this.channels;
    }

    public int getBytesPerPixel() {
        return this.bytesPerPixel;
    }

    public int getRowAlignment() {
        final int rowBytes = this.width * this.bytesPerPixel;
        int alignment = 8;
        while (alignment > 1 && (rowBytes % alignment != 0)) {
            alignment /= 2;
        }
        return alignment;
    }

    @Override
    protected void free0() {
        if (this.freePixels) {
            this.pixels.free();
        }
    }

    private static int getChannelsFromPixelFormat(final int pixelFormat) {
        return switch (pixelFormat) {
            case GL11C.GL_RGBA, GL12C.GL_BGRA -> 4;
            case GL11C.GL_RGB, GL12C.GL_BGR -> 3;
            case GL30C.GL_RG -> 2;
            case GL11C.GL_RED, GL11C.GL_GREEN, GL11C.GL_BLUE, GL11C.GL_ALPHA, GL11C.GL_STENCIL_INDEX -> 1;
            default -> throw new IllegalArgumentException("Unsupported pixel format: " + pixelFormat);
        };
    }

    private static int getBytesPerPixelFromPixelFormat(final int pixelFormat, final int pixelDataType) {
        final int channels = getChannelsFromPixelFormat(pixelFormat);
        return switch (pixelDataType) {
            case GL11C.GL_UNSIGNED_BYTE, GL11C.GL_BYTE -> channels * Byte.BYTES;
            case GL11C.GL_UNSIGNED_SHORT, GL11C.GL_SHORT, GL30C.GL_HALF_FLOAT -> channels * Short.BYTES;
            case GL11C.GL_UNSIGNED_INT, GL11C.GL_INT, GL11C.GL_FLOAT -> channels * Integer.BYTES;
            default -> throw new IllegalArgumentException("Unsupported pixel data type: " + pixelDataType);
        };
    }

}
