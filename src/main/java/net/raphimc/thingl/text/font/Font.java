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
package net.raphimc.thingl.text.font;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.raphimc.thingl.ThinGL;
import org.lwjgl.util.harfbuzz.HarfBuzz;

import java.nio.ByteBuffer;

public abstract class Font {

    private final int size;
    private final Int2ObjectMap<Glyph> indexToGlyph = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Glyph> codePointToGlyph = new Int2ObjectOpenHashMap<>();
    private final Glyph[] asciiCodePointToGlyph = new Glyph[256];
    private long harfBuzzInstance = 0L;

    protected Font(final int size) {
        this.size = size;
    }

    public Glyph getGlyphByIndex(final int glyphIndex) {
        return this.indexToGlyph.computeIfAbsent(glyphIndex, this::loadGlyphByIndex);
    }

    public Glyph getGlyphByCodePoint(final int codePoint) {
        if (codePoint >= 0 && codePoint < this.asciiCodePointToGlyph.length) {
            final Glyph glyph = this.asciiCodePointToGlyph[codePoint];
            if (glyph != null) {
                return glyph;
            } else {
                return this.asciiCodePointToGlyph[codePoint] = this.loadGlyphByCodePoint(codePoint);
            }
        } else {
            return this.codePointToGlyph.computeIfAbsent(codePoint, this::loadGlyphByCodePoint);
        }
    }

    public abstract GlyphBitmap createGlyphBitmap(final Glyph glyph, final GlyphBitmap.RenderMode renderMode);

    public void free() {
        if (this.harfBuzzInstance != 0L) {
            HarfBuzz.hb_font_destroy(this.harfBuzzInstance);
        }
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

    public abstract String getPostScriptName();

    public abstract String getFamilyName();

    public abstract String getSubFamilyName();

    public long getHarfBuzzInstance() {
        if (this.harfBuzzInstance == 0L) {
            ThinGL.capabilities().ensureHarfBuzzPresent();
            this.harfBuzzInstance = this.createHarfBuzzInstance();
        }
        return this.harfBuzzInstance;
    }

    protected abstract Glyph loadGlyphByIndex(final int glyphIndex);

    protected abstract Glyph loadGlyphByCodePoint(final int codePoint);

    protected abstract long createHarfBuzzInstance();

    public record Glyph(Font font, int glyphIndex, float width, float height, float xAdvance, float bearingX, float bearingY) {
    }

    public record GlyphBitmap(int width, int height, float xOffset, float yOffset, int pixelFormat, ByteBuffer pixelBuffer) {

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
