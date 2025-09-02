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

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatStack;
import net.raphimc.thingl.renderer.Renderer;
import net.raphimc.thingl.text.TextBlock;
import net.raphimc.thingl.text.TextLine;
import net.raphimc.thingl.text.TextRun;
import net.raphimc.thingl.text.renderer.TextRenderer;
import net.raphimc.thingl.text.shaping.ShapedTextBlock;
import net.raphimc.thingl.text.shaping.ShapedTextLine;
import net.raphimc.thingl.text.shaping.ShapedTextRun;
import org.joml.Matrix4f;

import java.util.Stack;

public class RendererText extends Renderer {

    protected final TextRenderer textRenderer;
    private final FloatStack globalScaleStack = new FloatArrayList();
    private final Stack<VerticalOrigin> verticalOriginStack = new Stack<>();
    private final Stack<HorizontalOrigin> horizontalOriginStack = new Stack<>();

    public RendererText(final TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
        this.verticalOriginStack.push(VerticalOrigin.VISUAL_TOP);
        this.horizontalOriginStack.push(HorizontalOrigin.VISUAL_LEFT);
    }

    public void textBlock(final Matrix4f positionMatrix, final TextBlock textBlock, final float x, final float y) {
        this.textBlock(positionMatrix, textBlock.shape(), x, y);
    }

    public void textBlock(final Matrix4f positionMatrix, final TextBlock textBlock, final float x, final float y, final VerticalOrigin verticalOrigin, final HorizontalOrigin horizontalOrigin) {
        this.textBlock(positionMatrix, textBlock.shape(), x, y, verticalOrigin, horizontalOrigin);
    }

    public void textBlock(final Matrix4f positionMatrix, final TextBlock textBlock, final float x, final float y, final float z) {
        this.textBlock(positionMatrix, textBlock.shape(), x, y, z);
    }

    public void textBlock(final Matrix4f positionMatrix, final TextBlock textBlock, final float x, final float y, final float z, final VerticalOrigin verticalOrigin, final HorizontalOrigin horizontalOrigin) {
        this.textBlock(positionMatrix, textBlock.shape(), x, y, z, verticalOrigin, horizontalOrigin);
    }

    public void textBlock(final Matrix4f positionMatrix, final ShapedTextBlock textBlock, final float x, final float y) {
        this.textBlock(positionMatrix, textBlock, x, y, this.getVerticalOrigin(), this.getHorizontalOrigin());
    }

    public void textBlock(final Matrix4f positionMatrix, final ShapedTextBlock textBlock, final float x, final float y, final VerticalOrigin verticalOrigin, final HorizontalOrigin horizontalOrigin) {
        this.textBlock(positionMatrix, textBlock, x, y, 0, verticalOrigin, horizontalOrigin);
    }

    public void textBlock(final Matrix4f positionMatrix, final ShapedTextBlock textBlock, final float x, final float y, final float z) {
        this.textBlock(positionMatrix, textBlock, x, y, z, this.getVerticalOrigin(), this.getHorizontalOrigin());
    }

    public void textBlock(final Matrix4f positionMatrix, final ShapedTextBlock textBlock, float x, float y, final float z, final VerticalOrigin verticalOrigin, final HorizontalOrigin horizontalOrigin) {
        y -= switch (verticalOrigin) {
            case BASELINE -> 0;
            case LOGICAL_TOP -> textBlock.logicalBounds().minY;
            case LOGICAL_CENTER -> textBlock.logicalBounds().minY + textBlock.logicalBounds().lengthY() / 2F;
            case LOGICAL_BOTTOM -> textBlock.logicalBounds().maxY;
            case VISUAL_TOP -> textBlock.visualBounds().minY;
            case VISUAL_CENTER -> textBlock.visualBounds().minY + textBlock.visualBounds().lengthY() / 2F;
            case VISUAL_BOTTOM -> textBlock.visualBounds().maxY;
        } * this.textRenderer.getGlobalScale();
        x -= switch (horizontalOrigin) {
            case LOGICAL_LEFT -> 0;
            case VISUAL_LEFT -> textBlock.visualBounds().minX;
            case VISUAL_CENTER -> textBlock.visualBounds().lengthX() / 2F;
            case VISUAL_RIGHT -> textBlock.visualBounds().maxX;
        } * this.textRenderer.getGlobalScale();

        this.textRenderer.renderTextBlock(positionMatrix, this.targetMultiDrawBatchDataHolder, textBlock, x, y, z);
        this.drawIfNotBuffering();
    }

    public void textLine(final Matrix4f positionMatrix, final TextLine textLine, final float x, final float y) {
        this.textLine(positionMatrix, textLine.shape(), x, y);
    }

    public void textLine(final Matrix4f positionMatrix, final TextLine textLine, final float x, final float y, final VerticalOrigin verticalOrigin, final HorizontalOrigin horizontalOrigin) {
        this.textLine(positionMatrix, textLine.shape(), x, y, verticalOrigin, horizontalOrigin);
    }

    public void textLine(final Matrix4f positionMatrix, final TextLine textLine, final float x, final float y, final float z) {
        this.textLine(positionMatrix, textLine.shape(), x, y, z);
    }

    public void textLine(final Matrix4f positionMatrix, final TextLine textLine, final float x, final float y, final float z, final VerticalOrigin verticalOrigin, final HorizontalOrigin horizontalOrigin) {
        this.textLine(positionMatrix, textLine.shape(), x, y, z, verticalOrigin, horizontalOrigin);
    }

    public void textLine(final Matrix4f positionMatrix, final ShapedTextLine textLine, final float x, final float y) {
        this.textLine(positionMatrix, textLine, x, y, this.getVerticalOrigin(), this.getHorizontalOrigin());
    }

    public void textLine(final Matrix4f positionMatrix, final ShapedTextLine textLine, final float x, final float y, final VerticalOrigin verticalOrigin, final HorizontalOrigin horizontalOrigin) {
        this.textLine(positionMatrix, textLine, x, y, 0, verticalOrigin, horizontalOrigin);
    }

    public void textLine(final Matrix4f positionMatrix, final ShapedTextLine textLine, final float x, final float y, final float z) {
        this.textLine(positionMatrix, textLine, x, y, z, this.getVerticalOrigin(), this.getHorizontalOrigin());
    }

    public void textLine(final Matrix4f positionMatrix, final ShapedTextLine textLine, float x, float y, final float z, final VerticalOrigin verticalOrigin, final HorizontalOrigin horizontalOrigin) {
        y -= switch (verticalOrigin) {
            case BASELINE -> 0;
            case LOGICAL_TOP -> textLine.logicalBounds().minY;
            case LOGICAL_CENTER -> textLine.logicalBounds().minY + textLine.logicalBounds().lengthY() / 2F;
            case LOGICAL_BOTTOM -> textLine.logicalBounds().maxY;
            case VISUAL_TOP -> textLine.visualBounds().minY;
            case VISUAL_CENTER -> textLine.visualBounds().minY + textLine.visualBounds().lengthY() / 2F;
            case VISUAL_BOTTOM -> textLine.visualBounds().maxY;
        } * this.textRenderer.getGlobalScale();
        x -= switch (horizontalOrigin) {
            case LOGICAL_LEFT -> 0;
            case VISUAL_LEFT -> textLine.visualBounds().minX;
            case VISUAL_CENTER -> textLine.visualBounds().lengthX() / 2F;
            case VISUAL_RIGHT -> textLine.visualBounds().maxX;
        } * this.textRenderer.getGlobalScale();

        this.textRenderer.renderTextLine(positionMatrix, this.targetMultiDrawBatchDataHolder, textLine, x, y, z);
        this.drawIfNotBuffering();
    }

    public void textRun(final Matrix4f positionMatrix, final TextRun textRun, final float x, final float y) {
        this.textRun(positionMatrix, textRun.shape(), x, y);
    }

    public void textRun(final Matrix4f positionMatrix, final TextRun textRun, final float x, final float y, final VerticalOrigin verticalOrigin, final HorizontalOrigin horizontalOrigin) {
        this.textRun(positionMatrix, textRun.shape(), x, y, verticalOrigin, horizontalOrigin);
    }

    public void textRun(final Matrix4f positionMatrix, final TextRun textRun, final float x, final float y, final float z) {
        this.textRun(positionMatrix, textRun.shape(), x, y, z);
    }

    public void textRun(final Matrix4f positionMatrix, final TextRun textRun, final float x, final float y, final float z, final VerticalOrigin verticalOrigin, final HorizontalOrigin horizontalOrigin) {
        this.textRun(positionMatrix, textRun.shape(), x, y, z, verticalOrigin, horizontalOrigin);
    }

    public void textRun(final Matrix4f positionMatrix, final ShapedTextRun textRun, final float x, final float y) {
        this.textRun(positionMatrix, textRun, x, y, this.getVerticalOrigin(), this.getHorizontalOrigin());
    }

    public void textRun(final Matrix4f positionMatrix, final ShapedTextRun textRun, final float x, final float y, final VerticalOrigin verticalOrigin, final HorizontalOrigin horizontalOrigin) {
        this.textRun(positionMatrix, textRun, x, y, 0, verticalOrigin, horizontalOrigin);
    }

    public void textRun(final Matrix4f positionMatrix, final ShapedTextRun textRun, final float x, final float y, final float z) {
        this.textRun(positionMatrix, textRun, x, y, z, this.getVerticalOrigin(), this.getHorizontalOrigin());
    }

    public void textRun(final Matrix4f positionMatrix, final ShapedTextRun textRun, float x, float y, final float z, final VerticalOrigin verticalOrigin, final HorizontalOrigin horizontalOrigin) {
        y -= switch (verticalOrigin) {
            case BASELINE -> 0;
            case LOGICAL_TOP -> textRun.logicalBounds().minY;
            case LOGICAL_CENTER -> textRun.logicalBounds().minY + textRun.logicalBounds().lengthY() / 2F;
            case LOGICAL_BOTTOM -> textRun.logicalBounds().maxY;
            case VISUAL_TOP -> textRun.visualBounds().minY;
            case VISUAL_CENTER -> textRun.visualBounds().minY + textRun.visualBounds().lengthY() / 2F;
            case VISUAL_BOTTOM -> textRun.visualBounds().maxY;
        } * this.textRenderer.getGlobalScale();
        x -= switch (horizontalOrigin) {
            case LOGICAL_LEFT -> 0;
            case VISUAL_LEFT -> textRun.visualBounds().minX;
            case VISUAL_CENTER -> textRun.visualBounds().lengthX() / 2F;
            case VISUAL_RIGHT -> textRun.visualBounds().maxX;
        } * this.textRenderer.getGlobalScale();

        this.textRenderer.renderTextRun(positionMatrix, this.targetMultiDrawBatchDataHolder, textRun, x, y, z);
        this.drawIfNotBuffering();
    }

    public float getVisualWidth(final ShapedTextLine textLine) {
        return textLine.visualBounds().lengthX() * this.textRenderer.getGlobalScale();
    }

    public float getVisualWidth(final ShapedTextRun textRun) {
        return textRun.visualBounds().lengthX() * this.textRenderer.getGlobalScale();
    }

    public float getVisualHeight(final ShapedTextLine textLine) {
        return textLine.visualBounds().lengthY() * this.textRenderer.getGlobalScale();
    }

    public float getVisualHeight(final ShapedTextRun textRun) {
        return textRun.visualBounds().lengthY() * this.textRenderer.getGlobalScale();
    }

    public float getLogicalHeight(final ShapedTextLine textLine) {
        return textLine.logicalBounds().lengthY() * this.textRenderer.getGlobalScale();
    }

    public float getLogicalHeight(final ShapedTextRun textRun) {
        return textRun.logicalBounds().lengthY() * this.textRenderer.getGlobalScale();
    }

    public void pushGlobalScale(final float scale) {
        this.globalScaleStack.push(this.textRenderer.getGlobalScale());
        this.textRenderer.setGlobalScale(scale);
    }

    public void popGlobalScale() {
        this.textRenderer.setGlobalScale(this.globalScaleStack.popFloat());
    }

    public float getGlobalScale() {
        return this.textRenderer.getGlobalScale();
    }

    public void pushVerticalOrigin(final VerticalOrigin verticalOrigin) {
        this.verticalOriginStack.push(verticalOrigin);
    }

    public void popVerticalOrigin() {
        this.verticalOriginStack.pop();
    }

    public VerticalOrigin getVerticalOrigin() {
        return this.verticalOriginStack.peek();
    }

    public void pushHorizontalOrigin(final HorizontalOrigin horizontalOrigin) {
        this.horizontalOriginStack.push(horizontalOrigin);
    }

    public void popHorizontalOrigin() {
        this.horizontalOriginStack.pop();
    }

    public HorizontalOrigin getHorizontalOrigin() {
        return this.horizontalOriginStack.peek();
    }

    @Override
    public void free() {
        super.free();
        this.textRenderer.free();
    }

    public enum VerticalOrigin {

        BASELINE,
        LOGICAL_TOP,
        LOGICAL_CENTER,
        LOGICAL_BOTTOM,
        VISUAL_TOP,
        VISUAL_CENTER,
        VISUAL_BOTTOM,

    }

    public enum HorizontalOrigin {

        LOGICAL_LEFT,
        VISUAL_LEFT,
        VISUAL_CENTER,
        VISUAL_RIGHT,

    }

}
