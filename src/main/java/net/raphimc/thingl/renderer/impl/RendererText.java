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

public class RendererText extends Renderer {

    protected final TextRenderer textRenderer;
    private final FloatStack globalScaleStack = new FloatArrayList();

    public RendererText(final TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
    }

    public void textBuffer(final Matrix4f positionMatrix, final TextBuffer textBuffer, final float x, final float y) {
        this.textBuffer(positionMatrix, textBuffer.shape(), x, y);
    }

    public void textBuffer(final Matrix4f positionMatrix, final TextBuffer textBuffer, final float x, final float y, final float z) {
        this.textBuffer(positionMatrix, textBuffer.shape(), x, y, z);
    }

    public void textBuffer(final Matrix4f positionMatrix, final TextBuffer textBuffer, final float x, final float y, final float z, final int flags) {
        this.textBuffer(positionMatrix, textBuffer.shape(), x, y, z, flags);
    }

    public void textBuffer(final Matrix4f positionMatrix, final ShapedTextBuffer textBuffer, final float x, final float y) {
        this.textBuffer(positionMatrix, textBuffer, x, y, 0);
    }

    public void textBuffer(final Matrix4f positionMatrix, final ShapedTextBuffer textBuffer, final float x, final float y, final float z) {
        this.textBuffer(positionMatrix, textBuffer, x, y, z, 0);
    }

    public void textBuffer(final Matrix4f positionMatrix, final ShapedTextBuffer textBuffer, final float x, final float y, final float z, final int flags) {
        this.textRenderer.renderTextBuffer(positionMatrix, this.targetMultiDrawBatchDataHolder, textBuffer, x, y, z, flags);
        this.drawIfNotBuffering();
    }

    public void textRun(final Matrix4f positionMatrix, final TextRun textRun, final float x, final float y) {
        this.textRun(positionMatrix, textRun.shape(), x, y);
    }

    public void textRun(final Matrix4f positionMatrix, final TextRun textRun, final float x, final float y, final float z) {
        this.textRun(positionMatrix, textRun.shape(), x, y, z);
    }

    public void textRun(final Matrix4f positionMatrix, final TextRun textRun, final float x, final float y, final float z, final int flags) {
        this.textRun(positionMatrix, textRun.shape(), x, y, z, flags);
    }

    public void textRun(final Matrix4f positionMatrix, final ShapedTextRun textRun, final float x, final float y) {
        this.textRun(positionMatrix, textRun, x, y, 0);
    }

    public void textRun(final Matrix4f positionMatrix, final ShapedTextRun textRun, final float x, final float y, final float z) {
        this.textRun(positionMatrix, textRun, x, y, z, 0);
    }

    public void textRun(final Matrix4f positionMatrix, final ShapedTextRun textRun, final float x, final float y, final float z, final int flags) {
        this.textRenderer.renderTextRun(positionMatrix, this.targetMultiDrawBatchDataHolder, textRun, x, y, z, flags);
        this.drawIfNotBuffering();
    }

    public float getWidth(final ShapedTextRun textRun) {
        return textRun.bounds().lengthX() * this.textRenderer.getGlobalScale();
    }

    public float getWidth(final ShapedTextBuffer textBuffer) {
        return textBuffer.bounds().lengthX() * this.textRenderer.getGlobalScale();
    }

    public float getHeight(final ShapedTextRun textRun) {
        return textRun.bounds().lengthY() * this.textRenderer.getGlobalScale();
    }

    public float getHeight(final ShapedTextBuffer textBuffer) {
        return textBuffer.bounds().lengthY() * this.textRenderer.getGlobalScale();
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

    @Override
    public void free() {
        super.free();
        this.textRenderer.free();
    }

}
