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
package net.raphimc.thingl.implementation;

import net.raphimc.thingl.ThinGL;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.NVFramebufferMixedSamples;

public class Capabilities {

    private final int maxSamples;
    private final boolean supportsNVFramebufferMixedSamples;
    private final int nvFramebufferMixedSamplesMaxRasterSamples;

    @ApiStatus.Internal
    public Capabilities(final ThinGL thinGL) {
        this.maxSamples = GL11C.glGetInteger(GL30C.GL_MAX_SAMPLES);
        this.supportsNVFramebufferMixedSamples = GL.getCapabilities().GL_NV_framebuffer_mixed_samples;
        if (this.supportsNVFramebufferMixedSamples) {
            this.nvFramebufferMixedSamplesMaxRasterSamples = GL11C.glGetInteger(NVFramebufferMixedSamples.GL_MAX_RASTER_SAMPLES_EXT);
        } else {
            this.nvFramebufferMixedSamplesMaxRasterSamples = 0;
        }
    }

    public int getMaxSamples() {
        return this.maxSamples;
    }

    public boolean supportsNVFramebufferMixedSamples() {
        return this.supportsNVFramebufferMixedSamples;
    }

    public int getNVFramebufferMixedSamplesMaxRasterSamples() {
        return this.nvFramebufferMixedSamplesMaxRasterSamples;
    }

}
