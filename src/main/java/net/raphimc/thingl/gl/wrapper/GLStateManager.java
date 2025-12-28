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
package net.raphimc.thingl.gl.wrapper;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.resource.framebuffer.Framebuffer;
import org.joml.primitives.Rectanglei;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL14C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

public class GLStateManager {

    public boolean getCapability(final int capability) {
        return ThinGL.glBackend().isEnabled(capability);
    }

    public void enable(final int capability) {
        this.setCapability(capability, true);
    }

    public void disable(final int capability) {
        this.setCapability(capability, false);
    }

    public void setCapability(final int capability, final boolean state) {
        if (state) {
            ThinGL.glBackend().enable(capability);
        } else {
            ThinGL.glBackend().disable(capability);
        }
    }

    public BlendFunc getBlendFunc() {
        return new BlendFunc(
                ThinGL.glBackend().getInteger(GL14C.GL_BLEND_SRC_RGB),
                ThinGL.glBackend().getInteger(GL14C.GL_BLEND_DST_RGB),
                ThinGL.glBackend().getInteger(GL14C.GL_BLEND_SRC_ALPHA),
                ThinGL.glBackend().getInteger(GL14C.GL_BLEND_DST_ALPHA)
        );
    }

    public void setBlendFunc(final BlendFunc blendFunc) {
        this.setBlendFunc(blendFunc.srcRGB, blendFunc.dstRGB, blendFunc.srcAlpha, blendFunc.dstAlpha);
    }

    public void setBlendFunc(final int src, final int dst) {
        this.setBlendFunc(src, dst, src, dst);
    }

    public void setBlendFunc(final int srcRGB, final int dstRGB, final int srcAlpha, final int dstAlpha) {
        if (srcRGB == srcAlpha && dstRGB == dstAlpha) {
            ThinGL.glBackend().blendFunc(srcRGB, dstRGB);
        } else {
            ThinGL.glBackend().blendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
        }
    }

    public int getDepthFunc() {
        return ThinGL.glBackend().getInteger(GL11C.GL_DEPTH_FUNC);
    }

    public void setDepthFunc(final int func) {
        ThinGL.glBackend().depthFunc(func);
    }

    public int getBlendEquation() {
        return ThinGL.glBackend().getInteger(GL14C.GL_BLEND_EQUATION);
    }

    public void setBlendEquation(final int mode) {
        ThinGL.glBackend().blendEquation(mode);
    }

    public ColorMask getColorMask() {
        final int[] colorMask = new int[4];
        ThinGL.glBackend().getIntegerv(GL11C.GL_COLOR_WRITEMASK, colorMask);
        return new ColorMask(colorMask[0] != GL11C.GL_FALSE,
                colorMask[1] != GL11C.GL_FALSE,
                colorMask[2] != GL11C.GL_FALSE,
                colorMask[3] != GL11C.GL_FALSE);
    }

    public void setColorMask(final ColorMask colorMask) {
        this.setColorMask(colorMask.red, colorMask.green, colorMask.blue, colorMask.alpha);
    }

    public void setColorMask(final boolean red, final boolean green, final boolean blue, final boolean alpha) {
        ThinGL.glBackend().colorMask(red, green, blue, alpha);
    }

    public boolean getDepthMask() {
        return ThinGL.glBackend().getBoolean(GL11C.GL_DEPTH_WRITEMASK);
    }

    public void setDepthMask(final boolean state) {
        ThinGL.glBackend().depthMask(state);
    }

    public StencilMask getStencilMask() {
        return new StencilMask(
                ThinGL.glBackend().getInteger(GL11C.GL_STENCIL_WRITEMASK),
                ThinGL.glBackend().getInteger(GL20C.GL_STENCIL_BACK_WRITEMASK)
        );
    }

    public void setStencilMask(final StencilMask stencilMask) {
        this.setStencilMask(stencilMask.front, stencilMask.back);
    }

    public void setStencilMask(final int mask) {
        this.setStencilMask(mask, mask);
    }

    public void setStencilMask(final int front, final int back) {
        if (front == back) {
            ThinGL.glBackend().stencilMask(front);
        } else {
            ThinGL.glBackend().stencilMaskSeparate(GL11C.GL_FRONT, front);
            ThinGL.glBackend().stencilMaskSeparate(GL11C.GL_BACK, back);
        }
    }

    public Scissor getScissor() {
        final int[] scissor = new int[4];
        ThinGL.glBackend().getIntegerv(GL11C.GL_SCISSOR_BOX, scissor);
        return new Scissor(scissor);
    }

    public void setScissor(final Rectanglei scissorRectangle) {
        this.setScissor(scissorRectangle.minX, scissorRectangle.minY, scissorRectangle.lengthX(), scissorRectangle.lengthY());
    }

    public void setScissor(final Scissor scissor) {
        this.setScissor(scissor.x, scissor.y, scissor.width, scissor.height);
    }

    public void setScissor(final int x, final int y, final int width, final int height) {
        ThinGL.glBackend().scissor(x, y, width, height);
    }

    public Viewport getViewport() {
        final int[] viewport = new int[4];
        ThinGL.glBackend().getIntegerv(GL11C.GL_VIEWPORT, viewport);
        return new Viewport(viewport);
    }

    public void setViewport(final Rectanglei viewportRectangle) {
        this.setViewport(viewportRectangle.minX, viewportRectangle.minY, viewportRectangle.lengthX(), viewportRectangle.lengthY());
    }

    public void setViewport(final Viewport viewport) {
        this.setViewport(viewport.x, viewport.y, viewport.width, viewport.height);
    }

    public void setViewport(final int x, final int y, final int width, final int height) {
        ThinGL.glBackend().viewport(x, y, width, height);
    }

    public int getCullFace() {
        return ThinGL.glBackend().getInteger(GL11C.GL_CULL_FACE_MODE);
    }

    public void setCullFace(final int mode) {
        ThinGL.glBackend().cullFace(mode);
    }

    public int getFrontFace() {
        return ThinGL.glBackend().getInteger(GL11C.GL_FRONT_FACE);
    }

    public void setFrontFace(final int dir) {
        ThinGL.glBackend().frontFace(dir);
    }

    public int getLogicOp() {
        return ThinGL.glBackend().getInteger(GL11C.GL_LOGIC_OP_MODE);
    }

    public void setLogicOp(final int op) {
        ThinGL.glBackend().logicOp(op);
    }

    public PolygonOffset getPolygonOffset() {
        return new PolygonOffset(
                ThinGL.glBackend().getFloat(GL11C.GL_POLYGON_OFFSET_FACTOR),
                ThinGL.glBackend().getFloat(GL11C.GL_POLYGON_OFFSET_UNITS)
        );
    }

    public void setPolygonOffset(final PolygonOffset polygonOffset) {
        this.setPolygonOffset(polygonOffset.factor, polygonOffset.units);
    }

    public void setPolygonOffset(final float factor, final float units) {
        ThinGL.glBackend().polygonOffset(factor, units);
    }

    public int getPixelStore(final int parameter) {
        return ThinGL.glBackend().getInteger(parameter);
    }

    public void setPixelStore(final int parameter, final int value) {
        ThinGL.glBackend().pixelStorei(parameter, value);
    }

    public int getProgram() {
        return ThinGL.glBackend().getInteger(GL20C.GL_CURRENT_PROGRAM);
    }

    public void setProgram(final int program) {
        ThinGL.glBackend().useProgram(program);
    }

    public int getVertexArray() {
        return ThinGL.glBackend().getInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
    }

    public void setVertexArray(final int vertexArray) {
        ThinGL.glBackend().bindVertexArray(vertexArray);
    }

    public Framebuffer getDrawFramebuffer() {
        return Framebuffer.fromGlIdUnsafe(ThinGL.glBackend().getInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING));
    }

    public void setDrawFramebuffer(final Framebuffer framebuffer) {
        ThinGL.glBackend().bindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, framebuffer.getGlId());
    }

    public record BlendFunc(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
    }

    public record ColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
    }

    public record StencilMask(int front, int back) {
    }

    public record Scissor(int x, int y, int width, int height) {

        public Scissor(final int[] scissorArray) {
            this(scissorArray[0], scissorArray[1], scissorArray[2], scissorArray[3]);
        }

        public int[] toArray() {
            return new int[]{this.x, this.y, this.width, this.height};
        }

    }

    public record Viewport(int x, int y, int width, int height) {

        public Viewport(final int[] viewportArray) {
            this(viewportArray[0], viewportArray[1], viewportArray[2], viewportArray[3]);
        }

        public int[] toArray() {
            return new int[]{this.x, this.y, this.width, this.height};
        }

    }

    public record PolygonOffset(float factor, float units) {
    }

}
