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

package net.raphimc.thingl;

import net.lenni0451.commons.logging.Logger;
import net.lenni0451.commons.logging.impl.Slf4jLogger;
import net.lenni0451.commons.logging.special.LazyInitLogger;
import net.raphimc.thingl.drawbuilder.builder.BufferRenderer;
import net.raphimc.thingl.drawbuilder.index.QuadIndexBuffer;
import net.raphimc.thingl.framebuffer.impl.MSAARenderBufferFramebuffer;
import net.raphimc.thingl.framebuffer.impl.MSAATextureFramebuffer;
import net.raphimc.thingl.implementation.ThinGLImplementation;
import net.raphimc.thingl.implementation.Workarounds;
import net.raphimc.thingl.program.BuiltinPrograms;
import net.raphimc.thingl.util.BufferUtil;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.opengl.GL11C;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ThinGL {

    public static final String VERSION = "${version}";
    public static final String IMPL_VERSION = "${impl_version}";

    public static Logger LOGGER = new LazyInitLogger(() -> new Slf4jLogger("ThinGL"));

    private static final List<BiConsumer<Integer, Integer>> WINDOW_FRAMEBUFFER_RESIZE_CALLBACKS = new ArrayList<>();
    private static final List<Runnable> END_FRAME_CALLBACKS = new ArrayList<>();
    private static final List<Runnable> PROGRAM_UNBIND_CALLBACKS = new ArrayList<>();
    private static final List<Runnable> END_FRAME_ACTIONS = new ArrayList<>();

    private static ThinGLImplementation IMPLEMENTATION;
    private static Workarounds WORKAROUNDS;
    private static GLFWFramebufferSizeCallback PREV_FRAMEBUFFER_SIZE_CALLBACK;
    private static Thread RENDER_THREAD;
    private static int WINDOW_FRAMEBUFFER_WIDTH = 0;
    private static int WINDOW_FRAMEBUFFER_HEIGHT = 0;

    public static synchronized void init(final ThinGLImplementation implementation) {
        if (IMPLEMENTATION != null) {
            throw new RuntimeException("ThinGL has already been initialized!");
        }
        final long windowHandle = GLFW.glfwGetCurrentContext();
        RENDER_THREAD = Thread.currentThread();
        IMPLEMENTATION = implementation;
        WORKAROUNDS = new Workarounds();

        final int[] windowFramebufferWidth = new int[1];
        final int[] windowFramebufferHeight = new int[1];
        GLFW.glfwGetFramebufferSize(windowHandle, windowFramebufferWidth, windowFramebufferHeight);
        registerWindowFramebufferResizeCallback((width, height) -> {
            WINDOW_FRAMEBUFFER_WIDTH = width;
            WINDOW_FRAMEBUFFER_HEIGHT = height;
        });
        onWindowFramebufferResize(windowHandle, windowFramebufferWidth[0], windowFramebufferHeight[0]);

        PREV_FRAMEBUFFER_SIZE_CALLBACK = GLFW.glfwSetFramebufferSizeCallback(windowHandle, ThinGL::onWindowFramebufferResize);

        try {
            MethodHandles.lookup().ensureInitialized(BuiltinPrograms.class); // Compile builtin programs
            MethodHandles.lookup().ensureInitialized(QuadIndexBuffer.class); // Allocate index buffer for quad rendering
            MethodHandles.lookup().ensureInitialized(BufferUtil.class); // Allocate empty buffer
            MethodHandles.lookup().ensureInitialized(BufferRenderer.class); // Allocate immediate mode buffers
            MethodHandles.lookup().ensureInitialized(MSAATextureFramebuffer.class); // Get max supported MSAA samples
            MethodHandles.lookup().ensureInitialized(MSAARenderBufferFramebuffer.class); // Get max supported MSAA samples
        } catch (IllegalAccessException ignored) {
        }

        final String gpuVendor = GL11C.glGetString(GL11C.GL_VENDOR);
        final String gpuModel = GL11C.glGetString(GL11C.GL_RENDERER);
        final String glVersion = GL11C.glGetString(GL11C.GL_VERSION);
        LOGGER.info("Initialized ThinGL " + VERSION + " on " + gpuModel + " (" + gpuVendor + ") with OpenGL " + glVersion);
    }

    public static synchronized void endFrame() {
        if (IMPLEMENTATION == null) {
            return;
        }
        for (Runnable action : END_FRAME_ACTIONS) {
            try {
                action.run();
            } catch (Throwable e) {
                LOGGER.error("Exception while invoking end frame action", e);
            }
        }
        END_FRAME_ACTIONS.clear();
        for (Runnable callback : END_FRAME_CALLBACKS) {
            try {
                callback.run();
            } catch (Throwable e) {
                LOGGER.error("Exception while invoking end frame callback", e);
            }
        }
    }

    @ApiStatus.Internal
    public static synchronized void onProgramUnbind() {
        if (IMPLEMENTATION == null) {
            return;
        }
        for (Runnable callback : PROGRAM_UNBIND_CALLBACKS) {
            try {
                callback.run();
            } catch (Throwable e) {
                LOGGER.error("Exception while invoking program unbind callback", e);
            }
        }
    }

    public static synchronized void registerWindowFramebufferResizeCallback(final BiConsumer<Integer, Integer> callback) {
        if (WINDOW_FRAMEBUFFER_RESIZE_CALLBACKS.contains(callback)) {
            throw new RuntimeException("Window framebuffer resize callback already registered");
        }
        WINDOW_FRAMEBUFFER_RESIZE_CALLBACKS.add(callback);
    }

    public static synchronized void unregisterWindowFramebufferResizeCallback(final BiConsumer<Integer, Integer> callback) {
        if (!WINDOW_FRAMEBUFFER_RESIZE_CALLBACKS.remove(callback)) {
            throw new RuntimeException("Window framebuffer resize callback not registered");
        }
    }

    public static synchronized void registerEndFrameCallback(final Runnable callback) {
        if (END_FRAME_CALLBACKS.contains(callback)) {
            throw new RuntimeException("End frame callback already registered");
        }
        END_FRAME_CALLBACKS.add(callback);
    }

    public static synchronized void unregisterEndFrameCallback(final Runnable callback) {
        if (!END_FRAME_CALLBACKS.remove(callback)) {
            throw new RuntimeException("End frame callback not registered");
        }
    }

    public static synchronized void registerProgramUnbindCallback(final Runnable callback) {
        if (PROGRAM_UNBIND_CALLBACKS.contains(callback)) {
            throw new RuntimeException("Program unbind callback already registered");
        }
        PROGRAM_UNBIND_CALLBACKS.add(callback);
    }

    public static synchronized void unregisterProgramUnbindCallback(final Runnable callback) {
        if (!PROGRAM_UNBIND_CALLBACKS.remove(callback)) {
            throw new RuntimeException("Program unbind callback not registered");
        }
    }

    public static synchronized void runOnRenderThread(final Runnable action) {
        if (isOnRenderThread()) {
            action.run();
        } else {
            END_FRAME_ACTIONS.add(action);
        }
    }

    public static void assertOnRenderThread() {
        if (!isOnRenderThread()) {
            throw new RuntimeException("Not on render thread");
        }
    }

    public static boolean isOnRenderThread() {
        if (RENDER_THREAD == null) {
            throw new RuntimeException("ThinGL has not been initialized!");
        }
        return Thread.currentThread() == RENDER_THREAD;
    }

    public static ThinGLImplementation getImplementation() {
        return IMPLEMENTATION;
    }

    public static Workarounds getWorkarounds() {
        return WORKAROUNDS;
    }

    public static int getWindowFramebufferWidth() {
        return WINDOW_FRAMEBUFFER_WIDTH;
    }

    public static int getWindowFramebufferHeight() {
        return WINDOW_FRAMEBUFFER_HEIGHT;
    }

    private static void onWindowFramebufferResize(final long windowId, final int width, final int height) {
        if (PREV_FRAMEBUFFER_SIZE_CALLBACK != null) {
            PREV_FRAMEBUFFER_SIZE_CALLBACK.invoke(windowId, width, height);
        }
        if (width == 0 || height == 0) {
            return;
        }
        for (BiConsumer<Integer, Integer> callback : WINDOW_FRAMEBUFFER_RESIZE_CALLBACKS) {
            try {
                callback.accept(width, height);
            } catch (Throwable e) {
                LOGGER.error("Exception while invoking window framebuffer resize callback", e);
            }
        }
    }

}
