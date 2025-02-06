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
package net.raphimc.thingl.util.font;

import it.unimi.dsi.fastutil.ints.IntSet;

@FunctionalInterface
public interface GlyphPredicate {

    static GlyphPredicate all() {
        return codePoint -> true;
    }

    static GlyphPredicate range(final int min, final int max) {
        return codePoint -> codePoint >= min && codePoint <= max;
    }

    static GlyphPredicate any(final IntSet codePoints) {
        return codePoints::contains;
    }

    static GlyphPredicate any(final int... codePoints) {
        return any(IntSet.of(codePoints));
    }


    boolean test(final int codePoint);

    default GlyphPredicate and(final GlyphPredicate other) {
        return codePoint -> this.test(codePoint) && other.test(codePoint);
    }

    default GlyphPredicate or(final GlyphPredicate other) {
        return codePoint -> this.test(codePoint) || other.test(codePoint);
    }

    default GlyphPredicate negate() {
        return codePoint -> !this.test(codePoint);
    }

    default GlyphPredicate exclude(final GlyphPredicate other) {
        return codePoint -> this.test(codePoint) && !other.test(codePoint);
    }

}
