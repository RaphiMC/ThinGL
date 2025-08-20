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
import net.raphimc.thingl.implementation.window.WindowInterface;
import net.raphimc.thingl.resource.framebuffer.Framebuffer;
import net.raphimc.thingl.wrapper.Blending;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.system.Callback;

import java.util.concurrent.CompletableFuture;

public abstract class ApplicationRunner {

    protected final Configuration configuration;
    protected final CompletableFuture<Void> launchFuture = new CompletableFuture<>();
    protected final CompletableFuture<Void> freeFuture = new CompletableFuture<>();
    protected WindowInterface windowInterface;
    protected ThinGL thinGL;
    private Callback debugMessageCallback;
    protected Framebuffer mainFramebuffer;

    public ApplicationRunner(final Configuration configuration) {
        this.configuration = configuration;
    }

    protected void launch() {
        if (!this.configuration.shouldUseSeparateThreads()) {
            try {
                this.launchWindowSystem();
                try {
                    this.launchGL();
                    this.launchFuture.complete(null);
                    try {
                        this.runRenderLoop();
                    } finally {
                        this.freeGL();
                    }
                } finally {
                    this.freeWindowSystem();
                }
                this.freeFuture.complete(null);
            } catch (Throwable e) {
                this.launchFuture.completeExceptionally(e);
                this.freeFuture.completeExceptionally(e);
                throw e;
            }
        } else {
            new Thread(() -> {
                try {
                    this.launchWindowSystem();
                    new Thread(() -> {
                        try {
                            this.launchGL();
                            this.launchFuture.complete(null);
                            try {
                                this.runRenderLoop();
                            } finally {
                                this.freeGL();
                            }
                        } catch (Throwable e) {
                            this.launchFuture.completeExceptionally(e);
                            this.freeFuture.completeExceptionally(e);
                            throw e;
                        }
                    }, this.getClass().getSimpleName() + " Render Thread").start();
                    try {
                        this.launchFuture.join();
                        this.runWindowLoop();
                    } finally {
                        this.freeWindowSystem();
                    }
                    this.freeFuture.complete(null);
                } catch (Throwable e) {
                    this.launchFuture.completeExceptionally(e);
                    this.freeFuture.completeExceptionally(e);
                    throw e;
                }
            }, this.getClass().getSimpleName() + " Window Thread").start();
        }
    }

    protected abstract void launchWindowSystem();

    protected void launchGL() {
        this.configureGLContext();
        GL.createCapabilities();
        this.initThinGL();
        if (this.configuration.isDebugMode()) {
            this.debugMessageCallback = DebugMessageCallback.install(this.configuration.isExtendedDebugMode());
        }
        this.init();
    }

    protected abstract void configureGLContext();

    protected void initThinGL() {
        this.thinGL = new ThinGL(this.windowInterface);
    }

    protected void init() {
        ThinGL.glStateManager().enable(GL11C.GL_BLEND);
        Blending.alphaBlending();
        ThinGL.glStateManager().enable(GL11C.GL_DEPTH_TEST);
        ThinGL.glStateManager().setDepthFunc(GL11C.GL_LEQUAL);
        ThinGL.glStateManager().enable(GL11C.GL_CULL_FACE);
        this.mainFramebuffer = new TextureFramebuffer();

        ThinGL.windowInterface().addRenderThreadFramebufferResizeCallback(this::loadProjectionMatrix);
        this.loadProjectionMatrix(ThinGL.windowInterface().getFramebufferWidth(), ThinGL.windowInterface().getFramebufferHeight());
    }

    protected void loadProjectionMatrix(final float width, final float height) {
        ThinGL.globalUniforms().getProjectionMatrix().setOrtho(0F, width, height, 0F, -1000F, 1000F);
    }

    protected void runRenderLoop() {
        while (!this.thinGL.getRenderThread().isInterrupted() && !this.windowInterface.getWindowThread().isInterrupted() && this.windowInterface.getWindowThread().isAlive()) {
            this.renderFrame(true);
            if (this.configuration.getFpsLimit() > 0) {
                final float timeToSleep = 1000F / this.configuration.getFpsLimit() - ThinGL.get().getFullFrameTime();
                if (timeToSleep > 0) {
                    ThinGL.windowInterface().responsiveSleep(timeToSleep);
                }
            }
        }
    }

    protected void runWindowLoop() {
        while (!this.windowInterface.getWindowThread().isInterrupted() && this.thinGL != null && this.thinGL.getRenderThread().isAlive()) {
            this.tickWindow();
            this.windowInterface.responsiveSleep(1F);
        }
    }

    protected void renderFrame(final boolean tickWindow) {
        this.thinGL.onFrameBegin();
        if (tickWindow && this.windowInterface.isOnWindowThread()) {
            this.tickWindow();
        }
        this.thinGL.onFrameStart();
        this.mainFramebuffer.bindAndConfigureViewport();
        this.mainFramebuffer.clear();
        this.render(new Matrix4fStack(8));
        this.mainFramebuffer.unbind();
        this.mainFramebuffer.blitTo(WindowFramebuffer.INSTANCE, true, false, false);
        this.thinGL.onFrameFinished();
        this.swapWindowBuffers();
        this.thinGL.onFrameEnd();
    }

    protected void tickWindow() {
        this.pollWindowEvents();
        this.windowInterface.runActions();
    }

    protected abstract void render(final Matrix4fStack positionMatrix);

    protected abstract void pollWindowEvents();

    protected abstract void swapWindowBuffers();

    protected void freeGL() {
        if (this.thinGL != null) {
            this.thinGL.free();
            this.thinGL = null;
        }
        GL.setCapabilities(null);
        if (this.debugMessageCallback != null) {
            this.debugMessageCallback.free();
            this.debugMessageCallback = null;
        }
    }

    protected abstract void freeWindowSystem();


    public static class Configuration {

        private String windowTitle = "ThinGL Application";
        private int windowWidth = 1280;
        private int windowHeight = 720;
        private boolean useVSync = true;
        private int fpsLimit = -1;
        private boolean useSeparateThreads = false;
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

        public boolean shouldUseSeparateThreads() {
            return this.useSeparateThreads;
        }

        public Configuration setUseSeparateThreads(final boolean useSeparateThreads) {
            this.useSeparateThreads = useSeparateThreads;
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
