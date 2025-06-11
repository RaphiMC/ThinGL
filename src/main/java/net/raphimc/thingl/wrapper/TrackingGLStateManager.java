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

import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.raphimc.thingl.ThinGL;
import org.jetbrains.annotations.ApiStatus;

public class TrackingGLStateManager extends GLStateManager {

    private final Int2BooleanMap capabilities = new Int2BooleanOpenHashMap();
    private BlendFunc blendFunc;
    private Integer depthFunc;
    private ColorMask colorMask;
    private Boolean depthMask;
    private Scissor scissor;
    private Viewport viewport;
    private Integer logicOp;
    private PolygonOffset polygonOffset;
    private final Int2IntMap pixelStores = new Int2IntOpenHashMap();
    private Integer program;
    private Integer vertexArray;

    @ApiStatus.Internal
    public TrackingGLStateManager(final ThinGL thinGL) {
        super(thinGL);
    }

    @Override
    public boolean getCapability(final int capability) {
        if (this.capabilities.containsKey(capability)) {
            return this.capabilities.get(capability);
        } else {
            final boolean state = super.getCapability(capability);
            this.capabilities.put(capability, state);
            return state;
        }
    }

    @Override
    public void setCapability(final int capability, final boolean state) {
        if (this.getCapability(capability) != state) {
            this.capabilities.put(capability, state);
            super.setCapability(capability, state);
        }
    }

    public void clearCapabilitiesCache() {
        this.capabilities.clear();
    }

    @Override
    public BlendFunc getBlendFunc() {
        if (this.blendFunc != null) {
            return this.blendFunc;
        } else {
            final BlendFunc blendFunc = super.getBlendFunc();
            this.blendFunc = blendFunc;
            return blendFunc;
        }
    }

    @Override
    public void setBlendFunc(final int srcRGB, final int dstRGB, final int srcAlpha, final int dstAlpha) {
        final BlendFunc currentBlendFunc = this.getBlendFunc();
        if (currentBlendFunc.srcRGB() != srcRGB || currentBlendFunc.dstRGB() != dstRGB || currentBlendFunc.srcAlpha() != srcAlpha || currentBlendFunc.dstAlpha() != dstAlpha) {
            this.blendFunc = new BlendFunc(srcRGB, dstRGB, srcAlpha, dstAlpha);
            super.setBlendFunc(srcRGB, dstRGB, srcAlpha, dstAlpha);
        }
    }

    public void clearBlendFuncCache() {
        this.blendFunc = null;
    }

    @Override
    public int getDepthFunc() {
        if (this.depthFunc != null) {
            return this.depthFunc;
        } else {
            final int depthFunc = super.getDepthFunc();
            this.depthFunc = depthFunc;
            return depthFunc;
        }
    }

    @Override
    public void setDepthFunc(final int depthFunc) {
        if (this.getDepthFunc() != depthFunc) {
            this.depthFunc = depthFunc;
            super.setDepthFunc(depthFunc);
        }
    }

    public void clearDepthFuncCache() {
        this.depthFunc = null;
    }

    @Override
    public ColorMask getColorMask() {
        if (this.colorMask != null) {
            return this.colorMask;
        } else {
            final ColorMask colorMask = super.getColorMask();
            this.colorMask = colorMask;
            return colorMask;
        }
    }

    @Override
    public void setColorMask(final boolean red, final boolean green, final boolean blue, final boolean alpha) {
        final ColorMask currentColorMask = this.getColorMask();
        if (currentColorMask.red() != red || currentColorMask.green() != green || currentColorMask.blue() != blue || currentColorMask.alpha() != alpha) {
            this.colorMask = new ColorMask(red, green, blue, alpha);
            super.setColorMask(red, green, blue, alpha);
        }
    }

    public void clearColorMaskCache() {
        this.colorMask = null;
    }

    @Override
    public boolean getDepthMask() {
        if (this.depthMask != null) {
            return this.depthMask;
        } else {
            final boolean depthMask = super.getDepthMask();
            this.depthMask = depthMask;
            return depthMask;
        }
    }

    @Override
    public void setDepthMask(final boolean state) {
        if (this.getDepthMask() != state) {
            this.depthMask = state;
            super.setDepthMask(state);
        }
    }

    public void clearDepthMaskCache() {
        this.depthMask = null;
    }

    @Override
    public Scissor getScissor() {
        if (this.scissor != null) {
            return this.scissor;
        } else {
            final Scissor scissor = super.getScissor();
            this.scissor = scissor;
            return scissor;
        }
    }

    @Override
    public void setScissor(final int x, final int y, final int width, final int height) {
        final Scissor currentScissor = this.getScissor();
        if (currentScissor.x() != x || currentScissor.y() != y || currentScissor.width() != width || currentScissor.height() != height) {
            this.scissor = new Scissor(x, y, width, height);
            super.setScissor(x, y, width, height);
        }
    }

    public void clearScissorCache() {
        this.scissor = null;
    }

    @Override
    public Viewport getViewport() {
        if (this.viewport != null) {
            return this.viewport;
        } else {
            final Viewport viewport = super.getViewport();
            this.viewport = viewport;
            return viewport;
        }
    }

    @Override
    public void setViewport(final int x, final int y, final int width, final int height) {
        final Viewport currentViewport = this.getViewport();
        if (currentViewport.x() != x || currentViewport.y() != y || currentViewport.width() != width || currentViewport.height() != height) {
            this.viewport = new Viewport(x, y, width, height);
            super.setViewport(x, y, width, height);
        }
    }

    public void clearViewportCache() {
        this.viewport = null;
    }

    @Override
    public int getLogicOp() {
        if (this.logicOp != null) {
            return this.logicOp;
        } else {
            final int logicOp = super.getLogicOp();
            this.logicOp = logicOp;
            return logicOp;
        }
    }

    @Override
    public void setLogicOp(final int op) {
        if (this.getLogicOp() != op) {
            this.logicOp = op;
            super.setLogicOp(op);
        }
    }

    public void clearLogicOpCache() {
        this.logicOp = null;
    }

    @Override
    public PolygonOffset getPolygonOffset() {
        if (this.polygonOffset != null) {
            return this.polygonOffset;
        } else {
            final PolygonOffset polygonOffset = super.getPolygonOffset();
            this.polygonOffset = polygonOffset;
            return polygonOffset;
        }
    }

    @Override
    public void setPolygonOffset(final float factor, final float units) {
        final PolygonOffset currentPolygonOffset = this.getPolygonOffset();
        if (currentPolygonOffset.factor() != factor || currentPolygonOffset.units() != units) {
            this.polygonOffset = new PolygonOffset(factor, units);
            super.setPolygonOffset(factor, units);
        }
    }

    public void clearPolygonOffsetCache() {
        this.polygonOffset = null;
    }

    @Override
    public int getPixelStore(final int parameter) {
        if (this.pixelStores.containsKey(parameter)) {
            return this.pixelStores.get(parameter);
        } else {
            final int value = super.getPixelStore(parameter);
            this.pixelStores.put(parameter, value);
            return value;
        }
    }

    @Override
    public void setPixelStore(final int parameter, final int value) {
        if (this.getPixelStore(parameter) != value) {
            this.pixelStores.put(parameter, value);
            super.setPixelStore(parameter, value);
        }
    }

    public void clearPixelStoresCache() {
        this.pixelStores.clear();
    }

    @Override
    public int getProgram() {
        if (this.program != null) {
            return this.program;
        } else {
            final int program = super.getProgram();
            this.program = program;
            return program;
        }
    }

    @Override
    public void setProgram(final int program) {
        if (this.getProgram() != program) {
            this.program = program;
            super.setProgram(program);
        }
    }

    public void clearProgramCache() {
        this.program = null;
    }

    @Override
    public int getVertexArray() {
        if (this.vertexArray != null) {
            return this.vertexArray;
        } else {
            final int vertexArray = super.getVertexArray();
            this.vertexArray = vertexArray;
            return vertexArray;
        }
    }

    @Override
    public void setVertexArray(final int vertexArray) {
        if (this.getVertexArray() != vertexArray) {
            this.vertexArray = vertexArray;
            super.setVertexArray(vertexArray);
        }
    }

    public void clearVertexArrayCache() {
        this.vertexArray = null;
    }

}
