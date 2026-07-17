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
package net.raphimc.thingl.implementation.window;

import net.raphimc.thingl.util.TimerHack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;

public class GLFWWindowInterface extends WindowInterface {

    private final long windowHandle;
    private final GLFWFramebufferSizeCallback framebufferSizeCallback;
    private final GLFWFramebufferSizeCallback previousFramebufferSizeCallback;

    public GLFWWindowInterface() {
        final long windowHandle = GLFW.glfwGetCurrentContext();
        if (windowHandle == 0L) {
            throw new IllegalStateException("Failed to get OpenGL context window handle");
        }
        this(windowHandle);
    }

    public GLFWWindowInterface(final long windowHandle) {
        this.windowHandle = windowHandle;

        final int[] framebufferWidth = new int[1];
        final int[] framebufferHeight = new int[1];
        GLFW.glfwGetFramebufferSize(windowHandle, framebufferWidth, framebufferHeight);
        this.callFramebufferResizeCallbacks(framebufferWidth[0], framebufferHeight[0]);

        this.framebufferSizeCallback = GLFWFramebufferSizeCallback.create(this::onSetFramebufferSize);
        this.previousFramebufferSizeCallback = GLFW.glfwSetFramebufferSizeCallback(windowHandle, this.framebufferSizeCallback);
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
        final GLFWFramebufferSizeCallback previousFramebufferSizeCallback = GLFW.glfwSetFramebufferSizeCallback(this.windowHandle, this.previousFramebufferSizeCallback);
        if (previousFramebufferSizeCallback != this.framebufferSizeCallback) {
            GLFW.glfwSetFramebufferSizeCallback(this.windowHandle, previousFramebufferSizeCallback);
        }
        this.framebufferSizeCallback.free();
    }

    private void onSetFramebufferSize(final long windowHandle, final int width, final int height) {
        if (this.previousFramebufferSizeCallback != null) {
            this.previousFramebufferSizeCallback.invoke(windowHandle, width, height);
        }
        this.callFramebufferResizeCallbacks(width, height);
    }

}
