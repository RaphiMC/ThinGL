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
package net.raphimc.thingl.image.animated;

import net.raphimc.thingl.resource.image.impl.ByteImage2D;
import net.raphimc.thingl.resource.memory.Memory;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;

public abstract class AnimatedImage extends ByteImage2D {

    private final int frameCount;

    protected AnimatedImage(final int width, final int height, final int frameCount, final int pixelFormat) {
        super(width, height, pixelFormat);
        this.frameCount = frameCount;
    }

    public abstract int loadNextFrame();

    public abstract boolean hasMoreFrames();

    public int getFrameCount() {
        return this.frameCount;
    }

    protected void drawImage(final ByteImage2D image, final int x, final int y, final TransparencyMode transparencyMode) {
        if (image.getPixelFormat() != this.getPixelFormat()) {
            throw new IllegalArgumentException("Image pixel format does not match target image pixel format");
        }
        if (this.getPixelFormat() != GL11C.GL_RGBA && this.getPixelFormat() != GL12C.GL_BGRA) {
            throw new IllegalStateException("Image is not RGBA or BGRA");
        }
        if (image.getWidth() + x > this.getWidth() || image.getHeight() + y > this.getHeight()) {
            throw new IllegalArgumentException("Image does not fit into the target image at the given position");
        }

        final int srcWidth = image.getWidth();
        final int srcHeight = image.getHeight();
        final Memory srcPixels = image.getPixels();
        final int dstWidth = this.getWidth();
        final Memory dstPixels = this.getPixels();
        switch (transparencyMode) {
            case OPAQUE -> {
                for (int yOffset = 0; yOffset < srcHeight; yOffset++) {
                    final long srcIndex = (long) yOffset * srcWidth * 4;
                    final long dstIndex = ((long) (y + yOffset) * dstWidth + x) * 4;
                    srcPixels.copyTo(dstPixels, srcIndex, dstIndex, (long) srcWidth * 4);
                }
            }
            case MASKED -> {
                for (int yOffset = 0; yOffset < srcHeight; yOffset++) {
                    for (int xOffset = 0; xOffset < srcWidth; xOffset++) {
                        final int srcIndex = (yOffset * srcWidth + xOffset) * 4;
                        final int alpha = srcPixels.getByte(srcIndex + 3) & 0xFF;
                        if (alpha >= 127) {
                            final int dstIndex = ((y + yOffset) * dstWidth + (x + xOffset)) * 4;
                            dstPixels.putInt(dstIndex, srcPixels.getInt(srcIndex));
                            dstPixels.putByte(dstIndex + 3, (byte) 255);
                        }
                    }
                }
            }
            case ALPHA_BLENDED -> {
                for (int yOffset = 0; yOffset < srcHeight; yOffset++) {
                    for (int xOffset = 0; xOffset < srcWidth; xOffset++) {
                        final int srcIndex = (yOffset * srcWidth + xOffset) * 4;
                        final int alpha = srcPixels.getByte(srcIndex + 3) & 0xFF;
                        if (alpha > 0) {
                            final int dstIndex = ((y + yOffset) * dstWidth + (x + xOffset)) * 4;
                            if (alpha == 255) {
                                dstPixels.putInt(dstIndex, srcPixels.getInt(srcIndex));
                            } else {
                                final int inverseAlpha = 255 - alpha;
                                final int srcC1 = srcPixels.getByte(srcIndex) & 0xFF;
                                final int srcC2 = srcPixels.getByte(srcIndex + 1) & 0xFF;
                                final int srcC3 = srcPixels.getByte(srcIndex + 2) & 0xFF;
                                final int dstC1 = dstPixels.getByte(dstIndex) & 0xFF;
                                final int dstC2 = dstPixels.getByte(dstIndex + 1) & 0xFF;
                                final int dstC3 = dstPixels.getByte(dstIndex + 2) & 0xFF;
                                final int outC1 = (srcC1 * alpha + dstC1 * inverseAlpha) / 255;
                                final int outC2 = (srcC2 * alpha + dstC2 * inverseAlpha) / 255;
                                final int outC3 = (srcC3 * alpha + dstC3 * inverseAlpha) / 255;
                                dstPixels.putByte(dstIndex, (byte) outC1);
                                dstPixels.putByte(dstIndex + 1, (byte) outC2);
                                dstPixels.putByte(dstIndex + 2, (byte) outC3);
                            }
                            dstPixels.putByte(dstIndex + 3, (byte) 255);
                        }
                    }
                }
            }
        }
    }

    public enum TransparencyMode {

        OPAQUE,
        MASKED,
        ALPHA_BLENDED,

    }

}
