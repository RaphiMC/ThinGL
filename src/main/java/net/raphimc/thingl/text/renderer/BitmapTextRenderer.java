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
package net.raphimc.thingl.text.renderer;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.drawbuilder.databuilder.holder.ShaderDataHolder;
import net.raphimc.thingl.drawbuilder.databuilder.holder.Std430ShaderDataHolder;
import net.raphimc.thingl.drawbuilder.drawbatchdataholder.MultiDrawBatchDataHolder;
import net.raphimc.thingl.text.TextSegment;
import net.raphimc.thingl.text.font.Font;
import net.raphimc.thingl.text.shaping.ShapedTextSegment;
import org.joml.Matrix4f;

public class BitmapTextRenderer extends TextRenderer {

    public BitmapTextRenderer() {
        super(() -> ThinGL.programs().getBitmapText());
    }

    @Override
    protected void renderTextSegment(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final ShapedTextSegment textSegment, final float x, final float y, float z) {
        final ShaderDataHolder textDataHolder = multiDrawBatchDataHolder.getShaderStorageDataHolder(this.getTextDrawBatch(), "ssbo_TextData", Std430ShaderDataHolder.SUPPLIER).ensureInTopLevelArray();
        final int regularTextDataIndex = textDataHolder.beginStruct(Integer.BYTES).putColor(textSegment.color()).endStructAndGetTopLevelArrayIndex();

        if ((textSegment.styleFlags() & TextSegment.STYLE_BOLD_BIT) != 0) {
            if (textSegment.outlineColor().getAlpha() == 0) {
                this.renderTextSegmentOutline(positionMatrix, multiDrawBatchDataHolder, textSegment, x, y, z, regularTextDataIndex);
            } else {
                // bold and outline isn't supported at the same time (yet). Use SDFTextRenderer/BSDFTextRenderer for that.
                final int outlineTextDataIndex = textDataHolder.beginStruct(Integer.BYTES).putColor(textSegment.outlineColor()).endStructAndGetTopLevelArrayIndex();
                this.renderTextSegmentOutline(positionMatrix, multiDrawBatchDataHolder, textSegment, x, y, z, outlineTextDataIndex);
            }
        } else if (textSegment.outlineColor().getAlpha() != 0) {
            final int outlineTextDataIndex = textDataHolder.beginStruct(Integer.BYTES).putColor(textSegment.outlineColor()).endStructAndGetTopLevelArrayIndex();
            this.renderTextSegmentOutline(positionMatrix, multiDrawBatchDataHolder, textSegment, x, y, z, outlineTextDataIndex);
        }

        this.renderTextSegment(positionMatrix, multiDrawBatchDataHolder, textSegment, x, y, z, regularTextDataIndex);
    }

    private void renderTextSegmentOutline(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final ShapedTextSegment textSegment, final float x, final float y, final float z, final int textDataIndex) {
        final float offsetMultiplier = textSegment.glyphs().get(0).fontGlyph().font().getSize() / TextRenderer.BOLD_OFFSET_DIVIDER;
        for (int xOffset = -1; xOffset <= 1; xOffset++) {
            for (int yOffset = -1; yOffset <= 1; yOffset++) {
                if (xOffset != 0 || yOffset != 0) {
                    this.renderTextSegment(positionMatrix, multiDrawBatchDataHolder, textSegment, x + xOffset * offsetMultiplier, y + yOffset * offsetMultiplier, z, textDataIndex);
                }
            }
        }
    }

    @Override
    protected Font.GlyphBitmap createGlyphBitmap(final Font.Glyph fontGlyph) {
        return fontGlyph.font().loadGlyphBitmap(fontGlyph.glyphIndex(), true, false);
    }

}
