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
import net.raphimc.thingl.drawbuilder.databuilder.holder.ShaderDataHolder;
import net.raphimc.thingl.drawbuilder.drawbatchdataholder.MultiDrawBatchDataHolder;
import net.raphimc.thingl.program.BuiltinPrograms;
import net.raphimc.thingl.util.FreeTypeInstance;
import org.joml.Matrix4f;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FreeType;

import java.nio.ByteBuffer;

public class BitmapTextRenderer extends TextRenderer {

    public BitmapTextRenderer(final Font... fonts) {
        super(BuiltinPrograms.BITMAP_TEXT, fonts);
    }

    @Override
    public float renderString(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final String text, final int startIndex, final int endIndex, float x, float y, float z, final Color textColor, final int styleFlags, final Color outlineColor) {
        final ShaderDataHolder stringDataHolder = multiDrawBatchDataHolder.getShaderDataHolder(this.textDrawBatch, "ssbo_StringData");
        final int regularStringDataIndex = stringDataHolder.rawInt(textColor.toRGBA()).endAndGetArrayIndex();

        x += this.globalXOffset * this.globalScale;
        y += this.globalYOffset * this.globalScale;

        if ((styleFlags & TextRenderer.BOLD_BIT) != 0) {
            if (outlineColor.getAlpha() == 0) {
                this.renderStringOutline(positionMatrix, multiDrawBatchDataHolder, text, startIndex, endIndex, x, y, z, textColor.toARGB(), styleFlags, regularStringDataIndex);
            } else {
                // bold and outline isn't supported at the same time (yet). Use SDFTextRenderer for that.
                final int outlineStringDataIndex = stringDataHolder.rawInt(outlineColor.toRGBA()).endAndGetArrayIndex();
                this.renderStringOutline(positionMatrix, multiDrawBatchDataHolder, text, startIndex, endIndex, x, y, z, textColor.toARGB(), styleFlags, outlineStringDataIndex);
            }
        } else if (outlineColor.getAlpha() != 0) {
            final int outlineStringDataIndex = stringDataHolder.rawInt(outlineColor.toRGBA()).endAndGetArrayIndex();
            this.renderStringOutline(positionMatrix, multiDrawBatchDataHolder, text, startIndex, endIndex, x, y, z, textColor.toARGB(), styleFlags, outlineStringDataIndex);
        }

        if ((styleFlags & TextRenderer.SHADOW_BIT) != 0) {
            final float shadowOffset = 0.075F * this.primaryFont.getSize() * this.globalScale;
            final Color shadowTextColor = textColor.multiply(0.25F);
            final int shadowStringDataIndex = stringDataHolder.rawInt(shadowTextColor.toRGBA()).endAndGetArrayIndex();

            if ((styleFlags & TextRenderer.BOLD_BIT) != 0) {
                this.renderStringOutline(positionMatrix, multiDrawBatchDataHolder, text, startIndex, endIndex, x + shadowOffset, y + shadowOffset, z, shadowTextColor.toARGB(), styleFlags, shadowStringDataIndex);
            }

            this.renderString(positionMatrix, multiDrawBatchDataHolder, text, startIndex, endIndex, x + shadowOffset, y + shadowOffset, z, shadowTextColor.toARGB(), styleFlags, shadowStringDataIndex);
            z += 0.01F;
        }

        return this.renderString(positionMatrix, multiDrawBatchDataHolder, text, startIndex, endIndex, x, y, z, textColor.toARGB(), styleFlags, regularStringDataIndex);
    }

    @Override
    protected GlyphBitmap getGlyphBitmap(final FontGlyph fontGlyph) {
        final FT_Face fontFace = fontGlyph.font().getFontFace();
        FreeTypeInstance.checkError(FreeType.FT_Load_Glyph(fontFace, fontGlyph.glyphIndex(), FreeType.FT_LOAD_DEFAULT), "Failed to load glyph");
        final FT_GlyphSlot glyphSlot = fontFace.glyph();
        FreeTypeInstance.checkError(FreeType.FT_Render_Glyph(glyphSlot, FreeType.FT_RENDER_MODE_NORMAL), "Failed to render glyph");

        if (glyphSlot.bitmap().pixel_mode() != FreeType.FT_PIXEL_MODE_GRAY) {
            throw new IllegalStateException("Unsupported pixel mode: " + glyphSlot.bitmap().pixel_mode());
        }

        final int width = glyphSlot.bitmap().width();
        final int height = glyphSlot.bitmap().rows();
        final int xOffset = glyphSlot.bitmap_left();
        final int yOffset = -glyphSlot.bitmap_top();
        final ByteBuffer pixels = glyphSlot.bitmap().buffer(width * height);
        return new GlyphBitmap(pixels, width, height, xOffset, yOffset, null);
    }

    private void renderStringOutline(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final String text, final int startIndex, final int endIndex, final float x, final float y, final float z, final int textColorArgb, final int styleFlags, final int stringDataIndex) {
        for (int xOffset = -1; xOffset <= 1; xOffset++) {
            for (int yOffset = -1; yOffset <= 1; yOffset++) {
                if (xOffset != 0 || yOffset != 0) {
                    this.renderString(positionMatrix, multiDrawBatchDataHolder, text, startIndex, endIndex, x + xOffset, y + yOffset, z, textColorArgb, styleFlags, stringDataIndex);
                }
            }
        }
    }

}
