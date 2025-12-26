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
import net.raphimc.thingl.resource.font.Font;
import net.raphimc.thingl.text.shaping.ShapedTextRun;
import net.raphimc.thingl.text.shaping.TextShaper;
import net.raphimc.thingl.text.shaping.impl.BasicTextShaper;

import java.util.List;

public record TextRun(Font font, List<TextSegment> segments) {

    public static TextRun fromString(final Font font, final String text) {
        return new TextRun(font, new TextSegment(text));
    }

    public static TextRun fromString(final Font font, final String text, final Color color) {
        return new TextRun(font, new TextSegment(text, color));
    }

    public static TextRun fromString(final Font font, final String text, final Color color, final int styleFlags) {
        return new TextRun(font, new TextSegment(text, color, styleFlags));
    }

    public static TextRun fromString(final Font font, final String text, final Color color, final int styleFlags, final Color outlineColor) {
        return new TextRun(font, new TextSegment(text, color, styleFlags, outlineColor));
    }

    public static TextRun fromString(final Font font, final String text, final TextStyle style) {
        return new TextRun(font, new TextSegment(text, style));
    }

    public TextRun(final Font font, final TextSegment... segments) {
        this(font, Lists.arrayList(segments));
    }

    public TextRun addSegment(final TextSegment segment) {
        this.segments.add(segment);
        return this;
    }

    public TextRun add(final TextSegment segment) {
        return this.addSegment(segment);
    }

    public ShapedTextRun shape() {
        return this.shape(BasicTextShaper.INSTANCE);
    }

    public ShapedTextRun shape(final TextShaper shaper) {
        return shaper.shape(this);
    }

}
