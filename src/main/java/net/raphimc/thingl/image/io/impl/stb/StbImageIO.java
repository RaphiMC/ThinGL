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
package net.raphimc.thingl.image.io.impl.stb;

import net.raphimc.thingl.image.io.ByteImage2DReader;
import net.raphimc.thingl.image.io.ByteImage2DWriter;
import net.raphimc.thingl.implementation.Capabilities;
import net.raphimc.thingl.memory.allocator.MemoryAllocator;
import net.raphimc.thingl.resource.image.impl.ByteImage2D;
import net.raphimc.thingl.resource.memory.Memory;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageWrite;

@SuppressWarnings("removal")
@Deprecated(forRemoval = true)
public class StbImageIO implements ByteImage2DReader, ByteImage2DWriter {

    static {
        Capabilities.assertStbAvailable();
    }

    public static final StbImageIO INSTANCE = new StbImageIO();

    @Override
    public ByteImage2D readByteImage2D(final Memory imageData, final boolean forceColor, final boolean freeImageData) {
        try {
            final int[] width = new int[1];
            final int[] height = new int[1];
            final int[] channels = new int[1];
            final Memory pixels = MemoryAllocator.wrapMemory(STBImage.stbi_load_from_memory(imageData.asByteBuffer(), width, height, channels, 0));
            if (pixels == null) {
                throw new IllegalArgumentException("Failed to read image: " + STBImage.stbi_failure_reason());
            }
            try {
                final int pixelFormat = switch (channels[0]) {
                    case 1 -> GL11C.GL_RED;
                    case 2 -> GL30C.GL_RG;
                    case 3 -> GL11C.GL_RGB;
                    case 4 -> GL11C.GL_RGBA;
                    default -> throw new IllegalStateException("Unexpected channel count: " + channels[0]);
                };
                ByteImage2D image = new ByteImage2D(width[0], height[0], pixelFormat, MemoryAllocator.copyMemory(pixels));
                if (forceColor && image.getChannels() <= 2) {
                    image = image.convertGrayscaleToColor();
                }
                return image;
            } finally {
                STBImage.nstbi_image_free(pixels.getAddress());
            }
        } finally {
            if (freeImageData) {
                imageData.free();
            }
        }
    }

    @Override
    public Memory writeByteImage2DToMemory(final ByteImage2D image) {
        final int channels = switch (image.getPixelFormat()) {
            case GL11C.GL_RED, GL11C.GL_GREEN, GL11C.GL_BLUE, GL11C.GL_ALPHA -> 1;
            case GL30C.GL_RG -> 2;
            case GL11C.GL_RGB -> 3;
            case GL11C.GL_RGBA -> 4;
            default -> throw new IllegalArgumentException("Unsupported pixel format: " + image.getPixelFormat());
        };
        final BufferedStbWriteCallback writeCallback = new BufferedStbWriteCallback();
        try {
            if (!STBImageWrite.stbi_write_png_to_func(writeCallback, 0L, image.getWidth(), image.getHeight(), channels, image.getPixels().asByteBuffer(), 0)) {
                throw new RuntimeException("Failed to write image: " + STBImage.stbi_failure_reason());
            }
            return writeCallback.getMemory();
        } finally {
            writeCallback.free();
        }
    }

}
