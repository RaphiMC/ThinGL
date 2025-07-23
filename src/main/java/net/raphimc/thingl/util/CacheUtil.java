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

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.util.function.Function;
import java.util.function.IntFunction;

public class CacheUtil {

    private static final int MEMOIZE_MAX_SIZE = 1024;

    public static <T, R> Function<T, R> memoizeReference(final Function<T, R> function) {
        return new Function<>() {
            private final Reference2ObjectMap<T, R> cache = new Reference2ObjectOpenHashMap<>();

            public R apply(final T object) {
                if (this.cache.size() > MEMOIZE_MAX_SIZE) {
                    this.cache.clear();
                }
                return this.cache.computeIfAbsent(object, function);
            }
        };
    }

    public static <R> IntFunction<R> memoizeInt(final IntFunction<R> function) {
        return new IntFunction<>() {
            private final Int2ObjectMap<R> cache = new Int2ObjectOpenHashMap<>();

            public R apply(final int i) {
                if (this.cache.size() > MEMOIZE_MAX_SIZE) {
                    this.cache.clear();
                }
                return this.cache.computeIfAbsent(i, function);
            }
        };
    }

}
