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
package net.raphimc.thingl.gl.util.pool;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.resource.sampler.Sampler;

public class SamplerCache {

    private final Object2ObjectMap<CacheKey, Sampler> samplerCache = new Object2ObjectOpenHashMap<>();

    public SamplerCache() {
        ThinGL.get().addFrameFinishedCallback(() -> {
            if (this.samplerCache.size() > 512) {
                ThinGL.LOGGER.warn("Sampler cache has grown to " + this.samplerCache.size() + " entries. Clearing the cache to prevent excessive memory usage.");
                this.samplerCache.values().forEach(Sampler::free);
                this.samplerCache.clear();
            }
        });
    }

    public Sampler getSampler(final int filter, final int wrap) {
        return this.getSampler(filter, filter, wrap);
    }

    public Sampler getSampler(final int minificationFilter, final int magnificationFilter, final int wrap) {
        ThinGL.get().assertOnRenderThread();
        final CacheKey cacheKey = new CacheKey(minificationFilter, magnificationFilter, wrap, wrap, wrap);
        return this.samplerCache.computeIfAbsent(cacheKey, this::createSampler);
    }

    public int getSize() {
        return this.samplerCache.size();
    }

    public void free() {
        this.samplerCache.values().forEach(Sampler::free);
    }

    private Sampler createSampler(final CacheKey cacheKey) {
        final Sampler sampler = new Sampler();
        sampler.setMinificationFilter(cacheKey.minificationFilter);
        sampler.setMagnificationFilter(cacheKey.magnificationFilter);
        sampler.setWrapS(cacheKey.wrapS);
        sampler.setWrapT(cacheKey.wrapT);
        sampler.setWrapR(cacheKey.wrapR);
        return sampler;
    }

    private record CacheKey(int minificationFilter, int magnificationFilter, int wrapS, int wrapT, int wrapR) {
    }

}
