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
package net.raphimc.thingl.resource.font.face.impl;

import net.raphimc.thingl.resource.font.face.FontFace;
import net.raphimc.thingl.resource.font.instance.FontInstance;
import net.raphimc.thingl.resource.font.instance.impl.AwtFontInstance;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;

public class AwtFontFace extends FontFace {

    private static final FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(null, true, true);

    private final Font font;
    private final String postScriptName;
    private final String familyName;
    private final int glyphCount;

    public AwtFontFace(final Font font) {
        this.font = font;
        this.postScriptName = font.getPSName();
        this.familyName = font.getFamily();
        this.glyphCount = font.getNumGlyphs();
    }

    @Override
    public AwtFontInstance getInstance(final int size) {
        return (AwtFontInstance) super.getInstance(size);
    }

    public Font getFont() {
        return this.font;
    }

    @Override
    public String getPostScriptName() {
        return this.postScriptName;
    }

    @Override
    public String getFamilyName() {
        return this.familyName;
    }

    @Override
    public String getSubFamilyName() {
        return null;
    }

    @Override
    public int getGlyphCount() {
        return this.glyphCount;
    }

    @Override
    protected int loadGlyphIndex(final int codePoint) {
        final GlyphVector glyphVector = this.font.createGlyphVector(FONT_RENDER_CONTEXT, Character.toChars(codePoint));
        if (glyphVector.getNumGlyphs() != 1) {
            throw new IllegalStateException("Glyph vector for code point " + codePoint + " does not map to exactly one glyph");
        }
        return glyphVector.getGlyphCode(0);
    }

    @Override
    protected FontInstance createInstance(final int size) {
        return new AwtFontInstance(this, size);
    }

    @Override
    protected long createHarfBuzzInstance() {
        throw new UnsupportedOperationException();
    }

}
