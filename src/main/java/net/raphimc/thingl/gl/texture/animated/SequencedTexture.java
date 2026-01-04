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
package net.raphimc.thingl.gl.texture.animated;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.resource.image.texture.impl.Texture2DArray;
import net.raphimc.thingl.image.animated.AnimatedImage;
import org.lwjgl.opengl.GL11C;

import java.util.NavigableMap;
import java.util.TreeMap;

public class SequencedTexture extends Texture2DArray {

    private final NavigableMap<Integer, Integer> frameTimes = new TreeMap<>();

    public SequencedTexture(final AnimatedImage animatedImage) {
        this(GL11C.GL_RGBA8, animatedImage);
    }

    public SequencedTexture(final int internalFormat, final AnimatedImage animatedImage) {
        super(internalFormat, animatedImage.getWidth(), animatedImage.getHeight(), Math.min(animatedImage.getFrameCount(), ThinGL.capabilities().getMaxArrayTextureLayers()));
        if (animatedImage.getFrameCount() > ThinGL.capabilities().getMaxArrayTextureLayers()) {
            ThinGL.LOGGER.warn("Animated image has more frames (" + animatedImage.getFrameCount() + ") than the maximum supported by the GPU (" + ThinGL.capabilities().getMaxArrayTextureLayers() + "). Only loading the maximum supported frames.");
        }

        try {
            int frameIndex = 0;
            int relativeTime = 0;
            while (frameIndex < this.getDepth()) {
                final int delay = animatedImage.loadNextFrame();
                this.uploadImage(0, 0, frameIndex, animatedImage);
                this.frameTimes.put(relativeTime, frameIndex);
                relativeTime += delay;
                frameIndex++;
            }
            this.frameTimes.put(relativeTime, frameIndex - 1);
        } catch (Throwable e) {
            this.free();
            throw e;
        } finally {
            animatedImage.free();
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
