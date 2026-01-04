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
package net.raphimc.thingl.image.animated.impl;

import com.ibasco.image.gif.GifFrame;
import com.ibasco.image.gif.GifImageReader;
import net.raphimc.thingl.image.animated.AnimatedImage;
import net.raphimc.thingl.implementation.Capabilities;
import net.raphimc.thingl.resource.memory.Memory;
import org.lwjgl.opengl.GL12C;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public class GifImage extends AnimatedImage {

    static {
        Capabilities.assertGifReaderAvailable();
    }

    private final GifImageReader gifReader;

    public GifImage(final byte[] imageBytes) throws IOException {
        this(new ByteArrayInputStream(imageBytes));
    }

    public GifImage(final InputStream imageStream) throws IOException {
        this(new GifImageReader(imageStream, true));
    }

    private GifImage(final GifImageReader gifReader) {
        super(gifReader.getMetadata().getWidth(), gifReader.getMetadata().getHeight(), gifReader.getTotalFrames(), GL12C.GL_BGRA);
        this.gifReader = gifReader;
    }

    @Override
    public int loadNextFrame() {
        try {
            final GifFrame frame = this.gifReader.read();
            final int[] frameData = frame.getData();
            final Memory pixels = this.getPixels();
            if ((long) frameData.length * Integer.BYTES != pixels.getSize()) {
                throw new IllegalStateException("Frame pixel data size does not match image size");
            }
            for (int i = 0; i < frameData.length; i++) {
                pixels.putInt((long) i * Integer.BYTES, frameData[i]);
            }
            return frame.getDelay() * 10;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean hasMoreFrames() {
        return this.gifReader.hasRemaining();
    }

    @Override
    protected void free0() {
        try {
            this.gifReader.close();
        } catch (IOException ignored) {
        }
        super.free0();
    }

}
