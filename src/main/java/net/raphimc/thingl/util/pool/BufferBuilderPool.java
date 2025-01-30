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

package net.raphimc.thingl.util.pool;

import it.unimi.dsi.fastutil.objects.Reference2LongMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.drawbuilder.builder.BufferBuilder;

public class BufferBuilderPool {

    private static final ReferenceList<BufferBuilder> FREE = new ReferenceArrayList<>();
    private static final ReferenceList<BufferBuilder> IN_USE = new ReferenceArrayList<>();
    private static final Reference2LongMap<BufferBuilder> BUFFER_BUILDER_ACCESS_TIME = new Reference2LongOpenHashMap<>();

    static {
        ThinGL.registerEndFrameCallback(() -> {
            if (!IN_USE.isEmpty()) {
                ThinGL.LOGGER.warn(IN_USE.size() + " BufferBuilder(s) were not returned to the pool. Forcibly reclaiming them.");
                for (BufferBuilder bufferBuilder : IN_USE) {
                    bufferBuilder.reset();
                }
                FREE.addAll(IN_USE);
                IN_USE.clear();
            }
            BUFFER_BUILDER_ACCESS_TIME.reference2LongEntrySet().removeIf(entry -> {
                if (System.currentTimeMillis() - entry.getLongValue() > 60 * 1000) {
                    if (FREE.contains(entry.getKey())) {
                        FREE.remove(entry.getKey());
                        entry.getKey().close();
                    }
                    return true;
                }
                return false;
            });
        });
    }

    public static BufferBuilder borrowBufferBuilder() {
        ThinGL.assertOnRenderThread();
        final BufferBuilder bufferBuilder;
        if (FREE.isEmpty()) {
            bufferBuilder = new BufferBuilder();
        } else {
            bufferBuilder = FREE.remove(0);
        }
        IN_USE.add(bufferBuilder);
        BUFFER_BUILDER_ACCESS_TIME.put(bufferBuilder, System.currentTimeMillis());
        return bufferBuilder;
    }

    public static void returnBufferBuilder(final BufferBuilder bufferBuilder) {
        ThinGL.assertOnRenderThread();
        if (!IN_USE.remove(bufferBuilder)) {
            throw new IllegalStateException("BufferBuilder is not part of the pool");
        }
        bufferBuilder.reset();
        FREE.add(bufferBuilder);
    }

    public static int getSize() {
        return FREE.size() + IN_USE.size();
    }

}
