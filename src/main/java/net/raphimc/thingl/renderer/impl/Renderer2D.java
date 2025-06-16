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

package net.raphimc.thingl.renderer.impl;

import earcut4j.Earcut;
import net.lenni0451.commons.color.Color;
import net.lenni0451.commons.math.shapes.triangle.TriangleD;
import net.lenni0451.commons.math.shapes.triangle.TriangleF;
import net.lenni0451.commons.math.shapes.triangle.TriangleI;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.drawbuilder.BuiltinDrawBatches;
import net.raphimc.thingl.drawbuilder.DrawBatch;
import net.raphimc.thingl.drawbuilder.DrawMode;
import net.raphimc.thingl.drawbuilder.builder.BufferBuilder;
import net.raphimc.thingl.drawbuilder.databuilder.holder.IndexDataHolder;
import net.raphimc.thingl.drawbuilder.databuilder.holder.VertexDataHolder;
import net.raphimc.thingl.renderer.Primitives;
import net.raphimc.thingl.renderer.Renderer;
import net.raphimc.thingl.util.CacheUtil;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.primitives.*;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.par.*;

import java.nio.IntBuffer;
import java.util.List;
import java.util.function.IntFunction;

public class Renderer2D extends Renderer {

    public static final int OUTLINE_STYLE_OUTER_BIT = 1 << 0;
    public static final int OUTLINE_STYLE_INNER_BIT = 1 << 1;

    protected final IntFunction<DrawBatch> texturedQuad = CacheUtil.memoizeInt(textureId -> new DrawBatch(() -> ThinGL.programs().getPositionTexture(), DrawMode.QUADS, BuiltinDrawBatches.POSITION_TEXTURE_LAYOUT, () -> {
        ThinGL.glStateStack().push();
        ThinGL.glStateStack().enable(GL11C.GL_BLEND);
        ThinGL.programs().getPositionTexture().setUniformSampler("u_Texture", textureId);
    }, () -> ThinGL.glStateStack().pop()));

    protected final IntFunction<DrawBatch> coloredTexturedQuad = CacheUtil.memoizeInt(textureId -> new DrawBatch(() -> ThinGL.programs().getPositionColorTexture(), DrawMode.QUADS, BuiltinDrawBatches.POSITION_COLOR_TEXTURE_LAYOUT, () -> {
        ThinGL.glStateStack().push();
        ThinGL.glStateStack().enable(GL11C.GL_BLEND);
        ThinGL.programs().getPositionColorTexture().setUniformSampler("u_Texture", textureId);
    }, () -> ThinGL.glStateStack().pop()));

    protected final IntFunction<DrawBatch> colorizedTexturedQuad = CacheUtil.memoizeInt(textureId -> new DrawBatch(() -> ThinGL.programs().getColorizedTexture(), DrawMode.QUADS, BuiltinDrawBatches.POSITION_COLOR_TEXTURE_LAYOUT, () -> {
        ThinGL.glStateStack().push();
        ThinGL.glStateStack().enable(GL11C.GL_BLEND);
        ThinGL.programs().getColorizedTexture().setUniformSampler("u_Texture", textureId);
    }, () -> ThinGL.glStateStack().pop()));

    public void filledRectangle(final Matrix4f positionMatrix, final Rectangled rectangle, final Color color) {
        this.filledRectangle(positionMatrix, rectangle, color, color, color, color);
    }

    public void filledRectangle(final Matrix4f positionMatrix, final Rectangled rectangle, final Color cbl, final Color cbr, final Color ctr, final Color ctl) {
        this.filledRectangle(positionMatrix, (float) rectangle.minX, (float) rectangle.minY, (float) rectangle.maxX, (float) rectangle.maxY, cbl, cbr, ctr, ctl);
    }

    public void filledRectangle(final Matrix4f positionMatrix, final Rectanglef rectangle, final Color color) {
        this.filledRectangle(positionMatrix, rectangle, color, color, color, color);
    }

    public void filledRectangle(final Matrix4f positionMatrix, final Rectanglef rectangle, final Color cbl, final Color cbr, final Color ctr, final Color ctl) {
        this.filledRectangle(positionMatrix, rectangle.minX, rectangle.minY, rectangle.maxX, rectangle.maxY, cbl, cbr, ctr, ctl);
    }

    public void filledRectangle(final Matrix4f positionMatrix, final Rectanglei rectangle, final Color color) {
        this.filledRectangle(positionMatrix, rectangle, color, color, color, color);
    }

    public void filledRectangle(final Matrix4f positionMatrix, final Rectanglei rectangle, final Color cbl, final Color cbr, final Color ctr, final Color ctl) {
        this.filledRectangle(positionMatrix, rectangle.minX, rectangle.minY, rectangle.maxX, rectangle.maxY, cbl, cbr, ctr, ctl);
    }

    public void filledRectangle(final Matrix4f positionMatrix, final float xtl, final float ytl, final float xbr, final float ybr, final Color color) {
        this.filledRectangle(positionMatrix, xtl, ytl, xbr, ybr, color, color, color, color);
    }

    public void filledRectangle(final Matrix4f positionMatrix, final float xtl, final float ytl, final float xbr, final float ybr, final Color cbl, final Color cbr, final Color ctr, final Color ctl) {
        Primitives.filledRectangle(positionMatrix, this.targetMultiDrawBatchDataHolder, xtl, ytl, xbr, ybr, cbl.toABGR(), cbr.toABGR(), ctr.toABGR(), ctl.toABGR());
        this.drawIfNotBuffering();
    }

    public void outlinedRectangle(final Matrix4f positionMatrix, final Rectangled rectangle, final Color color, final float width) {
        this.outlinedRectangle(positionMatrix, rectangle, color, width, OUTLINE_STYLE_OUTER_BIT);
    }

    public void outlinedRectangle(final Matrix4f positionMatrix, final Rectangled rectangle, final Color color, final float width, final int styleFlags) {
        this.outlinedRectangle(positionMatrix, (float) rectangle.minX, (float) rectangle.minY, (float) rectangle.maxX, (float) rectangle.maxY, color, width, styleFlags);
    }

    public void outlinedRectangle(final Matrix4f positionMatrix, final Rectanglef rectangle, final Color color, final float width) {
        this.outlinedRectangle(positionMatrix, rectangle, color, width, OUTLINE_STYLE_OUTER_BIT);
    }

    public void outlinedRectangle(final Matrix4f positionMatrix, final Rectanglef rectangle, final Color color, final float width, final int styleFlags) {
        this.outlinedRectangle(positionMatrix, rectangle.minX, rectangle.minY, rectangle.maxX, rectangle.maxY, color, width, styleFlags);
    }

    public void outlinedRectangle(final Matrix4f positionMatrix, final Rectanglei rectangle, final Color color, final float width) {
        this.outlinedRectangle(positionMatrix, rectangle, color, width, OUTLINE_STYLE_OUTER_BIT);
    }

    public void outlinedRectangle(final Matrix4f positionMatrix, final Rectanglei rectangle, final Color color, final float width, final int styleFlags) {
        this.outlinedRectangle(positionMatrix, rectangle.minX, rectangle.minY, rectangle.maxX, rectangle.maxY, color, width, styleFlags);
    }

    public void outlinedRectangle(final Matrix4f positionMatrix, final float xtl, final float ytl, final float xbr, final float ybr, final Color color, final float width) {
        this.outlinedRectangle(positionMatrix, xtl, ytl, xbr, ybr, color, width, OUTLINE_STYLE_OUTER_BIT);
    }

    public void outlinedRectangle(final Matrix4f positionMatrix, final float xtl, final float ytl, final float xbr, final float ybr, final Color color, final float width, final int styleFlags) {
        final int abgrColor = color.toABGR();
        if ((styleFlags & OUTLINE_STYLE_OUTER_BIT) != 0) {
            Primitives.filledRectangle(positionMatrix, this.targetMultiDrawBatchDataHolder, xtl - width, ytl - width, xbr + width, ytl, abgrColor); // top line
            Primitives.filledRectangle(positionMatrix, this.targetMultiDrawBatchDataHolder, xtl - width, ybr, xbr + width, ybr + width, abgrColor); // bottom line
            Primitives.filledRectangle(positionMatrix, this.targetMultiDrawBatchDataHolder, xtl - width, ytl, xtl, ybr, abgrColor); // left line
            Primitives.filledRectangle(positionMatrix, this.targetMultiDrawBatchDataHolder, xbr, ytl, xbr + width, ybr, abgrColor); // right line
        }
        if ((styleFlags & OUTLINE_STYLE_INNER_BIT) != 0) {
            Primitives.filledRectangle(positionMatrix, this.targetMultiDrawBatchDataHolder, xtl, ytl, xbr, ytl + width, abgrColor); // top line
            Primitives.filledRectangle(positionMatrix, this.targetMultiDrawBatchDataHolder, xtl, ybr - width, xbr, ybr, abgrColor); // bottom line
            Primitives.filledRectangle(positionMatrix, this.targetMultiDrawBatchDataHolder, xtl, ytl + width, xtl + width, ybr - width, abgrColor); // left line
            Primitives.filledRectangle(positionMatrix, this.targetMultiDrawBatchDataHolder, xbr - width, ytl + width, xbr, ybr - width, abgrColor); // right line
        }
        this.drawIfNotBuffering();
    }

    public void filledRoundedRectangle(final Matrix4f positionMatrix, final Rectangled rectangle, final float radius, final Color color) {
        this.filledRoundedRectangle(positionMatrix, rectangle, radius, radius, radius, radius, color);
    }

    public void filledRoundedRectangle(final Matrix4f positionMatrix, final Rectangled rectangle, final float rbl, final float rbr, final float rtr, final float rtl, final Color color) {
        this.filledRoundedRectangle(positionMatrix, (float) rectangle.minX, (float) rectangle.minY, (float) rectangle.maxX, (float) rectangle.maxY, rbl, rbr, rtr, rtl, color);
    }

    public void filledRoundedRectangle(final Matrix4f positionMatrix, final Rectanglef rectangle, final float radius, final Color color) {
        this.filledRoundedRectangle(positionMatrix, rectangle, radius, radius, radius, radius, color);
    }

    public void filledRoundedRectangle(final Matrix4f positionMatrix, final Rectanglef rectangle, final float rbl, final float rbr, final float rtr, final float rtl, final Color color) {
        this.filledRoundedRectangle(positionMatrix, rectangle.minX, rectangle.minY, rectangle.maxX, rectangle.maxY, rbl, rbr, rtr, rtl, color);
    }

    public void filledRoundedRectangle(final Matrix4f positionMatrix, final Rectanglei rectangle, final float radius, final Color color) {
        this.filledRoundedRectangle(positionMatrix, rectangle, radius, radius, radius, radius, color);
    }

    public void filledRoundedRectangle(final Matrix4f positionMatrix, final Rectanglei rectangle, final float rbl, final float rbr, final float rtr, final float rtl, final Color color) {
        this.filledRoundedRectangle(positionMatrix, rectangle.minX, rectangle.minY, rectangle.maxX, rectangle.maxY, rbl, rbr, rtr, rtl, color);
    }

    public void filledRoundedRectangle(final Matrix4f positionMatrix, final float xtl, final float ytl, final float xbr, final float ybr, final float radius, final Color color) {
        this.filledRoundedRectangle(positionMatrix, xtl, ytl, xbr, ybr, radius, radius, radius, radius, color);
    }

    public void filledRoundedRectangle(final Matrix4f positionMatrix, final float xtl, final float ytl, final float xbr, final float ybr, final float rbl, final float rbr, final float rtr, final float rtl, final Color color) {
        final VertexDataHolder vertexDataHolder = this.targetMultiDrawBatchDataHolder.getVertexDataHolder(BuiltinDrawBatches.COLORED_TRIANGLE_FAN);
        final int abgrColor = color.toABGR();

        vertexDataHolder.putVector3f(positionMatrix, (xtl + xbr) / 2F, (ytl + ybr) / 2F, 0F).putColor(abgrColor).endVertex();
        Primitives._filledCircle(positionMatrix, vertexDataHolder, xtl + rtl, ytl + rtl, 0F, rtl, 270, 360, abgrColor);
        Primitives._filledCircle(positionMatrix, vertexDataHolder, xtl + rbl, ybr - rbl, 0F, rbl, 180, 270, abgrColor);
        Primitives._filledCircle(positionMatrix, vertexDataHolder, xbr - rbr, ybr - rbr, 0F, rbr, 90, 180, abgrColor);
        Primitives._filledCircle(positionMatrix, vertexDataHolder, xbr - rtr, ytl + rtr, 0F, rtr, 0, 90, abgrColor);
        vertexDataHolder.putVector3f(positionMatrix, xtl + rtl, ytl, 0F).putColor(abgrColor).endVertex();
        vertexDataHolder.endConnectedPrimitive();

        this.drawIfNotBuffering();
    }

    public void outlinedRoundedRectangle(final Matrix4f positionMatrix, final Rectangled rectangle, final float radius, final Color color, final float width) {
        this.outlinedRoundedRectangle(positionMatrix, rectangle, radius, color, width, OUTLINE_STYLE_OUTER_BIT);
    }

    public void outlinedRoundedRectangle(final Matrix4f positionMatrix, final Rectangled rectangle, final float radius, final Color color, final float width, final int styleFlags) {
        this.outlinedRoundedRectangle(positionMatrix, rectangle, radius, radius, radius, radius, color, width, styleFlags);
    }

    public void outlinedRoundedRectangle(final Matrix4f positionMatrix, final Rectangled rectangle, final float rbl, final float rbr, final float rtr, final float rtl, final Color color, final float width, final int styleFlags) {
        this.outlinedRoundedRectangle(positionMatrix, (float) rectangle.minX, (float) rectangle.minY, (float) rectangle.maxX, (float) rectangle.maxY, rbl, rbr, rtr, rtl, color, width, styleFlags);
    }

    public void outlinedRoundedRectangle(final Matrix4f positionMatrix, final Rectanglef rectangle, final float radius, final Color color, final float width) {
        this.outlinedRoundedRectangle(positionMatrix, rectangle, radius, color, width, OUTLINE_STYLE_OUTER_BIT);
    }

    public void outlinedRoundedRectangle(final Matrix4f positionMatrix, final Rectanglef rectangle, final float radius, final Color color, final float width, final int styleFlags) {
        this.outlinedRoundedRectangle(positionMatrix, rectangle, radius, radius, radius, radius, color, width, styleFlags);
    }

    public void outlinedRoundedRectangle(final Matrix4f positionMatrix, final Rectanglef rectangle, final float rbl, final float rbr, final float rtr, final float rtl, final Color color, final float width, final int styleFlags) {
        this.outlinedRoundedRectangle(positionMatrix, rectangle.minX, rectangle.minY, rectangle.maxX, rectangle.maxY, rbl, rbr, rtr, rtl, color, width, styleFlags);
    }

    public void outlinedRoundedRectangle(final Matrix4f positionMatrix, final Rectanglei rectangle, final float radius, final Color color, final float width) {
        this.outlinedRoundedRectangle(positionMatrix, rectangle, radius, color, width, OUTLINE_STYLE_OUTER_BIT);
    }

    public void outlinedRoundedRectangle(final Matrix4f positionMatrix, final Rectanglei rectangle, final float radius, final Color color, final float width, final int styleFlags) {
        this.outlinedRoundedRectangle(positionMatrix, rectangle, radius, radius, radius, radius, color, width, styleFlags);
    }

    public void outlinedRoundedRectangle(final Matrix4f positionMatrix, final Rectanglei rectangle, final float rbl, final float rbr, final float rtr, final float rtl, final Color color, final float width, final int styleFlags) {
        this.outlinedRoundedRectangle(positionMatrix, rectangle.minX, rectangle.minY, rectangle.maxX, rectangle.maxY, rbl, rbr, rtr, rtl, color, width, styleFlags);
    }

    public void outlinedRoundedRectangle(final Matrix4f positionMatrix, final float xtl, final float ytl, final float xbr, final float ybr, final float radius, final Color color, final float width) {
        this.outlinedRoundedRectangle(positionMatrix, xtl, ytl, xbr, ybr, radius, color, width, OUTLINE_STYLE_OUTER_BIT);
    }

    public void outlinedRoundedRectangle(final Matrix4f positionMatrix, final float xtl, final float ytl, final float xbr, final float ybr, final float radius, final Color color, final float width, final int styleFlags) {
        this.outlinedRoundedRectangle(positionMatrix, xtl, ytl, xbr, ybr, radius, radius, radius, radius, color, width, styleFlags);
    }

    public void outlinedRoundedRectangle(final Matrix4f positionMatrix, final float xtl, final float ytl, final float xbr, final float ybr, final float rbl, final float rbr, final float rtr, final float rtl, final Color color, final float width, final int styleFlags) {
        final VertexDataHolder vertexDataHolder = this.targetMultiDrawBatchDataHolder.getVertexDataHolder(BuiltinDrawBatches.COLORED_TRIANGLE_STRIP);
        final int abgrColor = color.toABGR();

        if ((styleFlags & OUTLINE_STYLE_OUTER_BIT) != 0) {
            Primitives._outlinedCircle(positionMatrix, vertexDataHolder, xtl + rtl, ytl + rtl, 0F, rtl + width / 2F, width, 270, 360, abgrColor);
            Primitives._outlinedCircle(positionMatrix, vertexDataHolder, xtl + rbl, ybr - rbl, 0F, rbl + width / 2F, width, 180, 270, abgrColor);
            Primitives._outlinedCircle(positionMatrix, vertexDataHolder, xbr - rbr, ybr - rbr, 0F, rbr + width / 2F, width, 90, 180, abgrColor);
            Primitives._outlinedCircle(positionMatrix, vertexDataHolder, xbr - rtr, ytl + rtr, 0F, rtr + width / 2F, width, 0, 90, abgrColor);
            vertexDataHolder.putVector3f(positionMatrix, xtl + rtl, ytl, 0F).putColor(abgrColor).endVertex();
            vertexDataHolder.putVector3f(positionMatrix, xtl + rtl, ytl - width, 0F).putColor(abgrColor).endVertex();
            vertexDataHolder.endConnectedPrimitive();
        }
        if ((styleFlags & OUTLINE_STYLE_INNER_BIT) != 0) {
            Primitives._outlinedCircle(positionMatrix, vertexDataHolder, xtl + rtl, ytl + rtl, 0F, rtl - width / 2F, width, 270, 360, abgrColor);
            Primitives._outlinedCircle(positionMatrix, vertexDataHolder, xtl + rbl, ybr - rbl, 0F, rbl - width / 2F, width, 180, 270, abgrColor);
            Primitives._outlinedCircle(positionMatrix, vertexDataHolder, xbr - rbr, ybr - rbr, 0F, rbr - width / 2F, width, 90, 180, abgrColor);
            Primitives._outlinedCircle(positionMatrix, vertexDataHolder, xbr - rtr, ytl + rtr, 0F, rtr - width / 2F, width, 0, 90, abgrColor);
            vertexDataHolder.putVector3f(positionMatrix, xtl + rtl, ytl + width, 0F).putColor(abgrColor).endVertex();
            vertexDataHolder.putVector3f(positionMatrix, xtl + rtl, ytl, 0F).putColor(abgrColor).endVertex();
            vertexDataHolder.endConnectedPrimitive();
        }

        this.drawIfNotBuffering();
    }

    public void filledTriangle(final Matrix4f positionMatrix, final TriangleD triangle, final Color color) {
        this.filledTriangle(positionMatrix, triangle, color, color, color);
    }

    public void filledTriangle(final Matrix4f positionMatrix, final TriangleD triangle, final Color cl, final Color cm, final Color cr) {
        this.filledTriangle(positionMatrix, (float) triangle.getX1(), (float) triangle.getY1(), (float) triangle.getX2(), (float) triangle.getY2(), (float) triangle.getX3(), (float) triangle.getY3(), cl, cm, cr);
    }

    public void filledTriangle(final Matrix4f positionMatrix, final TriangleF triangle, final Color color) {
        this.filledTriangle(positionMatrix, triangle, color, color, color);
    }

    public void filledTriangle(final Matrix4f positionMatrix, final TriangleF triangle, final Color cl, final Color cm, final Color cr) {
        this.filledTriangle(positionMatrix, triangle.getX1(), triangle.getY1(), triangle.getX2(), triangle.getY2(), triangle.getX3(), triangle.getY3(), cl, cm, cr);
    }

    public void filledTriangle(final Matrix4f positionMatrix, final TriangleI triangle, final Color color) {
        this.filledTriangle(positionMatrix, triangle, color, color, color);
    }

    public void filledTriangle(final Matrix4f positionMatrix, final TriangleI triangle, final Color cl, final Color cm, final Color cr) {
        this.filledTriangle(positionMatrix, triangle.getX1(), triangle.getY1(), triangle.getX2(), triangle.getY2(), triangle.getX3(), triangle.getY3(), cl, cm, cr);
    }

    public void filledTriangle(final Matrix4f positionMatrix, final float xl, final float yl, final float xm, final float ym, final float xr, final float yr, final Color color) {
        this.filledTriangle(positionMatrix, xl, yl, xm, ym, xr, yr, color, color, color);
    }

    public void filledTriangle(final Matrix4f positionMatrix, final float xl, final float yl, final float xm, final float ym, final float xr, final float yr, final Color cl, final Color cm, final Color cr) {
        Primitives.filledTriangle(positionMatrix, this.targetMultiDrawBatchDataHolder, xl, yl, xm, ym, xr, yr, cl.toABGR(), cm.toABGR(), cr.toABGR());
        this.drawIfNotBuffering();
    }

    public void filledCircle(final Matrix4f positionMatrix, final Circled circle, final Color color) {
        this.filledCircle(positionMatrix, (float) circle.x, (float) circle.y, (float) circle.r, color);
    }

    public void filledCircle(final Matrix4f positionMatrix, final Circlef circle, final Color color) {
        this.filledCircle(positionMatrix, circle.x, circle.y, circle.r, color);
    }

    public void filledCircle(final Matrix4f positionMatrix, final float x, final float y, final float radius, final Color color) {
        Primitives.filledCircle(positionMatrix, this.targetMultiDrawBatchDataHolder, x, y, radius, color.toABGR());
        this.drawIfNotBuffering();
    }

    public void filledCircle(final Matrix4f positionMatrix, final Circled circle, final float degStart, final float degEnd, final Color color) {
        this.filledCircle(positionMatrix, (float) circle.x, (float) circle.y, (float) circle.r, degStart, degEnd, color);
    }

    public void filledCircle(final Matrix4f positionMatrix, final Circlef circle, final float degStart, final float degEnd, final Color color) {
        this.filledCircle(positionMatrix, circle.x, circle.y, circle.r, degStart, degEnd, color);
    }

    public void filledCircle(final Matrix4f positionMatrix, final float x, final float y, final float radius, final float degStart, final float degEnd, final Color color) {
        Primitives.filledCircle(positionMatrix, this.targetMultiDrawBatchDataHolder, x, y, radius, degStart, degEnd, color.toABGR());
        this.drawIfNotBuffering();
    }

    public void outlinedCircle(final Matrix4f positionMatrix, final Circled circle, final Color color, final float width) {
        this.outlinedCircle(positionMatrix, circle, color, width, OUTLINE_STYLE_OUTER_BIT);
    }

    public void outlinedCircle(final Matrix4f positionMatrix, final Circled circle, final Color color, final float width, final int styleFlags) {
        this.outlinedCircle(positionMatrix, (float) circle.x, (float) circle.y, (float) circle.r, color, width, styleFlags);
    }

    public void outlinedCircle(final Matrix4f positionMatrix, final Circlef circle, final Color color, final float width) {
        this.outlinedCircle(positionMatrix, circle, color, width, OUTLINE_STYLE_OUTER_BIT);
    }

    public void outlinedCircle(final Matrix4f positionMatrix, final Circlef circle, final Color color, final float width, final int styleFlags) {
        this.outlinedCircle(positionMatrix, circle.x, circle.y, circle.r, color, width, styleFlags);
    }

    public void outlinedCircle(final Matrix4f positionMatrix, final float x, final float y, final float radius, final Color color, final float width) {
        this.outlinedCircle(positionMatrix, x, y, radius, color, width, OUTLINE_STYLE_OUTER_BIT);
    }

    public void outlinedCircle(final Matrix4f positionMatrix, final float x, final float y, final float radius, final Color color, final float width, final int styleFlags) {
        if ((styleFlags & OUTLINE_STYLE_OUTER_BIT) != 0) {
            Primitives.outlinedCircle(positionMatrix, this.targetMultiDrawBatchDataHolder, x, y, radius + width / 2F, width, color.toABGR());
        }
        if ((styleFlags & OUTLINE_STYLE_INNER_BIT) != 0) {
            Primitives.outlinedCircle(positionMatrix, this.targetMultiDrawBatchDataHolder, x, y, radius - width / 2F, width, color.toABGR());
        }
        this.drawIfNotBuffering();
    }

    public void outlinedCircle(final Matrix4f positionMatrix, final Circled circle, final float degStart, final float degEnd, final Color color, final float width) {
        this.outlinedCircle(positionMatrix, circle, degStart, degEnd, color, width, OUTLINE_STYLE_OUTER_BIT);
    }

    public void outlinedCircle(final Matrix4f positionMatrix, final Circled circle, final float degStart, final float degEnd, final Color color, final float width, final int styleFlags) {
        this.outlinedCircle(positionMatrix, (float) circle.x, (float) circle.y, (float) circle.r, degStart, degEnd, color, width, styleFlags);
    }

    public void outlinedCircle(final Matrix4f positionMatrix, final Circlef circle, final float degStart, final float degEnd, final Color color, final float width) {
        this.outlinedCircle(positionMatrix, circle, degStart, degEnd, color, width, OUTLINE_STYLE_OUTER_BIT);
    }

    public void outlinedCircle(final Matrix4f positionMatrix, final Circlef circle, final float degStart, final float degEnd, final Color color, final float width, final int styleFlags) {
        this.outlinedCircle(positionMatrix, circle.x, circle.y, circle.r, degStart, degEnd, color, width, styleFlags);
    }

    public void outlinedCircle(final Matrix4f positionMatrix, final float x, final float y, final float radius, final float degStart, final float degEnd, final Color color, final float width) {
        this.outlinedCircle(positionMatrix, x, y, radius, degStart, degEnd, color, width, OUTLINE_STYLE_OUTER_BIT);
    }

    public void outlinedCircle(final Matrix4f positionMatrix, final float x, final float y, final float radius, final float degStart, final float degEnd, final Color color, final float width, final int styleFlags) {
        if ((styleFlags & OUTLINE_STYLE_OUTER_BIT) != 0) {
            Primitives.outlinedCircle(positionMatrix, this.targetMultiDrawBatchDataHolder, x, y, radius + width / 2F, width, degStart, degEnd, color.toABGR());
        }
        if ((styleFlags & OUTLINE_STYLE_INNER_BIT) != 0) {
            Primitives.outlinedCircle(positionMatrix, this.targetMultiDrawBatchDataHolder, x, y, radius - width / 2F, width, degStart, degEnd, color.toABGR());
        }
        this.drawIfNotBuffering();
    }

    public void rectLine(final Matrix4f positionMatrix, final Vector2d start, final Vector2d end, final float width, final Color color) {
        this.rectLine(positionMatrix, (float) start.x, (float) start.y, (float) end.x, (float) end.y, width, color);
    }

    public void rectLine(final Matrix4f positionMatrix, final Vector2f start, final Vector2f end, final float width, final Color color) {
        this.rectLine(positionMatrix, start.x, start.y, end.x, end.y, width, color);
    }

    public void rectLine(final Matrix4f positionMatrix, final Vector2i start, final Vector2i end, final float width, final Color color) {
        this.rectLine(positionMatrix, start.x, start.y, end.x, end.y, width, color);
    }

    public void rectLine(final Matrix4f positionMatrix, final float x1, final float y1, final float x2, final float y2, final float width, final Color color) {
        Primitives.rectLine(positionMatrix, this.targetMultiDrawBatchDataHolder, x1, y1, x2, y2, width, color.toABGR());
        this.drawIfNotBuffering();
    }

    public void rectLine(final Matrix4f positionMatrix, final Vector2d start, final Vector2d end, final float width, final Color startColor, final Color endColor) {
        this.rectLine(positionMatrix, (float) start.x, (float) start.y, (float) end.x, (float) end.y, width, startColor, endColor);
    }

    public void rectLine(final Matrix4f positionMatrix, final Vector2f start, final Vector2f end, final float width, final Color startColor, final Color endColor) {
        this.rectLine(positionMatrix, start.x, start.y, end.x, end.y, width, startColor, endColor);
    }

    public void rectLine(final Matrix4f positionMatrix, final Vector2i start, final Vector2i end, final float width, final Color startColor, final Color endColor) {
        this.rectLine(positionMatrix, start.x, start.y, end.x, end.y, width, startColor, endColor);
    }

    public void rectLine(final Matrix4f positionMatrix, final float x1, final float y1, final float x2, final float y2, final float width, final Color startColor, final Color endColor) {
        Primitives.rectLine(positionMatrix, this.targetMultiDrawBatchDataHolder, x1, y1, x2, y2, width, startColor.toABGR(), endColor.toABGR());
        this.drawIfNotBuffering();
    }

    public void glLine(final Matrix4f positionMatrix, final Vector2d start, final Vector2d end, final Color color) {
        this.glLine(positionMatrix, (float) start.x, (float) start.y, (float) end.x, (float) end.y, color);
    }

    public void glLine(final Matrix4f positionMatrix, final Vector2f start, final Vector2f end, final Color color) {
        this.glLine(positionMatrix, start.x, start.y, end.x, end.y, color);
    }

    public void glLine(final Matrix4f positionMatrix, final Vector2i start, final Vector2i end, final Color color) {
        this.glLine(positionMatrix, start.x, start.y, end.x, end.y, color);
    }

    public void glLine(final Matrix4f positionMatrix, final float x1, final float y1, final float x2, final float y2, final Color color) {
        Primitives.glLine(positionMatrix, this.targetMultiDrawBatchDataHolder, x1, y1, x2, y2, color.toABGR());
        this.drawIfNotBuffering();
    }

    public void glLine(final Matrix4f positionMatrix, final Vector2d start, final Vector2d end, final Color startColor, final Color endColor) {
        this.glLine(positionMatrix, (float) start.x, (float) start.y, (float) end.x, (float) end.y, startColor, endColor);
    }

    public void glLine(final Matrix4f positionMatrix, final Vector2f start, final Vector2f end, final Color startColor, final Color endColor) {
        this.glLine(positionMatrix, start.x, start.y, end.x, end.y, startColor, endColor);
    }

    public void glLine(final Matrix4f positionMatrix, final Vector2i start, final Vector2i end, final Color startColor, final Color endColor) {
        this.glLine(positionMatrix, start.x, start.y, end.x, end.y, startColor, endColor);
    }

    public void glLine(final Matrix4f positionMatrix, final float x1, final float y1, final float x2, final float y2, final Color startColor, final Color endColor) {
        Primitives.glLine(positionMatrix, this.targetMultiDrawBatchDataHolder, x1, y1, x2, y2, startColor.toABGR(), endColor.toABGR());
        this.drawIfNotBuffering();
    }

    public void line(final Matrix4f positionMatrix, final Vector2d start, final Vector2d end, final float width, final Color color) {
        this.line(positionMatrix, (float) start.x, (float) start.y, (float) end.x, (float) end.y, width, color);
    }

    public void line(final Matrix4f positionMatrix, final Vector2f start, final Vector2f end, final float width, final Color color) {
        this.line(positionMatrix, start.x, start.y, end.x, end.y, width, color);
    }

    public void line(final Matrix4f positionMatrix, final Vector2i start, final Vector2i end, final float width, final Color color) {
        this.line(positionMatrix, start.x, start.y, end.x, end.y, width, color);
    }

    public void line(final Matrix4f positionMatrix, final float x1, final float y1, final float x2, final float y2, final float width, final Color color) {
        Primitives.line(positionMatrix, this.targetMultiDrawBatchDataHolder, x1, y1, x2, y2, width, color.toABGR());
        this.drawIfNotBuffering();
    }

    public void line(final Matrix4f positionMatrix, final Vector2d start, final Vector2d end, final float width, final Color startColor, final Color endColor) {
        this.line(positionMatrix, (float) start.x, (float) start.y, (float) end.x, (float) end.y, width, startColor, endColor);
    }

    public void line(final Matrix4f positionMatrix, final Vector2f start, final Vector2f end, final float width, final Color startColor, final Color endColor) {
        this.line(positionMatrix, start.x, start.y, end.x, end.y, width, startColor, endColor);
    }

    public void line(final Matrix4f positionMatrix, final Vector2i start, final Vector2i end, final float width, final Color startColor, final Color endColor) {
        this.line(positionMatrix, start.x, start.y, end.x, end.y, width, startColor, endColor);
    }

    public void line(final Matrix4f positionMatrix, final float x1, final float y1, final float x2, final float y2, final float width, final Color startColor, final Color endColor) {
        Primitives.line(positionMatrix, this.targetMultiDrawBatchDataHolder, x1, y1, x2, y2, width, startColor.toABGR(), endColor.toABGR());
        this.drawIfNotBuffering();
    }

    public void connectedLine(final Matrix4f positionMatrix, final List<Vector2f> points, final float width, final Color color) {
        this.connectedLine(positionMatrix, points, width, color, false);
    }

    public void connectedLine(final Matrix4f positionMatrix, final List<Vector2f> points, final float width, final Color color, final boolean closedLoop) {
        ThinGL.capabilities().ensureParPresent();
        final VertexDataHolder vertexDataHolder = this.targetMultiDrawBatchDataHolder.getVertexDataHolder(BuiltinDrawBatches.INDEXED_COLORED_TRIANGLE);
        final IndexDataHolder indexDataHolder = this.targetMultiDrawBatchDataHolder.getIndexDataHolder(BuiltinDrawBatches.INDEXED_COLORED_TRIANGLE);
        final int abgrColor = color.toABGR();

        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            final ParSLConfig config = ParSLConfig.calloc(memoryStack).thickness(width).miter_limit(width * 10F);
            final long ctx = ParStreamlines.parsl_create_context(config);

            final BufferBuilder positionsBuilder = new BufferBuilder(memoryStack, points.size() * Float.BYTES * 2);
            for (Vector2f point : points) {
                positionsBuilder.putVector2f(point);
            }

            final ParSLSpineList lineList = ParSLSpineList.malloc(memoryStack).set(new ParSLPosition.Buffer(positionsBuilder.finish()), memoryStack.shorts((short) points.size()), closedLoop);
            final ParSLMesh mesh = ParStreamlines.parsl_mesh_from_lines(ctx, lineList);

            final int vertexCount = mesh.num_vertices();
            final ParSLPosition.Buffer verticesBuffer = mesh.positions();
            for (int i = 0; i < vertexCount; i++) {
                final ParSLPosition position = verticesBuffer.get(i);
                vertexDataHolder.putVector3f(positionMatrix, position.x(), position.y(), 0F).putColor(abgrColor).endVertex();
            }

            final int triangleCount = mesh.num_triangles();
            final IntBuffer indicesBuffer = mesh.triangle_indices(triangleCount * 3);
            for (int i = 0; i < indicesBuffer.capacity(); i++) {
                indexDataHolder.putIndex(indicesBuffer.get(i));
            }

            ParStreamlines.parsl_destroy_context(ctx);
        }

        this.drawIfNotBuffering();
    }

    public void filledPolygon(final Matrix4f positionMatrix, final List<Vector2f> points, final Color color) {
        ThinGL.capabilities().ensureEarcut4jPresent();
        final VertexDataHolder vertexDataHolder = this.targetMultiDrawBatchDataHolder.getVertexDataHolder(BuiltinDrawBatches.INDEXED_COLORED_TRIANGLE);
        final IndexDataHolder indexDataHolder = this.targetMultiDrawBatchDataHolder.getIndexDataHolder(BuiltinDrawBatches.INDEXED_COLORED_TRIANGLE);
        final int abgrColor = color.toABGR();

        final double[] data = new double[points.size() * 2];
        for (int i = 0; i < points.size(); i++) {
            final Vector2f point = points.get(i);
            data[i * 2] = point.x;
            data[i * 2 + 1] = point.y;
            vertexDataHolder.putVector3f(positionMatrix, point.x, point.y, 0F).putColor(abgrColor).endVertex();
        }
        final List<Integer> indices = Earcut.earcut(data);
        for (int index : indices) {
            indexDataHolder.putIndex(index);
        }

        this.drawIfNotBuffering();
    }

    public void texture(final Matrix4f positionMatrix, final int id, final float x, final float y, final float width, final float height) {
        final VertexDataHolder vertexDataHolder = this.targetMultiDrawBatchDataHolder.getVertexDataHolder(this.texturedQuad.apply(id));
        this.texture(positionMatrix, vertexDataHolder, x, y, width, height, 0F, 0F, 1F, 1F);
        this.drawIfNotBuffering();
    }

    public void texture(final Matrix4f positionMatrix, final int id, final float x, final float y, final float width, final float height, final float u, final float v, final float texWidth, final float texHeight) {
        final VertexDataHolder vertexDataHolder = this.targetMultiDrawBatchDataHolder.getVertexDataHolder(this.texturedQuad.apply(id));
        this.texture(positionMatrix, vertexDataHolder, x, y, width, height, u / texWidth, v / texHeight, width / texWidth, height / texHeight);
        this.drawIfNotBuffering();
    }

    public void texture(final Matrix4f positionMatrix, final int id, final float x, final float y, final float width, final float height, final float u, final float v, final float uWidth, final float vHeight, final float texWidth, final float texHeight) {
        final VertexDataHolder vertexDataHolder = this.targetMultiDrawBatchDataHolder.getVertexDataHolder(this.texturedQuad.apply(id));
        this.texture(positionMatrix, vertexDataHolder, x, y, width, height, u / texWidth, v / texHeight, uWidth / texWidth, vHeight / texHeight);
        this.drawIfNotBuffering();
    }

    private void texture(final Matrix4f positionMatrix, final VertexDataHolder vertexDataHolder, final float x, final float y, final float width, final float height, final float u, final float v, final float uWidth, final float vHeight) {
        vertexDataHolder.putVector3f(positionMatrix, x, y + height, 0F).putTextureCoords(u, v + vHeight).endVertex();
        vertexDataHolder.putVector3f(positionMatrix, x + width, y + height, 0F).putTextureCoords(u + uWidth, v + vHeight).endVertex();
        vertexDataHolder.putVector3f(positionMatrix, x + width, y, 0F).putTextureCoords(u + uWidth, v).endVertex();
        vertexDataHolder.putVector3f(positionMatrix, x, y, 0F).putTextureCoords(u, v).endVertex();
    }

    public void coloredTexture(final Matrix4f positionMatrix, final int id, final float x, final float y, final float width, final float height, final Color color) {
        final VertexDataHolder vertexDataHolder = this.targetMultiDrawBatchDataHolder.getVertexDataHolder(this.coloredTexturedQuad.apply(id));
        this.coloredTexture(positionMatrix, vertexDataHolder, x, y, width, height, 0F, 0F, 1F, 1F, color);
        this.drawIfNotBuffering();
    }

    public void coloredTexture(final Matrix4f positionMatrix, final int id, final float x, final float y, final float width, final float height, final float u, final float v, final float texWidth, final float texHeight, final Color color) {
        final VertexDataHolder vertexDataHolder = this.targetMultiDrawBatchDataHolder.getVertexDataHolder(this.coloredTexturedQuad.apply(id));
        this.coloredTexture(positionMatrix, vertexDataHolder, x, y, width, height, u / texWidth, v / texHeight, width / texWidth, height / texHeight, color);
        this.drawIfNotBuffering();
    }

    public void coloredTexture(final Matrix4f positionMatrix, final int id, final float x, final float y, final float width, final float height, final float u, final float v, final float uWidth, final float vHeight, final float texWidth, final float texHeight, final Color color) {
        final VertexDataHolder vertexDataHolder = this.targetMultiDrawBatchDataHolder.getVertexDataHolder(this.coloredTexturedQuad.apply(id));
        this.coloredTexture(positionMatrix, vertexDataHolder, x, y, width, height, u / texWidth, v / texHeight, uWidth / texWidth, vHeight / texHeight, color);
        this.drawIfNotBuffering();
    }

    private void coloredTexture(final Matrix4f positionMatrix, final VertexDataHolder vertexDataHolder, final float x, final float y, final float width, final float height, final float u, final float v, final float uWidth, final float vHeight, final Color color) {
        vertexDataHolder.putVector3f(positionMatrix, x, y + height, 0F).putColor(color).putTextureCoords(u, v + vHeight).endVertex();
        vertexDataHolder.putVector3f(positionMatrix, x + width, y + height, 0F).putColor(color).putTextureCoords(u + uWidth, v + vHeight).endVertex();
        vertexDataHolder.putVector3f(positionMatrix, x + width, y, 0F).putColor(color).putTextureCoords(u + uWidth, v).endVertex();
        vertexDataHolder.putVector3f(positionMatrix, x, y, 0F).putColor(color).putTextureCoords(u, v).endVertex();
    }

    public void colorizedTexture(final Matrix4f positionMatrix, final int id, final float x, final float y, final float width, final float height, final Color color) {
        final VertexDataHolder vertexDataHolder = this.targetMultiDrawBatchDataHolder.getVertexDataHolder(this.colorizedTexturedQuad.apply(id));
        this.coloredTexture(positionMatrix, vertexDataHolder, x, y, width, height, 0F, 0F, 1F, 1F, color);
        this.drawIfNotBuffering();
    }

    public void colorizedTexture(final Matrix4f positionMatrix, final int id, final float x, final float y, final float width, final float height, final float u, final float v, final float texWidth, final float texHeight, final Color color) {
        final VertexDataHolder vertexDataHolder = this.targetMultiDrawBatchDataHolder.getVertexDataHolder(this.colorizedTexturedQuad.apply(id));
        this.coloredTexture(positionMatrix, vertexDataHolder, x, y, width, height, u / texWidth, v / texHeight, width / texWidth, height / texHeight, color);
        this.drawIfNotBuffering();
    }

    public void colorizedTexture(final Matrix4f positionMatrix, final int id, final float x, final float y, final float width, final float height, final float u, final float v, final float uWidth, final float vHeight, final float texWidth, final float texHeight, final Color color) {
        final VertexDataHolder vertexDataHolder = this.targetMultiDrawBatchDataHolder.getVertexDataHolder(this.colorizedTexturedQuad.apply(id));
        this.coloredTexture(positionMatrix, vertexDataHolder, x, y, width, height, u / texWidth, v / texHeight, uWidth / texWidth, vHeight / texHeight, color);
        this.drawIfNotBuffering();
    }

}
