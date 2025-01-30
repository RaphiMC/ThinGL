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

package net.raphimc.thingl.wrapper;

import org.joml.Math;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.NVFramebufferMixedSamples;
import org.lwjgl.system.MathUtil;

public class NvidiaAntiAliasing {

    private static final boolean SUPPORTED = GL.getCapabilities().GL_NV_framebuffer_mixed_samples;
    private static final int MIN_SAMPLES = SUPPORTED ? 2 : 0;
    private static final int MAX_SAMPLES = SUPPORTED ? GL11C.glGetInteger(NVFramebufferMixedSamples.GL_MAX_RASTER_SAMPLES_EXT) : 0;

    public static void begin(final int samples) {
        if (!SUPPORTED) return;
        if (!MathUtil.mathIsPoT(samples)) {
            throw new IllegalArgumentException("The number of samples must be a power of two");
        }

        GLStateTracker.push();
        GLStateTracker.disable(GL11C.GL_DEPTH_TEST);
        GLStateTracker.disable(GL11C.GL_STENCIL_TEST);
        GLStateTracker.enable(NVFramebufferMixedSamples.GL_RASTER_MULTISAMPLE_EXT);
        GLStateTracker.enable(NVFramebufferMixedSamples.GL_COVERAGE_MODULATION_TABLE_NV);
        NVFramebufferMixedSamples.glCoverageModulationNV(GL11C.GL_ALPHA);
        NVFramebufferMixedSamples.glRasterSamplesEXT(Math.clamp(samples, MIN_SAMPLES, MAX_SAMPLES), true);
    }

    public static void end() {
        GLStateTracker.pop();
    }

}
