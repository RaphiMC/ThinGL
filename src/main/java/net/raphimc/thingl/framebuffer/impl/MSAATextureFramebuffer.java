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

package net.raphimc.thingl.framebuffer.impl;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.framebuffer.ResizingFramebuffer;
import net.raphimc.thingl.resource.texture.AbstractTexture;
import net.raphimc.thingl.resource.texture.MultisampleTexture2D;
import org.joml.Math;
import org.lwjgl.system.MathUtil;

public class MSAATextureFramebuffer extends ResizingFramebuffer {

    private final int samples;

    public MSAATextureFramebuffer(final int samples) {
        super((width, height) -> {
            return new MultisampleTexture2D(AbstractTexture.InternalFormat.RGBA8, width, height, returnSamples(samples));
        }, (width, height) -> {
            return new MultisampleTexture2D(AbstractTexture.InternalFormat.DEPTH32_STENCIL8, width, height, returnSamples(samples));
        });
        this.samples = returnSamples(samples);
        this.init();
    }

    public int getSamples() {
        return this.samples;
    }

    private static int returnSamples(final int samples) {
        if (!MathUtil.mathIsPoT(samples)) {
            throw new IllegalArgumentException("The number of samples must be a power of two");
        }
        return Math.clamp(samples, 2, ThinGL.capabilities().getMaxSamples());
    }

}
