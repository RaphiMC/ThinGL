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
package net.raphimc.thingl.text.font;

import it.unimi.dsi.fastutil.Pair;
import net.raphimc.thingl.resource.font.Font;

import java.util.ArrayList;
import java.util.List;

public class FontSet {

    private final Font mainFont;
    private final List<Pair<Font, GlyphPredicate>> fonts = new ArrayList<>();

    public FontSet(final Font mainFont) {
        this(mainFont, GlyphPredicate.all());
    }

    public FontSet(final Font mainFont, final GlyphPredicate predicate) {
        this.mainFont = mainFont;
        this.addFont(mainFont, predicate);
    }

    public FontSet(final List<Font> fonts) {
        if (fonts.isEmpty()) {
            throw new IllegalArgumentException("Font list must contain at least one font");
        }
        this.mainFont = fonts.getFirst();
        this.addFonts(fonts);
    }

    public FontSet addFont(final Font font) {
        return this.addFont(font, GlyphPredicate.all());
    }

    public FontSet addFont(final Font font, final GlyphPredicate predicate) {
        this.fonts.add(Pair.of(font, predicate));
        return this;
    }

    public FontSet addFonts(final Iterable<Font> fonts) {
        for (Font font : fonts) {
            this.addFont(font);
        }
        return this;
    }

    public Font getMainFont() {
        return this.mainFont;
    }

    public Font getFont(final int codePoint) {
        for (Pair<Font, GlyphPredicate> pair : this.fonts) {
            final Font font = pair.left();
            final GlyphPredicate predicate = pair.right();
            if (predicate.test(codePoint) && font.getGlyphByCodePoint(codePoint).glyphIndex() != 0) {
                return font;
            }
        }
        return null;
    }

    public void free() {
        for (Pair<Font, GlyphPredicate> pair : this.fonts) {
            pair.left().free();
        }
    }

}
