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
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.drawbuilder.BuiltinDrawBatches;
import net.raphimc.thingl.drawbuilder.DrawBatch;
import net.raphimc.thingl.drawbuilder.DrawMode;
import net.raphimc.thingl.drawbuilder.databuilder.holder.ShaderDataHolder;
import net.raphimc.thingl.drawbuilder.databuilder.holder.VertexDataHolder;
import net.raphimc.thingl.drawbuilder.drawbatchdataholder.DrawBatchDataHolder;
import net.raphimc.thingl.drawbuilder.drawbatchdataholder.MultiDrawBatchDataHolder;
import net.raphimc.thingl.renderer.Primitives;
import net.raphimc.thingl.resource.program.Program;
import net.raphimc.thingl.resource.texture.AbstractTexture;
import net.raphimc.thingl.texture.StaticAtlasTexture;
import net.raphimc.thingl.util.font.Font;
import net.raphimc.thingl.util.font.FontGlyph;
import net.raphimc.thingl.util.font.GlyphBitmap;
import net.raphimc.thingl.util.rectpack.Slot;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class TextRenderer {

    public static final int STYLE_SHADOW_BIT = 1 << 0;
    public static final int STYLE_BOLD_BIT = 1 << 1;
    public static final int STYLE_ITALIC_BIT = 1 << 2;
    public static final int STYLE_UNDERLINE_BIT = 1 << 3;
    public static final int STYLE_STRIKETHROUGH_BIT = 1 << 4;
    public static final int STYLE_OUTLINE_BIT = 1 << 5;
    public static final int ORIGIN_BASELINE_BIT = 1 << 16;
    public static final int INTERNAL_NO_BEARING_BIT = 1 << 24;

    private static final int ATLAS_SIZE = 1024;

    public static int buildStyleFlags(final boolean shadow, final boolean bold, final boolean italic, final boolean underline, final boolean strikethrough, final boolean outline) {
        int flags = 0;
        if (shadow) {
            flags |= STYLE_SHADOW_BIT;
        }
        if (bold) {
            flags |= STYLE_BOLD_BIT;
        }
        if (italic) {
            flags |= STYLE_ITALIC_BIT;
        }
        if (underline) {
            flags |= STYLE_UNDERLINE_BIT;
        }
        if (strikethrough) {
            flags |= STYLE_STRIKETHROUGH_BIT;
        }
        if (outline) {
            flags |= STYLE_OUTLINE_BIT;
        }
        return flags;
    }


    protected final Font[] fonts;
    protected final Font primaryFont;
    private final int freeTypeRenderMode;
    protected final DrawBatch textDrawBatch;
    private final List<StaticAtlasTexture> glyphAtlases = new ArrayList<>();
    private final Int2ObjectMap<AtlasGlyph> atlasGlyphs = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<FontGlyph> fontGlyphs = new Int2ObjectOpenHashMap<>();
    private float italicShearFactor = (float) Math.tan(Math.toRadians(14));
    protected float globalScale = 1F;

    public TextRenderer(final Supplier<Program> program, final int freeTypeRenderMode, final Font... fonts) {
        if (fonts.length == 0) {
            throw new IllegalArgumentException("At least one font must be provided");
        }
        this.fonts = fonts;
        this.primaryFont = fonts[0];
        this.freeTypeRenderMode = freeTypeRenderMode;

        this.textDrawBatch = new DrawBatch(program, DrawMode.QUADS, BuiltinDrawBatches.POSITION_TEXTURE_LAYOUT, () -> {
            ThinGL.glStateTracker().push();
            ThinGL.glStateTracker().enable(GL11C.GL_BLEND);
            final int[] textureIds = new int[this.glyphAtlases.size()];
            for (int i = 0; i < this.glyphAtlases.size(); i++) {
                textureIds[i] = this.glyphAtlases.get(i).getGlId();
            }
            program.get().setUniformSamplerArray("u_Textures", textureIds);
        }, () -> ThinGL.glStateTracker().pop());
    }

    public float renderString(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final String text, float x, final float y, final float z, final Color textColor) {
        return this.renderString(positionMatrix, multiDrawBatchDataHolder, text, x, y, z, textColor, 0);
    }

    public float renderString(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final String text, float x, float y, final float z, final Color textColor, final int flags) {
        return this.renderString(positionMatrix, multiDrawBatchDataHolder, text, x, y, z, textColor, flags, Color.TRANSPARENT);
    }

    public float renderString(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final String text, float x, float y, final float z, final Color textColor, final int flags, final Color outlineColor) {
        return this.renderString(positionMatrix, multiDrawBatchDataHolder, text, 0, text.length(), x, y, z, textColor, flags, outlineColor);
    }

    public abstract float renderString(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final String text, final int startIndex, final int endIndex, float x, float y, float z, final Color textColor, final int flags, final Color outlineColor);

    public float calculateWidth(final String text) {
        return this.calculateWidth(text, 0);
    }

    public float calculateWidth(final String text, final int flags) {
        float width = 0;
        for (int i = 0; i < text.length(); i++) {
            final int codePoint = text.codePointAt(i);
            if (codePoint >= Character.MIN_SUPPLEMENTARY_CODE_POINT) {
                i++;
            }

            final FontGlyph fontGlyph = this.getFontGlyph(codePoint);
            if (i == 0 && (flags & TextRenderer.INTERNAL_NO_BEARING_BIT) == 0) {
                width -= fontGlyph.bearingX();
            }
            if (i != text.length() - 1) {
                width += fontGlyph.advance();
            } else {
                width += fontGlyph.bearingX() + fontGlyph.width();
                if ((flags & TextRenderer.STYLE_ITALIC_BIT) != 0) {
                    width += this.italicShearFactor * fontGlyph.bearingY();
                }
            }
        }

        if ((flags & TextRenderer.STYLE_SHADOW_BIT) != 0) {
            width += 0.075F * this.primaryFont.getSize();
        }
        if ((flags & TextRenderer.STYLE_BOLD_BIT) != 0 || (flags & TextRenderer.STYLE_OUTLINE_BIT) != 0) {
            width += 2F;
        }

        return width * this.globalScale;
    }

    public float calculateHeight(final String text) {
        return this.calculateHeight(text, 0);
    }

    public float calculateHeight(final String text, final int flags) {
        float minY = 0;
        float maxY = 0;
        for (int i = 0; i < text.length(); i++) {
            final int codePoint = text.codePointAt(i);
            if (codePoint >= Character.MIN_SUPPLEMENTARY_CODE_POINT) {
                i++;
            }

            final FontGlyph fontGlyph = this.getFontGlyph(codePoint);
            final float glyphMinY = -fontGlyph.bearingY();
            if (glyphMinY < minY) {
                minY = glyphMinY;
            }
            final float glyphMaxY = glyphMinY + fontGlyph.height();
            if (glyphMaxY > maxY) {
                maxY = glyphMaxY;
            }
        }
        float height = maxY - minY;

        if ((flags & TextRenderer.STYLE_SHADOW_BIT) != 0) {
            height += 0.075F * this.primaryFont.getSize();
        }
        if ((flags & TextRenderer.STYLE_BOLD_BIT) != 0 || (flags & TextRenderer.STYLE_OUTLINE_BIT) != 0) {
            height += 2F;
        }

        return height * this.globalScale;
    }

    public float calculateBaselineHeight(final String text) {
        return this.calculateBaselineHeight(text, 0);
    }

    public float calculateBaselineHeight(final String text, final int flags) {
        float height = 0;
        for (int i = 0; i < text.length(); i++) {
            final int codePoint = text.codePointAt(i);
            if (codePoint >= Character.MIN_SUPPLEMENTARY_CODE_POINT) {
                i++;
            }

            final FontGlyph fontGlyph = this.getFontGlyph(codePoint);
            final float glyphHeight = fontGlyph.bearingY();
            if (glyphHeight > height) {
                height = glyphHeight;
            }
        }
        return height * this.globalScale;
    }

    public float getPaddedHeight() {
        return this.primaryFont.getPaddedHeight() * this.globalScale;
    }

    public void free() {
        this.glyphAtlases.forEach(StaticAtlasTexture::free);
        this.glyphAtlases.clear();
        this.atlasGlyphs.clear();
        this.fontGlyphs.clear();
    }

    public Font[] getFonts() {
        return this.fonts;
    }

    public DrawBatch getTextDrawBatch() {
        return this.textDrawBatch;
    }

    public float getItalicAngle() {
        return (float) Math.toDegrees(Math.atan(this.italicShearFactor));
    }

    public void setItalicAngle(final float italicAngle) {
        this.italicShearFactor = (float) Math.tan(Math.toRadians(italicAngle));
    }

    public float getGlobalScale() {
        return this.globalScale;
    }

    public void setGlobalScale(final float globalScale) {
        this.globalScale = globalScale;
    }

    protected float renderString(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final String text, final int startIndex, final int endIndex, float x, float y, float z, final int abgrTextColor, final int flags, final int stringDataIndex) {
        final DrawBatchDataHolder drawBatchDataHolder = multiDrawBatchDataHolder.getDrawBatchDataHolder(this.textDrawBatch);
        final VertexDataHolder vertexDataHolder = drawBatchDataHolder.getVertexDataHolder();
        final ShaderDataHolder charDataHolder = drawBatchDataHolder.getShaderDataHolder("ssbo_CharData");
        final float originX = x;

        if ((flags & ORIGIN_BASELINE_BIT) == 0) {
            y += this.calculateBaselineHeight(text, flags);
        }

        for (int i = startIndex; i < endIndex; i++) {
            final int codePoint = text.codePointAt(i);
            if (codePoint >= Character.MIN_SUPPLEMENTARY_CODE_POINT) {
                i++;
            }

            final AtlasGlyph atlasGlyph = this.getAtlasGlyph(codePoint);
            final FontGlyph fontGlyph = atlasGlyph.fontGlyph();
            if (x == originX && (flags & TextRenderer.INTERNAL_NO_BEARING_BIT) == 0) {
                x -= fontGlyph.bearingX() * this.globalScale;
            }
            if (atlasGlyph.atlasIndex() != -1) {
                this.renderGlyph(positionMatrix, vertexDataHolder, charDataHolder, atlasGlyph, x, y, z, flags, stringDataIndex);
            }

            x += fontGlyph.advance() * this.globalScale;
        }
        this.renderTextDecorations(positionMatrix, multiDrawBatchDataHolder, originX, x, y, z, abgrTextColor, flags);

        return x - originX;
    }

    protected void renderGlyph(final Matrix4f positionMatrix, final VertexDataHolder vertexDataHolder, final ShaderDataHolder charDataHolder, final AtlasGlyph glyph, final float x, final float y, final float z, final int flags, final int stringDataIndex) {
        final float x1 = x + glyph.xOffset() * this.globalScale;
        final float x2 = x1 + glyph.width() * this.globalScale;
        final float y1 = y + glyph.yOffset() * this.globalScale;
        final float y2 = y1 + glyph.height() * this.globalScale;

        float topOffset = 0F;
        float bottomOffset = 0F;
        if ((flags & TextRenderer.STYLE_ITALIC_BIT) != 0) {
            topOffset = this.italicShearFactor * -(y1 - y);
            bottomOffset = this.italicShearFactor * (y2 - y);
        }

        charDataHolder.rawInt((glyph.atlasIndex() << 27) | stringDataIndex).end();

        vertexDataHolder.position(positionMatrix, x1 - bottomOffset, y2, z).texture(glyph.u1(), glyph.v2()).endVertex();
        vertexDataHolder.position(positionMatrix, x2 - bottomOffset, y2, z).texture(glyph.u2(), glyph.v2()).endVertex();
        vertexDataHolder.position(positionMatrix, x2 + topOffset, y1, z).texture(glyph.u2(), glyph.v1()).endVertex();
        vertexDataHolder.position(positionMatrix, x1 + topOffset, y1, z).texture(glyph.u1(), glyph.v1()).endVertex();
    }

    protected void renderTextDecorations(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final float startX, final float endX, final float y, final float z, final int abgrTextColor, final int flags) {
        float lineThickness = this.primaryFont.getLineThickness() * this.globalScale;
        if ((flags & TextRenderer.STYLE_BOLD_BIT) != 0) {
            lineThickness *= 1.4F;
        }
        if ((flags & TextRenderer.STYLE_UNDERLINE_BIT) != 0) {
            final float lineY = y + lineThickness;
            Primitives.filledRectangle(positionMatrix, multiDrawBatchDataHolder, startX, lineY, endX, lineY + lineThickness, z, abgrTextColor);
        }
        if ((flags & TextRenderer.STYLE_STRIKETHROUGH_BIT) != 0) {
            final float lineY = y - this.primaryFont.getSize() * 0.3F - lineThickness;
            Primitives.filledRectangle(positionMatrix, multiDrawBatchDataHolder, startX, lineY, endX, lineY + lineThickness, z, abgrTextColor);
        }
    }

    private AtlasGlyph getAtlasGlyph(final int codePoint) {
        return this.atlasGlyphs.computeIfAbsent(codePoint, this::createAtlasGlyph);
    }

    private AtlasGlyph createAtlasGlyph(final int codePoint) {
        final FontGlyph fontGlyph = this.getFontGlyph(codePoint);
        final GlyphBitmap glyphBitmap = fontGlyph.font().loadGlyphBitmap(fontGlyph.glyphIndex(), this.freeTypeRenderMode);

        if (glyphBitmap.pixels() == null) {
            return new AtlasGlyph(fontGlyph, -1, 0F, 0F, 0F, 0F, glyphBitmap.width(), glyphBitmap.height(), glyphBitmap.xOffset(), glyphBitmap.yOffset());
        }

        Slot atlasSlot = null;
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
        if (atlasSlot == null) {
            throw new IllegalStateException("Glyph " + codePoint + " is too large to fit in atlas (" + glyphBitmap.width() + "x" + glyphBitmap.height() + ")");
        }
        if (!this.glyphAtlases.contains(atlas)) {
            this.glyphAtlases.add(atlas);
        }

        return new AtlasGlyph(fontGlyph, this.glyphAtlases.indexOf(atlas), atlasSlot.u1(), atlasSlot.v1(), atlasSlot.u2(), atlasSlot.v2(), glyphBitmap.width(), glyphBitmap.height(), glyphBitmap.xOffset(), glyphBitmap.yOffset());
    }

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
