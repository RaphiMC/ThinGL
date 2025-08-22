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
import net.raphimc.thingl.text.TextBuffer;
import net.raphimc.thingl.text.TextRun;
import net.raphimc.thingl.text.renderer.TextRenderer;
import net.raphimc.thingl.text.shaper.ShapedTextBuffer;
import net.raphimc.thingl.text.shaper.ShapedTextRun;
import org.joml.Matrix4f;

import java.util.Stack;

public class RendererText extends Renderer {

    protected final TextRenderer textRenderer;
    private final FloatStack globalScaleStack = new FloatArrayList();
    private final Stack<VerticalOrigin> verticalOriginStack = new Stack<>();
    private final Stack<HorizontalOrigin> horizontalOriginStack = new Stack<>();

    public RendererText(final TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
        this.verticalOriginStack.push(VerticalOrigin.EXACT_TOP);
        this.horizontalOriginStack.push(HorizontalOrigin.EXACT_LEFT);
    }

    public void textBuffer(final Matrix4f positionMatrix, final TextBuffer textBuffer, final float x, final float y) {
        this.textBuffer(positionMatrix, textBuffer.shape(), x, y);
    }

    public void textBuffer(final Matrix4f positionMatrix, final TextBuffer textBuffer, final float x, final float y, final VerticalOrigin verticalOrigin, final HorizontalOrigin horizontalOrigin) {
        this.textBuffer(positionMatrix, textBuffer.shape(), x, y, verticalOrigin, horizontalOrigin);
    }

    public void textBuffer(final Matrix4f positionMatrix, final TextBuffer textBuffer, final float x, final float y, final float z) {
        this.textBuffer(positionMatrix, textBuffer.shape(), x, y, z);
    }

    public void textBuffer(final Matrix4f positionMatrix, final TextBuffer textBuffer, final float x, final float y, final float z, final VerticalOrigin verticalOrigin, final HorizontalOrigin horizontalOrigin) {
        this.textBuffer(positionMatrix, textBuffer.shape(), x, y, z, verticalOrigin, horizontalOrigin);
    }

    public void textBuffer(final Matrix4f positionMatrix, final ShapedTextBuffer textBuffer, final float x, final float y) {
        this.textBuffer(positionMatrix, textBuffer, x, y, this.getVerticalOrigin(), this.getHorizontalOrigin());
    }

    public void textBuffer(final Matrix4f positionMatrix, final ShapedTextBuffer textBuffer, final float x, final float y, final VerticalOrigin verticalOrigin, final HorizontalOrigin horizontalOrigin) {
        this.textBuffer(positionMatrix, textBuffer, x, y, 0, verticalOrigin, horizontalOrigin);
    }

    public void textBuffer(final Matrix4f positionMatrix, final ShapedTextBuffer textBuffer, final float x, final float y, final float z) {
        this.textBuffer(positionMatrix, textBuffer, x, y, z, this.getVerticalOrigin(), this.getHorizontalOrigin());
    }

    public void textBuffer(final Matrix4f positionMatrix, final ShapedTextBuffer textBuffer, float x, float y, final float z, final VerticalOrigin verticalOrigin, final HorizontalOrigin horizontalOrigin) {
        y -= switch (verticalOrigin) {
            case BASELINE -> 0;
            case TOP -> textBuffer.fontBounds().minY;
            case CENTER -> textBuffer.fontBounds().minY + textBuffer.fontBounds().lengthY() / 2F;
            case BOTTOM -> textBuffer.fontBounds().maxY;
            case EXACT_TOP -> textBuffer.bounds().minY;
            case EXACT_CENTER -> textBuffer.bounds().minY + textBuffer.bounds().lengthY() / 2F;
            case EXACT_BOTTOM -> textBuffer.bounds().maxY;
        } * this.textRenderer.getGlobalScale();
        x -= switch (horizontalOrigin) {
            case LEFT -> 0;
            case EXACT_LEFT -> textBuffer.bounds().minX;
            case EXACT_CENTER -> textBuffer.bounds().lengthX() / 2F;
            case EXACT_RIGHT -> textBuffer.bounds().maxX;
        } * this.textRenderer.getGlobalScale();

        this.textRenderer.renderTextBuffer(positionMatrix, this.targetMultiDrawBatchDataHolder, textBuffer, x, y, z);
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
            case TOP -> textRun.fontBounds().minY;
            case CENTER -> textRun.fontBounds().minY + textRun.fontBounds().lengthY() / 2F;
            case BOTTOM -> textRun.fontBounds().maxY;
            case EXACT_TOP -> textRun.bounds().minY;
            case EXACT_CENTER -> textRun.bounds().minY + textRun.bounds().lengthY() / 2F;
            case EXACT_BOTTOM -> textRun.bounds().maxY;
        } * this.textRenderer.getGlobalScale();
        x -= switch (horizontalOrigin) {
            case LEFT -> 0;
            case EXACT_LEFT -> textRun.bounds().minX;
            case EXACT_CENTER -> textRun.bounds().lengthX() / 2F;
            case EXACT_RIGHT -> textRun.bounds().maxX;
        } * this.textRenderer.getGlobalScale();

        this.textRenderer.renderTextRun(positionMatrix, this.targetMultiDrawBatchDataHolder, textRun, x, y, z);
        this.drawIfNotBuffering();
    }

    public float getExactWidth(final ShapedTextBuffer textBuffer) {
        return textBuffer.bounds().lengthX() * this.textRenderer.getGlobalScale();
    }

    public float getExactWidth(final ShapedTextRun textRun) {
        return textRun.bounds().lengthX() * this.textRenderer.getGlobalScale();
    }

    public float getExactHeight(final ShapedTextBuffer textBuffer) {
        return textBuffer.bounds().lengthY() * this.textRenderer.getGlobalScale();
    }

    public float getExactHeight(final ShapedTextRun textRun) {
        return textRun.bounds().lengthY() * this.textRenderer.getGlobalScale();
    }

    public float getFontHeight(final ShapedTextBuffer textBuffer) {
        return textBuffer.fontBounds().lengthY() * this.textRenderer.getGlobalScale();
    }

    public float getFontHeight(final ShapedTextRun textRun) {
        return textRun.fontBounds().lengthY() * this.textRenderer.getGlobalScale();
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
        TOP,
        CENTER,
        BOTTOM,
        EXACT_TOP,
        EXACT_CENTER,
        EXACT_BOTTOM,

    }

    public enum HorizontalOrigin {

        LEFT,
        EXACT_LEFT,
        EXACT_CENTER,
        EXACT_RIGHT,

    }

}
