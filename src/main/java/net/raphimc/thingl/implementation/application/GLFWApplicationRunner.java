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
package net.raphimc.thingl.implementation.application;

import net.raphimc.thingl.implementation.window.GLFWWindowInterface;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

public abstract class GLFWApplicationRunner extends ApplicationRunner {

    protected long window;

    public GLFWApplicationRunner(final Configuration configuration) {
        super(configuration);
    }

    @Override
    protected void launchWindowSystem() {
        this.initGLFW();
        this.setWindowHints();
        this.createWindow();
        this.windowInterface = new GLFWWindowInterface(this.window);
    }

    protected void initGLFW() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }
    }

    protected void setWindowHints() {
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_API);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_CREATION_API, GLFW.GLFW_NATIVE_CONTEXT_API);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, this.configuration.getOpenGLMajorVersion());
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, this.configuration.getOpenGLMinorVersion());
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
    }

    protected void createWindow() {
        this.window = GLFW.glfwCreateWindow(this.configuration.getWindowWidth(), this.configuration.getWindowHeight(), this.configuration.getWindowTitle(), 0L, 0L);
        if (this.window == 0L) {
            if (GLFW.glfwGetError(null) == GLFW.GLFW_VERSION_UNAVAILABLE && this.configuration.getOpenGLMajorVersion() == 4 && this.configuration.getOpenGLMinorVersion() > 1) {
                this.configuration.setOpenGLMinorVersion(this.configuration.getOpenGLMinorVersion() - 1);
                GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, this.configuration.getOpenGLMinorVersion());
                this.createWindow();
                return;
            }

            throw new RuntimeException("Failed to create window");
        }
    }

    @Override
    protected void configureGLContext() {
        GLFW.glfwMakeContextCurrent(this.window);
        GLFW.glfwSwapInterval(this.configuration.shouldUseVSync() ? 1 : 0);
    }

    @Override
    protected void pollWindowEvents() {
        GLFW.glfwPollEvents();
        if (GLFW.glfwWindowShouldClose(this.window)) {
            this.thinGL.getRenderThread().interrupt();
        }
    }

    @Override
    protected void swapWindowBuffers() {
        GLFW.glfwSwapBuffers(this.window);
    }

    @Override
    protected void freeWindowSystem() {
        this.freeWindow();
        this.freeGLFW();
    }

    protected void freeWindow() {
        if (this.windowInterface != null) {
            this.windowInterface.free();
            this.windowInterface = null;
        }
        if (this.window != 0L) {
            GLFW.glfwDestroyWindow(this.window);
            this.window = 0L;
        }
    }

    protected void freeGLFW() {
        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null).free();
    }

}
