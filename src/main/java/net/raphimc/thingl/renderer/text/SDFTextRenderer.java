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
import net.raphimc.thingl.program.BuiltinPrograms;
import net.raphimc.thingl.util.GlobalObjects;
import net.raphimc.thingl.util.font.Font;
import net.raphimc.thingl.util.font.FontGlyph;
import net.raphimc.thingl.util.font.FreeTypeInstance;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FreeType;

import java.nio.ByteBuffer;

public class SDFTextRenderer extends TextRenderer {

    public SDFTextRenderer(final Font... fonts) {
        super(BuiltinPrograms.SDF_TEXT, fonts);
    }

    @Override
    public float renderString(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final String text, final int startIndex, final int endIndex, float x, float y, float z, final Color textColor, final int styleFlags, final Color outlineColor) {
        final ShaderDataHolder stringDataHolder = multiDrawBatchDataHolder.getShaderDataHolder(this.textDrawBatch, "ssbo_StringData");

        final Vector3f scale = positionMatrix.getScale(GlobalObjects.VECTOR3F);
        if ((ThinGL.getImplementation().getProjectionMatrix().properties() & Matrix4fc.PROPERTY_AFFINE) != 0) { // If orthographic projection
            final Vector2f scaleFactor = ThinGL.getImplementation().get2DScaleFactor();
            scale.mul(scaleFactor.x, scaleFactor.y, 0F);
        }
        scale.mul(this.globalScale);
        final float maxScale = Math.max(Math.max(scale.x, scale.y), scale.z);
        final float smoothing = 1F / maxScale / 10F;

        final int regularStringDataIndex = stringDataHolder.rawInt(textColor.toRGBA()).rawInt(outlineColor.toRGBA()).rawInt(styleFlags).rawFloat(smoothing).endAndGetArrayIndex();

        if ((styleFlags & TextRenderer.SHADOW_BIT) != 0) {
            final float shadowOffset = 0.075F * this.primaryFont.getSize() * this.globalScale;
            final Color shadowTextColor = textColor.multiply(0.25F);
            final int shadowStringDataIndex = stringDataHolder.rawInt(shadowTextColor.toRGBA()).rawInt(0).rawInt(styleFlags).rawFloat(smoothing).endAndGetArrayIndex();
            this.renderString(positionMatrix, multiDrawBatchDataHolder, text, startIndex, endIndex, x + shadowOffset, y + shadowOffset, z, shadowTextColor.toARGB(), styleFlags, shadowStringDataIndex);
            z += 0.01F;
        }

        return this.renderString(positionMatrix, multiDrawBatchDataHolder, text, startIndex, endIndex, x, y, z, textColor.toARGB(), styleFlags, regularStringDataIndex);
    }

    @Override
    protected GlyphBitmap createGlyphBitmap(final FontGlyph fontGlyph) {
        final FT_Face fontFace = fontGlyph.font().getFontFace();
        FreeTypeInstance.checkError(FreeType.FT_Load_Glyph(fontFace, fontGlyph.glyphIndex(), FreeType.FT_LOAD_DEFAULT), "Failed to load glyph");
        final FT_GlyphSlot glyphSlot = fontFace.glyph();
        FreeTypeInstance.checkError(FreeType.FT_Render_Glyph(glyphSlot, FreeType.FT_RENDER_MODE_SDF), "Failed to render glyph");

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

}
