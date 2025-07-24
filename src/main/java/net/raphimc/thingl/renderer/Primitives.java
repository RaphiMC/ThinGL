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
package net.raphimc.thingl.renderer;

import net.lenni0451.commons.math.MathUtils;
import net.raphimc.thingl.drawbuilder.BuiltinDrawBatches;
import net.raphimc.thingl.drawbuilder.databuilder.holder.VertexDataHolder;
import net.raphimc.thingl.drawbuilder.drawbatchdataholder.MultiDrawBatchDataHolder;
import net.raphimc.thingl.util.MathUtil;
import org.joml.Math;
import org.joml.Matrix4f;

import java.util.function.BiConsumer;

public class Primitives {

    /**
     * <pre>
     * (xtl, ytl, c) ───── (xbr, ytl, c)
     * │                               │
     * │                               │
     * │                               │
     * (xtl, ybr, c) ───── (xbr, ybr, c)
     * </pre>
     */
    public static void filledRectangle(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float xtl, final float ytl, final float xbr, final float ybr, final int c) {
        filledRectangle(positionMatrix, multiDrawBatchDataHolder, xtl, ybr, 0F, xbr, ybr, 0F, xbr, ytl, 0F, xtl, ytl, 0F, c, c, c, c);
    }

    /**
     * <pre>
     * (xtl, ytl, ctl) ───── (xbr, ytl, ctr)
     * │                                   │
     * │                                   │
     * │                                   │
     * (xtl, ybr, cbl) ───── (xbr, ybr, cbr)
     * </pre>
     */
    public static void filledRectangle(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float xtl, final float ytl, final float xbr, final float ybr, final int cbl, final int cbr, final int ctr, final int ctl) {
        filledRectangle(positionMatrix, multiDrawBatchDataHolder, xtl, ybr, 0F, xbr, ybr, 0F, xbr, ytl, 0F, xtl, ytl, 0F, cbl, cbr, ctr, ctl);
    }

    /**
     * <pre>
     * (xtl, ytl, z, c) ───── (xbr, ytl, z, c)
     * │                                     │
     * │                                     │
     * │                                     │
     * (xtl, ybr, z, c) ───── (xbr, ybr, z, c)
     * </pre>
     */
    public static void filledRectangle(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float xtl, final float ytl, final float xbr, final float ybr, final float z, final int c) {
        filledRectangle(positionMatrix, multiDrawBatchDataHolder, xtl, ybr, z, xbr, ybr, z, xbr, ytl, z, xtl, ytl, z, c, c, c, c);
    }

    /**
     * <pre>
     * (xtl, ytl, z, ctl) ───── (xbr, ytl, z, ctr)
     * │                                         │
     * │                                         │
     * │                                         │
     * (xtl, ybr, z, cbl) ───── (xbr, ybr, z, cbr)
     * </pre>
     */
    public static void filledRectangle(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float xtl, final float ytl, final float xbr, final float ybr, final float z, final int cbl, final int cbr, final int ctr, final int ctl) {
        filledRectangle(positionMatrix, multiDrawBatchDataHolder, xtl, ybr, z, xbr, ybr, z, xbr, ytl, z, xtl, ytl, z, cbl, cbr, ctr, ctl);
    }


    /**
     * <pre>
     * (xtl, ytl, c) ───── (xtr, ytr, c)
     * │                               │
     * │                               │
     * │                               │
     * (xbl, ybl, c) ───── (xbr, ybr, c)
     * </pre>
     */
    public static void filledRectangle(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float xbl, final float ybl, final float xbr, final float ybr, final float xtr, final float ytr, final float xtl, final float ytl, final int c) {
        filledRectangle(positionMatrix, multiDrawBatchDataHolder, xbl, ybl, 0F, xbr, ybr, 0F, xtr, ytr, 0F, xtl, ytl, 0F, c, c, c, c);
    }

    /**
     * <pre>
     * (xtl, ytl, ctl) ───── (xtr, ytr, ctr)
     * │                                   │
     * │                                   │
     * │                                   │
     * (xbl, ybl, cbl) ───── (xbr, ybr, cbr)
     * </pre>
     */
    public static void filledRectangle(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float xbl, final float ybl, final float xbr, final float ybr, final float xtr, final float ytr, final float xtl, final float ytl, final int cbl, final int cbr, final int ctr, final int ctl) {
        filledRectangle(positionMatrix, multiDrawBatchDataHolder, xbl, ybl, 0F, xbr, ybr, 0F, xtr, ytr, 0F, xtl, ytl, 0F, cbl, cbr, ctr, ctl);
    }

    /**
     * <pre>
     * (xtl, ytl, ztl, c) ───── (xtr, ytr, ztr, c)
     * │                                         │
     * │                                         │
     * │                                         │
     * (xbl, ybl, zbl, c) ───── (xbr, ybr, zbr, c)
     * </pre>
     */
    public static void filledRectangle(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float xbl, final float ybl, final float zbl, final float xbr, final float ybr, final float zbr, final float xtr, final float ytr, final float ztr, final float xtl, final float ytl, final float ztl, final int c) {
        filledRectangle(positionMatrix, multiDrawBatchDataHolder, xbl, ybl, zbl, xbr, ybr, zbr, xtr, ytr, ztr, xtl, ytl, ztl, c, c, c, c);
    }

    /**
     * <pre>
     * (xtl, ytl, ztl, ctl) ───── (xtr, ytr, ztr, ctr)
     * │                                             │
     * │                                             │
     * │                                             │
     * (xbl, ybl, zbl, cbl) ───── (xbr, ybr, zbr, cbr)
     * </pre>
     */
    public static void filledRectangle(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float xbl, final float ybl, final float zbl, final float xbr, final float ybr, final float zbr, final float xtr, final float ytr, final float ztr, final float xtl, final float ytl, final float ztl, final int cbl, final int cbr, final int ctr, final int ctl) {
        final VertexDataHolder vertexDataHolder = multiDrawBatchDataHolder.getVertexDataHolder(BuiltinDrawBatches.COLOR_QUAD);

        vertexDataHolder.putVector3f(positionMatrix, xbl, ybl, zbl).putColor(cbl).endVertex();
        vertexDataHolder.putVector3f(positionMatrix, xbr, ybr, zbr).putColor(cbr).endVertex();
        vertexDataHolder.putVector3f(positionMatrix, xtr, ytr, ztr).putColor(ctr).endVertex();
        vertexDataHolder.putVector3f(positionMatrix, xtl, ytl, ztl).putColor(ctl).endVertex();
    }

    /**
     * <pre>
     *         (xm, ym, c)
     *         /         \
     *   /                    \
     * (xl, yl, c) ───── (xr, yr, c)
     * </pre>
     */
    public static void filledTriangle(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float xl, final float yl, final float xm, final float ym, final float xr, final float yr, final int c) {
        filledTriangle(positionMatrix, multiDrawBatchDataHolder, xl, yl, 0F, xm, ym, 0F, xr, yr, 0F, c, c, c);
    }

    /**
     * <pre>
     *           (xm, ym, cm)
     *           /         \
     *     /                    \
     * (xl, yl, cl) ───── (xr, yr, cr)
     * </pre>
     */
    public static void filledTriangle(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float xl, final float yl, final float xm, final float ym, final float xr, final float yr, final int cl, final int cm, final int cr) {
        filledTriangle(positionMatrix, multiDrawBatchDataHolder, xl, yl, 0F, xm, ym, 0F, xr, yr, 0F, cl, cm, cr);
    }

    /**
     * <pre>
     *            (xm, ym, zm, c)
     *            /            \
     *      /                       \
     * (xl, yl, zl, c) ───── (xr, yr, zr, c)
     * </pre>
     */
    public static void filledTriangle(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float xl, final float yl, final float zl, final float xm, final float ym, final float zm, final float xr, final float yr, final float zr, final int c) {
        filledTriangle(positionMatrix, multiDrawBatchDataHolder, xl, yl, zl, xm, ym, zm, xr, yr, zr, c, c, c);
    }

    /**
     * <pre>
     *           (xm, ym, zm, cm)
     *            /            \
     *      /                       \
     * (xl, yl, zl, cl) ───── (xr, yr, zr, cr)
     * </pre>
     */
    public static void filledTriangle(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float xl, final float yl, final float zl, final float xm, final float ym, final float zm, final float xr, final float yr, final float zr, final int cl, final int cm, final int cr) {
        final VertexDataHolder vertexDataHolder = multiDrawBatchDataHolder.getVertexDataHolder(BuiltinDrawBatches.COLOR_TRIANGLE);

        vertexDataHolder.putVector3f(positionMatrix, xl, yl, zl).putColor(cl).endVertex();
        vertexDataHolder.putVector3f(positionMatrix, xm, ym, zm).putColor(cm).endVertex();
        vertexDataHolder.putVector3f(positionMatrix, xr, yr, zr).putColor(cr).endVertex();
    }

    public static void filledCircle(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float x, final float y, final float radius, final int c) {
        filledCircle(positionMatrix, multiDrawBatchDataHolder, x, y, 0F, radius, 0, 360, c);
    }

    public static void filledCircle(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float x, final float y, final float z, final float radius, final int c) {
        filledCircle(positionMatrix, multiDrawBatchDataHolder, x, y, z, radius, 0, 360, c);
    }

    public static void filledCircle(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float x, final float y, final float radius, final float degStart, final float degEnd, final int c) {
        filledCircle(positionMatrix, multiDrawBatchDataHolder, x, y, 0F, radius, degStart, degEnd, c);
    }

    public static void filledCircle(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float x, final float y, final float z, final float radius, final float degStart, final float degEnd, final int c) {
        final VertexDataHolder vertexDataHolder = multiDrawBatchDataHolder.getVertexDataHolder(BuiltinDrawBatches.COLOR_TRIANGLE_FAN);

        vertexDataHolder.putVector3f(positionMatrix, x, y, z).putColor(c).endVertex();
        _filledCircle(positionMatrix, vertexDataHolder, x, y, z, radius, degStart, degEnd, c);
        vertexDataHolder.endConnectedPrimitive();
    }

    public static void outlinedCircle(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float x, final float y, final float radius, final float w, final int c) {
        outlinedCircle(positionMatrix, multiDrawBatchDataHolder, x, y, 0F, radius, w, 0, 360, c);
    }

    public static void outlinedCircle(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float x, final float y, final float z, final float radius, final float w, final int c) {
        outlinedCircle(positionMatrix, multiDrawBatchDataHolder, x, y, z, radius, w, 0, 360, c);
    }

    public static void outlinedCircle(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float x, final float y, final float radius, final float w, final float degStart, final float degEnd, final int c) {
        outlinedCircle(positionMatrix, multiDrawBatchDataHolder, x, y, 0F, radius, w, degStart, degEnd, c);
    }

    public static void outlinedCircle(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float x, final float y, final float z, final float radius, final float w, final float degStart, final float degEnd, final int c) {
        final VertexDataHolder vertexDataHolder = multiDrawBatchDataHolder.getVertexDataHolder(BuiltinDrawBatches.COLOR_TRIANGLE_STRIP);

        _outlinedCircle(positionMatrix, vertexDataHolder, x, y, z, radius, w, degStart, degEnd, c);
        vertexDataHolder.endConnectedPrimitive();
    }

    /**
     * <pre>
     * (x1, y1, c) ───── (x2, y2, c)
     * </pre>
     */
    public static void rectLine(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float x1, final float y1, final float x2, final float y2, final float w, final int c) {
        rectLine(positionMatrix, multiDrawBatchDataHolder, x1, y1, x2, y2, w, c, c);
    }

    /**
     * <pre>
     * (x1, y1, c1) ───── (x2, y2, c2)
     * </pre>
     */
    public static void rectLine(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float x1, final float y1, final float x2, final float y2, final float w, final int c1, final int c2) {
        final float angle = MathUtil.PI - Math.atan2(y2 - y1, x2 - x1);
        final float xOffset = Math.sin(angle) * w / 2F;
        final float yOffset = -Math.cos(angle) * w / 2F;

        filledRectangle(positionMatrix, multiDrawBatchDataHolder, x1 - xOffset, y1 + yOffset, x2 - xOffset, y2 + yOffset, x2 + xOffset, y2 - yOffset, x1 + xOffset, y1 - yOffset, c1, c2, c1, c2);
    }

    /**
     * <pre>
     * (x1, y1, c) ───── (x2, y2, c)
     * </pre>
     */
    public static void glLine(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float x1, final float y1, final float x2, final float y2, final int c) {
        glLine(positionMatrix, multiDrawBatchDataHolder, x1, y1, 0F, x2, y2, 0F, c, c);
    }

    /**
     * <pre>
     * (x1, y1, z1, c) ───── (x2, y2, z2, c)
     * </pre>
     */
    public static void glLine(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float x1, final float y1, final float z1, final float x2, final float y2, final float z2, final int c) {
        glLine(positionMatrix, multiDrawBatchDataHolder, x1, y1, z1, x2, y2, z2, c, c);
    }

    /**
     * <pre>
     * (x1, y1, c1) ───── (x2, y2, c2)
     * </pre>
     */
    public static void glLine(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float x1, final float y1, final float x2, final float y2, final int c1, final int c2) {
        glLine(positionMatrix, multiDrawBatchDataHolder, x1, y1, 0F, x2, y2, 0F, c1, c2);
    }

    /**
     * <pre>
     * (x1, y1, z1, c1) ───── (x2, y2, z2, c2)
     * </pre>
     */
    public static void glLine(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float x1, final float y1, final float z1, final float x2, final float y2, final float z2, final int c1, final int c2) {
        final VertexDataHolder vertexDataHolder = multiDrawBatchDataHolder.getVertexDataHolder(BuiltinDrawBatches.COLOR_GL_LINE);

        vertexDataHolder.putVector3f(positionMatrix, x1, y1, z1).putColor(c1).endVertex();
        vertexDataHolder.putVector3f(positionMatrix, x2, y2, z2).putColor(c2).endVertex();
    }

    /**
     * <pre>
     * (x1, y1, c) ───── (x2, y2, c)
     * </pre>
     */
    public static void line(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float x1, final float y1, final float x2, final float y2, final float width, final int c) {
        line(positionMatrix, multiDrawBatchDataHolder, x1, y1, 0F, x2, y2, 0F, width, c, c);
    }

    /**
     * <pre>
     * (x1, y1, z1, c) ───── (x2, y2, z2, c)
     * </pre>
     */
    public static void line(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float x1, final float y1, final float z1, final float x2, final float y2, final float z2, final float width, final int c) {
        line(positionMatrix, multiDrawBatchDataHolder, x1, y1, z1, x2, y2, z2, width, c, c);
    }

    /**
     * <pre>
     * (x1, y1, c1) ───── (x2, y2, c2)
     * </pre>
     */
    public static void line(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float x1, final float y1, final float x2, final float y2, final float width, final int c1, final int c2) {
        line(positionMatrix, multiDrawBatchDataHolder, x1, y1, 0F, x2, y2, 0F, width, c1, c2);
    }

    /**
     * <pre>
     * (x1, y1, z1, c1) ───── (x2, y2, z2, c2)
     * </pre>
     */
    public static void line(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float x1, final float y1, final float z1, final float x2, final float y2, final float z2, final float width, final int c1, final int c2) {
        final VertexDataHolder vertexDataHolder = multiDrawBatchDataHolder.getVertexDataHolder(BuiltinDrawBatches.COLOR_LINE);

        vertexDataHolder.putVector3f(positionMatrix, x1, y1, z1).putColor(c1).putFloat(width).endVertex();
        vertexDataHolder.putVector3f(positionMatrix, x2, y2, z2).putColor(c2).putFloat(width).endVertex();
    }


    public static void _filledCircle(final Matrix4f positionMatrix, final VertexDataHolder vertexDataHolder, final float x, final float y, final float z, final float radius, final float degStart, final float degEnd, final int c) {
        _circle(radius, degStart, degEnd, (xc, yc) -> vertexDataHolder.putVector3f(positionMatrix, x + xc, y + yc, z).putColor(c).endVertex());
    }

    public static void _outlinedCircle(final Matrix4f positionMatrix, final VertexDataHolder vertexDataHolder, final float x, final float y, final float z, final float radius, final float w, final float degStart, final float degEnd, final int c) {
        _circle(radius, degStart, degEnd, (xc, yc) -> {
            vertexDataHolder.putVector3f(positionMatrix, x + (xc / radius * (radius - w / 2F)), y + (yc / radius * (radius - w / 2F)), z).putColor(c).endVertex();
            vertexDataHolder.putVector3f(positionMatrix, x + (xc / radius * (radius + w / 2F)), y + (yc / radius * (radius + w / 2F)), z).putColor(c).endVertex();
        });
    }

    public static void _circle(final float radius, final float degStart, final float degEnd, final BiConsumer<Float, Float> valueConsumer) {
        final float stepSize = MathUtils.clamp(180F / (MathUtil.PI * radius), 2F, 20F);

        for (float angle = degEnd; angle >= degStart; angle -= stepSize) {
            final float rad = Math.toRadians(angle) - MathUtil.HALF_PI;
            final float x = radius * Math.cos(rad);
            final float y = radius * Math.sin(rad);
            valueConsumer.accept(x, y);
        }

        final float radStart = Math.toRadians(degStart) - MathUtil.HALF_PI;
        final float xStart = radius * Math.cos(radStart);
        final float yStart = radius * Math.sin(radStart);
        valueConsumer.accept(xStart, yStart);
    }

}
