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
import net.raphimc.thingl.framebuffer.impl.TextureFramebuffer;
import net.raphimc.thingl.resource.texture.Texture2D;

public class FramebufferPool {

    private static final ReferenceList<TextureFramebuffer> FREE = new ReferenceArrayList<>();
    private static final ReferenceList<TextureFramebuffer> IN_USE = new ReferenceArrayList<>();
    private static final Reference2LongMap<TextureFramebuffer> FRAME_BUFFER_ACCESS_TIME = new Reference2LongOpenHashMap<>();

    static {
        ThinGL.registerEndFrameCallback(() -> {
            if (!IN_USE.isEmpty()) {
                ThinGL.LOGGER.warn(IN_USE.size() + " Framebuffer(s) were not returned to the pool. Forcibly reclaiming them.");
                FREE.addAll(IN_USE);
                IN_USE.clear();
            }
            FRAME_BUFFER_ACCESS_TIME.reference2LongEntrySet().removeIf(entry -> {
                if (System.currentTimeMillis() - entry.getLongValue() > 60 * 1000) {
                    if (FREE.contains(entry.getKey())) {
                        FREE.remove(entry.getKey());
                        entry.getKey().delete();
                    }
                    return true;
                }
                return false;
            });
        });
    }

    public static TextureFramebuffer borrowFramebuffer(final int textureFilter) {
        ThinGL.assertOnRenderThread();
        final TextureFramebuffer framebuffer;
        if (FREE.isEmpty()) {
            framebuffer = new TextureFramebuffer(textureFilter);
        } else {
            framebuffer = FREE.remove(0);
            framebuffer.clear();
        }
        IN_USE.add(framebuffer);
        FRAME_BUFFER_ACCESS_TIME.put(framebuffer, System.currentTimeMillis());
        final Texture2D colorAttachment = framebuffer.getColorAttachment();
        if (textureFilter != colorAttachment.getMinificationFilter() || colorAttachment.getMagnificationFilter() != colorAttachment.getMinificationFilter()) {
            colorAttachment.setFilter(textureFilter);
        }
        return framebuffer;
    }

    public static void returnFramebuffer(final TextureFramebuffer framebuffer) {
        ThinGL.assertOnRenderThread();
        if (!IN_USE.remove(framebuffer)) {
            throw new IllegalStateException("Framebuffer is not part of the pool");
        }
        FREE.add(framebuffer);
    }

    public static int getSize() {
        return FREE.size() + IN_USE.size();
    }

}
