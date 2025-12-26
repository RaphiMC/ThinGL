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

import net.lenni0451.commons.color.Color;
import org.joml.Vector2f;

public record TextSegment(String text, TextStyle style) {

    public TextSegment(final String text, final Color color, final int styleFlags, final Color outlineColor) {
        this(text, new TextStyle(color, styleFlags, outlineColor));
    }

    public TextSegment(final String text, final Color color, final int styleFlags) {
        this(text, new TextStyle(color, styleFlags));
    }

    public TextSegment(final String text, final Color color) {
        this(text, new TextStyle(color));
    }

    public TextSegment(final String text) {
        this(text, new TextStyle());
    }


    @Deprecated(forRemoval = true)
    public static final int STYLE_SHADOW_BIT = 1 << 0;

    @Deprecated(forRemoval = true)
    public static final int STYLE_BOLD_BIT = 1 << 1;

    @Deprecated(forRemoval = true)
    public static final int STYLE_ITALIC_BIT = 1 << 2;

    @Deprecated(forRemoval = true)
    public static final int STYLE_UNDERLINE_BIT = 1 << 3;

    @Deprecated(forRemoval = true)
    public static final int STYLE_STRIKETHROUGH_BIT = 1 << 4;

    @Deprecated(forRemoval = true)
    public static int buildStyleFlags(final boolean shadow, final boolean bold, final boolean italic, final boolean underline, final boolean strikethrough) {
        int flags = 0;
        if (shadow) {
            flags |= STYLE_SHADOW_BIT;
        }
        if (bold) {
            flags |= STYLE_BOLD_BIT;
        }
        if (italic) {
            flags |= STYLE_ITALIC_BIT;
        }
        if (underline) {
            flags |= STYLE_UNDERLINE_BIT;
        }
        if (strikethrough) {
            flags |= STYLE_STRIKETHROUGH_BIT;
        }
        return flags;
    }

    @Deprecated(forRemoval = true)
    public TextSegment(final String text, final Color color, final int styleFlags, final Color outlineColor, final Vector2f visualOffset) {
        this(text, new TextStyle(color, styleFlags, outlineColor, visualOffset));
    }

    @Deprecated(forRemoval = true)
    public Color color() {
        return this.style.color();
    }

    @Deprecated(forRemoval = true)
    public int styleFlags() {
        return this.style.flags();
    }

    @Deprecated(forRemoval = true)
    public Color outlineColor() {
        return this.style.outlineColor();
    }

    @Deprecated(forRemoval = true)
    public Vector2f visualOffset() {
        return this.style.visualOffset();
    }

}
