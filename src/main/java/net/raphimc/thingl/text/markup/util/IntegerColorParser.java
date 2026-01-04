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
package net.raphimc.thingl.text.markup.util;

import net.lenni0451.commons.color.Color;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum IntegerColorParser {

    RGB(rgb -> Color.fromRGB(rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF)),
    RGBA(rgba -> Color.fromRGBA(rgba[0] & 0xFF, rgba[1] & 0xFF, rgba[2] & 0xFF, rgba[3] & 0xFF)),
    ARGB(argb -> Color.fromRGBA(argb[1] & 0xFF, argb[2] & 0xFF, argb[3] & 0xFF, argb[0] & 0xFF)),
    ;

    private final String name;
    private final String[] components;
    private final String bracketFormat;
    private final String hexFormat;
    private final Function<byte[], Color> constructor;

    IntegerColorParser(final Function<byte[], Color> constructor) {
        this.name = this.name().toLowerCase(Locale.ROOT);
        this.components = this.name.chars().mapToObj(c -> String.valueOf((char) c)).toArray(String[]::new);
        this.bracketFormat = '(' + String.join(",", this.components) + ')';
        this.hexFormat = '#' + Arrays.stream(this.components).map(c -> c.toUpperCase(Locale.ROOT) + c.toUpperCase(Locale.ROOT)).collect(Collectors.joining());
        this.constructor = constructor;
    }

    public Color parse(final String value) {
        if (value.startsWith("(")) {
            if (!value.endsWith(")")) {
                throw new IllegalArgumentException("Invalid " + this.name + " value: '" + value + "', expected format: " + this.bracketFormat);
            }
            final String[] parts = value.substring(1, value.length() - 1).split(",");
            if (parts.length != this.components.length) {
                throw new IllegalArgumentException("Invalid " + this.name + " value: '" + value + "', expected format: " + this.bracketFormat);
            }
            final byte[] components = new byte[parts.length];
            for (int i = 0; i < parts.length; i++) {
                try {
                    final int component = Integer.parseInt(parts[i].trim());
                    if (component < 0 || component > 255) {
                        throw new IllegalArgumentException("Invalid " + this.name + " component value: '" + component + "', expected range: 0-255");
                    }
                    components[i] = (byte) component;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid " + this.name + " component value: '" + parts[i].trim() + "', expected an integer");
                }
            }
            return this.constructor.apply(components);
        } else if (value.startsWith("#")) {
            final byte[] color = HexFormat.of().parseHex(value.substring(1));
            if (color.length != this.components.length) {
                throw new IllegalArgumentException("Invalid " + this.name + " value: '" + value + "', expected format: " + this.hexFormat);
            }
            return this.constructor.apply(color);
        } else {
            throw new IllegalArgumentException("Invalid " + this.name + " value: '" + value + "', expected format: " + this.bracketFormat + " or " + this.hexFormat);
        }
    }

}
