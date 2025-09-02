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

public record TextSegment(String text, Color color, int styleFlags, Color outlineColor, Vector2f visualOffset) {

    public static final int STYLE_SHADOW_BIT = 1 << 0;
    public static final int STYLE_BOLD_BIT = 1 << 1;
    public static final int STYLE_ITALIC_BIT = 1 << 2;
    public static final int STYLE_UNDERLINE_BIT = 1 << 3;
    public static final int STYLE_STRIKETHROUGH_BIT = 1 << 4;

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

    public TextSegment(final String text, final Color color, final int styleFlags, final Color outlineColor) {
        this(text, color, styleFlags, outlineColor, new Vector2f());
    }

    public TextSegment(final String text, final Color color, final int styleFlags) {
        this(text, color, styleFlags, Color.TRANSPARENT);
    }

    public TextSegment(final String text, final Color color) {
        this(text, color, 0);
    }

    public TextSegment(final String text) {
        this(text, Color.WHITE);
    }

}
