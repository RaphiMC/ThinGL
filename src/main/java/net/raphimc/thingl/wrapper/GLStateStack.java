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

import java.util.Stack;

public class GLStateStack {

    private final Stack<Int2BooleanMap> capabilitiesStack = new Stack<>();
    private final Stack<GLStateManager.BlendFunc> blendFuncStack = new Stack<>();
    private final IntStack depthFuncStack = new IntArrayList();
    private final IntStack blendEquationStack = new IntArrayList();
    private final Stack<GLStateManager.ColorMask> colorMaskStack = new Stack<>();
    private final BooleanStack depthMaskStack = new BooleanArrayList();
    private final Stack<GLStateManager.StencilMask> stencilMaskStack = new Stack<>();
    private final Stack<GLStateManager.Scissor> scissorStack = new Stack<>();
    private final Stack<GLStateManager.Viewport> viewportStack = new Stack<>();
    private final IntStack cullFaceStack = new IntArrayList();
    private final IntStack frontFaceStack = new IntArrayList();
    private final IntStack logicOpStack = new IntArrayList();
    private final Stack<GLStateManager.PolygonOffset> polygonOffsetStack = new Stack<>();
    private final Stack<Int2IntMap> pixelStoresStack = new Stack<>();
    private final Stack<Framebuffer> framebufferStack = new Stack<>();
    private final IntStack programStack = new IntArrayList();
    private final IntStack vertexArrayStack = new IntArrayList();

    public GLStateStack() {
        ThinGL.get().addFinishFrameCallback(() -> {
            if (!this.capabilitiesStack.isEmpty()) {
                while (!this.capabilitiesStack.isEmpty()) this.pop();
                ThinGL.LOGGER.warn("GLStateStack capabilities stack was not empty at the end of the frame!");
            }
            if (!this.blendFuncStack.isEmpty()) {
                while (!this.blendFuncStack.isEmpty()) this.popBlendFunc();
                ThinGL.LOGGER.warn("GLStateStack blend func stack was not empty at the end of the frame!");
            }
            if (!this.depthFuncStack.isEmpty()) {
                while (!this.depthFuncStack.isEmpty()) this.popDepthFunc();
                ThinGL.LOGGER.warn("GLStateStack depth func stack was not empty at the end of the frame!");
            }
            if (!this.blendEquationStack.isEmpty()) {
                while (!this.blendEquationStack.isEmpty()) this.popBlendEquation();
                ThinGL.LOGGER.warn("GLStateStack blend equation stack was not empty at the end of the frame!");
            }
            if (!this.colorMaskStack.isEmpty()) {
                while (!this.colorMaskStack.isEmpty()) this.popColorMask();
                ThinGL.LOGGER.warn("GLStateStack color mask stack was not empty at the end of the frame!");
            }
            if (!this.depthMaskStack.isEmpty()) {
                while (!this.depthMaskStack.isEmpty()) this.popDepthMask();
                ThinGL.LOGGER.warn("GLStateStack depth mask stack was not empty at the end of the frame!");
            }
            if (!this.stencilMaskStack.isEmpty()) {
                while (!this.stencilMaskStack.isEmpty()) this.popStencilMask();
                ThinGL.LOGGER.warn("GLStateStack stencil mask stack was not empty at the end of the frame!");
            }
            if (!this.scissorStack.isEmpty()) {
                while (!this.scissorStack.isEmpty()) this.popScissor();
                ThinGL.LOGGER.warn("GLStateStack scissor stack was not empty at the end of the frame!");
            }
            if (!this.viewportStack.isEmpty()) {
                while (!this.viewportStack.isEmpty()) this.popViewport();
                ThinGL.LOGGER.warn("GLStateStack viewport stack was not empty at the end of the frame!");
            }
            if (!this.cullFaceStack.isEmpty()) {
                while (!this.cullFaceStack.isEmpty()) this.popCullFace();
                ThinGL.LOGGER.warn("GLStateStack cull face stack was not empty at the end of the frame!");
            }
            if (!this.frontFaceStack.isEmpty()) {
                while (!this.frontFaceStack.isEmpty()) this.popFrontFace();
                ThinGL.LOGGER.warn("GLStateStack front face stack was not empty at the end of the frame!");
            }
            if (!this.logicOpStack.isEmpty()) {
                while (!this.logicOpStack.isEmpty()) this.popLogicOp();
                ThinGL.LOGGER.warn("GLStateStack logic op stack was not empty at the end of the frame!");
            }
            if (!this.polygonOffsetStack.isEmpty()) {
                while (!this.polygonOffsetStack.isEmpty()) this.popPolygonOffset();
                ThinGL.LOGGER.warn("GLStateStack polygon offset stack was not empty at the end of the frame!");
            }
            if (!this.pixelStoresStack.isEmpty()) {
                while (!this.pixelStoresStack.isEmpty()) this.popPixelStore();
                ThinGL.LOGGER.warn("GLStateStack pixel stores stack was not empty at the end of the frame!");
            }
            if (!this.framebufferStack.isEmpty()) {
                while (!this.framebufferStack.isEmpty()) this.popFramebuffer();
                ThinGL.LOGGER.warn("GLStateStack framebuffer stack was not empty at the end of the frame!");
            }
            if (!this.programStack.isEmpty()) {
                while (!this.programStack.isEmpty()) this.popProgram();
                ThinGL.LOGGER.warn("GLStateStack program stack was not empty at the end of the frame!");
            }
            if (!this.vertexArrayStack.isEmpty()) {
                while (!this.vertexArrayStack.isEmpty()) this.popVertexArray();
                ThinGL.LOGGER.warn("GLStateStack vertex array stack was not empty at the end of the frame!");
            }
        });
    }

    public void push() {
        this.capabilitiesStack.push(new Int2BooleanOpenHashMap(6));
    }

    public void enable(final int capability) {
        this.set(capability, true);
    }

    public void disable(final int capability) {
        this.set(capability, false);
    }

    private void set(final int capability, final boolean state) {
        final boolean currentState = ThinGL.glStateManager().getCapability(capability);
        if (currentState != state) {
            this.capabilitiesStack.peek().put(capability, currentState);
            ThinGL.glStateManager().setCapability(capability, state);
        }
    }

    public void pop() {
        final Int2BooleanMap capabilities = this.capabilitiesStack.pop();
        for (Int2BooleanMap.Entry entry : capabilities.int2BooleanEntrySet()) {
            ThinGL.glStateManager().setCapability(entry.getIntKey(), entry.getBooleanValue());
        }
    }

    public void pushBlendFunc() {
        this.blendFuncStack.push(ThinGL.glStateManager().getBlendFunc());
    }

    public void popBlendFunc() {
        final GLStateManager.BlendFunc blendFunc = this.blendFuncStack.pop();
        ThinGL.glStateManager().setBlendFunc(blendFunc.srcRGB(), blendFunc.dstRGB(), blendFunc.srcAlpha(), blendFunc.dstAlpha());
    }

    public void pushDepthFunc() {
        this.depthFuncStack.push(ThinGL.glStateManager().getDepthFunc());
    }

    public void popDepthFunc() {
        ThinGL.glStateManager().setDepthFunc(this.depthFuncStack.popInt());
    }

    public void pushBlendEquation() {
        this.blendEquationStack.push(ThinGL.glStateManager().getBlendEquation());
    }

    public void popBlendEquation() {
        ThinGL.glStateManager().setBlendEquation(this.blendEquationStack.popInt());
    }

    public void pushColorMask() {
        this.colorMaskStack.push(ThinGL.glStateManager().getColorMask());
    }

    public void popColorMask() {
        final GLStateManager.ColorMask colorMask = this.colorMaskStack.pop();
        ThinGL.glStateManager().setColorMask(colorMask.red(), colorMask.green(), colorMask.blue(), colorMask.alpha());
    }

    public void pushDepthMask() {
        this.depthMaskStack.push(ThinGL.glStateManager().getDepthMask());
    }

    public void popDepthMask() {
        ThinGL.glStateManager().setDepthMask(this.depthMaskStack.popBoolean());
    }

    public void pushStencilMask() {
        this.stencilMaskStack.push(ThinGL.glStateManager().getStencilMask());
    }

    public void popStencilMask() {
        final GLStateManager.StencilMask stencilMask = this.stencilMaskStack.pop();
        ThinGL.glStateManager().setStencilMask(stencilMask.front(), stencilMask.back());
    }

    public void pushScissor() {
        this.scissorStack.push(ThinGL.glStateManager().getScissor());
    }

    public void popScissor() {
        final GLStateManager.Scissor scissor = this.scissorStack.pop();
        ThinGL.glStateManager().setScissor(scissor.x(), scissor.y(), scissor.width(), scissor.height());
    }

    public void pushViewport() {
        this.viewportStack.push(ThinGL.glStateManager().getViewport());
    }

    public void popViewport() {
        final GLStateManager.Viewport viewport = this.viewportStack.pop();
        ThinGL.glStateManager().setViewport(viewport.x(), viewport.y(), viewport.width(), viewport.height());
    }

    public void pushCullFace() {
        this.cullFaceStack.push(ThinGL.glStateManager().getCullFace());
    }

    public void popCullFace() {
        ThinGL.glStateManager().setCullFace(this.cullFaceStack.popInt());
    }

    public void pushFrontFace() {
        this.frontFaceStack.push(ThinGL.glStateManager().getFrontFace());
    }

    public void popFrontFace() {
        ThinGL.glStateManager().setFrontFace(this.frontFaceStack.popInt());
    }

    public void pushLogicOp() {
        this.logicOpStack.push(ThinGL.glStateManager().getLogicOp());
    }

    public void popLogicOp() {
        ThinGL.glStateManager().setLogicOp(this.logicOpStack.popInt());
    }

    public void pushPolygonOffset() {
        this.polygonOffsetStack.push(ThinGL.glStateManager().getPolygonOffset());
    }

    public void popPolygonOffset() {
        final GLStateManager.PolygonOffset polygonOffset = this.polygonOffsetStack.pop();
        ThinGL.glStateManager().setPolygonOffset(polygonOffset.factor(), polygonOffset.units());
    }

    public void pushPixelStore() {
        this.pixelStoresStack.push(new Int2IntOpenHashMap(6));
    }

    public void pixelStore(final int parameter, final int value) {
        final int currentValue = ThinGL.glStateManager().getPixelStore(parameter);
        if (currentValue != value) {
            this.pixelStoresStack.peek().put(parameter, currentValue);
            ThinGL.glStateManager().setPixelStore(parameter, value);
        }
    }

    public void popPixelStore() {
        final Int2IntMap pixelStores = this.pixelStoresStack.pop();
        for (Int2IntMap.Entry entry : pixelStores.int2IntEntrySet()) {
            ThinGL.glStateManager().setPixelStore(entry.getIntKey(), entry.getIntValue());
        }
    }

    public void pushFramebuffer() {
        this.framebufferStack.push(ThinGL.applicationInterface().getCurrentFramebuffer());
    }

    public void popFramebuffer() {
        popFramebuffer(false);
    }

    public void popFramebuffer(final boolean setViewport) {
        final Framebuffer framebuffer = this.framebufferStack.pop();
        if (!framebuffer.isAllocated()) throw new IllegalStateException("Framebuffer is no longer available");
        framebuffer.bind(setViewport);
    }

    public void pushProgram() {
        this.programStack.push(ThinGL.glStateManager().getProgram());
    }

    public void popProgram() {
        ThinGL.glStateManager().setProgram(this.programStack.popInt());
    }

    public void pushVertexArray() {
        this.vertexArrayStack.push(ThinGL.glStateManager().getVertexArray());
    }

    public void popVertexArray() {
        ThinGL.glStateManager().setVertexArray(this.vertexArrayStack.popInt());
    }

}
