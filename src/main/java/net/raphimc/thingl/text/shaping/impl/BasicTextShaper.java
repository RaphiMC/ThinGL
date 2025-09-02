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
package net.raphimc.thingl.text.shaping.impl;

import net.raphimc.thingl.text.TextRun;
import net.raphimc.thingl.text.TextSegment;
import net.raphimc.thingl.text.font.Font;
import net.raphimc.thingl.text.shaping.ShapedTextRun;
import net.raphimc.thingl.text.shaping.ShapedTextSegment;
import net.raphimc.thingl.text.shaping.TextShaper;

import java.util.ArrayList;
import java.util.List;

public class BasicTextShaper extends TextShaper {

    public static final BasicTextShaper INSTANCE = new BasicTextShaper();

    @Override
    public ShapedTextRun shape(final TextRun textRun) {
        float x = 0F;
        final List<ShapedTextSegment> shapedTextSegments = new ArrayList<>(textRun.segments().size());
        for (TextSegment textSegment : textRun.segments()) {
            final String text = textSegment.text();
            final List<Glyph> glyphs = new ArrayList<>(text.length());
            for (int i = 0; i < text.length(); i++) {
                final int codePoint = text.codePointAt(i);
                if (codePoint >= Character.MIN_SUPPLEMENTARY_CODE_POINT) {
                    i++;
                }
                final Font.Glyph fontGlyph = textRun.font().getGlyphByCodePoint(codePoint);
                glyphs.add(new Glyph(fontGlyph, x, 0F));
                x += fontGlyph.xAdvance();
            }
            shapedTextSegments.add(new ShapedTextSegment(glyphs, textSegment.color(), textSegment.styleFlags(), textSegment.outlineColor(), textSegment.visualOffset()));
        }
        return new ShapedTextRun(textRun.font(), shapedTextSegments);
    }

}
