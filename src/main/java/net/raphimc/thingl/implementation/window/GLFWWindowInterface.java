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
package net.raphimc.thingl.implementation.window;

import net.raphimc.thingl.util.TimerHack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;

public class GLFWWindowInterface extends WindowInterface {

    private final long windowHandle;
    private final GLFWFramebufferSizeCallback originalFramebufferSizeCallback;

    public GLFWWindowInterface() {
        this(GLFW.glfwGetCurrentContext());
    }

    public GLFWWindowInterface(final long windowHandle) {
        this.windowHandle = windowHandle;

        final int[] framebufferWidth = new int[1];
        final int[] framebufferHeight = new int[1];
        GLFW.glfwGetFramebufferSize(windowHandle, framebufferWidth, framebufferHeight);
        this.callFramebufferResizeCallbacks(framebufferWidth[0], framebufferHeight[0]);

        this.originalFramebufferSizeCallback = GLFW.glfwSetFramebufferSizeCallback(windowHandle, this::onSetFramebufferSize);
    }

    @Override
    public void responsiveSleep(final float millis) {
        if (this.isOnWindowThread()) {
            TimerHack.ensureRunning();
            final double endTime = GLFW.glfwGetTime() + millis / 1_000;
            for (double time = GLFW.glfwGetTime(); time < endTime; time = GLFW.glfwGetTime()) {
                GLFW.glfwWaitEventsTimeout(endTime - time);
            }
        } else {
            super.responsiveSleep(millis);
        }
    }

    public long getWindowHandle() {
        return this.windowHandle;
    }

    @Override
    public void free() {
        this.assertOnWindowThread();
        GLFW.glfwSetFramebufferSizeCallback(this.windowHandle, this.originalFramebufferSizeCallback).free();
    }

    private void onSetFramebufferSize(final long windowHandle, final int width, final int height) {
        if (this.originalFramebufferSizeCallback != null) {
            this.originalFramebufferSizeCallback.invoke(windowHandle, width, height);
        }
        if (width != 0 && height != 0) {
            this.callFramebufferResizeCallbacks(width, height);
        }
    }

}
