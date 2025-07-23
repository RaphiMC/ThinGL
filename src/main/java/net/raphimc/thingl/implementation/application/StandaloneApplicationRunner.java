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
package net.raphimc.thingl.implementation.application;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.framebuffer.impl.TextureFramebuffer;
import net.raphimc.thingl.framebuffer.impl.WindowFramebuffer;
import net.raphimc.thingl.implementation.DebugMessageCallback;
import net.raphimc.thingl.implementation.window.GLFWWindowInterface;
import net.raphimc.thingl.resource.framebuffer.Framebuffer;
import net.raphimc.thingl.util.FPSLimiter;
import net.raphimc.thingl.wrapper.Blending;
import org.joml.Matrix4fStack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;

public abstract class StandaloneApplicationRunner {

    protected final Configuration configuration;
    protected long window;
    protected Framebuffer mainFramebuffer;

    public StandaloneApplicationRunner(final Configuration configuration) {
        this.configuration = configuration;
    }

    protected void launch() {
        this.initGLFW();
        this.setWindowFlags();
        this.createWindow();

        GLFW.glfwMakeContextCurrent(this.window);
        GLFW.glfwSwapInterval(this.configuration.shouldUseVSync() ? 1 : 0);
        GL.createCapabilities();

        this.createThinGL(); // Init ThinGL
        if (this.configuration.debugMode) {
            DebugMessageCallback.install(this.configuration.extendedDebugMode);
        }

        this.init();
        final Matrix4fStack positionMatrix = new Matrix4fStack(8);

        while (!GLFW.glfwWindowShouldClose(this.window)) {
            ThinGL.get().onStartFrame(); // Let ThinGL know that the current frame is starting
            this.mainFramebuffer.bind(true); // Bind the main framebuffer
            this.mainFramebuffer.clear(); // Clear the main framebuffer

            positionMatrix.identity();
            this.render(positionMatrix);

            this.mainFramebuffer.unbind();
            this.mainFramebuffer.blitTo(WindowFramebuffer.INSTANCE, true, false, false); // Blit the main framebuffer to the window framebuffer
            ThinGL.get().onFinishFrame(); // Let ThinGL know that the current frame is done rendering and ready to be presented
            GLFW.glfwSwapBuffers(this.window);
            GLFW.glfwPollEvents();
            ThinGL.get().onEndFrame(); // Let ThinGL know that the current frame is done and the next frame can start
            if (this.configuration.fpsLimit > 0) {
                FPSLimiter.limitFPS(this.configuration.fpsLimit);
            }
        }

        this.free();
    }

    protected void initGLFW() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
    }

    protected void setWindowFlags() {
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_API);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_CREATION_API, GLFW.GLFW_NATIVE_CONTEXT_API);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 5);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
    }

    protected void createWindow() {
        this.window = GLFW.glfwCreateWindow(this.configuration.windowWidth, this.configuration.windowHeight, this.configuration.windowTitle, 0L, 0L);
        if (this.window == 0L) {
            throw new RuntimeException("Failed to create the GLFW window");
        }
    }

    protected ThinGL createThinGL() {
        return new ThinGL(StandaloneApplicationInterface::new, GLFWWindowInterface::new);
    }

    protected void init() {
        ThinGL.glStateManager().enable(GL11C.GL_BLEND);
        Blending.alphaBlending();
        ThinGL.glStateManager().enable(GL11C.GL_DEPTH_TEST);
        ThinGL.glStateManager().setDepthFunc(GL11C.GL_LEQUAL);
        ThinGL.glStateManager().enable(GL11C.GL_CULL_FACE);
        this.mainFramebuffer = new TextureFramebuffer();
    }

    protected abstract void render(final Matrix4fStack positionMatrix);

    protected void free() {
        ThinGL.get().free(); // Destroy the ThinGL instance and free all resources
        GLFW.glfwDestroyWindow(this.window);
        GLFW.glfwTerminate();
    }


    public static class Configuration {

        private String windowTitle = "ThinGL Application";
        private int windowWidth = 1280;
        private int windowHeight = 720;
        private boolean useVSync = true;
        private int fpsLimit = -1;
        private boolean debugMode = true;
        private boolean extendedDebugMode = false;

        public String getWindowTitle() {
            return this.windowTitle;
        }

        public Configuration setWindowTitle(final String windowTitle) {
            this.windowTitle = windowTitle;
            return this;
        }

        public int getWindowWidth() {
            return this.windowWidth;
        }

        public Configuration setWindowWidth(final int windowWidth) {
            this.windowWidth = windowWidth;
            return this;
        }

        public int getWindowHeight() {
            return this.windowHeight;
        }

        public Configuration setWindowHeight(final int windowHeight) {
            this.windowHeight = windowHeight;
            return this;
        }

        public boolean shouldUseVSync() {
            return this.useVSync;
        }

        public Configuration setUseVSync(final boolean useVSync) {
            this.useVSync = useVSync;
            return this;
        }

        public int getFpsLimit() {
            return this.fpsLimit;
        }

        public Configuration setFpsLimit(final int fpsLimit) {
            this.fpsLimit = fpsLimit;
            return this;
        }

        public boolean isDebugMode() {
            return this.debugMode;
        }

        public Configuration setDebugMode(final boolean debugMode) {
            this.debugMode = debugMode;
            return this;
        }

        public boolean isExtendedDebugMode() {
            return this.extendedDebugMode;
        }

        public Configuration setExtendedDebugMode(final boolean extendedDebugMode) {
            this.extendedDebugMode = extendedDebugMode;
            return this;
        }

    }

}
