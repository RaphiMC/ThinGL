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
package net.raphimc.thingl.texture.animated;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.resource.image.texture.Texture2D;
import net.raphimc.thingl.resource.image.texture.Texture2DArray;
import net.raphimc.thingl.texture.animated.frameprovider.FrameProvider;
import org.lwjgl.opengl.GL11C;

import java.io.IOException;
import java.util.NavigableMap;
import java.util.TreeMap;

public class SequencedTexture extends Texture2DArray {

    private final NavigableMap<Integer, Integer> frameTimes = new TreeMap<>();

    public SequencedTexture(final FrameProvider frameProvider) throws IOException {
        this(GL11C.GL_RGBA8, frameProvider);
    }

    public SequencedTexture(final int internalFormat, final FrameProvider frameProvider) throws IOException {
        super(internalFormat, frameProvider.getWidth(), frameProvider.getHeight(), Math.min(frameProvider.getFrameCount(), ThinGL.capabilities().getMaxArrayTextureLayers()));
        if (frameProvider.getFrameCount() > ThinGL.capabilities().getMaxArrayTextureLayers()) {
            ThinGL.LOGGER.warn("Frame provider has more frames (" + frameProvider.getFrameCount() + ") than the maximum supported by the GPU (" + ThinGL.capabilities().getMaxArrayTextureLayers() + "). Only loading the maximum supported frames.");
        }

        final Texture2D target = new Texture2D(internalFormat, this.getWidth(), this.getHeight());
        try {
            int frameIndex = 0;
            int relativeTime = 0;
            while (frameIndex < this.getDepth()) {
                final int delay = frameProvider.loadNextFrame(target);
                target.copyTo(this, 0, 0, 0, 0, frameIndex, this.getWidth(), this.getHeight());
                this.frameTimes.put(relativeTime, frameIndex);
                relativeTime += delay;
                frameIndex++;
            }
            this.frameTimes.put(relativeTime, frameIndex - 1);
        } catch (Throwable e) {
            this.free();
            throw e;
        } finally {
            target.free();
            frameProvider.free();
        }
    }

    public int getFrameIndex(final int time) {
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
