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
package net.raphimc.thingl.resource.font.instance;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.raphimc.thingl.implementation.Capabilities;
import net.raphimc.thingl.resource.Resource;
import net.raphimc.thingl.resource.font.face.FontFace;
import net.raphimc.thingl.resource.image.Image;
import net.raphimc.thingl.util.ArrayCache;
import net.raphimc.thingl.util.MathUtil;
import org.lwjgl.util.harfbuzz.HarfBuzz;

public abstract class FontInstance extends Resource {

    private final FontFace face;
    private final int size;
    private final ArrayCache<GlyphMetrics> glyphMetricsCache = new ArrayCache<>(Integer.MAX_VALUE, this::loadGlyphMetrics);
    private final Int2ObjectMap<FontInstance> scaledInstances = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>());
    private long harfBuzzInstance = 0L;

    protected FontInstance(final FontFace face, final int size) {
        this.face = face;
        this.size = size;
    }

    public GlyphMetrics getGlyphMetrics(final int glyphIndex) {
        return this.glyphMetricsCache.getOrLoad(glyphIndex);
    }

    public abstract GlyphBitmap createGlyphBitmap(final int glyphIndex, final GlyphBitmap.RenderMode renderMode);

    public FontInstance getScaledInstance(final int size) {
        if (size != this.size) {
            return this.scaledInstances.computeIfAbsent(size, s -> new ScaledFontInstance(this, s));
        } else {
            return this;
        }
    }

    public FontFace getFace() {
        return this.face;
    }

    public int getSize() {
        return this.size;
    }

    public abstract float getAscent();

    public abstract float getDescent();

    public abstract float getHeight();

    public abstract float getUnderlinePosition();

    public abstract float getUnderlineThickness();

    public abstract float getStrikethroughPosition();

    public abstract float getStrikethroughThickness();

    public long getHarfBuzzInstance() {
        if (this.harfBuzzInstance == 0L) {
            Capabilities.assertHarfBuzzAvailable();
            this.harfBuzzInstance = HarfBuzz.hb_font_create(this.face.getHarfBuzzInstance());
            HarfBuzz.hb_font_set_scale(this.harfBuzzInstance, this.size * (int) MathUtil.FIXED_26_6, this.size * (int) MathUtil.FIXED_26_6);
        }
        return this.harfBuzzInstance;
    }

    protected abstract GlyphMetrics loadGlyphMetrics(final int glyphIndex);

    @Override
    protected void free0() {
        this.scaledInstances.values().forEach(FontInstance::free);
        this.scaledInstances.clear();
        if (this.harfBuzzInstance != 0L) {
            HarfBuzz.hb_font_destroy(this.harfBuzzInstance);
        }
    }

    public record GlyphMetrics(float width, float height, float xAdvance, float bearingX, float bearingY) {
    }

    public record GlyphBitmap(Image image, int xOffset, int yOffset) {

        public enum RenderMode {

            PIXELATED,
            COLORED_PIXELATED,
            ANTIALIASED,
            COLORED_ANTIALIASED,
            BSDF,
            SDF,
            MSDF,

        }

    }

}
