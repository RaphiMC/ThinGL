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
import net.lenni0451.commons.logging.LoggerFactory;
import net.raphimc.thingl.drawbuilder.drawbatchdataholder.ImmediateMultiDrawBatchDataHolder;
import net.raphimc.thingl.drawbuilder.index.QuadIndexBuffer;
import net.raphimc.thingl.implementation.Capabilities;
import net.raphimc.thingl.implementation.Config;
import net.raphimc.thingl.implementation.GlobalUniforms;
import net.raphimc.thingl.implementation.Workarounds;
import net.raphimc.thingl.implementation.window.WindowInterface;
import net.raphimc.thingl.program.Programs;
import net.raphimc.thingl.renderer.impl.Renderer2D;
import net.raphimc.thingl.renderer.impl.Renderer3D;
import net.raphimc.thingl.renderer.impl.RendererText;
import net.raphimc.thingl.text.FreeTypeLibrary;
import net.raphimc.thingl.text.renderer.BSDFTextRenderer;
import net.raphimc.thingl.util.SyncManager;
import net.raphimc.thingl.util.pool.BufferBuilderPool;
import net.raphimc.thingl.util.pool.FramebufferPool;
import net.raphimc.thingl.util.pool.GpuBufferPool;
import net.raphimc.thingl.util.pool.ImmediateVertexArrays;
import net.raphimc.thingl.wrapper.*;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.system.Configuration;
import org.lwjgl.util.freetype.FreeType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ThinGL {

    public static final String VERSION = "${version}";
    public static final String IMPL_VERSION = "${version}+${commit_hash}";
    public static final Logger LOGGER = LoggerFactory.getLogger("ThinGL");

    private static ThinGL INSTANCE;

    public static ThinGL get() {
        if (INSTANCE == null) {
            throw new IllegalStateException("ThinGL has not been initialized yet");
        }
        return INSTANCE;
    }

    public static boolean isInitialized() {
        return INSTANCE != null;
    }

    public static WindowInterface windowInterface() {
        return get().getWindowInterface();
    }

    public static GLStateManager glStateManager() {
        return get().getGLStateManager();
    }

    public static Config config() {
        return get().getConfig();
    }

    public static Capabilities capabilities() {
        return get().getCapabilities();
    }

    public static Workarounds workarounds() {
        return get().getWorkarounds();
    }

    public static GLStateStack glStateStack() {
        return get().getGLStateStack();
    }

    public static ScissorStack scissorStack() {
        return get().getScissorStack();
    }

    public static StencilStack stencilStack() {
        return get().getStencilStack();
    }

    public static Programs programs() {
        return get().getPrograms();
    }

    public static Renderer2D renderer2D() {
        return get().getRenderer2D();
    }

    public static Renderer3D renderer3D() {
        return get().getRenderer3D();
    }

    public static RendererText rendererText() {
        return get().getRendererText();
    }

    public static ImmediateMultiDrawBatchDataHolder globalDrawBatch() {
        return get().getGlobalDrawBatch();
    }

    public static GlobalUniforms globalUniforms() {
        return get().getGlobalUniforms();
    }

    public static BufferBuilderPool bufferBuilderPool() {
        return get().getBufferBuilderPool();
    }

    public static GpuBufferPool gpuBufferPool() {
        return get().getGpuBufferPool();
    }

    public static FramebufferPool framebufferPool() {
        return get().getFramebufferPool();
    }

    public static ImmediateVertexArrays immediateVertexArrays() {
        return get().getImmediateVertexArrays();
    }

    public static QuadIndexBuffer quadIndexBuffer() {
        return get().getQuadIndexBuffer();
    }

    public static SyncManager syncManager() {
        return get().getSyncManager();
    }

    public static FreeTypeLibrary freeTypeLibrary() {
        return get().getFreeTypeLibrary();
    }

    private final Thread renderThread;
    private final WindowInterface windowInterface;
    private final Config config;
    private final GLStateManager glStateManager;
    private final Capabilities capabilities;
    private final Workarounds workarounds;

    private final GLStateStack glStateStack;
    private final ScissorStack scissorStack;
    private final StencilStack stencilStack;
    private final Programs programs;
    private final Renderer2D renderer2D;
    private final Renderer3D renderer3D;
    private final RendererText rendererText;

    private final GlobalUniforms globalUniforms;
    private final ImmediateMultiDrawBatchDataHolder globalDrawBatch;
    private final BufferBuilderPool bufferBuilderPool;
    private final GpuBufferPool gpuBufferPool;
    private final FramebufferPool framebufferPool;
    private final ImmediateVertexArrays immediateVertexArrays;
    private final QuadIndexBuffer quadIndexBuffer;
    private final SyncManager syncManager;

    private final FreeTypeLibrary freeTypeLibrary;

    private final List<Runnable> finishFrameCallbacks = new ArrayList<>();
    private final List<Runnable> finishFrameActions = new ArrayList<>();

    private long frameStartTime = System.nanoTime();
    private float frameTime = 0F;
    private float fullFrameTime = 0F;
    private long lastFpsUpdateTime = System.nanoTime();
    private int fpsCounter = 0;
    private int fps = 0;

    public ThinGL(final Supplier<WindowInterface> windowInterface) {
        this(windowInterface.get());
    }

    public ThinGL(final WindowInterface windowInterface) {
        if (INSTANCE != null) {
            throw new IllegalStateException("ThinGL has already been initialized");
        }
        INSTANCE = this;
        this.renderThread = Thread.currentThread();
        this.windowInterface = windowInterface;
        this.config = this.createConfig();
        this.glStateManager = this.createGLStateManager();
        this.capabilities = this.createCapabilities();
        this.workarounds = this.createWorkarounds();
        this.glStateStack = this.createGLStateStack();
        this.scissorStack = this.createScissorStack();
        this.stencilStack = this.createStencilStack();
        this.programs = this.createPrograms();
        this.renderer2D = this.createRenderer2D();
        this.renderer3D = this.createRenderer3D();
        this.rendererText = this.createRendererText();
        this.globalUniforms = this.createGlobalUniforms();
        this.globalDrawBatch = this.createGlobalDrawBatch();
        this.bufferBuilderPool = this.createBufferBuilderPool();
        this.gpuBufferPool = this.createGpuBufferPool();
        this.framebufferPool = this.createFramebufferPool();
        this.immediateVertexArrays = this.createImmediateVertexArrays();
        this.quadIndexBuffer = this.createQuadIndexBuffer();
        this.syncManager = this.createSyncManager();
        this.freeTypeLibrary = this.createFreeTypeLibrary();

        if (this.capabilities.isFreeTypePresent() && this.capabilities.isHarfBuzzPresent()) {
            Configuration.HARFBUZZ_LIBRARY_NAME.set(FreeType.getLibrary());
        }
        this.addFinishFrameCallback(() -> {
            if (this.globalDrawBatch.hasDrawBatches()) {
                this.globalDrawBatch.free();
                ThinGL.LOGGER.warn("Global draw batch was not empty at the end of the frame!");
            }
        });

        final String gpuVendor = GL11C.glGetString(GL11C.GL_VENDOR);
        final String gpuModel = GL11C.glGetString(GL11C.GL_RENDERER);
        final String glVersion = GL11C.glGetString(GL11C.GL_VERSION);
        LOGGER.info("Initialized ThinGL " + IMPL_VERSION + " on " + gpuModel + " (" + gpuVendor + ") with OpenGL " + glVersion);
    }

    public synchronized void onStartFrame() {
        this.frameStartTime = System.nanoTime();
    }

    public synchronized void onFinishFrame() {
        for (Runnable action : this.finishFrameActions) {
            try {
                action.run();
            } catch (Throwable e) {
                LOGGER.error("Exception while invoking finish frame action", e);
            }
        }
        this.finishFrameActions.clear();
        for (Runnable callback : this.finishFrameCallbacks) {
            try {
                callback.run();
            } catch (Throwable e) {
                LOGGER.error("Exception while invoking finish frame callback", e);
            }
        }

        final long currentTime = System.nanoTime();
        this.frameTime = (currentTime - this.frameStartTime) / 1_000_000F;
        this.fpsCounter++;
        if (currentTime - this.lastFpsUpdateTime >= 1_000_000_000L) {
            this.lastFpsUpdateTime = currentTime;
            this.fps = this.fpsCounter;
            this.fpsCounter = 0;
        }
    }

    public synchronized void onEndFrame() {
        this.fullFrameTime = (System.nanoTime() - this.frameStartTime) / 1_000_000F;
    }

    public synchronized void addFinishFrameCallback(final Runnable callback) {
        if (this.finishFrameCallbacks.contains(callback)) {
            throw new RuntimeException("Finish frame callback already registered");
        }
        this.finishFrameCallbacks.add(callback);
    }

    public synchronized void removeFinishFrameCallback(final Runnable callback) {
        if (!this.finishFrameCallbacks.remove(callback)) {
            throw new RuntimeException("Finish frame callback not registered");
        }
    }

    public void runOnRenderThread(final Runnable action) {
        if (this.isOnRenderThread()) {
            action.run();
        } else {
            synchronized (this) {
                this.finishFrameActions.add(action);
            }
        }
    }

    public void assertOnRenderThread() {
        if (!this.isOnRenderThread()) {
            throw new RuntimeException("Not on render thread");
        }
    }

    public boolean isOnRenderThread() {
        return Thread.currentThread() == this.renderThread;
    }

    public float getFrameTime() {
        return this.frameTime;
    }

    public float getFullFrameTime() {
        return this.fullFrameTime;
    }

    public int getFPS() {
        return this.fps;
    }

    public void free() {
        this.assertOnRenderThread();
        this.windowInterface.free();
        this.programs.free();
        this.renderer2D.free();
        this.renderer3D.free();
        this.rendererText.free();
        this.globalDrawBatch.free();
        this.bufferBuilderPool.free();
        this.gpuBufferPool.free();
        this.framebufferPool.free();
        this.immediateVertexArrays.free();
        this.quadIndexBuffer.free();
        if (this.freeTypeLibrary != null) {
            this.freeTypeLibrary.free();
        }
        if (INSTANCE == this) {
            INSTANCE = null;
        }
    }

    public Thread getRenderThread() {
        return this.renderThread;
    }

    public WindowInterface getWindowInterface() {
        return this.windowInterface;
    }

    public GLStateManager getGLStateManager() {
        return this.glStateManager;
    }

    public Config getConfig() {
        return this.config;
    }

    public Capabilities getCapabilities() {
        return this.capabilities;
    }

    public Workarounds getWorkarounds() {
        return this.workarounds;
    }

    public GLStateStack getGLStateStack() {
        return this.glStateStack;
    }

    public ScissorStack getScissorStack() {
        return this.scissorStack;
    }

    public StencilStack getStencilStack() {
        return this.stencilStack;
    }

    public Programs getPrograms() {
        return this.programs;
    }

    public Renderer2D getRenderer2D() {
        return this.renderer2D;
    }

    public Renderer3D getRenderer3D() {
        return this.renderer3D;
    }

    public RendererText getRendererText() {
        return this.rendererText;
    }

    public ImmediateMultiDrawBatchDataHolder getGlobalDrawBatch() {
        return this.globalDrawBatch;
    }

    public GlobalUniforms getGlobalUniforms() {
        return this.globalUniforms;
    }

    public BufferBuilderPool getBufferBuilderPool() {
        return this.bufferBuilderPool;
    }

    public GpuBufferPool getGpuBufferPool() {
        return this.gpuBufferPool;
    }

    public FramebufferPool getFramebufferPool() {
        return this.framebufferPool;
    }

    public ImmediateVertexArrays getImmediateVertexArrays() {
        return this.immediateVertexArrays;
    }

    public QuadIndexBuffer getQuadIndexBuffer() {
        return this.quadIndexBuffer;
    }

    public SyncManager getSyncManager() {
        return this.syncManager;
    }

    public FreeTypeLibrary getFreeTypeLibrary() {
        return this.freeTypeLibrary;
    }

    protected Config createConfig() {
        return new Config();
    }

    protected GLStateManager createGLStateManager() {
        return new TrackingGLStateManager();
    }

    protected Capabilities createCapabilities() {
        return new Capabilities();
    }

    protected Workarounds createWorkarounds() {
        return new Workarounds();
    }

    protected GLStateStack createGLStateStack() {
        return new GLStateStack();
    }

    protected ScissorStack createScissorStack() {
        return new ScissorStack();
    }

    protected StencilStack createStencilStack() {
        return new StencilStack();
    }

    protected Programs createPrograms() {
        return new Programs();
    }

    protected Renderer2D createRenderer2D() {
        return new Renderer2D();
    }

    protected Renderer3D createRenderer3D() {
        return new Renderer3D();
    }

    protected RendererText createRendererText() {
        return new RendererText(new BSDFTextRenderer());
    }

    protected GlobalUniforms createGlobalUniforms() {
        return new GlobalUniforms();
    }

    protected ImmediateMultiDrawBatchDataHolder createGlobalDrawBatch() {
        return new ImmediateMultiDrawBatchDataHolder();
    }

    protected BufferBuilderPool createBufferBuilderPool() {
        return new BufferBuilderPool();
    }

    protected GpuBufferPool createGpuBufferPool() {
        return new GpuBufferPool();
    }

    protected FramebufferPool createFramebufferPool() {
        return new FramebufferPool();
    }

    protected ImmediateVertexArrays createImmediateVertexArrays() {
        return new ImmediateVertexArrays();
    }

    protected QuadIndexBuffer createQuadIndexBuffer() {
        return new QuadIndexBuffer();
    }

    protected SyncManager createSyncManager() {
        return new SyncManager();
    }

    protected FreeTypeLibrary createFreeTypeLibrary() {
        if (this.capabilities.isFreeTypePresent()) {
            return new FreeTypeLibrary();
        } else {
            return null;
        }
    }

}
