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
package net.raphimc.thingl.implementation;

import net.raphimc.thingl.ThinGL;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class WindowInterface {

    protected final ThinGL thinGL;
    private final List<BiConsumer<Integer, Integer>> framebufferResizeCallbacks = new ArrayList<>();
    private int framebufferWidth;
    private int framebufferHeight;

    public WindowInterface(final ThinGL thinGL) {
        this.thinGL = thinGL;
        this.addFramebufferResizeCallback((width, height) -> {
            this.framebufferWidth = width;
            this.framebufferHeight = height;
        });
    }

    public synchronized void addFramebufferResizeCallback(final BiConsumer<Integer, Integer> callback) {
        if (this.framebufferResizeCallbacks.contains(callback)) {
            throw new RuntimeException("Framebuffer resize callback already registered");
        }
        this.framebufferResizeCallbacks.add(callback);
    }

    public synchronized void removeFramebufferResizeCallback(final BiConsumer<Integer, Integer> callback) {
        if (!this.framebufferResizeCallbacks.remove(callback)) {
            throw new RuntimeException("Framebuffer resize callback not registered");
        }
    }

    public int getFramebufferWidth() {
        return this.framebufferWidth;
    }

    public int getFramebufferHeight() {
        return this.framebufferHeight;
    }

    @ApiStatus.Internal
    public abstract void free();

    protected final void callFramebufferResizeCallbacks(final int width, final int height) {
        this.thinGL.runOnRenderThread(() -> {
            for (BiConsumer<Integer, Integer> callback : this.framebufferResizeCallbacks) {
                try {
                    callback.accept(width, height);
                } catch (Throwable e) {
                    ThinGL.LOGGER.error("Exception while invoking framebuffer resize callback", e);
                }
            }
        });
    }

}
