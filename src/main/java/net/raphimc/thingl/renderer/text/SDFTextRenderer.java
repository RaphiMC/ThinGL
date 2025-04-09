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

package net.raphimc.thingl.renderer.text;

import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.drawbuilder.databuilder.holder.ShaderDataHolder;
import net.raphimc.thingl.drawbuilder.drawbatchdataholder.MultiDrawBatchDataHolder;
import net.raphimc.thingl.util.font.Font;
import net.raphimc.thingl.util.font.FontGlyph;
import net.raphimc.thingl.util.font.GlyphBitmap;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class SDFTextRenderer extends TextRenderer {

    public SDFTextRenderer(final Font... fonts) {
        super(() -> ThinGL.programs().getSdfText(), fonts);
    }

    @Override
    public float renderString(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final String text, final int startIndex, final int endIndex, float x, float y, float z, final Color textColor, final int flags, final Color outlineColor) {
        final ShaderDataHolder stringDataHolder = multiDrawBatchDataHolder.getShaderDataHolder(this.textDrawBatch, "ssbo_StringData");

        final Vector3f scale = positionMatrix.getScale(new Vector3f());
        if ((ThinGL.applicationInterface().getProjectionMatrix().properties() & Matrix4fc.PROPERTY_AFFINE) != 0) { // If orthographic projection
            final Vector2f scaleFactor = ThinGL.applicationInterface().get2DScaleFactor();
            scale.mul(scaleFactor.x, scaleFactor.y, 0F);
        }
        scale.mul(this.globalScale);
        final float maxScale = Math.max(Math.max(scale.x, scale.y), scale.z);
        final float smoothing = 1F / maxScale / 10F;

        final int regularStringDataIndex = stringDataHolder.rawInt(textColor.toABGR()).rawInt(outlineColor.toABGR()).rawInt(flags).rawFloat(smoothing).endAndGetArrayIndex();

        if ((flags & TextRenderer.STYLE_SHADOW_BIT) != 0) {
            final float shadowOffset = 0.075F * this.primaryFont.getSize() * this.globalScale;
            final Color shadowTextColor = textColor.multiply(0.25F);
            final int shadowStringDataIndex = stringDataHolder.rawInt(shadowTextColor.toABGR()).rawInt(0).rawInt(flags).rawFloat(smoothing).endAndGetArrayIndex();
            this.renderString(positionMatrix, multiDrawBatchDataHolder, text, startIndex, endIndex, x + shadowOffset, y + shadowOffset, z, shadowTextColor.toABGR(), flags, shadowStringDataIndex);
            z += 0.01F;
        }

        return this.renderString(positionMatrix, multiDrawBatchDataHolder, text, startIndex, endIndex, x, y, z, textColor.toABGR(), flags, regularStringDataIndex);
    }

    @Override
    protected GlyphBitmap createGlyphBitmap(final FontGlyph fontGlyph) {
        return fontGlyph.font().loadGlyphBitmap(fontGlyph.glyphIndex(), false, true);
    }

}
