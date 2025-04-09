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

public class BitmapTextRenderer extends TextRenderer {

    public BitmapTextRenderer(final Font... fonts) {
        super(() -> ThinGL.programs().getBitmapText(), fonts);
    }

    @Override
    public float renderString(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final String text, final int startIndex, final int endIndex, float x, float y, float z, final Color textColor, final int flags, final Color outlineColor) {
        final int abgrTextColor = textColor.toABGR();
        final ShaderDataHolder stringDataHolder = multiDrawBatchDataHolder.getShaderDataHolder(this.textDrawBatch, "ssbo_StringData");
        final int regularStringDataIndex = stringDataHolder.rawInt(abgrTextColor).endAndGetArrayIndex();

        if ((flags & TextRenderer.STYLE_BOLD_BIT) != 0) {
            if (outlineColor.getAlpha() == 0) {
                this.renderStringOutline(positionMatrix, multiDrawBatchDataHolder, text, startIndex, endIndex, x, y, z, abgrTextColor, flags, regularStringDataIndex);
            } else {
                // bold and outline isn't supported at the same time (yet). Use SDFTextRenderer for that.
                final int outlineStringDataIndex = stringDataHolder.rawInt(outlineColor.toABGR()).endAndGetArrayIndex();
                this.renderStringOutline(positionMatrix, multiDrawBatchDataHolder, text, startIndex, endIndex, x, y, z, abgrTextColor, flags, outlineStringDataIndex);
            }
        } else if (outlineColor.getAlpha() != 0) {
            final int outlineStringDataIndex = stringDataHolder.rawInt(outlineColor.toABGR()).endAndGetArrayIndex();
            this.renderStringOutline(positionMatrix, multiDrawBatchDataHolder, text, startIndex, endIndex, x, y, z, abgrTextColor, flags, outlineStringDataIndex);
        }

        if ((flags & TextRenderer.STYLE_SHADOW_BIT) != 0) {
            final float shadowOffset = 0.075F * this.primaryFont.getSize() * this.globalScale;
            final Color shadowTextColor = textColor.multiply(0.25F);
            final int abgrShadowTextColor = shadowTextColor.toABGR();
            final int shadowStringDataIndex = stringDataHolder.rawInt(abgrShadowTextColor).endAndGetArrayIndex();

            if ((flags & TextRenderer.STYLE_BOLD_BIT) != 0) {
                this.renderStringOutline(positionMatrix, multiDrawBatchDataHolder, text, startIndex, endIndex, x + shadowOffset, y + shadowOffset, z, abgrShadowTextColor, flags, shadowStringDataIndex);
            }

            this.renderString(positionMatrix, multiDrawBatchDataHolder, text, startIndex, endIndex, x + shadowOffset, y + shadowOffset, z, abgrShadowTextColor, flags, shadowStringDataIndex);
            z += 0.01F;
        }

        return this.renderString(positionMatrix, multiDrawBatchDataHolder, text, startIndex, endIndex, x, y, z, abgrTextColor, flags, regularStringDataIndex);
    }

    private void renderStringOutline(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final String text, final int startIndex, final int endIndex, final float x, final float y, final float z, final int abgrTextColor, final int flags, final int stringDataIndex) {
        for (int xOffset = -1; xOffset <= 1; xOffset++) {
            for (int yOffset = -1; yOffset <= 1; yOffset++) {
                if (xOffset != 0 || yOffset != 0) {
                    this.renderString(positionMatrix, multiDrawBatchDataHolder, text, startIndex, endIndex, x + xOffset, y + yOffset, z, abgrTextColor, flags, stringDataIndex);
                }
            }
        }
    }

    @Override
    protected GlyphBitmap createGlyphBitmap(final FontGlyph fontGlyph) {
        return fontGlyph.font().loadGlyphBitmap(fontGlyph.glyphIndex(), true, false);
    }

}
