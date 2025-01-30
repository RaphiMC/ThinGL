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

import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.drawbuilder.BuiltinDrawBatches;
import net.raphimc.thingl.drawbuilder.builder.BufferBuilder;
import net.raphimc.thingl.drawbuilder.databuilder.holder.VertexDataHolder;
import net.raphimc.thingl.renderer.Primitives;
import net.raphimc.thingl.renderer.Renderer;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.util.par.*;

import java.nio.IntBuffer;

public class Renderer2D extends Renderer {

    public static final Renderer2D INSTANCE = new Renderer2D();

    public void filledRect(final Matrix4f positionMatrix, final float xtl, final float ytl, final float xbr, final float ybr, final Color color) {
        Primitives.filledRectangle(positionMatrix, this.targetMultiDrawBatchDataHolder, xtl, ytl, xbr, ybr, color.toABGR());
        super.drawIfNotBuffering();
    }

    public void outlineRect(final Matrix4f positionMatrix, final float xtl, final float ytl, final float xbr, final float ybr, final float lineWidth, final Color color) {
        Primitives.filledRectangle(positionMatrix, this.targetMultiDrawBatchDataHolder, xtl - lineWidth, ytl - lineWidth, xbr + lineWidth, ytl, color.toABGR()); // top line
        Primitives.filledRectangle(positionMatrix, this.targetMultiDrawBatchDataHolder, xtl - lineWidth, ybr, xbr + lineWidth, ybr + lineWidth, color.toABGR()); // bottom line
        Primitives.filledRectangle(positionMatrix, this.targetMultiDrawBatchDataHolder, xtl - lineWidth, ytl, xtl, ybr, color.toABGR()); // left line
        Primitives.filledRectangle(positionMatrix, this.targetMultiDrawBatchDataHolder, xbr, ytl, xbr + lineWidth, ybr, color.toABGR()); // right line
        super.drawIfNotBuffering();
    }

    public void filledTrapeze(final Matrix4f positionMatrix, final float xtl, final float ytl, final float xbr, final float ybr, final float left, final float right, final boolean top, final Color color) {
        if (top) {
            Primitives.filledRectangle(positionMatrix, this.targetMultiDrawBatchDataHolder, xtl, ybr, xbr, ybr, xbr + right, ytl, xtl - left, ytl, color.toABGR());
        } else {
            Primitives.filledRectangle(positionMatrix, this.targetMultiDrawBatchDataHolder, xtl - left, ybr, xbr + right, ybr, xbr, ytl, xtl, ytl, color.toABGR());
        }
        super.drawIfNotBuffering();
    }

    public void filledTriangle(final Matrix4f positionMatrix, final float xl, final float yl, final float xm, final float ym, final float xr, final float yr, final Color color) {
        Primitives.filledTriangle(positionMatrix, this.targetMultiDrawBatchDataHolder, xl, yl, xm, ym, xr, yr, color.toABGR());
        super.drawIfNotBuffering();
    }

    public void filledTriangle(final Matrix4f positionMatrix, final float xl, final float yl, final float xm, final float ym, final float xr, final float yr, final Color leftColor, final Color middleColor, final Color rightColor) {
        Primitives.filledTriangle(positionMatrix, this.targetMultiDrawBatchDataHolder, xl, yl, xm, ym, xr, yr, leftColor.toABGR(), middleColor.toABGR(), rightColor.toABGR());
        super.drawIfNotBuffering();
    }

    public void filledCircle(final Matrix4f positionMatrix, final float x, final float y, final float radius, final Color color) {
        Primitives.filledCircle(positionMatrix, this.targetMultiDrawBatchDataHolder, x, y, radius, color.toABGR());
        super.drawIfNotBuffering();
    }

    public void filledCircle(final Matrix4f positionMatrix, final float x, final float y, final float radius, final float degStart, final float degEnd, final Color color) {
        Primitives.filledCircle(positionMatrix, this.targetMultiDrawBatchDataHolder, x, y, radius, degStart, degEnd, color.toABGR());
        super.drawIfNotBuffering();
    }

    public void outlineCircle(final Matrix4f positionMatrix, final float x, final float y, final float radius, final float width, final float degStart, final float degEnd, final Color color) {
        Primitives.outlineCircle(positionMatrix, this.targetMultiDrawBatchDataHolder, x, y, radius, width, degStart, degEnd, color.toABGR());
        super.drawIfNotBuffering();
    }

    public void outlineCircle(final Matrix4f positionMatrix, final float x, final float y, final float radius, final float width, final Color color) {
        Primitives.outlineCircle(positionMatrix, this.targetMultiDrawBatchDataHolder, x, y, radius, width, color.toABGR());
        super.drawIfNotBuffering();
    }

    public void rectLine(final Matrix4f positionMatrix, final float x1, final float y1, final float x2, final float y2, final float width, final Color color) {
        Primitives.rectLine(positionMatrix, this.targetMultiDrawBatchDataHolder, x1, y1, x2, y2, width, color.toABGR());
        super.drawIfNotBuffering();
    }

    public void rectLine(final Matrix4f positionMatrix, final float x1, final float y1, final float x2, final float y2, final float width, final Color startColor, final Color endColor) {
        Primitives.rectLine(positionMatrix, this.targetMultiDrawBatchDataHolder, x1, y1, x2, y2, width, startColor.toABGR(), endColor.toABGR());
        super.drawIfNotBuffering();
    }

    public void glLine(final Matrix4f positionMatrix, final float x1, final float y1, final float x2, final float y2, final Color color) {
        Primitives.glLine(positionMatrix, this.targetMultiDrawBatchDataHolder, x1, y1, x2, y2, color.toABGR());
        super.drawIfNotBuffering();
    }

    public void glLine(final Matrix4f positionMatrix, final float x1, final float y1, final float x2, final float y2, final Color startColor, final Color endColor) {
        Primitives.glLine(positionMatrix, this.targetMultiDrawBatchDataHolder, x1, y1, x2, y2, startColor.toABGR(), endColor.toABGR());
        super.drawIfNotBuffering();
    }

    public void line(final Matrix4f positionMatrix, final float x1, final float y1, final float x2, final float y2, final float width, final Color color) {
        Primitives.line(positionMatrix, this.targetMultiDrawBatchDataHolder, x1, y1, x2, y2, width, color.toABGR());
        super.drawIfNotBuffering();
    }

    public void line(final Matrix4f positionMatrix, final float x1, final float y1, final float x2, final float y2, final float width, final Color startColor, final Color endColor) {
        Primitives.line(positionMatrix, this.targetMultiDrawBatchDataHolder, x1, y1, x2, y2, width, startColor.toABGR(), endColor.toABGR());
        super.drawIfNotBuffering();
    }

    public void connectedLine(final Matrix4f positionMatrix, final Vector2f[] points, final float width, final Color color) {
        final BufferBuilder positionsBuilder = new BufferBuilder();
        for (Vector2f point : points) {
            positionsBuilder.putVec2f(point);
        }
        final BufferBuilder sizeBuilder = new BufferBuilder(2);
        sizeBuilder.putShort((short) points.length);

        final ParSLConfig config = ParSLConfig.create().thickness(width);
        final long ctx = ParStreamlines.parsl_create_context(config);

        final ParSLSpineList lineList = ParSLSpineList.create().set(new ParSLPosition.Buffer(positionsBuilder.finish()), sizeBuilder.finish().asShortBuffer(), false);
        final ParSLMesh mesh = ParStreamlines.parsl_mesh_from_lines(ctx, lineList);

        positionsBuilder.close();
        sizeBuilder.close();

        final int triangleCount = mesh.num_triangles();
        final ParSLPosition.Buffer verticesBuffer = mesh.positions();
        final IntBuffer indicesBuffer = mesh.triangle_indices(triangleCount * 3);
        for (int i = 0; i < triangleCount; i++) {
            final ParSLPosition left = verticesBuffer.get(indicesBuffer.get(i * 3));
            final ParSLPosition right = verticesBuffer.get(indicesBuffer.get(i * 3 + 1));
            final ParSLPosition middle = verticesBuffer.get(indicesBuffer.get(i * 3 + 2));
            Primitives.filledTriangle(positionMatrix, this.targetMultiDrawBatchDataHolder, left.x(), left.y(), middle.x(), middle.y(), right.x(), right.y(), color.toABGR());
        }

        ParStreamlines.parsl_destroy_context(ctx);

        super.drawIfNotBuffering();
    }

    public void filledRoundedRect(final Matrix4f positionMatrix, final float xtl, final float ytl, final float xbr, final float ybr, final float radius, final Color color) {
        final VertexDataHolder vertexDataHolder = this.targetMultiDrawBatchDataHolder.getVertexDataHolder(BuiltinDrawBatches.FILLED_CIRCLE);

        Primitives._filledCircle(positionMatrix, vertexDataHolder, xtl + radius, ytl + radius, 0F, radius, 270, 360, color.toABGR());
        Primitives._filledCircle(positionMatrix, vertexDataHolder, xtl + radius, ybr - radius, 0F, radius, 180, 270, color.toABGR());
        Primitives._filledCircle(positionMatrix, vertexDataHolder, xbr - radius, ybr - radius, 0F, radius, 90, 180, color.toABGR());
        Primitives._filledCircle(positionMatrix, vertexDataHolder, xbr - radius, ytl + radius, 0F, radius, 0, 90, color.toABGR());
        vertexDataHolder.endConnectedPrimitive();

        super.drawIfNotBuffering();
    }

    public void texture(final Matrix4f positionMatrix, final int id, final float x, final float y, final float width, final float height) {
        final VertexDataHolder vertexDataHolder = this.targetMultiDrawBatchDataHolder.getVertexDataHolder(BuiltinDrawBatches.TEXTURE.apply(id));
        this.texture(positionMatrix, vertexDataHolder, x, y, width, height, 0F, 0F, 1F, 1F);
        super.drawIfNotBuffering();
    }

    public void texture(final Matrix4f positionMatrix, final int id, final float x, final float y, final float width, final float height, final float u, final float v, final float texWidth, final float texHeight) {
        final VertexDataHolder vertexDataHolder = this.targetMultiDrawBatchDataHolder.getVertexDataHolder(BuiltinDrawBatches.TEXTURE.apply(id));
        this.texture(positionMatrix, vertexDataHolder, x, y, width, height, u / texWidth, v / texHeight, width / texWidth, height / texHeight);
        super.drawIfNotBuffering();
    }

    public void texture(final Matrix4f positionMatrix, final int id, final float x, final float y, final float width, final float height, final float u, final float v, final float uWidth, final float vHeight, final float texWidth, final float texHeight) {
        final VertexDataHolder vertexDataHolder = this.targetMultiDrawBatchDataHolder.getVertexDataHolder(BuiltinDrawBatches.TEXTURE.apply(id));
        this.texture(positionMatrix, vertexDataHolder, x, y, width, height, u / texWidth, v / texHeight, uWidth / texWidth, vHeight / texHeight);
        super.drawIfNotBuffering();
    }

    private void texture(final Matrix4f positionMatrix, final VertexDataHolder vertexDataHolder, final float x, final float y, final float width, final float height, final float u, final float v, final float uWidth, final float vHeight) {
        vertexDataHolder.position(positionMatrix, x, y + height, 0F).texture(u, v + vHeight).endVertex();
        vertexDataHolder.position(positionMatrix, x + width, y + height, 0F).texture(u + uWidth, v + vHeight).endVertex();
        vertexDataHolder.position(positionMatrix, x + width, y, 0F).texture(u + uWidth, v).endVertex();
        vertexDataHolder.position(positionMatrix, x, y, 0F).texture(u, v).endVertex();
    }

    public void coloredTexture(final Matrix4f positionMatrix, final int id, final float x, final float y, final float width, final float height, final Color color) {
        final VertexDataHolder vertexDataHolder = this.targetMultiDrawBatchDataHolder.getVertexDataHolder(BuiltinDrawBatches.COLORED_TEXTURE.apply(id));
        this.coloredTexture(positionMatrix, vertexDataHolder, x, y, width, height, 0F, 0F, 1F, 1F, color);
        super.drawIfNotBuffering();
    }

    public void coloredTexture(final Matrix4f positionMatrix, final int id, final float x, final float y, final float width, final float height, final float u, final float v, final float texWidth, final float texHeight, final Color color) {
        final VertexDataHolder vertexDataHolder = this.targetMultiDrawBatchDataHolder.getVertexDataHolder(BuiltinDrawBatches.COLORED_TEXTURE.apply(id));
        this.coloredTexture(positionMatrix, vertexDataHolder, x, y, width, height, u / texWidth, v / texHeight, width / texWidth, height / texHeight, color);
        super.drawIfNotBuffering();
    }

    public void coloredTexture(final Matrix4f positionMatrix, final int id, final float x, final float y, final float width, final float height, final float u, final float v, final float uWidth, final float vHeight, final float texWidth, final float texHeight, final Color color) {
        final VertexDataHolder vertexDataHolder = this.targetMultiDrawBatchDataHolder.getVertexDataHolder(BuiltinDrawBatches.COLORED_TEXTURE.apply(id));
        this.coloredTexture(positionMatrix, vertexDataHolder, x, y, width, height, u / texWidth, v / texHeight, uWidth / texWidth, vHeight / texHeight, color);
        super.drawIfNotBuffering();
    }

    private void coloredTexture(final Matrix4f positionMatrix, final VertexDataHolder vertexDataHolder, final float x, final float y, final float width, final float height, final float u, final float v, final float uWidth, final float vHeight, final Color color) {
        vertexDataHolder.position(positionMatrix, x, y + height, 0F).color(color).texture(u, v + vHeight).endVertex();
        vertexDataHolder.position(positionMatrix, x + width, y + height, 0F).color(color).texture(u + uWidth, v + vHeight).endVertex();
        vertexDataHolder.position(positionMatrix, x + width, y, 0F).color(color).texture(u + uWidth, v).endVertex();
        vertexDataHolder.position(positionMatrix, x, y, 0F).color(color).texture(u, v).endVertex();
    }

}
