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
package base;

import net.lenni0451.commons.logging.impl.SysoutLogger;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.framebuffer.impl.TextureFramebuffer;
import net.raphimc.thingl.framebuffer.impl.WindowFramebuffer;
import net.raphimc.thingl.implementation.DebugMessageCallback;
import net.raphimc.thingl.implementation.GLFWWindowInterface;
import net.raphimc.thingl.wrapper.Blending;
import org.joml.Matrix4fStack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;

public abstract class ExampleBase implements Runnable {

    @Override
    public void run() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_API);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_CREATION_API, GLFW.GLFW_NATIVE_CONTEXT_API);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 5);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        final long window = GLFW.glfwCreateWindow(1280, 720, "ThinGL Example (" + this.getClass().getSimpleName() + ")", 0L, 0L);
        if (window == 0L) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1);
        GL.createCapabilities();

        ThinGL.LOGGER = SysoutLogger.builder().name("ThinGL").build();
        ThinGL.setInstance(new ThinGL(ExampleThinGLImplementation::new, GLFWWindowInterface::new)); // Init ThinGL
        DebugMessageCallback.install(true); // Enable synchronous debug messages to ensure stack traces are correct (Only use this for debugging!)

        GL11C.glEnable(GL11C.GL_BLEND);
        Blending.standardBlending();
        GL11C.glDisable(GL11C.GL_DEPTH_TEST);
        GL11C.glDepthFunc(GL11C.GL_LEQUAL);
        this.init(); // Initialize the example
        final TextureFramebuffer mainFramebuffer = new TextureFramebuffer(); // Create the main framebuffer
        final Matrix4fStack positionMatrix = new Matrix4fStack(8);

        while (!GLFW.glfwWindowShouldClose(window)) {
            ThinGL.get().onStartFrame(); // Let ThinGL know that the current frame is starting
            mainFramebuffer.bind(true); // Bind the main framebuffer
            mainFramebuffer.clear(); // Clear the main framebuffer

            positionMatrix.pushMatrix();
            this.render(positionMatrix); // Render the example
            positionMatrix.popMatrix();

            mainFramebuffer.unbind();
            mainFramebuffer.blitTo(WindowFramebuffer.INSTANCE, true, false, false); // Blit the main framebuffer to the window framebuffer
            ThinGL.get().onFinishFrame(); // Let ThinGL know that the current frame is done rendering and ready to be presented
            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
            ThinGL.get().onEndFrame(); // Let ThinGL know that the current frame is done and the next frame can start
            // FPSLimiter.limitFPS(30); // Example to limit the FPS to 30 FPS
        }

        ThinGL.get().free(); // Destroy the ThinGL instance and free all resources
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    protected void init() {
    }

    protected abstract void render(final Matrix4fStack positionMatrix);

}
