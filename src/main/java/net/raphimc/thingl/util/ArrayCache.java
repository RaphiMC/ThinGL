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
package net.raphimc.thingl.util;

import java.util.function.IntFunction;

public class ArrayCache<T> {

    private final int blockSize;
    private final int blockShift;
    private final int blockMask;
    private final T[][] blocks;
    private final IntFunction<T> loadFunction;

    public ArrayCache(final int length, final IntFunction<T> loadFunction) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be > 0");
        }
        this.blockSize = org.lwjgl.system.MathUtil.mathRoundPoT((int) Math.ceil(Math.sqrt(length)));
        this.blockShift = Integer.numberOfTrailingZeros(this.blockSize);
        this.blockMask = this.blockSize - 1;
        this.blocks = (T[][]) new Object[(int) Math.ceil((double) length / this.blockSize)][];
        this.loadFunction = loadFunction;
    }

    public T getOrLoad(final int index) {
        final T value = this.get(index);
        if (value != null) {
            return value;
        } else {
            return this.load(index);
        }
    }

    private T get(final int index) {
        final T[] block = this.blocks[index >> this.blockShift];
        if (block != null) {
            return block[index & this.blockMask];
        } else {
            return null;
        }
    }

    private synchronized T load(final int index) {
        final int blockIndex = index >> this.blockShift;
        final int blockOffset = index & this.blockMask;
        T[] block = this.blocks[blockIndex];
        if (block == null) {
            block = (T[]) new Object[this.blockSize];
            this.blocks[blockIndex] = block;
        }
        T value = block[blockOffset];
        if (value == null) {
            value = this.loadFunction.apply(index);
            if (value != null) {
                block[blockOffset] = value;
            } else {
                throw new IllegalStateException("Load function returned null for index " + index);
            }
        }
        return value;
    }

}
