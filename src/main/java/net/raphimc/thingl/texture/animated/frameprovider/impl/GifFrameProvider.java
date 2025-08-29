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
package net.raphimc.thingl.texture.animated.frameprovider.impl;

import com.ibasco.image.gif.GifFrame;
import com.ibasco.image.gif.GifImageReader;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.resource.image.texture.Texture2D;
import net.raphimc.thingl.texture.animated.frameprovider.FrameProvider;
import org.lwjgl.opengl.GL12C;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public class GifFrameProvider implements FrameProvider {

    private final ThinGL thinGL;
    private final GifImageReader gifReader;

    public GifFrameProvider(final byte[] imageBytes) throws IOException {
        this(new ByteArrayInputStream(imageBytes));
    }

    public GifFrameProvider(final InputStream imageStream) throws IOException {
        ThinGL.capabilities().ensureGifReaderPresent();
        this.thinGL = ThinGL.get();
        this.gifReader = new GifImageReader(imageStream, true);
    }

    @Override
    public int loadNextFrame(final Texture2D target) throws IOException {
        if (this.gifReader.hasRemaining()) {
            final GifFrame frame = this.gifReader.read();

            if (!this.thinGL.isAllocated()) { // If ThinGL was freed while the image was loading
                return -1;
            }

            final CompletableFuture<Void> uploadFuture = new CompletableFuture<>();
            this.thinGL.runOnRenderThread(() -> {
                try {
                    target.uploadPixels(0, 0, frame.getWidth(), frame.getHeight(), GL12C.GL_BGRA, frame.getData(), false);
                } finally {
                    uploadFuture.complete(null);
                }
            });
            uploadFuture.join();

            return frame.getDelay() * 10;
        } else {
            return -1;
        }
    }

    @Override
    public int getWidth() {
        return this.gifReader.getMetadata().getWidth();
    }

    @Override
    public int getHeight() {
        return this.gifReader.getMetadata().getHeight();
    }

    @Override
    public int getFrameCount() {
        return this.gifReader.getTotalFrames();
    }

    @Override
    public void free() {
        try {
            this.gifReader.close();
        } catch (IOException ignored) {
        }
    }

}
