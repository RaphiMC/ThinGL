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

package net.raphimc.thingl.wrapper;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanStack;
import it.unimi.dsi.fastutil.ints.*;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.resource.framebuffer.Framebuffer;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL14C;

import java.util.Stack;

public class GLStateTracker {

    private final Stack<Int2BooleanMap> stateStack = new Stack<>();
    private final Stack<Int2IntMap> pixelStoreStack = new Stack<>();
    private final Stack<BlendFunc> blendFuncStack = new Stack<>();
    private final IntStack depthFuncStack = new IntArrayList();
    private final BooleanStack depthMaskStack = new BooleanArrayList();
    private final Stack<int[]> viewportStack = new Stack<>();
    private final Stack<Framebuffer> framebufferStack = new Stack<>();

    @ApiStatus.Internal
    public GLStateTracker(final ThinGL thinGL) {
        thinGL.addEndFrameCallback(() -> {
            if (!this.stateStack.isEmpty()) {
                while (!this.stateStack.isEmpty()) this.pop();
                ThinGL.LOGGER.warn("GLStateTracker state stack was not empty at the end of the frame!");
            }
            if (!this.pixelStoreStack.isEmpty()) {
                while (!this.pixelStoreStack.isEmpty()) this.popPixelStore();
                ThinGL.LOGGER.warn("GLStateTracker pixel store stack was not empty at the end of the frame!");
            }
            if (!this.blendFuncStack.isEmpty()) {
                while (!this.blendFuncStack.isEmpty()) this.popBlendFunc();
                ThinGL.LOGGER.warn("GLStateTracker blend func stack was not empty at the end of the frame!");
            }
            if (!this.depthFuncStack.isEmpty()) {
                while (!this.depthFuncStack.isEmpty()) this.popDepthFunc();
                ThinGL.LOGGER.warn("GLStateTracker depth func stack was not empty at the end of the frame!");
            }
            if (!this.depthMaskStack.isEmpty()) {
                while (!this.depthMaskStack.isEmpty()) this.popDepthMask();
                ThinGL.LOGGER.warn("GLStateTracker depth mask stack was not empty at the end of the frame!");
            }
            if (!this.viewportStack.isEmpty()) {
                while (!this.viewportStack.isEmpty()) this.popViewport();
                ThinGL.LOGGER.warn("GLStateTracker viewport stack was not empty at the end of the frame!");
            }
            if (!this.framebufferStack.isEmpty()) {
                while (!this.framebufferStack.isEmpty()) this.popFramebuffer();
                ThinGL.LOGGER.warn("GLStateTracker framebuffer stack was not empty at the end of the frame!");
            }
        });
    }

    public void push() {
        this.stateStack.push(new Int2BooleanOpenHashMap());
    }

    public void enable(final int capability) {
        this.set(capability, true);
    }

    public void disable(final int capability) {
        this.set(capability, false);
    }

    private void set(final int capability, final boolean state) {
        final boolean currentState = GL11C.glIsEnabled(capability);
        if (currentState == state) return;

        this.stateStack.peek().put(capability, currentState);
        if (state) GL11C.glEnable(capability);
        else GL11C.glDisable(capability);
    }

    public void pop() {
        final Int2BooleanMap states = this.stateStack.pop();
        for (Int2BooleanMap.Entry entry : states.int2BooleanEntrySet()) {
            if (entry.getBooleanValue()) GL11C.glEnable(entry.getIntKey());
            else GL11C.glDisable(entry.getIntKey());
        }
    }

    public void pushPixelStore() {
        this.pixelStoreStack.push(new Int2IntOpenHashMap());
    }

    public void pixelStore(final int parameter, final int value) {
        final int currentValue = GL11C.glGetInteger(parameter);
        if (currentValue == value) return;

        this.pixelStoreStack.peek().put(parameter, currentValue);
        GL11C.glPixelStorei(parameter, value);
    }

    public void popPixelStore() {
        final Int2IntMap pixelStores = this.pixelStoreStack.pop();
        for (Int2IntMap.Entry entry : pixelStores.int2IntEntrySet()) {
            GL11C.glPixelStorei(entry.getIntKey(), entry.getIntValue());
        }
    }

    public void pushBlendFunc() {
        this.blendFuncStack.push(new BlendFunc());
    }

    public void popBlendFunc() {
        final BlendFunc blendFunc = this.blendFuncStack.pop();
        GL14C.glBlendFuncSeparate(blendFunc.GL_BLEND_SRC_RGB, blendFunc.GL_BLEND_DST_RGB, blendFunc.GL_BLEND_SRC_ALPHA, blendFunc.GL_BLEND_DST_ALPHA);
    }

    public void pushDepthFunc() {
        this.depthFuncStack.push(GL11C.glGetInteger(GL11C.GL_DEPTH_FUNC));
    }

    public void popDepthFunc() {
        GL11C.glDepthFunc(this.depthFuncStack.popInt());
    }

    public void pushDepthMask() {
        this.depthMaskStack.push(GL11C.glGetBoolean(GL11C.GL_DEPTH_WRITEMASK));
    }

    public void popDepthMask() {
        GL11C.glDepthMask(this.depthMaskStack.popBoolean());
    }

    public void pushViewport() {
        final int[] viewport = new int[4];
        GL11C.glGetIntegerv(GL11C.GL_VIEWPORT, viewport);
        this.viewportStack.push(viewport);
    }

    public void popViewport() {
        final int[] viewport = this.viewportStack.pop();
        GL11C.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
    }

    public void pushFramebuffer() {
        this.framebufferStack.push(ThinGL.applicationInterface().getCurrentFramebuffer());
    }

    public void popFramebuffer() {
        popFramebuffer(false);
    }

    public void popFramebuffer(final boolean setViewport) {
        final Framebuffer framebuffer = this.framebufferStack.pop();
        if (framebuffer.getGlId() < 0) throw new IllegalStateException("Framebuffer is no longer available");
        framebuffer.bind(setViewport);
    }


    private record BlendFunc(int GL_BLEND_SRC_RGB, int GL_BLEND_SRC_ALPHA, int GL_BLEND_DST_RGB, int GL_BLEND_DST_ALPHA) {
        private BlendFunc() {
            this(GL11C.glGetInteger(GL14C.GL_BLEND_SRC_RGB), GL11C.glGetInteger(GL14C.GL_BLEND_SRC_ALPHA), GL11C.glGetInteger(GL14C.GL_BLEND_DST_RGB), GL11C.glGetInteger(GL14C.GL_BLEND_DST_ALPHA));
        }
    }

}
