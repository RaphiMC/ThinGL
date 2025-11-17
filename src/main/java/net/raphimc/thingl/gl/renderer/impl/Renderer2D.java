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
package net.raphimc.thingl.gl.renderer.impl;

import earcut4j.Earcut;
import net.lenni0451.commons.color.Color;
import net.lenni0451.commons.math.shapes.triangle.TriangleD;
import net.lenni0451.commons.math.shapes.triangle.TriangleF;
import net.lenni0451.commons.math.shapes.triangle.TriangleI;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.renderer.Primitives;
import net.raphimc.thingl.gl.renderer.Renderer;
import net.raphimc.thingl.gl.resource.image.texture.impl.Texture2D;
import net.raphimc.thingl.gl.resource.image.texture.impl.Texture2DArray;
import net.raphimc.thingl.implementation.Capabilities;
import net.raphimc.thingl.rendering.DrawBatch;
import net.raphimc.thingl.rendering.DrawBatches;
import net.raphimc.thingl.rendering.bufferbuilder.impl.IndexBufferBuilder;
import net.raphimc.thingl.rendering.bufferbuilder.impl.VertexBufferBuilder;
import net.raphimc.thingl.util.CacheUtil;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.primitives.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

public class Renderer2D extends Renderer {

    public static final int OUTLINE_STYLE_OUTER_BIT = 1 << 0;
    public static final int OUTLINE_STYLE_INNER_BIT = 1 << 1;

    protected final IntFunction<DrawBatch> textureQuad = CacheUtil.memoizeInt(textureId -> new DrawBatch.Builder(DrawBatches.TEXTURE_SNIPPET)
            .appendSetupAction(p -> p.setUniformSampler("u_Texture", textureId))
            .build());

    protected final IntFunction<DrawBatch> textureArrayLayerQuad = CacheUtil.memoizeInt(textureId -> new DrawBatch.Builder(DrawBatches.TEXTURE_SNIPPET)
            .program(() -> ThinGL.programs().getTextureArrayLayer())
            .vertexDataLayout(DrawBatches.POSITION_TEXTURE_ARRAY_LAYER_LAYOUT)
            .appendSetupAction(p -> p.setUniformSampler("u_Texture", textureId))
            .build());

    protected final IntFunction<DrawBatch> coloredTextureQuad = CacheUtil.memoizeInt(textureId -> new DrawBatch.Builder(DrawBatches.TEXTURE_SNIPPET)
            .program(() -> ThinGL.programs().getColoredTexture())
            .vertexDataLayout(DrawBatches.POSITION_COLOR_TEXTURE_LAYOUT)
            .appendSetupAction(p -> p.setUniformSampler("u_Texture", textureId))
            .build());

    protected final IntFunction<DrawBatch> colorizedTextureQuad = CacheUtil.memoizeInt(textureId -> new DrawBatch.Builder(DrawBatches.TEXTURE_SNIPPET)
            .program(() -> ThinGL.programs().getColorizedTexture())
            .vertexDataLayout(DrawBatches.POSITION_COLOR_TEXTURE_LAYOUT)
            .appendSetupAction(p -> p.setUniformSampler("u_Texture", textureId))
            .build());

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
        final VertexBufferBuilder vertexBufferBuilder = this.targetMultiDrawBatchDataHolder.getVertexBufferBuilder(DrawBatches.COLOR_TRIANGLE_FAN);
        final int abgrColor = color.toABGR();

        vertexBufferBuilder.writeVector3f(positionMatrix, (xtl + xbr) / 2F, (ytl + ybr) / 2F, 0F).writeColor(abgrColor).endVertex();
        Primitives._filledCircle(positionMatrix, vertexBufferBuilder, xtl + rtl, ytl + rtl, 0F, rtl, 270, 360, abgrColor);
        Primitives._filledCircle(positionMatrix, vertexBufferBuilder, xtl + rbl, ybr - rbl, 0F, rbl, 180, 270, abgrColor);
        Primitives._filledCircle(positionMatrix, vertexBufferBuilder, xbr - rbr, ybr - rbr, 0F, rbr, 90, 180, abgrColor);
        Primitives._filledCircle(positionMatrix, vertexBufferBuilder, xbr - rtr, ytl + rtr, 0F, rtr, 0, 90, abgrColor);
        vertexBufferBuilder.writeVector3f(positionMatrix, xtl + rtl, ytl, 0F).writeColor(abgrColor).endVertex();
        vertexBufferBuilder.endConnectedPrimitive();

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
        final VertexBufferBuilder vertexBufferBuilder = this.targetMultiDrawBatchDataHolder.getVertexBufferBuilder(DrawBatches.COLOR_TRIANGLE_STRIP);
        final int abgrColor = color.toABGR();

        if ((styleFlags & OUTLINE_STYLE_OUTER_BIT) != 0) {
            Primitives._outlinedCircle(positionMatrix, vertexBufferBuilder, xtl + rtl, ytl + rtl, 0F, rtl + width / 2F, width, 270, 360, abgrColor);
            Primitives._outlinedCircle(positionMatrix, vertexBufferBuilder, xtl + rbl, ybr - rbl, 0F, rbl + width / 2F, width, 180, 270, abgrColor);
            Primitives._outlinedCircle(positionMatrix, vertexBufferBuilder, xbr - rbr, ybr - rbr, 0F, rbr + width / 2F, width, 90, 180, abgrColor);
            Primitives._outlinedCircle(positionMatrix, vertexBufferBuilder, xbr - rtr, ytl + rtr, 0F, rtr + width / 2F, width, 0, 90, abgrColor);
            vertexBufferBuilder.writeVector3f(positionMatrix, xtl + rtl, ytl, 0F).writeColor(abgrColor).endVertex();
            vertexBufferBuilder.writeVector3f(positionMatrix, xtl + rtl, ytl - width, 0F).writeColor(abgrColor).endVertex();
            vertexBufferBuilder.endConnectedPrimitive();
        }
        if ((styleFlags & OUTLINE_STYLE_INNER_BIT) != 0) {
            Primitives._outlinedCircle(positionMatrix, vertexBufferBuilder, xtl + rtl, ytl + rtl, 0F, rtl - width / 2F, width, 270, 360, abgrColor);
            Primitives._outlinedCircle(positionMatrix, vertexBufferBuilder, xtl + rbl, ybr - rbl, 0F, rbl - width / 2F, width, 180, 270, abgrColor);
            Primitives._outlinedCircle(positionMatrix, vertexBufferBuilder, xbr - rbr, ybr - rbr, 0F, rbr - width / 2F, width, 90, 180, abgrColor);
            Primitives._outlinedCircle(positionMatrix, vertexBufferBuilder, xbr - rtr, ytl + rtr, 0F, rtr - width / 2F, width, 0, 90, abgrColor);
            vertexBufferBuilder.writeVector3f(positionMatrix, xtl + rtl, ytl + width, 0F).writeColor(abgrColor).endVertex();
            vertexBufferBuilder.writeVector3f(positionMatrix, xtl + rtl, ytl, 0F).writeColor(abgrColor).endVertex();
            vertexBufferBuilder.endConnectedPrimitive();
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

    public void polyLine(final Matrix4f positionMatrix, final List<Vector2f> points, final float width, final Color color) {
        final VertexBufferBuilder vertexBufferBuilder = this.targetMultiDrawBatchDataHolder.getVertexBufferBuilder(DrawBatches.INDEXED_COLOR_TRIANGLE);
        final IndexBufferBuilder indexBufferBuilder = this.targetMultiDrawBatchDataHolder.getIndexBufferBuilder(DrawBatches.INDEXED_COLOR_TRIANGLE);
        float halfWidth = width * 0.5F;
        float maxMiterLength = halfWidth * 10F;
        final int abgrColor = color.toABGR();

        for (int i = 0; i < points.size(); i++) {
            final Vector2f curr = points.get(i);
            int prevIndex = i - 1;
            while (prevIndex >= 0 && points.get(prevIndex).equals(curr)) {
                prevIndex--;
            }
            final Vector2f prev = prevIndex >= 0 ? points.get(prevIndex) : curr;
            int nextIndex = i + 1;
            while (nextIndex < points.size() && points.get(nextIndex).equals(curr)) {
                nextIndex++;
            }
            final Vector2f next = nextIndex < points.size() ? points.get(nextIndex) : curr;
            final Vector2f dirPrev = (prev.equals(curr) ? new Vector2f(next).sub(curr) : new Vector2f(curr).sub(prev)).normalize();
            final Vector2f dirNext = (next.equals(curr) ? new Vector2f(curr).sub(prev) : new Vector2f(next).sub(curr)).normalize();

            final Vector2f tangent = new Vector2f(dirPrev).add(dirNext).normalize().perpendicular().mul(-1F);
            final float dot = tangent.dot(dirPrev.perpendicular().mul(-1F));
            tangent.mul(Math.min(halfWidth / dot, maxMiterLength));

            vertexBufferBuilder.writeVector3f(positionMatrix, curr.x + tangent.x, curr.y + tangent.y, 0F).writeColor(abgrColor).endVertex();
            vertexBufferBuilder.writeVector3f(positionMatrix, curr.x - tangent.x, curr.y - tangent.y, 0F).writeColor(abgrColor).endVertex();
        }
        for (int i = 0; i < points.size() - 1; i++) {
            final int base = i * 2;
            indexBufferBuilder.writeRelativeIndex(base).writeRelativeIndex(base + 2).writeRelativeIndex(base + 1);
            indexBufferBuilder.writeRelativeIndex(base + 2).writeRelativeIndex(base + 3).writeRelativeIndex(base + 1);
        }

        this.drawIfNotBuffering();
    }

    public void filledPolygon(final Matrix4f positionMatrix, final List<Vector2f> points, final Color color) {
        Capabilities.assertEarcut4jAvailable();
        final VertexBufferBuilder vertexBufferBuilder = this.targetMultiDrawBatchDataHolder.getVertexBufferBuilder(DrawBatches.INDEXED_COLOR_TRIANGLE);
        final IndexBufferBuilder indexBufferBuilder = this.targetMultiDrawBatchDataHolder.getIndexBufferBuilder(DrawBatches.INDEXED_COLOR_TRIANGLE);
        final int abgrColor = color.toABGR();

        final double[] data = new double[points.size() * 2];
        for (int i = 0; i < points.size(); i++) {
            final Vector2f point = points.get(i);
            data[i * 2] = point.x;
            data[i * 2 + 1] = point.y;
            vertexBufferBuilder.writeVector3f(positionMatrix, point.x, point.y, 0F).writeColor(abgrColor).endVertex();
        }
        final List<Integer> indices = Earcut.earcut(data);
        for (int i = indices.size() - 1; i >= 0; i--) {
            indexBufferBuilder.writeRelativeIndex(indices.get(i));
        }

        this.drawIfNotBuffering();
    }

    public void texture(final Matrix4f positionMatrix, final Texture2D texture, final float x, final float y) {
        this.texture(positionMatrix, texture, x, y, texture.getWidth(), texture.getHeight());
    }

    public void texture(final Matrix4f positionMatrix, final Texture2D texture, final float x, final float y, final float width, final float height) {
        this.textureWithRawTexCoord(positionMatrix, texture, x, y, width, height, 0F, 0F, 1F, 1F);
    }

    public void texture(final Matrix4f positionMatrix, final Texture2D texture, final float x, final float y, final float width, final float height, final float u, final float v) {
        this.texture(positionMatrix, texture, x, y, width, height, u, v, width, height);
    }

    public void texture(final Matrix4f positionMatrix, final Texture2D texture, final float x, final float y, final float width, final float height, final float u, final float v, final float uWidth, final float vHeight) {
        this.textureWithRawTexCoord(positionMatrix, texture, x, y, width, height, u / texture.getWidth(), v / texture.getHeight(), uWidth / texture.getWidth(), vHeight / texture.getHeight());
    }

    public void textureWithRawTexCoord(final Matrix4f positionMatrix, final Texture2D texture, final float x, final float y, final float width, final float height, final float u, final float v, final float uWidth, final float vHeight) {
        final VertexBufferBuilder vertexBufferBuilder = this.targetMultiDrawBatchDataHolder.getVertexBufferBuilder(this.textureQuad.apply(texture.getGlId()));
        vertexBufferBuilder.writeVector3f(positionMatrix, x, y + height, 0F).writeTextureCoord(u, v + vHeight).endVertex();
        vertexBufferBuilder.writeVector3f(positionMatrix, x + width, y + height, 0F).writeTextureCoord(u + uWidth, v + vHeight).endVertex();
        vertexBufferBuilder.writeVector3f(positionMatrix, x + width, y, 0F).writeTextureCoord(u + uWidth, v).endVertex();
        vertexBufferBuilder.writeVector3f(positionMatrix, x, y, 0F).writeTextureCoord(u, v).endVertex();
        this.drawIfNotBuffering();
    }

    public void textureArrayLayer(final Matrix4f positionMatrix, final Texture2DArray texture, final int layer, final float x, final float y) {
        this.textureArrayLayer(positionMatrix, texture, layer, x, y, texture.getWidth(), texture.getHeight());
    }

    public void textureArrayLayer(final Matrix4f positionMatrix, final Texture2DArray texture, final int layer, final float x, final float y, final float width, final float height) {
        this.textureArrayLayerWithRawTexCoord(positionMatrix, texture, layer, x, y, width, height, 0F, 0F, 1F, 1F);
    }

    public void textureArrayLayer(final Matrix4f positionMatrix, final Texture2DArray texture, final int layer, final float x, final float y, final float width, final float height, final float u, final float v) {
        this.textureArrayLayer(positionMatrix, texture, layer, x, y, width, height, u, v, width, height);
    }

    public void textureArrayLayer(final Matrix4f positionMatrix, final Texture2DArray texture, final int layer, final float x, final float y, final float width, final float height, final float u, final float v, final float uWidth, final float vHeight) {
        this.textureArrayLayerWithRawTexCoord(positionMatrix, texture, layer, x, y, width, height, u / texture.getWidth(), v / texture.getHeight(), uWidth / texture.getWidth(), vHeight / texture.getHeight());
    }

    public void textureArrayLayerWithRawTexCoord(final Matrix4f positionMatrix, final Texture2DArray texture, final int layer, final float x, final float y, final float width, final float height, final float u, final float v, final float uWidth, final float vHeight) {
        final VertexBufferBuilder vertexBufferBuilder = this.targetMultiDrawBatchDataHolder.getVertexBufferBuilder(this.textureArrayLayerQuad.apply(texture.getGlId()));
        vertexBufferBuilder.writeVector3f(positionMatrix, x, y + height, 0F).writeTextureCoord(u, v + vHeight).writeShort((short) layer).endVertex();
        vertexBufferBuilder.writeVector3f(positionMatrix, x + width, y + height, 0F).writeTextureCoord(u + uWidth, v + vHeight).writeShort((short) layer).endVertex();
        vertexBufferBuilder.writeVector3f(positionMatrix, x + width, y, 0F).writeTextureCoord(u + uWidth, v).writeShort((short) layer).endVertex();
        vertexBufferBuilder.writeVector3f(positionMatrix, x, y, 0F).writeTextureCoord(u, v).writeShort((short) layer).endVertex();
        this.drawIfNotBuffering();
    }

    public void coloredTexture(final Matrix4f positionMatrix, final Texture2D texture, final float x, final float y, final Color color) {
        this.coloredTexture(positionMatrix, texture, x, y, texture.getWidth(), texture.getHeight(), color);
    }

    public void coloredTexture(final Matrix4f positionMatrix, final Texture2D texture, final float x, final float y, final float width, final float height, final Color color) {
        this.coloredTextureWithRawTexCoord(positionMatrix, texture, x, y, width, height, 0F, 0F, 1F, 1F, color);
    }

    public void coloredTexture(final Matrix4f positionMatrix, final Texture2D texture, final float x, final float y, final float width, final float height, final float u, final float v, final Color color) {
        this.coloredTexture(positionMatrix, texture, x, y, width, height, u, v, width, height, color);
    }

    public void coloredTexture(final Matrix4f positionMatrix, final Texture2D texture, final float x, final float y, final float width, final float height, final float u, final float v, final float uWidth, final float vHeight, final Color color) {
        this.coloredTextureWithRawTexCoord(positionMatrix, texture, x, y, width, height, u / texture.getWidth(), v / texture.getHeight(), uWidth / texture.getWidth(), vHeight / texture.getHeight(), color);
    }

    public void coloredTextureWithRawTexCoord(final Matrix4f positionMatrix, final Texture2D texture, final float x, final float y, final float width, final float height, final float u, final float v, final float uWidth, final float vHeight, final Color color) {
        final VertexBufferBuilder vertexBufferBuilder = this.targetMultiDrawBatchDataHolder.getVertexBufferBuilder(this.coloredTextureQuad.apply(texture.getGlId()));
        vertexBufferBuilder.writeVector3f(positionMatrix, x, y + height, 0F).writeColor(color).writeTextureCoord(u, v + vHeight).endVertex();
        vertexBufferBuilder.writeVector3f(positionMatrix, x + width, y + height, 0F).writeColor(color).writeTextureCoord(u + uWidth, v + vHeight).endVertex();
        vertexBufferBuilder.writeVector3f(positionMatrix, x + width, y, 0F).writeColor(color).writeTextureCoord(u + uWidth, v).endVertex();
        vertexBufferBuilder.writeVector3f(positionMatrix, x, y, 0F).writeColor(color).writeTextureCoord(u, v).endVertex();
        this.drawIfNotBuffering();
    }

    public void colorizedTexture(final Matrix4f positionMatrix, final Texture2D texture, final float x, final float y, final Color color) {
        this.colorizedTexture(positionMatrix, texture, x, y, texture.getWidth(), texture.getHeight(), color);
    }

    public void colorizedTexture(final Matrix4f positionMatrix, final Texture2D texture, final float x, final float y, final float width, final float height, final Color color) {
        this.colorizedTextureWithRawTexCoord(positionMatrix, texture, x, y, width, height, 0F, 0F, 1F, 1F, color);
    }

    public void colorizedTexture(final Matrix4f positionMatrix, final Texture2D texture, final float x, final float y, final float width, final float height, final float u, final float v, final Color color) {
        this.colorizedTexture(positionMatrix, texture, x, y, width, height, u, v, width, height, color);
    }

    public void colorizedTexture(final Matrix4f positionMatrix, final Texture2D texture, final float x, final float y, final float width, final float height, final float u, final float v, final float uWidth, final float vHeight, final Color color) {
        this.colorizedTextureWithRawTexCoord(positionMatrix, texture, x, y, width, height, u / texture.getWidth(), v / texture.getHeight(), uWidth / texture.getWidth(), vHeight / texture.getHeight(), color);
    }

    public void colorizedTextureWithRawTexCoord(final Matrix4f positionMatrix, final Texture2D texture, final float x, final float y, final float width, final float height, final float u, final float v, final float uWidth, final float vHeight, final Color color) {
        final VertexBufferBuilder vertexBufferBuilder = this.targetMultiDrawBatchDataHolder.getVertexBufferBuilder(this.colorizedTextureQuad.apply(texture.getGlId()));
        vertexBufferBuilder.writeVector3f(positionMatrix, x, y + height, 0F).writeColor(color).writeTextureCoord(u, v + vHeight).endVertex();
        vertexBufferBuilder.writeVector3f(positionMatrix, x + width, y + height, 0F).writeColor(color).writeTextureCoord(u + uWidth, v + vHeight).endVertex();
        vertexBufferBuilder.writeVector3f(positionMatrix, x + width, y, 0F).writeColor(color).writeTextureCoord(u + uWidth, v).endVertex();
        vertexBufferBuilder.writeVector3f(positionMatrix, x, y, 0F).writeColor(color).writeTextureCoord(u, v).endVertex();
        this.drawIfNotBuffering();
    }

    @Deprecated(forRemoval = true)
    public void connectedLine(final Matrix4f positionMatrix, final List<Vector2f> points, final float width, final Color color) {
        this.connectedLine(positionMatrix, points, width, color, false);
    }

    @Deprecated(forRemoval = true)
    public void connectedLine(final Matrix4f positionMatrix, final List<Vector2f> points, final float width, final Color color, final boolean closedLoop) {
        if (points.isEmpty()) {
            return;
        }

        if (closedLoop) {
            final List<Vector2f> newPoints = new ArrayList<>(points.size() + 1);
            newPoints.addAll(points);
            newPoints.add(points.get(0));
            this.polyLine(positionMatrix, newPoints, width, color);
        } else {
            this.polyLine(positionMatrix, points, width, color);
        }
    }

}
