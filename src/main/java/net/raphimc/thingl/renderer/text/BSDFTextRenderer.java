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

package net.raphimc.thingl.renderer.text;

import net.raphimc.thingl.util.font.Font;
import net.raphimc.thingl.util.font.FontGlyph;
import net.raphimc.thingl.util.font.GlyphBitmap;

public class BSDFTextRenderer extends SDFTextRenderer {

    public BSDFTextRenderer(final Font... fonts) {
        super(fonts);
    }

    @Override
    protected GlyphBitmap createGlyphBitmap(final FontGlyph fontGlyph) {
        return fontGlyph.font().loadGlyphBitmap(fontGlyph.glyphIndex(), true, true);
    }

}
