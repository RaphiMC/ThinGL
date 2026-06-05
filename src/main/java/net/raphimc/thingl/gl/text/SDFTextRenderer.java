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
import net.raphimc.thingl.resource.font.instance.FontInstance;
import net.raphimc.thingl.resource.font.instance.ScaledFontInstance;
import net.raphimc.thingl.text.TextStyle;
import net.raphimc.thingl.text.shaping.ShapedTextSegment;
import org.joml.Matrix4f;

public class SDFTextRenderer extends TextRenderer {

    public static final int DF_PX_RANGE = 8;

    public SDFTextRenderer() {
        this(FontInstance.GlyphBitmap.RenderMode.BSDF);
    }

    public SDFTextRenderer(final FontInstance.GlyphBitmap.RenderMode glyphRenderMode) {
        super(() -> ThinGL.programs().getSdfText(), glyphRenderMode);
    }

    @Override
    protected void renderTextSegment(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final FontInstance font, final ShapedTextSegment textSegment, final float x, final float y, final float z) {
        final ShaderBufferBuilder textDataBufferBuilder = multiDrawBatchDataHolder.getShaderStorageBufferBuilder(this.getDrawBatch(), "ssbo_TextData", Std430ShaderBufferBuilder.SUPPLIER).ensureInTopLevelArray();
        final TextStyle textStyle = textSegment.style();
        final float boldnessExpansion = this.getBaseFont(font).getSize() * (textStyle.boldnessStrength() / 100F);
        final int textDataIndex = textDataBufferBuilder.beginStruct(Integer.BYTES).writeColor(textStyle.color()).writeColor(textStyle.outlineColor()).writeInt(textStyle.flags()).writeFloat(boldnessExpansion).endStructAndGetTopLevelArrayIndex();
        this.renderTextSegment(positionMatrix, multiDrawBatchDataHolder, font, textSegment, x, y, z, textDataIndex);
    }

    private FontInstance getBaseFont(FontInstance font) {
        while (font instanceof ScaledFontInstance scaledFont) {
            font = scaledFont.getBaseInstance();
        }
        return font;
    }

}
