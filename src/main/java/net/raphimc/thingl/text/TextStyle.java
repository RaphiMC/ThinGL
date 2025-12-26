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

public record TextStyle(Color color, int flags, Color outlineColor, Vector2f visualOffset) {

    public static final int STYLE_SHADOW_BIT = 1 << 0;
    public static final int STYLE_BOLD_BIT = 1 << 1;
    public static final int STYLE_ITALIC_BIT = 1 << 2;
    public static final int STYLE_UNDERLINE_BIT = 1 << 3;
    public static final int STYLE_STRIKETHROUGH_BIT = 1 << 4;

    public static int buildFlags(final boolean shadow, final boolean bold, final boolean italic, final boolean underline, final boolean strikethrough) {
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

    public TextStyle(final Color color, final int flags, final Color outlineColor) {
        this(color, flags, outlineColor, new Vector2f());
    }

    public TextStyle(final Color color, final int flags) {
        this(color, flags, Color.TRANSPARENT);
    }

    public TextStyle(final Color color) {
        this(color, 0);
    }

    public TextStyle() {
        this(Color.WHITE);
    }

    public boolean isShadow() {
        return (this.flags & STYLE_SHADOW_BIT) != 0;
    }

    public boolean isBold() {
        return (this.flags & STYLE_BOLD_BIT) != 0;
    }

    public boolean isItalic() {
        return (this.flags & STYLE_ITALIC_BIT) != 0;
    }

    public boolean isUnderline() {
        return (this.flags & STYLE_UNDERLINE_BIT) != 0;
    }

    public boolean isStrikethrough() {
        return (this.flags & STYLE_STRIKETHROUGH_BIT) != 0;
    }

    public TextStyle withColor(final Color color) {
        return new TextStyle(color, this.flags, this.outlineColor, this.visualOffset);
    }

    public TextStyle withShadow(final boolean shadow) {
        int newFlags = this.flags;
        if (shadow) {
            newFlags |= STYLE_SHADOW_BIT;
        } else {
            newFlags &= ~STYLE_SHADOW_BIT;
        }
        return new TextStyle(this.color, newFlags, this.outlineColor, this.visualOffset);
    }

    public TextStyle withBold(final boolean bold) {
        int newFlags = this.flags;
        if (bold) {
            newFlags |= STYLE_BOLD_BIT;
        } else {
            newFlags &= ~STYLE_BOLD_BIT;
        }
        return new TextStyle(this.color, newFlags, this.outlineColor, this.visualOffset);
    }

    public TextStyle withItalic(final boolean italic) {
        int newFlags = this.flags;
        if (italic) {
            newFlags |= STYLE_ITALIC_BIT;
        } else {
            newFlags &= ~STYLE_ITALIC_BIT;
        }
        return new TextStyle(this.color, newFlags, this.outlineColor, this.visualOffset);
    }

    public TextStyle withUnderline(final boolean underline) {
        int newFlags = this.flags;
        if (underline) {
            newFlags |= STYLE_UNDERLINE_BIT;
        } else {
            newFlags &= ~STYLE_UNDERLINE_BIT;
        }
        return new TextStyle(this.color, newFlags, this.outlineColor, this.visualOffset);
    }

    public TextStyle withStrikethrough(final boolean strikethrough) {
        int newFlags = this.flags;
        if (strikethrough) {
            newFlags |= STYLE_STRIKETHROUGH_BIT;
        } else {
            newFlags &= ~STYLE_STRIKETHROUGH_BIT;
        }
        return new TextStyle(this.color, newFlags, this.outlineColor, this.visualOffset);
    }

    public TextStyle withOutlineColor(final Color outlineColor) {
        return new TextStyle(this.color, this.flags, outlineColor, this.visualOffset);
    }

    public TextStyle withVisualOffset(final Vector2f visualOffset) {
        return new TextStyle(this.color, this.flags, this.outlineColor, visualOffset);
    }

}
