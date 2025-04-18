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

    private final boolean isFreeTypePresent;
    private final boolean isHarfBuzzPresent;
    private final boolean isMeshOptimizerPresent;
    private final boolean isParPresent;
    private final boolean isEarcut4jPresent;
    private final int maxSamples;
    private final boolean supportsNVFramebufferMixedSamples;
    private final int nvFramebufferMixedSamplesMaxRasterSamples;

    @ApiStatus.Internal
    public Capabilities(final ThinGL thinGL) {
        this.isFreeTypePresent = isClassPresent("org.lwjgl.util.freetype.FreeType");
        this.isHarfBuzzPresent = isClassPresent("org.lwjgl.util.harfbuzz.HarfBuzz");
        this.isMeshOptimizerPresent = isClassPresent("org.lwjgl.util.meshoptimizer.LibMeshOptimizer");
        this.isParPresent = isClassPresent("org.lwjgl.util.par.LibPar");
        this.isEarcut4jPresent = isClassPresent("earcut4j.Earcut");
        this.maxSamples = GL11C.glGetInteger(GL30C.GL_MAX_SAMPLES);
        this.supportsNVFramebufferMixedSamples = GL.getCapabilities().GL_NV_framebuffer_mixed_samples;
        if (this.supportsNVFramebufferMixedSamples) {
            this.nvFramebufferMixedSamplesMaxRasterSamples = GL11C.glGetInteger(NVFramebufferMixedSamples.GL_MAX_RASTER_SAMPLES_EXT);
        } else {
            this.nvFramebufferMixedSamplesMaxRasterSamples = 0;
        }
    }

    public void ensureFreeTypePresent() {
        if (!this.isFreeTypePresent) {
            throw new UnsupportedOperationException("FreeType is not present. Please add the LWJGL FreeType module to your project.");
        }
    }

    public void ensureHarfBuzzPresent() {
        if (!this.isHarfBuzzPresent) {
            throw new UnsupportedOperationException("HarfBuzz is not present. Please add the LWJGL HarfBuzz module to your project.");
        }
    }

    public void ensureMeshOptimizerPresent() {
        if (!this.isMeshOptimizerPresent) {
            throw new UnsupportedOperationException("MeshOptimizer is not present. Please add the LWJGL MeshOptimizer module to your project.");
        }
    }

    public void ensureParPresent() {
        if (!this.isParPresent) {
            throw new UnsupportedOperationException("Par is not present. Please add the LWJGL Par module to your project.");
        }
    }

    public void ensureEarcut4jPresent() {
        if (!this.isEarcut4jPresent) {
            throw new UnsupportedOperationException("Earcut4j is not present. Please add the Earcut4j library to your project.");
        }
    }

    public boolean isFreeTypePresent() {
        return this.isFreeTypePresent;
    }

    public boolean isHarfBuzzPresent() {
        return this.isHarfBuzzPresent;
    }

    public boolean isMeshOptimizerPresent() {
        return this.isMeshOptimizerPresent;
    }

    public boolean isParPresent() {
        return this.isParPresent;
    }

    public boolean isEarcut4jPresent() {
        return this.isEarcut4jPresent;
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

    private static boolean isClassPresent(final String className) {
        try {
            Class.forName(className, false, Capabilities.class.getClassLoader());
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

}
