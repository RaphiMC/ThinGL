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
package net.raphimc.thingl.texture;

import com.ibasco.image.gif.GifFrame;
import com.ibasco.image.gif.GifImageReader;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.resource.texture.AbstractTexture;
import net.raphimc.thingl.resource.texture.Texture2DArray;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.NavigableMap;
import java.util.TreeMap;

public class SequencedTexture extends Texture2DArray {

    private final NavigableMap<Integer, Integer> frameTimes = new TreeMap<>();

    public SequencedTexture(final InternalFormat internalFormat, final int width, final int height, final int frameCount) {
        super(internalFormat, width, height, frameCount);
        this.frameTimes.put(0, 0);
    }

    public static SequencedTexture fromGif(final byte[] imageData) throws IOException {
        return fromGif(new ByteArrayInputStream(imageData));
    }

    public static SequencedTexture fromGif(final InputStream imageDataStream) throws IOException {
        ThinGL.capabilities().ensureGifReaderPresent();
        try (GifImageReader gifReader = new GifImageReader(imageDataStream, true)) {
            int frameCount = gifReader.getTotalFrames();
            if (frameCount > ThinGL.capabilities().getMaxArrayTextureLayers()) {
                ThinGL.LOGGER.warn("GIF has more frames (" + frameCount + ") than the maximum supported by the GPU (" + ThinGL.capabilities().getMaxArrayTextureLayers() + "). Using the maximum supported frames.");
                frameCount = ThinGL.capabilities().getMaxArrayTextureLayers();
            }
            final SequencedTexture sequencedTexture = new SequencedTexture(AbstractTexture.InternalFormat.RGBA8, gifReader.getMetadata().getWidth(), gifReader.getMetadata().getHeight(), frameCount);
            int relativeTime = 0;
            while (gifReader.hasRemaining()) {
                final GifFrame frame = gifReader.read();
                final int frameIndex = frame.getIndex();
                if (frameIndex >= frameCount) {
                    break;
                }
                sequencedTexture.uploadPixels(0, 0, frameIndex, frame.getWidth(), frame.getHeight(), AbstractTexture.PixelFormat.BGRA, frame.getData(), false);
                sequencedTexture.getFrameTimes().put(relativeTime, frameIndex);
                relativeTime += frame.getDelay() * 10;
            }
            sequencedTexture.getFrameTimes().put(relativeTime, frameCount - 1);
            return sequencedTexture;
        }
    }

    public int getFrame(final int time) {
        if (this.getDuration() != 0) {
            return this.frameTimes.floorEntry(time % this.getDuration()).getValue();
        } else {
            return 0;
        }
    }

    public int getDuration() {
        return this.frameTimes.lastKey();
    }

    public NavigableMap<Integer, Integer> getFrameTimes() {
        return this.frameTimes;
    }

}
