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

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.drawbuilder.BuiltinDrawBatches;
import net.raphimc.thingl.drawbuilder.DrawBatch;
import net.raphimc.thingl.drawbuilder.DrawMode;
import net.raphimc.thingl.drawbuilder.databuilder.holder.ShaderDataHolder;
import net.raphimc.thingl.drawbuilder.databuilder.holder.VertexDataHolder;
import net.raphimc.thingl.drawbuilder.drawbatchdataholder.DrawBatchDataHolder;
import net.raphimc.thingl.drawbuilder.drawbatchdataholder.MultiDrawBatchDataHolder;
import net.raphimc.thingl.program.RegularProgram;
import net.raphimc.thingl.renderer.Primitives;
import net.raphimc.thingl.resource.texture.AbstractTexture;
import net.raphimc.thingl.texture.StaticAtlasTexture;
import net.raphimc.thingl.util.font.Font;
import net.raphimc.thingl.util.font.FontGlyph;
import net.raphimc.thingl.wrapper.GLStateTracker;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;

import java.util.ArrayList;
import java.util.List;

public abstract class TextRenderer {

    public static final int SHADOW_BIT = 1 << 0;
    public static final int BOLD_BIT = 1 << 1;
    public static final int ITALIC_BIT = 1 << 2;
    public static final int UNDERLINE_BIT = 1 << 3;
    public static final int STRIKETHROUGH_BIT = 1 << 4;
    public static final int WIDTH_CALCULATION_ONLY_OUTLINE_BIT = 1 << 29;
    public static final int INTERNAL_NO_BEARING_BIT = 1 << 30;
    public static final int INTERNAL_NO_NEWLINE_BIT = 1 << 31;
    private static final int ATLAS_SIZE = 1024;

    public static int calculateStyleFlags(final boolean shadow, final boolean bold, final boolean italic, final boolean underline, final boolean strikethrough) {
        int styleFlags = 0;
        if (shadow) {
            styleFlags |= SHADOW_BIT;
        }
        if (bold) {
            styleFlags |= BOLD_BIT;
        }
        if (italic) {
            styleFlags |= ITALIC_BIT;
        }
        if (underline) {
            styleFlags |= UNDERLINE_BIT;
        }
        if (strikethrough) {
            styleFlags |= STRIKETHROUGH_BIT;
        }
        return styleFlags;
    }


    protected final Font[] fonts;
    protected final Font primaryFont;
    protected final DrawBatch textDrawBatch;
    protected final List<StaticAtlasTexture> glyphAtlases = new ArrayList<>();
    protected final Int2ObjectMap<AtlasGlyph> atlasGlyphs = new Int2ObjectOpenHashMap<>();
    protected final Int2ObjectMap<FontGlyph> fontGlyphs = new Int2ObjectOpenHashMap<>();
    protected float globalScale = 1F;

    public TextRenderer(final RegularProgram shader, final Font... fonts) {
        if (fonts.length == 0) {
            throw new IllegalArgumentException("At least one font must be provided");
        }
        this.fonts = fonts;
        this.primaryFont = fonts[0];

        this.textDrawBatch = new DrawBatch(() -> shader, DrawMode.QUADS, BuiltinDrawBatches.POSITION_TEXTURE_LAYOUT, () -> {
            GLStateTracker.push();
            GLStateTracker.enable(GL11C.GL_BLEND);
            final int[] textureIds = new int[this.glyphAtlases.size()];
            for (int i = 0; i < this.glyphAtlases.size(); i++) {
                textureIds[i] = this.glyphAtlases.get(i).getGlId();
            }
            shader.setUniformTextureArray("u_Textures", textureIds);
        }, GLStateTracker::pop);
    }

    public float renderString(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final String text, float x, final float y, final float z, final Color textColor) {
        return this.renderString(positionMatrix, multiDrawBatchDataHolder, text, x, y, z, textColor, 0, Color.TRANSPARENT);
    }

    public float renderString(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final String text, float x, float y, final float z, final Color textColor, final int styleFlags, final Color outlineColor) {
        return this.renderString(positionMatrix, multiDrawBatchDataHolder, text, 0, text.length(), x, y, z, textColor, styleFlags, outlineColor);
    }

    public abstract float renderString(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final String text, final int startIndex, final int endIndex, float x, float y, float z, final Color textColor, final int styleFlags, final Color outlineColor);

    public float calculateWidth(final String text) {
        return this.calculateWidth(text, 0);
    }

    public float calculateWidth(final String text, final int styleFlags) {
        float width = 0;
        for (int i = 0; i < text.length(); i++) {
            final int codePoint = text.codePointAt(i);
            final FontGlyph fontGlyph = this.getFontGlyph(codePoint);
            if (i == 0 && (styleFlags & TextRenderer.INTERNAL_NO_BEARING_BIT) == 0) {
                width -= fontGlyph.bearingX() * this.globalScale;
            }
            if (i != text.length() - 1) {
                width += fontGlyph.advance() * this.globalScale;
            } else {
                width += fontGlyph.width() * this.globalScale;
                width += fontGlyph.bearingX() * this.globalScale;
            }
        }

        if ((styleFlags & TextRenderer.SHADOW_BIT) != 0) {
            width += 0.075F * this.primaryFont.getSize() * this.globalScale;
        }
        if ((styleFlags & TextRenderer.BOLD_BIT) != 0 || (styleFlags & TextRenderer.WIDTH_CALCULATION_ONLY_OUTLINE_BIT) != 0) {
            width += 2F * this.globalScale;
        }

        return width;
    }

    public float getExactHeight() {
        // return this.primaryFont.getSize() * this.globalScale;
        return (this.primaryFont.getBoundingHeight() - this.primaryFont.getDescent()) * this.globalScale;
    }

    public float getBaseLineHeight() {
        return this.primaryFont.getBaseLineHeight() * this.globalScale;
    }

    public float getPaddedHeight() {
        return this.primaryFont.getPaddedHeight() * this.globalScale;
    }

    public void delete() {
        this.glyphAtlases.forEach(StaticAtlasTexture::delete);
        this.glyphAtlases.clear();
        this.atlasGlyphs.clear();
        this.fontGlyphs.clear();
    }

    public Font[] getFonts() {
        return this.fonts;
    }

    public float getGlobalScale() {
        return this.globalScale;
    }

    public void setGlobalScale(final float globalScale) {
        this.globalScale = globalScale;
    }

    protected float renderString(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final String text, final int startIndex, final int endIndex, float x, float y, float z, final int textColorArgb, final int styleFlags, final int stringDataIndex) {
        final DrawBatchDataHolder drawBatchDataHolder = multiDrawBatchDataHolder.getDrawBatchDataHolder(this.textDrawBatch);
        final VertexDataHolder vertexDataHolder = drawBatchDataHolder.getVertexDataHolder();
        final ShaderDataHolder charDataHolder = drawBatchDataHolder.getShaderDataHolder("ssbo_CharData");

        final float originX = x;
        for (int i = startIndex; i < endIndex; i++) {
            final int codePoint = text.codePointAt(i);
            if (codePoint == '\n' && (styleFlags & TextRenderer.INTERNAL_NO_NEWLINE_BIT) == 0) {
                this.renderTextDecorations(positionMatrix, multiDrawBatchDataHolder, originX, x, y, z, textColorArgb, styleFlags);
                x = originX;
                y += this.primaryFont.getPaddedHeight() * this.globalScale;
                continue;
            }
            final AtlasGlyph atlasGlyph = this.getAtlasGlyph(codePoint);
            final FontGlyph fontGlyph = atlasGlyph.fontGlyph();
            if (x == originX && (styleFlags & TextRenderer.INTERNAL_NO_BEARING_BIT) == 0) {
                x -= fontGlyph.bearingX() * this.globalScale;
            }
            if (atlasGlyph.atlasIndex() != -1) {
                this.renderGlyph(positionMatrix, vertexDataHolder, charDataHolder, atlasGlyph, x, y, z, styleFlags, stringDataIndex);
            }

            x += fontGlyph.advance() * this.globalScale;
        }
        this.renderTextDecorations(positionMatrix, multiDrawBatchDataHolder, originX, x, y, z, textColorArgb, styleFlags);

        return x - originX;
    }

    protected void renderGlyph(final Matrix4f positionMatrix, final VertexDataHolder vertexDataHolder, final ShaderDataHolder charDataHolder, final AtlasGlyph glyph, final float x, final float y, final float z, final int styleFlags, final int stringDataIndex) {
        final float x1 = x + glyph.xOffset() * this.globalScale;
        final float x2 = x1 + glyph.width() * this.globalScale;
        final float y1 = y + (glyph.yOffset() + glyph.fontGlyph().font().getBoundingHeight()) * this.globalScale;
        final float y2 = y1 + glyph.height() * this.globalScale;

        float topOffset = 0F;
        float bottomOffset = 0F;
        if ((styleFlags & TextRenderer.ITALIC_BIT) != 0) {
            topOffset = 4F * this.globalScale;
            bottomOffset = 2F * this.globalScale;
        }

        charDataHolder.rawInt((glyph.atlasIndex() << 27) | stringDataIndex).end();

        vertexDataHolder.position(positionMatrix, x1 - bottomOffset, y2, z).texture(glyph.u1(), glyph.v2()).endVertex();
        vertexDataHolder.position(positionMatrix, x2 - bottomOffset, y2, z).texture(glyph.u2(), glyph.v2()).endVertex();
        vertexDataHolder.position(positionMatrix, x2 + topOffset, y1, z).texture(glyph.u2(), glyph.v1()).endVertex();
        vertexDataHolder.position(positionMatrix, x1 + topOffset, y1, z).texture(glyph.u1(), glyph.v1()).endVertex();
    }

    protected void renderTextDecorations(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float startX, final float endX, final float y, final float z, final int textColorArgb, final int styleFlags) {
        float lineThickness = this.primaryFont.getLineThickness() * this.globalScale;
        if ((styleFlags & TextRenderer.BOLD_BIT) != 0) {
            lineThickness *= 1.4F;
        }
        if ((styleFlags & TextRenderer.UNDERLINE_BIT) != 0) {
            final float lineY = y + this.primaryFont.getBoundingHeight() * this.globalScale + lineThickness;
            Primitives.filledRectangle(positionMatrix, multiDrawBatchDataHolder, startX, lineY, endX, lineY + lineThickness, z, textColorArgb);
        }
        if ((styleFlags & TextRenderer.STRIKETHROUGH_BIT) != 0) {
            final float lineY = y + this.primaryFont.getBoundingHeight() * 0.6F * this.globalScale + lineThickness;
            Primitives.filledRectangle(positionMatrix, multiDrawBatchDataHolder, startX, lineY, endX, lineY + lineThickness, z, textColorArgb);
        }
    }

    private AtlasGlyph getAtlasGlyph(final int codePoint) {
        return this.atlasGlyphs.computeIfAbsent(codePoint, this::createAtlasGlyph);
    }

    private AtlasGlyph createAtlasGlyph(final int codePoint) {
        final FontGlyph fontGlyph = this.getFontGlyph(codePoint);
        final GlyphBitmap glyphBitmap = this.createGlyphBitmap(fontGlyph);

        if (glyphBitmap.pixels() == null) {
            return new AtlasGlyph(fontGlyph, -1, 0F, 0F, 0F, 0F, glyphBitmap.width(), glyphBitmap.height(), glyphBitmap.xOffset(), glyphBitmap.yOffset());
        }

        StaticAtlasTexture.Slot atlasSlot = null;
        StaticAtlasTexture atlas = null;
        for (int i = 0; i < this.glyphAtlases.size() + 1; i++) {
            if (i == this.glyphAtlases.size()) {
                atlas = new StaticAtlasTexture(AbstractTexture.InternalFormat.R8, ATLAS_SIZE, ATLAS_SIZE);
            } else {
                atlas = this.glyphAtlases.get(i);
            }
            atlasSlot = atlas.addSlot(glyphBitmap.width(), glyphBitmap.height(), AbstractTexture.PixelFormat.R, glyphBitmap.pixels());
            if (atlasSlot != null) {
                break;
            }
        }
        if (glyphBitmap.freeAction() != null) {
            glyphBitmap.freeAction().accept(glyphBitmap);
        }
        if (atlasSlot == null) {
            throw new IllegalStateException("Glyph (" + codePoint + ") is too large to fit in atlas (" + glyphBitmap.width() + "x" + glyphBitmap.height() + ")");
        }
        if (!this.glyphAtlases.contains(atlas)) {
            this.glyphAtlases.add(atlas);
        }

        return new AtlasGlyph(fontGlyph, this.glyphAtlases.indexOf(atlas), atlasSlot.u1(), atlasSlot.v1(), atlasSlot.u2(), atlasSlot.v2(), glyphBitmap.width(), glyphBitmap.height(), glyphBitmap.xOffset(), glyphBitmap.yOffset());
    }

    protected abstract GlyphBitmap createGlyphBitmap(final FontGlyph fontGlyph);

    private FontGlyph getFontGlyph(final int codePoint) {
        return this.fontGlyphs.computeIfAbsent(codePoint, this::createFontGlyph);
    }

    private FontGlyph createFontGlyph(final int codePoint) {
        for (Font font : this.fonts) {
            final FontGlyph fontGlyph = font.loadGlyphByCodePoint(codePoint);
            if (fontGlyph.glyphIndex() != 0) {
                return fontGlyph;
            }
        }
        return this.primaryFont.loadGlyphByCodePoint(codePoint);
    }

}
