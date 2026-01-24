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

import net.raphimc.thingl.implementation.Capabilities;
import net.raphimc.thingl.memory.allocator.MemoryAllocator;
import net.raphimc.thingl.resource.memory.Memory;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.stb.STBImage;

public class StbByteImage2D extends ByteImage2D {

    static {
        Capabilities.assertStbAvailable();
    }

    public StbByteImage2D(final byte[] imageBytes) {
        this(imageBytes, true);
    }

    public StbByteImage2D(final byte[] imageBytes, final boolean forceColor) {
        this(MemoryAllocator.allocateMemory(imageBytes), forceColor);
    }

    public StbByteImage2D(final Memory imageData) {
        this(imageData, true);
    }

    public StbByteImage2D(final Memory imageData, final boolean forceColor) {
        this(imageData, forceColor, true);
    }

    public StbByteImage2D(final Memory imageData, final boolean forceColor, final boolean freeImageData) {
        final int superWidth;
        final int superHeight;
        final int superPixelFormat;
        final Memory superPixels;
        try {
            int desiredChannels = STBImage.STBI_default;
            if (forceColor) {
                final int[] channels = new int[1];
                if (!STBImage.stbi_info_from_memory(imageData.asByteBuffer(), new int[1], new int[1], channels)) {
                    throw new IllegalArgumentException("Failed to read image: " + STBImage.stbi_failure_reason());
                }
                if (channels[0] == STBImage.STBI_grey) {
                    desiredChannels = STBImage.STBI_rgb;
                } else if (channels[0] == STBImage.STBI_grey_alpha) {
                    desiredChannels = STBImage.STBI_rgb_alpha;
                }
            }

            final int[] width = new int[1];
            final int[] height = new int[1];
            final int[] channels = new int[1];
            final Memory pixels = MemoryAllocator.wrapMemory(STBImage.stbi_load_from_memory(imageData.asByteBuffer(), width, height, channels, desiredChannels));
            if (pixels == null) {
                throw new IllegalArgumentException("Failed to read image: " + STBImage.stbi_failure_reason());
            }
            try {
                if (desiredChannels != STBImage.STBI_default) {
                    channels[0] = desiredChannels;
                }
                superWidth = width[0];
                superHeight = height[0];
                superPixelFormat = switch (channels[0]) {
                    case STBImage.STBI_grey -> GL11C.GL_RED;
                    case STBImage.STBI_grey_alpha -> GL30C.GL_RG;
                    case STBImage.STBI_rgb -> GL11C.GL_RGB;
                    case STBImage.STBI_rgb_alpha -> GL11C.GL_RGBA;
                    default -> throw new IllegalStateException("Unexpected channel count: " + channels[0]);
                };
                superPixels = MemoryAllocator.copyMemory(pixels);
            } finally {
                STBImage.nstbi_image_free(pixels.getAddress());
            }
        } finally {
            if (freeImageData) {
                imageData.free();
            }
        }
        super(superWidth, superHeight, superPixelFormat, superPixels);
    }

}
