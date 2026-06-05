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
package net.raphimc.thingl.resource.font.face;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.raphimc.thingl.implementation.Capabilities;
import net.raphimc.thingl.resource.Resource;
import net.raphimc.thingl.resource.font.instance.FontInstance;
import net.raphimc.thingl.util.ArrayCache;
import org.lwjgl.util.harfbuzz.HarfBuzz;

public abstract class FontFace extends Resource {

    private final ArrayCache<Integer> glyphIndexCache = new ArrayCache<>(Character.MAX_CODE_POINT + 1, this::loadGlyphIndex);
    private final Int2ObjectMap<FontInstance> instances = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>());
    private long harfBuzzInstance = 0L;

    public int getGlyphIndex(final int codePoint) {
        return this.glyphIndexCache.getOrLoad(codePoint);
    }

    public FontInstance getInstance(final int size) {
        return this.instances.computeIfAbsent(size, this::createInstance);
    }

    public abstract String getPostScriptName();

    public abstract String getFamilyName();

    public abstract String getSubFamilyName();

    public long getHarfBuzzInstance() {
        if (this.harfBuzzInstance == 0L) {
            Capabilities.assertHarfBuzzAvailable();
            this.harfBuzzInstance = this.createHarfBuzzInstance();
        }
        return this.harfBuzzInstance;
    }

    protected abstract int loadGlyphIndex(final int codePoint);

    protected abstract FontInstance createInstance(final int size);

    protected abstract long createHarfBuzzInstance();

    @Override
    protected void free0() {
        this.instances.values().forEach(FontInstance::free);
        this.instances.clear();
        if (this.harfBuzzInstance != 0L) {
            HarfBuzz.hb_face_destroy(this.harfBuzzInstance);
        }
    }

}
