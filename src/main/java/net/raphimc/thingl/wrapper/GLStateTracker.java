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
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL14C;

import java.util.Stack;

public class GLStateTracker {

    private static final Stack<Int2BooleanMap> STATE_STACK = new Stack<>();
    private static final Stack<Int2IntMap> PIXEL_STORE_STACK = new Stack<>();
    private static final Stack<BlendFunc> BLEND_FUNC_STACK = new Stack<>();
    private static final IntStack DEPTH_FUNC_STACK = new IntArrayList();
    private static final BooleanStack DEPTH_MASK_STACK = new BooleanArrayList();
    private static final Stack<int[]> VIEWPORT_STACK = new Stack<>();
    private static final Stack<Framebuffer> FRAMEBUFFER_STACK = new Stack<>();

    static {
        ThinGL.registerEndFrameCallback(() -> {
            if (!STATE_STACK.isEmpty()) {
                while (!STATE_STACK.isEmpty()) pop();
                ThinGL.LOGGER.warn("GLStateTracker STATE_STACK was not empty after rendering one frame!");
            }
            if (!PIXEL_STORE_STACK.isEmpty()) {
                while (!PIXEL_STORE_STACK.isEmpty()) popPixelStore();
                ThinGL.LOGGER.warn("GLStateTracker PIXEL_STORE_STACK was not empty after rendering one frame!");
            }
            if (!BLEND_FUNC_STACK.isEmpty()) {
                while (!BLEND_FUNC_STACK.isEmpty()) popBlendFunc();
                ThinGL.LOGGER.warn("GLStateTracker BLEND_FUNC_STACK was not empty after rendering one frame!");
            }
            if (!DEPTH_FUNC_STACK.isEmpty()) {
                while (!DEPTH_FUNC_STACK.isEmpty()) popDepthFunc();
                ThinGL.LOGGER.warn("GLStateTracker DEPTH_FUNC_STACK was not empty after rendering one frame!");
            }
            if (!DEPTH_MASK_STACK.isEmpty()) {
                while (!DEPTH_MASK_STACK.isEmpty()) popDepthMask();
                ThinGL.LOGGER.warn("GLStateTracker DEPTH_MASK_STACK was not empty after rendering one frame!");
            }
            if (!VIEWPORT_STACK.isEmpty()) {
                while (!VIEWPORT_STACK.isEmpty()) popViewport();
                ThinGL.LOGGER.warn("GLStateTracker VIEWPORT_STACK was not empty after rendering one frame!");
            }
            if (!FRAMEBUFFER_STACK.isEmpty()) {
                while (!FRAMEBUFFER_STACK.isEmpty()) popFramebuffer();
                ThinGL.LOGGER.warn("GLStateTracker FRAMEBUFFER_STACK was not empty after rendering one frame!");
            }
        });
    }

    public static void push() {
        STATE_STACK.push(new Int2BooleanOpenHashMap());
    }

    public static void enable(final int capability) {
        set(capability, true);
    }

    public static void disable(final int capability) {
        set(capability, false);
    }

    private static void set(final int capability, final boolean state) {
        final boolean currentState = GL11C.glIsEnabled(capability);
        if (currentState == state) return;

        STATE_STACK.peek().put(capability, currentState);
        if (state) GL11C.glEnable(capability);
        else GL11C.glDisable(capability);
    }

    public static void pop() {
        final Int2BooleanMap states = STATE_STACK.pop();
        for (Int2BooleanMap.Entry entry : states.int2BooleanEntrySet()) {
            if (entry.getBooleanValue()) GL11C.glEnable(entry.getIntKey());
            else GL11C.glDisable(entry.getIntKey());
        }
    }

    public static void pushPixelStore() {
        PIXEL_STORE_STACK.push(new Int2IntOpenHashMap());
    }

    public static void pixelStore(final int parameter, final int value) {
        final int currentValue = GL11C.glGetInteger(parameter);
        if (currentValue == value) return;

        PIXEL_STORE_STACK.peek().put(parameter, currentValue);
        GL11C.glPixelStorei(parameter, value);
    }

    public static void popPixelStore() {
        final Int2IntMap pixelStores = PIXEL_STORE_STACK.pop();
        for (Int2IntMap.Entry entry : pixelStores.int2IntEntrySet()) {
            GL11C.glPixelStorei(entry.getIntKey(), entry.getIntValue());
        }
    }

    public static void pushBlendFunc() {
        BLEND_FUNC_STACK.push(new BlendFunc());
    }

    public static void popBlendFunc() {
        final BlendFunc blendFunc = BLEND_FUNC_STACK.pop();
        GL14C.glBlendFuncSeparate(blendFunc.GL_BLEND_SRC_RGB, blendFunc.GL_BLEND_DST_RGB, blendFunc.GL_BLEND_SRC_ALPHA, blendFunc.GL_BLEND_DST_ALPHA);
    }

    public static void pushDepthFunc() {
        DEPTH_FUNC_STACK.push(GL11C.glGetInteger(GL11C.GL_DEPTH_FUNC));
    }

    public static void popDepthFunc() {
        GL11C.glDepthFunc(DEPTH_FUNC_STACK.popInt());
    }

    public static void pushDepthMask() {
        DEPTH_MASK_STACK.push(GL11C.glGetBoolean(GL11C.GL_DEPTH_WRITEMASK));
    }

    public static void popDepthMask() {
        GL11C.glDepthMask(DEPTH_MASK_STACK.popBoolean());
    }

    public static void pushViewport() {
        final int[] viewport = new int[4];
        GL11C.glGetIntegerv(GL11C.GL_VIEWPORT, viewport);
        VIEWPORT_STACK.push(viewport);
    }

    public static void popViewport() {
        final int[] viewport = VIEWPORT_STACK.pop();
        GL11C.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
    }

    public static void pushFramebuffer() {
        FRAMEBUFFER_STACK.push(ThinGL.getImplementation().getCurrentFramebuffer());
    }

    public static void popFramebuffer() {
        popFramebuffer(false);
    }

    public static void popFramebuffer(final boolean setViewport) {
        final Framebuffer framebuffer = FRAMEBUFFER_STACK.pop();
        if (framebuffer.getGlId() < 0) throw new IllegalStateException("Framebuffer is no longer available");
        framebuffer.bind(setViewport);
    }


    private record BlendFunc(int GL_BLEND_SRC_RGB, int GL_BLEND_SRC_ALPHA, int GL_BLEND_DST_RGB, int GL_BLEND_DST_ALPHA) {
        private BlendFunc() {
            this(GL11C.glGetInteger(GL14C.GL_BLEND_SRC_RGB), GL11C.glGetInteger(GL14C.GL_BLEND_SRC_ALPHA), GL11C.glGetInteger(GL14C.GL_BLEND_DST_RGB), GL11C.glGetInteger(GL14C.GL_BLEND_DST_ALPHA));
        }
    }

}
