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

package net.raphimc.thingl.util;

import org.joml.Math;

public class MathUtil {

    public static final float PI = (float) Math.PI;
    public static final float TAU = PI * 2F;
    public static final float HALF_PI = PI / 2F;

    public static int align(final int value, final int alignment) {
        // return (value + alignment - 1) & -alignment; // Fast POT alignment
        if (alignment <= 0) {
            throw new IllegalArgumentException("Alignment must be a positive integer");
        }

        final int remainder = value % alignment;
        if (remainder == 0) {
            return value;
        }
        return value + alignment - remainder;
    }

    public static long align(final long value, final long alignment) {
        // return (value + alignment - 1) & -alignment; // Fast POT alignment
        if (alignment <= 0) {
            throw new IllegalArgumentException("Alignment must be a positive integer");
        }

        final long remainder = value % alignment;
        if (remainder == 0) {
            return value;
        }
        return value + alignment - remainder;
    }

    /**
     * Encodes a float value into a half precision float (IEEE-754 fp16) value.
     * @param v The float value to encode
     * @return The encoded half precision float
     */
    public static short encodeHalfFloat(final float v) {
        final int bits = Float.floatToRawIntBits(v);
        final int s = (bits >>> 16) & 0x8000;
        final int em = bits & 0x7fffffff;

        int h = (em - (112 << 23) + (1 << 12)) >> 13;
        h = (em < (113 << 23)) ? 0 : h;
        h = (em >= (143 << 23)) ? 0x7c00 : h;
        h = (em > (255 << 23)) ? 0x7e00 : h;

        return (short) (s | h);
    }

    /**
     * Decodes a half precision float (IEEE-754 fp16) value into a float value.
     * @param v The half precision float to decode
     * @return The decoded float value
     */
    public static float decodeHalfFloat(final short v) {
        final int s = (v & 0x8000) << 16;
        final int em = v & 0x7fff;

        int r = (em + (112 << 10)) << 13;
        r = (em < (1 << 10)) ? 0 : r;
        r += (em >= (31 << 10)) ? (112 << 23) : 0;

        return Float.intBitsToFloat(s | r);
    }

}
