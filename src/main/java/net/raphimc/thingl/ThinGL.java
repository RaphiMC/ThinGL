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
import net.raphimc.thingl.drawbuilder.drawbatchdataholder.ImmediateMultiDrawBatchDataHolder;
import net.raphimc.thingl.drawbuilder.index.QuadIndexBuffer;
import net.raphimc.thingl.implementation.ApplicationInterface;
import net.raphimc.thingl.implementation.Capabilities;
import net.raphimc.thingl.implementation.WindowInterface;
import net.raphimc.thingl.implementation.Workarounds;
import net.raphimc.thingl.program.Programs;
import net.raphimc.thingl.renderer.impl.Renderer2D;
import net.raphimc.thingl.renderer.impl.Renderer3D;
import net.raphimc.thingl.renderer.impl.RendererText;
import net.raphimc.thingl.text.FreeTypeLibrary;
import net.raphimc.thingl.text.renderer.BSDFTextRenderer;
import net.raphimc.thingl.util.pool.BufferBuilderPool;
import net.raphimc.thingl.util.pool.BufferPool;
import net.raphimc.thingl.util.pool.FramebufferPool;
import net.raphimc.thingl.util.pool.ImmediateVertexArrays;
import net.raphimc.thingl.wrapper.GLStateStack;
import net.raphimc.thingl.wrapper.ScissorStack;
import net.raphimc.thingl.wrapper.StencilStack;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.system.Configuration;
import org.lwjgl.util.freetype.FreeType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ThinGL {

    public static final String VERSION = "${version}";
    public static final String IMPL_VERSION = "${impl_version}";
    public static Logger LOGGER = new LazyInitLogger(() -> new Slf4jLogger("ThinGL"));

    private static ThinGL INSTANCE;

    public static void setInstance(final ThinGL instance) {
        if (instance == null) {
            throw new IllegalArgumentException("Instance cannot be null");
        }
        if (INSTANCE != null) {
            throw new IllegalStateException("ThinGL instance is already set");
        }
        INSTANCE = instance;
    }

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

    public static ApplicationInterface applicationInterface() {
        return get().getApplicationInterface();
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

    public static BufferBuilderPool bufferBuilderPool() {
        return get().getBufferBuilderPool();
    }

    public static BufferPool bufferPool() {
        return get().getBufferPool();
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

    public static FreeTypeLibrary freeTypeLibrary() {
        return get().getFreeTypeLibrary();
    }

    private final Thread renderThread;
    private final WindowInterface windowInterface;
    private final ApplicationInterface applicationInterface;
    private final Capabilities capabilities;
    private final Workarounds workarounds;

    private final GLStateStack glStateStack;
    private final ScissorStack scissorStack;
    private final StencilStack stencilStack;
    private final Programs programs;
    private final Renderer2D renderer2D;
    private final Renderer3D renderer3D;
    private final RendererText rendererText;

    private final ImmediateMultiDrawBatchDataHolder globalDrawBatch;
    private final BufferBuilderPool bufferBuilderPool;
    private final BufferPool bufferPool;
    private final FramebufferPool framebufferPool;
    private final ImmediateVertexArrays immediateVertexArrays;
    private final QuadIndexBuffer quadIndexBuffer;

    private final FreeTypeLibrary freeTypeLibrary;

    private final List<Runnable> endFrameCallbacks = new ArrayList<>();
    private final List<Runnable> endFrameActions = new ArrayList<>();

    public ThinGL(final Function<ThinGL, ApplicationInterface> applicationInterface, final Function<ThinGL, WindowInterface> windowInterface) {
        this.renderThread = Thread.currentThread();
        this.windowInterface = windowInterface.apply(this);
        this.applicationInterface = applicationInterface.apply(this);
        this.capabilities = new Capabilities(this);
        this.workarounds = new Workarounds(this);
        this.glStateStack = new GLStateStack(this);
        this.scissorStack = new ScissorStack(this);
        this.stencilStack = new StencilStack(this);
        this.programs = new Programs(this);
        this.renderer2D = new Renderer2D();
        this.renderer3D = new Renderer3D();
        this.rendererText = new RendererText(new BSDFTextRenderer());
        this.globalDrawBatch = new ImmediateMultiDrawBatchDataHolder();
        this.bufferBuilderPool = new BufferBuilderPool(this);
        this.bufferPool = new BufferPool(this);
        this.framebufferPool = new FramebufferPool(this);
        this.immediateVertexArrays = new ImmediateVertexArrays(this);
        this.quadIndexBuffer = new QuadIndexBuffer(this);
        if (this.capabilities.isFreeTypePresent()) {
            this.freeTypeLibrary = new FreeTypeLibrary(this);
            if (this.capabilities.isHarfBuzzPresent()) {
                Configuration.HARFBUZZ_LIBRARY_NAME.set(FreeType.getLibrary());
            }
        } else {
            this.freeTypeLibrary = null;
        }

        this.addEndFrameCallback(() -> {
            if (this.globalDrawBatch.hasDrawBatches()) {
                this.globalDrawBatch.free();
                ThinGL.LOGGER.warn("Global draw batch was not empty at the end of the frame!");
            }
        });

        final String gpuVendor = GL11C.glGetString(GL11C.GL_VENDOR);
        final String gpuModel = GL11C.glGetString(GL11C.GL_RENDERER);
        final String glVersion = GL11C.glGetString(GL11C.GL_VERSION);
        LOGGER.info("Initialized ThinGL " + VERSION + " on " + gpuModel + " (" + gpuVendor + ") with OpenGL " + glVersion);
    }

    public synchronized void onEndFrame() {
        for (Runnable action : this.endFrameActions) {
            try {
                action.run();
            } catch (Throwable e) {
                LOGGER.error("Exception while invoking end frame action", e);
            }
        }
        this.endFrameActions.clear();
        for (Runnable callback : this.endFrameCallbacks) {
            try {
                callback.run();
            } catch (Throwable e) {
                LOGGER.error("Exception while invoking end frame callback", e);
            }
        }
    }

    public synchronized void addEndFrameCallback(final Runnable callback) {
        if (this.endFrameCallbacks.contains(callback)) {
            throw new RuntimeException("End frame callback already registered");
        }
        this.endFrameCallbacks.add(callback);
    }

    public synchronized void removeEndFrameCallback(final Runnable callback) {
        if (!this.endFrameCallbacks.remove(callback)) {
            throw new RuntimeException("End frame callback not registered");
        }
    }

    public void runOnRenderThread(final Runnable action) {
        if (this.isOnRenderThread()) {
            action.run();
        } else {
            synchronized (this) {
                this.endFrameActions.add(action);
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

    public void free() {
        this.assertOnRenderThread();
        this.windowInterface.free();
        this.programs.free();
        this.renderer2D.free();
        this.renderer3D.free();
        this.rendererText.free();
        this.globalDrawBatch.free();
        this.bufferBuilderPool.free();
        this.bufferPool.free();
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

    public ApplicationInterface getApplicationInterface() {
        return this.applicationInterface;
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

    public BufferBuilderPool getBufferBuilderPool() {
        return this.bufferBuilderPool;
    }

    public BufferPool getBufferPool() {
        return this.bufferPool;
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

    public FreeTypeLibrary getFreeTypeLibrary() {
        return this.freeTypeLibrary;
    }

}
