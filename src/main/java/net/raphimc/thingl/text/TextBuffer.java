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
package net.raphimc.thingl.text;

import net.lenni0451.commons.collections.Lists;
import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.text.font.Font;
import net.raphimc.thingl.text.font.FontSet;
import net.raphimc.thingl.text.shaper.ShapedTextBuffer;
import net.raphimc.thingl.text.shaper.TextShaper;
import net.raphimc.thingl.text.shaper.impl.BasicTextShaper;

import java.util.ArrayList;
import java.util.List;

public record TextBuffer(List<TextRun> runs) {

    public static TextBuffer fromString(final Font font, final String text) {
        return new TextBuffer(TextRun.fromString(font, text));
    }

    public static TextBuffer fromString(final Font font, final String text, final Color color) {
        return new TextBuffer(TextRun.fromString(font, text, color));
    }

    public static TextBuffer fromString(final Font font, final String text, final Color color, final int styleFlags) {
        return new TextBuffer(TextRun.fromString(font, text, color, styleFlags));
    }

    public static TextBuffer fromString(final FontSet fontSet, final String text) {
        return fromString(fontSet, text, Color.WHITE);
    }

    public static TextBuffer fromString(final FontSet fontSet, final String text, final Color color) {
        return fromString(fontSet, text, color, 0);
    }

    public static TextBuffer fromString(final FontSet fontSet, final String text, final Color color, final int styleFlags) {
        Font currentFont = fontSet.getMainFont();
        final StringBuilder currentText = new StringBuilder(text.length());
        final List<TextRun> runs = new ArrayList<>();
        for (int i = 0; i < text.length(); i++) {
            final int codePoint = text.codePointAt(i);
            if (codePoint >= Character.MIN_SUPPLEMENTARY_CODE_POINT) {
                i++;
            }
            Font font = fontSet.getFont(codePoint);
            if (font == null) { // If the character is not supported by the font set, use the main font to display the missing glyph character
                font = fontSet.getMainFont();
            }
            if (font != currentFont) {
                if (!currentText.isEmpty()) {
                    runs.add(new TextRun(currentFont, new TextSegment(currentText.toString(), color, styleFlags)));
                    currentText.setLength(0);
                }
                currentFont = font;
            }
            currentText.appendCodePoint(codePoint);
        }
        if (!currentText.isEmpty()) {
            runs.add(new TextRun(currentFont, new TextSegment(currentText.toString(), color, styleFlags)));
        }
        return new TextBuffer(runs);
    }

    public TextBuffer(final TextRun... run) {
        this(Lists.arrayList(run));
    }

    public TextBuffer addRun(final TextRun run) {
        this.runs.add(run);
        return this;
    }

    public TextBuffer add(final TextRun run) {
        return this.addRun(run);
    }

    public ShapedTextBuffer shape() {
        return BasicTextShaper.INSTANCE.shape(this);
    }

    public ShapedTextBuffer shape(final TextShaper shaper) {
        return shaper.shape(this);
    }

}
