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

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.drawbuilder.BuiltinDrawBatches;
import net.raphimc.thingl.drawbuilder.DrawBatch;
import net.raphimc.thingl.drawbuilder.databuilder.holder.ShaderDataHolder;
import net.raphimc.thingl.drawbuilder.databuilder.holder.Std430ShaderDataHolder;
import net.raphimc.thingl.drawbuilder.databuilder.holder.VertexDataHolder;
import net.raphimc.thingl.drawbuilder.drawbatchdataholder.DrawBatchDataHolder;
import net.raphimc.thingl.drawbuilder.drawbatchdataholder.MultiDrawBatchDataHolder;
import net.raphimc.thingl.renderer.Primitives;
import net.raphimc.thingl.resource.program.Program;
import net.raphimc.thingl.text.TextSegment;
import net.raphimc.thingl.text.font.Font;
import net.raphimc.thingl.text.shaping.*;
import net.raphimc.thingl.texture.StaticAtlasTexture;
import net.raphimc.thingl.util.rectpack.Slot;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class TextRenderer {

    public static final float ITALIC_SHEAR_FACTOR = (float) Math.tan(Math.toRadians(14)); // 14 degrees
    public static final float SHADOW_OFFSET_FACTOR = 0.075F;
    public static final float BOLD_OFFSET_DIVIDER = 64F;

    private static final int ATLAS_SIZE = 1024;

    private final DrawBatch drawBatch;
    private final Font.GlyphBitmap.RenderMode glyphRenderMode;
    private final List<StaticAtlasTexture> glyphAtlases = new ArrayList<>();
    private final Reference2ObjectMap<Font.Glyph, AtlasGlyph> atlasGlyphs = new Reference2ObjectOpenHashMap<>();
    private float globalScale = 1F;

    public TextRenderer(final Supplier<Program> program, final Font.GlyphBitmap.RenderMode glyphRenderMode) {
        this(new DrawBatch.Builder(BuiltinDrawBatches.TEXTURE_SNIPPET).program(program).build(), glyphRenderMode);
    }

    public TextRenderer(final DrawBatch drawBatch, final Font.GlyphBitmap.RenderMode glyphRenderMode) {
        this.drawBatch = new DrawBatch.Builder(drawBatch)
                .appendSetupAction(p -> {
                    final int[] textureIds = new int[this.glyphAtlases.size()];
                    for (int i = 0; i < this.glyphAtlases.size(); i++) {
                        textureIds[i] = this.glyphAtlases.get(i).getGlId();
                    }
                    p.setUniformSamplerArray("u_Textures", textureIds);
                })
                .build();
        this.glyphRenderMode = glyphRenderMode;
    }

    public void renderTextBlock(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final ShapedTextBlock textBlock, final float x, float y, final float z) {
        for (ShapedTextLine textLine : textBlock.lines()) {
            this.renderTextLine(positionMatrix, multiDrawBatchDataHolder, textLine, x, y, z);
            y += textLine.logicalBounds().lengthY() * this.globalScale;
        }
    }

    public void renderTextLine(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final ShapedTextLine textLine, float x, final float y, final float z) {
        for (int i = 0; i < textLine.runs().size(); i++) {
            final ShapedTextRun textRun = textLine.runs().get(i);
            if (i == 0) {
                this.renderTextRun(positionMatrix, multiDrawBatchDataHolder, textRun, x, y, z, textRun.font());
                x += textRun.visualBounds().minX * this.globalScale;
            } else {
                this.renderTextRun(positionMatrix, multiDrawBatchDataHolder, textRun, x - textRun.visualBounds().minX * this.globalScale, y, z, textLine.runs().get(0).font());
            }
            x += textRun.logicalBounds().maxX * this.globalScale;
        }
    }

    public void renderTextRun(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final ShapedTextRun textRun, final float x, final float y, final float z) {
        this.renderTextRun(positionMatrix, multiDrawBatchDataHolder, textRun, x, y, z, textRun.font());
    }

    public void free() {
        this.glyphAtlases.forEach(StaticAtlasTexture::free);
    }

    public DrawBatch getDrawBatch() {
        return this.drawBatch;
    }

    public Font.GlyphBitmap.RenderMode getGlyphRenderMode() {
        return this.glyphRenderMode;
    }

    public float getGlobalScale() {
        return this.globalScale;
    }

    public void setGlobalScale(final float globalScale) {
        this.globalScale = globalScale;
    }

    protected void renderTextRun(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final ShapedTextRun textRun, final float x, final float y, final float z, final Font decorationFont) {
        for (ShapedTextSegment textSegment : textRun.segments()) {
            if (textSegment.glyphs().isEmpty()) {
                continue;
            }
            final float xOffset = textSegment.visualOffset().x * this.globalScale;
            final float yOffset = textSegment.visualOffset().y * this.globalScale;

            if ((textSegment.styleFlags() & TextSegment.STYLE_SHADOW_BIT) != 0) {
                int shadowSegmentStyleFlags = textSegment.styleFlags() & ~TextSegment.STYLE_STRIKETHROUGH_BIT;
                if (textSegment.outlineColor().getAlpha() > 0) {
                    shadowSegmentStyleFlags |= TextSegment.STYLE_BOLD_BIT;
                }
                final ShapedTextSegment shadowTextSegment = new ShapedTextSegment(textSegment.glyphs(), textSegment.color().multiply(0.25F), shadowSegmentStyleFlags, Color.TRANSPARENT, textSegment.visualOffset(), textSegment.visualBounds(), textSegment.logicalBounds());
                final ShapedTextSegment nonShadowTextSegment = new ShapedTextSegment(textSegment.glyphs(), textSegment.color(), textSegment.styleFlags() & ~TextSegment.STYLE_SHADOW_BIT, textSegment.outlineColor(), textSegment.visualOffset(), textSegment.visualBounds(), textSegment.logicalBounds());
                final float shadowOffset = SHADOW_OFFSET_FACTOR * textRun.font().getSize() * this.globalScale;
                this.renderTextSegment(positionMatrix, multiDrawBatchDataHolder, shadowTextSegment, x + xOffset + shadowOffset, y + yOffset + shadowOffset, z);
                this.renderTextDecorations(positionMatrix, multiDrawBatchDataHolder, shadowTextSegment, x + xOffset + shadowOffset, y + yOffset + shadowOffset, z, decorationFont);
                this.renderTextSegment(positionMatrix, multiDrawBatchDataHolder, nonShadowTextSegment, x + xOffset, y + yOffset, z + 0.01F);
                this.renderTextDecorations(positionMatrix, multiDrawBatchDataHolder, nonShadowTextSegment, x + xOffset, y + yOffset, z + 0.01F, decorationFont);
            } else {
                this.renderTextSegment(positionMatrix, multiDrawBatchDataHolder, textSegment, x + xOffset, y + yOffset, z);
                this.renderTextDecorations(positionMatrix, multiDrawBatchDataHolder, textSegment, x + xOffset, y + yOffset, z, decorationFont);
            }
        }
    }

    protected abstract void renderTextSegment(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final ShapedTextSegment textSegment, final float x, final float y, final float z);

    protected void renderTextSegment(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final ShapedTextSegment textSegment, final float x, final float y, final float z, final int textDataIndex) {
        final DrawBatchDataHolder drawBatchDataHolder = multiDrawBatchDataHolder.getDrawBatchDataHolder(this.drawBatch);
        final VertexDataHolder vertexDataHolder = drawBatchDataHolder.getVertexDataHolder();
        final ShaderDataHolder glyphDataHolder = drawBatchDataHolder.getShaderStorageDataHolder("ssbo_GlyphData", Std430ShaderDataHolder.SUPPLIER).ensureInTopLevelArray();

        for (TextShaper.Glyph shapedGlyph : textSegment.glyphs()) {
            final Font.Glyph fontGlyph = shapedGlyph.fontGlyph();
            final AtlasGlyph atlasGlyph = this.getAtlasGlyph(fontGlyph);
            if (atlasGlyph != null) {
                final float glyphX = shapedGlyph.x() * this.globalScale;
                final float glyphY = shapedGlyph.y() * this.globalScale;
                this.renderGlyph(positionMatrix, vertexDataHolder, glyphDataHolder, atlasGlyph, x + glyphX, y + glyphY, z, textSegment.styleFlags(), textDataIndex);
            }
        }
    }

    protected void renderTextDecorations(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final ShapedTextSegment textSegment, final float x, final float y, final float z, final Font font) {
        final int styleFlags = textSegment.styleFlags();
        if ((styleFlags & TextSegment.STYLE_UNDERLINE_BIT) != 0 || (styleFlags & TextSegment.STYLE_STRIKETHROUGH_BIT) != 0) {
            final int textColor = textSegment.color().toABGR();
            if ((styleFlags & TextSegment.STYLE_UNDERLINE_BIT) != 0) {
                float halfLineThickness = (font.getUnderlineThickness() * this.globalScale) / 2F;
                if ((styleFlags & TextSegment.STYLE_BOLD_BIT) != 0) {
                    halfLineThickness *= 1.5F;
                }
                final float lineY = y + font.getUnderlinePosition() * this.globalScale;
                Primitives.filledRectangle(positionMatrix, multiDrawBatchDataHolder, x + textSegment.logicalBounds().minX * this.globalScale, lineY - halfLineThickness, x + textSegment.logicalBounds().maxX * this.globalScale, lineY + halfLineThickness, z, textColor);
            }
            if ((styleFlags & TextSegment.STYLE_STRIKETHROUGH_BIT) != 0) {
                float lineThickness = font.getStrikethroughThickness() * this.globalScale;
                if ((styleFlags & TextSegment.STYLE_BOLD_BIT) != 0) {
                    lineThickness *= 1.5F;
                }
                final float lineY = y + font.getStrikethroughPosition() * this.globalScale;
                Primitives.filledRectangle(positionMatrix, multiDrawBatchDataHolder, x + textSegment.logicalBounds().minX * this.globalScale, lineY, x + textSegment.logicalBounds().maxX * this.globalScale, lineY + lineThickness, z, textColor);
            }
        }
    }

    private void renderGlyph(final Matrix4f positionMatrix, final VertexDataHolder vertexDataHolder, final ShaderDataHolder glyphDataHolder, final AtlasGlyph glyph, final float x, final float y, final float z, final int styleFlags, final int textDataIndex) {
        final float x1 = x + glyph.xOffset() * this.globalScale;
        final float x2 = x1 + glyph.width() * this.globalScale;
        final float y1 = y + glyph.yOffset() * this.globalScale;
        final float y2 = y1 + glyph.height() * this.globalScale;

        float topOffset = 0F;
        float bottomOffset = 0F;
        if ((styleFlags & TextSegment.STYLE_ITALIC_BIT) != 0) {
            topOffset = ITALIC_SHEAR_FACTOR * -(y1 - y);
            bottomOffset = ITALIC_SHEAR_FACTOR * (y2 - y);
        }

        glyphDataHolder.ensureInTopLevelArray().beginStruct(Integer.BYTES).putInt((glyph.atlasIndex() << 27) | textDataIndex).endStruct();

        vertexDataHolder.putVector3f(positionMatrix, x1 - bottomOffset, y2, z).putTextureCoord(glyph.u1(), glyph.v2()).endVertex();
        vertexDataHolder.putVector3f(positionMatrix, x2 - bottomOffset, y2, z).putTextureCoord(glyph.u2(), glyph.v2()).endVertex();
        vertexDataHolder.putVector3f(positionMatrix, x2 + topOffset, y1, z).putTextureCoord(glyph.u2(), glyph.v1()).endVertex();
        vertexDataHolder.putVector3f(positionMatrix, x1 + topOffset, y1, z).putTextureCoord(glyph.u1(), glyph.v1()).endVertex();
    }

    private AtlasGlyph getAtlasGlyph(final Font.Glyph fontGlyph) {
        return this.atlasGlyphs.computeIfAbsent(fontGlyph, this::createAtlasGlyph);
    }

    private AtlasGlyph createAtlasGlyph(final Font.Glyph fontGlyph) {
        final Font.GlyphBitmap glyphBitmap = fontGlyph.font().createGlyphBitmap(fontGlyph.glyphIndex(), this.glyphRenderMode);
        if (glyphBitmap == null) {
            return null;
        }

        Slot atlasSlot = null;
        StaticAtlasTexture atlas = null;
        for (int i = 0; i <= this.glyphAtlases.size(); i++) {
            if (i == this.glyphAtlases.size()) {
                atlas = new StaticAtlasTexture(this.glyphRenderMode.getTextureFormat(), ATLAS_SIZE, ATLAS_SIZE);
                atlas.setFilter(this.glyphRenderMode.getTextureFilter());
            } else {
                atlas = this.glyphAtlases.get(i);
            }
            atlasSlot = atlas.addSlot(glyphBitmap.width(), glyphBitmap.height(), glyphBitmap.pixelFormat(), glyphBitmap.pixelBuffer());
            if (atlasSlot != null) {
                break;
            }
        }
        MemoryUtil.memFree(glyphBitmap.pixelBuffer());
        if (atlasSlot == null) { // Should never happen
            throw new IllegalStateException("Failed to find a free slot for glyph in atlas");
        }
        if (!this.glyphAtlases.contains(atlas)) {
            this.glyphAtlases.add(atlas);
        }

        return new AtlasGlyph(this.glyphAtlases.indexOf(atlas), atlasSlot.u1(), atlasSlot.v1(), atlasSlot.u2(), atlasSlot.v2(), glyphBitmap.width(), glyphBitmap.height(), glyphBitmap.xOffset(), glyphBitmap.yOffset());
    }

    private record AtlasGlyph(int atlasIndex, float u1, float v1, float u2, float v2, float width, float height, float xOffset, float yOffset) {
    }

}
