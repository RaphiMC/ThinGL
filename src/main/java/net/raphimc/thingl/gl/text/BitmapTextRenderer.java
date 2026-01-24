/*
 * This file is part of ThinGL - https://github.com/RaphiMC/ThinGL
 * Copyright (C) 2024-2026 RK_01/RaphiMC and contributors
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
package net.raphimc.thingl.gl.text;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.rendering.bufferbuilder.ShaderBufferBuilder;
import net.raphimc.thingl.rendering.bufferbuilder.impl.Std430ShaderBufferBuilder;
import net.raphimc.thingl.rendering.dataholder.MultiDrawBatchDataHolder;
import net.raphimc.thingl.resource.font.Font;
import net.raphimc.thingl.text.TextStyle;
import net.raphimc.thingl.text.shaping.ShapedTextSegment;
import org.joml.Matrix4f;

public class BitmapTextRenderer extends TextRenderer {

    public BitmapTextRenderer() {
        this(Font.GlyphBitmap.RenderMode.ANTIALIASED);
    }

    public BitmapTextRenderer(final Font.GlyphBitmap.RenderMode glyphRenderMode) {
        this(glyphRenderMode, glyphRenderMode == Font.GlyphBitmap.RenderMode.ANTIALIASED);
    }

    public BitmapTextRenderer(final Font.GlyphBitmap.RenderMode glyphRenderMode, final boolean edgeSharpening) {
        super(() -> ThinGL.programs().getBitmapText(), glyphRenderMode, p -> p.setUniformBoolean("u_EdgeSharpening", edgeSharpening));
    }

    @Override
    protected void renderTextSegment(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final ShapedTextSegment textSegment, final float x, final float y, float z) {
        final ShaderBufferBuilder textDataBufferBuilder = multiDrawBatchDataHolder.getShaderStorageBufferBuilder(this.getDrawBatch(), "ssbo_TextData", Std430ShaderBufferBuilder.SUPPLIER).ensureInTopLevelArray();
        final TextStyle textStyle = textSegment.style();
        final int regularTextDataIndex = textDataBufferBuilder.beginStruct(Integer.BYTES).writeColor(textStyle.color()).endStructAndGetTopLevelArrayIndex();

        if (textStyle.isBold()) {
            if (textStyle.outlineColor().getAlpha() == 0) {
                this.renderTextSegmentOutline(positionMatrix, multiDrawBatchDataHolder, textSegment, x, y, z, regularTextDataIndex);
            } else {
                // bold and outline isn't supported at the same time (yet). Use SDFTextRenderer for that.
                final int outlineTextDataIndex = textDataBufferBuilder.beginStruct(Integer.BYTES).writeColor(textStyle.outlineColor()).endStructAndGetTopLevelArrayIndex();
                this.renderTextSegmentOutline(positionMatrix, multiDrawBatchDataHolder, textSegment, x, y, z, outlineTextDataIndex);
            }
        } else if (textStyle.outlineColor().getAlpha() != 0) {
            final int outlineTextDataIndex = textDataBufferBuilder.beginStruct(Integer.BYTES).writeColor(textStyle.outlineColor()).endStructAndGetTopLevelArrayIndex();
            this.renderTextSegmentOutline(positionMatrix, multiDrawBatchDataHolder, textSegment, x, y, z, outlineTextDataIndex);
        }

        this.renderTextSegment(positionMatrix, multiDrawBatchDataHolder, textSegment, x, y, z, regularTextDataIndex);
    }

    private void renderTextSegmentOutline(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final ShapedTextSegment textSegment, final float x, final float y, final float z, final int textDataIndex) {
        final float offsetMultiplier = textSegment.glyphs().getFirst().fontGlyph().font().getSize() / TextRenderer.BOLD_OFFSET_DIVIDER;
        for (int xOffset = -1; xOffset <= 1; xOffset++) {
            for (int yOffset = -1; yOffset <= 1; yOffset++) {
                if (xOffset != 0 || yOffset != 0) {
                    this.renderTextSegment(positionMatrix, multiDrawBatchDataHolder, textSegment, x + xOffset * offsetMultiplier, y + yOffset * offsetMultiplier, z, textDataIndex);
                }
            }
        }
    }

}
