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
import net.raphimc.thingl.text.font.Font;
import net.raphimc.thingl.text.shaper.ShapedTextSegment;
import org.joml.Matrix4f;

public class SDFTextRenderer extends TextRenderer {

    public static final int DF_PX_RANGE = 6;

    public SDFTextRenderer() {
        super(() -> ThinGL.programs().getSdfText());
    }

    @Override
    protected void renderTextSegment(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final ShapedTextSegment textSegment, final float x, final float y, final float z) {
        final ShaderDataHolder textDataHolder = multiDrawBatchDataHolder.getShaderDataHolder(this.getTextDrawBatch(), "ssbo_TextData", Std430ShaderDataHolder.SUPPLIER).ensureInTopLevelArray();
        final int fontSize = textSegment.glyphs().get(0).fontGlyph().font().getSize();
        final int textDataIndex = textDataHolder.beginStruct(Integer.BYTES).putInt(fontSize).putColor(textSegment.color()).putColor(textSegment.outlineColor()).putInt(textSegment.styleFlags()).endStructAndGetTopLevelArrayIndex();
        this.renderTextSegment(positionMatrix, multiDrawBatchDataHolder, textSegment, x, y, z, textDataIndex);
    }

    @Override
    protected Font.GlyphBitmap createGlyphBitmap(final Font.Glyph fontGlyph) {
        return fontGlyph.font().loadGlyphBitmap(fontGlyph.glyphIndex(), false, true);
    }

}
