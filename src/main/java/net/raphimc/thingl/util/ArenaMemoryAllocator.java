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

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

import java.util.Map;
import java.util.TreeMap;

public class ArenaMemoryAllocator {

    private final long address;
    private final long size;
    private final Long2LongMap allocationMap = new Long2LongOpenHashMap(); // Address -> Size
    private final TreeMap<Long, Long> freeMap = new TreeMap<>(); // Address -> Size

    public ArenaMemoryAllocator(final long address, final long size) {
        this.address = address;
        this.size = size;
        this.freeMap.put(address, size);
    }

    public long alloc(final long segmentSize) {
        if (segmentSize <= 0) {
            return -1;
        }

        for (Map.Entry<Long, Long> entry : this.freeMap.entrySet()) {
            final long freeSegmentAddress = entry.getKey();
            final long freeSegmentSize = entry.getValue();

            if (freeSegmentSize >= segmentSize) {
                this.freeMap.remove(freeSegmentAddress);

                if (freeSegmentSize > segmentSize) {
                    final long remainingSize = freeSegmentSize - segmentSize;
                    final long remainingAddress = freeSegmentAddress + segmentSize;
                    this.freeMap.put(remainingAddress, remainingSize);
                }

                this.allocationMap.put(freeSegmentAddress, segmentSize);
                return freeSegmentAddress;
            }
        }

        return -1;
    }

    public void free(final long ptr) {
        long segmentSize = this.allocationMap.remove(ptr);
        if (segmentSize == 0) {
            return;
        }

        long start = ptr;
        long end = start + segmentSize;
        final Long lower = this.freeMap.floorKey(start);
        if (lower != null && lower + this.freeMap.get(lower) == start) {
            start = lower;
            segmentSize += this.freeMap.get(lower);
            this.freeMap.remove(lower);
        }
        final Long higher = this.freeMap.ceilingKey(end);
        if (higher != null && higher == end) {
            segmentSize += this.freeMap.get(higher);
            this.freeMap.remove(higher);
        }

        this.freeMap.put(start, segmentSize);
    }

    public long getUsedMemory() {
        return this.allocationMap.values().longStream().sum();
    }

    public long getFreeMemory() {
        return this.freeMap.values().stream().mapToLong(Long::longValue).sum();
    }

    public long getAddress() {
        return this.address;
    }

    public long getSize() {
        return this.size;
    }

}
