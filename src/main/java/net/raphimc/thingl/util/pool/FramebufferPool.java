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
import net.raphimc.thingl.resource.image.texture.Texture2D;

public class FramebufferPool {

    private final ReferenceList<TextureFramebuffer> free = new ReferenceArrayList<>();
    private final ReferenceList<TextureFramebuffer> inUse = new ReferenceArrayList<>();
    private final Reference2LongMap<TextureFramebuffer> framebufferAccessTime = new Reference2LongOpenHashMap<>();

    public FramebufferPool() {
        ThinGL.get().addFinishFrameCallback(() -> {
            if (!this.inUse.isEmpty()) {
                ThinGL.LOGGER.warn(this.inUse.size() + " Framebuffer(s) were not returned to the pool. Forcibly reclaiming them.");
                this.free.addAll(this.inUse);
                this.inUse.clear();
            }
            this.framebufferAccessTime.reference2LongEntrySet().removeIf(entry -> {
                if (System.nanoTime() - entry.getLongValue() > 60_000_000_000L) {
                    if (this.free.contains(entry.getKey())) {
                        this.free.remove(entry.getKey());
                        entry.getKey().freeFully();
                    }
                    return true;
                }
                return false;
            });
        });
    }

    public TextureFramebuffer borrowFramebuffer(final int textureFilter) {
        ThinGL.get().assertOnRenderThread();
        final TextureFramebuffer framebuffer;
        if (this.free.isEmpty()) {
            framebuffer = new TextureFramebuffer(textureFilter);
            framebuffer.setDebugName("Framebuffer Pool Framebuffer " + this.getSize());
        } else {
            framebuffer = this.free.remove(0);
            framebuffer.clear();
        }
        this.inUse.add(framebuffer);
        this.framebufferAccessTime.put(framebuffer, System.nanoTime());
        final Texture2D colorAttachment = framebuffer.getColorAttachment(0);
        if (textureFilter != colorAttachment.getMinificationFilter() || colorAttachment.getMagnificationFilter() != colorAttachment.getMinificationFilter()) {
            colorAttachment.setFilter(textureFilter);
        }
        final Texture2D depthAttachment = framebuffer.getDepthAttachment();
        if (textureFilter != depthAttachment.getMinificationFilter() || depthAttachment.getMagnificationFilter() != depthAttachment.getMinificationFilter()) {
            depthAttachment.setFilter(textureFilter);
        }
        return framebuffer;
    }

    public void returnFramebuffer(final TextureFramebuffer framebuffer) {
        ThinGL.get().assertOnRenderThread();
        if (!this.inUse.remove(framebuffer)) {
            throw new IllegalStateException("Framebuffer is not part of the pool");
        }
        this.free.add(framebuffer);
    }

    public int getSize() {
        return this.free.size() + this.inUse.size();
    }

    public void free() {
        for (TextureFramebuffer framebuffer : this.free) {
            framebuffer.freeFully();
        }
        for (TextureFramebuffer framebuffer : this.inUse) {
            framebuffer.freeFully();
        }
    }

}
