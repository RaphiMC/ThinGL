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
package net.raphimc.thingl.text.markup.util;

import net.lenni0451.commons.collections.Maps;
import net.lenni0451.commons.color.Color;

import java.util.Locale;
import java.util.Map;

public class ConstantColorParser {

    private static final Map<String, Color> COLORS = Maps.hashMap(
            "transparent", Color.TRANSPARENT,
            "black", Color.BLACK,
            "light_gray", Color.LIGHT_GRAY,
            "gray", Color.GRAY,
            "dark_gray", Color.DARK_GRAY,
            "white", Color.WHITE,
            "red", Color.RED,
            "green", Color.GREEN,
            "blue", Color.BLUE,
            "orange", Color.ORANGE,
            "yellow", Color.YELLOW,
            "cyan", Color.CYAN,
            "pink", Color.PINK,
            "magenta", Color.MAGENTA
    );

    public static Color parse(final String value) {
        final Color color = COLORS.get(value.toLowerCase(Locale.ROOT));
        if (color != null) {
            return color;
        } else {
            throw new IllegalArgumentException("Unknown color constant: '" + value + "', expected one of: " + COLORS.keySet());
        }
    }

}
