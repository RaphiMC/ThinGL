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

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.gl.renderer.Primitives;
import net.raphimc.thingl.gl.resource.program.Program;
import net.raphimc.thingl.gl.texture.StaticAtlasTexture;
import net.raphimc.thingl.rendering.DrawBatch;
import net.raphimc.thingl.rendering.DrawBatches;
import net.raphimc.thingl.rendering.bufferbuilder.impl.VertexBufferBuilder;
import net.raphimc.thingl.rendering.dataholder.MultiDrawBatchDataHolder;
import net.raphimc.thingl.resource.font.instance.FontInstance;
import net.raphimc.thingl.resource.font.instance.ScaledFontInstance;
import net.raphimc.thingl.text.TextStyle;
import net.raphimc.thingl.text.shaping.*;
import net.raphimc.thingl.util.ArrayCache;
import net.raphimc.thingl.util.rectpack.Slot;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL33C;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class TextRenderer {

    private static final int ATLAS_SIZE = 1024;

    private final DrawBatch drawBatch;
    private final FontInstance.GlyphBitmap.RenderMode glyphRenderMode;
    private final List<StaticAtlasTexture> glyphAtlases = new ArrayList<>();
    private final Reference2ObjectMap<FontInstance, ArrayCache<AtlasGlyph>> fontAtlasGlyphs = new Reference2ObjectOpenHashMap<>();

    protected TextRenderer(final Supplier<Program> program, final FontInstance.GlyphBitmap.RenderMode glyphRenderMode) {
        this(program, glyphRenderMode, _ -> {
        });
    }

    protected TextRenderer(final Supplier<Program> program, final FontInstance.GlyphBitmap.RenderMode glyphRenderMode, final Consumer<Program> programSetup) {
        this.drawBatch = new DrawBatch.Builder(DrawBatches.TEXTURE_SNIPPET)
                .program(program)
                .vertexDataLayout(DrawBatches.TEXT_GLYPH_LAYOUT)
                .appendSetupAction(programSetup)
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
            y += textLine.logicalBounds().lengthY();
        }
    }

    public void renderTextLine(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final ShapedTextLine textLine, float x, final float y, final float z) {
        for (ShapedTextRun textRun : textLine.runs()) {
            this.renderTextRun(positionMatrix, multiDrawBatchDataHolder, textRun, x, y, z, textLine.runs().getFirst().font());
            x += textRun.logicalBounds().lengthX();
        }
    }

    public void renderTextRun(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final ShapedTextRun textRun, final float x, final float y, final float z) {
        this.renderTextRun(positionMatrix, multiDrawBatchDataHolder, textRun, x, y, z, textRun.font());
    }

    public void preloadGlyphs(final ShapedTextBlock textBlock) {
        for (ShapedTextLine textLine : textBlock.lines()) {
            this.preloadGlyphs(textLine);
        }
    }

    public void preloadGlyphs(final ShapedTextLine textLine) {
        for (ShapedTextRun textRun : textLine.runs()) {
            this.preloadGlyphs(textRun);
        }
    }

    public void preloadGlyphs(final ShapedTextRun textRun) {
        final ArrayCache<AtlasGlyph> atlasGlyphs = this.getAtlasGlyphs(textRun.font());
        for (ShapedTextSegment textSegment : textRun.segments()) {
            for (TextShaper.Glyph shapedGlyph : textSegment.glyphs()) {
                atlasGlyphs.getOrLoad(shapedGlyph.index());
            }
        }
    }

    public void free() {
        this.glyphAtlases.forEach(StaticAtlasTexture::free);
        this.glyphAtlases.clear();
        this.fontAtlasGlyphs.clear();
    }

    public DrawBatch getDrawBatch() {
        return this.drawBatch;
    }

    public FontInstance.GlyphBitmap.RenderMode getGlyphRenderMode() {
        return this.glyphRenderMode;
    }

    protected void renderTextRun(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final ShapedTextRun textRun, final float x, final float y, final float z, final FontInstance decorationFont) {
        final FontInstance font = textRun.font();
        for (ShapedTextSegment textSegment : textRun.segments()) {
            if (textSegment.glyphs().isEmpty()) {
                continue;
            }
            final TextStyle style = textSegment.style();
            if (style.isShadow()) {
                TextStyle shadowStyle = style.withStrikethrough(false);
                if (style.shadowColor() != null) {
                    shadowStyle = shadowStyle.withColor(style.shadowColor()).withOutlineColor(style.shadowColor().withAlpha(style.outlineColor().getAlpha()));
                } else {
                    final Color shadowColor = style.color().multiply(0.25F);
                    shadowStyle = shadowStyle.withColor(shadowColor).withOutlineColor(shadowColor.withAlpha(style.outlineColor().getAlpha()));
                }
                final ShapedTextSegment shadowTextSegment = new ShapedTextSegment(textSegment.glyphs(), shadowStyle, textSegment.visualBounds(), textSegment.logicalBounds());
                final ShapedTextSegment nonShadowTextSegment = new ShapedTextSegment(textSegment.glyphs(), style.withShadow(false), textSegment.visualBounds(), textSegment.logicalBounds());
                final float shadowOffset = font.getSize() * (style.shadowOffset() / 100F);
                this.renderTextSegment(positionMatrix, multiDrawBatchDataHolder, font, shadowTextSegment, x + shadowOffset, y + shadowOffset, z);
                this.renderTextDecorations(positionMatrix, multiDrawBatchDataHolder, decorationFont, shadowTextSegment, x + shadowOffset, y + shadowOffset, z);
                this.renderTextSegment(positionMatrix, multiDrawBatchDataHolder, font, nonShadowTextSegment, x, y, z);
                this.renderTextDecorations(positionMatrix, multiDrawBatchDataHolder, decorationFont, nonShadowTextSegment, x, y, z);
            } else {
                this.renderTextSegment(positionMatrix, multiDrawBatchDataHolder, font, textSegment, x, y, z);
                this.renderTextDecorations(positionMatrix, multiDrawBatchDataHolder, decorationFont, textSegment, x, y, z);
            }
        }
    }

    protected abstract void renderTextSegment(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final FontInstance font, final ShapedTextSegment textSegment, final float x, final float y, final float z);

    protected void renderTextSegment(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final FontInstance font, final ShapedTextSegment textSegment, final float x, final float y, final float z, final int textDataIndex) {
        final VertexBufferBuilder vertexBufferBuilder = multiDrawBatchDataHolder.getVertexBufferBuilder(this.drawBatch);
        final ArrayCache<AtlasGlyph> atlasGlyphs = this.getAtlasGlyphs(font);
        for (TextShaper.Glyph shapedGlyph : textSegment.glyphs()) {
            final AtlasGlyph atlasGlyph = atlasGlyphs.getOrLoad(shapedGlyph.index());
            if (atlasGlyph != AtlasGlyph.EMPTY) {
                this.renderGlyph(positionMatrix, vertexBufferBuilder, atlasGlyph, x + shapedGlyph.x(), y + shapedGlyph.y(), z, textSegment.style(), textDataIndex);
            }
        }
    }

    protected void renderTextDecorations(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final FontInstance font, final ShapedTextSegment textSegment, final float x, final float y, final float z) {
        final TextStyle style = textSegment.style();
        if (style.isUnderline()) {
            final float lineY = y + font.getUnderlinePosition();
            final float halfLineThickness = font.getUnderlineThickness() / 2F;
            Primitives.filledRectangle(positionMatrix, multiDrawBatchDataHolder, x + textSegment.logicalBounds().minX, lineY - halfLineThickness, x + textSegment.logicalBounds().maxX, lineY + halfLineThickness, z, style.color().toABGR());
        }
        if (style.isStrikethrough()) {
            final float lineY = y + font.getStrikethroughPosition();
            final float halfLineThickness = font.getStrikethroughThickness() / 2F;
            Primitives.filledRectangle(positionMatrix, multiDrawBatchDataHolder, x + textSegment.logicalBounds().minX, lineY - halfLineThickness, x + textSegment.logicalBounds().maxX, lineY + halfLineThickness, z, style.color().toABGR());
        }
    }

    private void renderGlyph(final Matrix4f positionMatrix, final VertexBufferBuilder vertexBufferBuilder, final AtlasGlyph glyph, final float x, final float y, final float z, final TextStyle textStyle, final int textDataIndex) {
        final float x1 = x + glyph.xOffset();
        final float x2 = x1 + glyph.width();
        final float y1 = y + glyph.yOffset();
        final float y2 = y1 + glyph.height();

        float topOffset = 0F;
        float bottomOffset = 0F;
        if (textStyle.isItalic()) {
            final float shearFactor = (float) Math.tan(Math.toRadians(textStyle.italicAngle()));
            topOffset = shearFactor * -(y1 - y);
            bottomOffset = shearFactor * (y2 - y);
        }

        vertexBufferBuilder.writeVector3f(positionMatrix, x1 - bottomOffset, y2, z).writeTextureCoord(glyph.u1(), glyph.v2()).writeByte((byte) glyph.atlasIndex()).writeShort((short) textDataIndex).endVertex();
        vertexBufferBuilder.writeVector3f(positionMatrix, x2 - bottomOffset, y2, z).writeTextureCoord(glyph.u2(), glyph.v2()).writeByte((byte) glyph.atlasIndex()).writeShort((short) textDataIndex).endVertex();
        vertexBufferBuilder.writeVector3f(positionMatrix, x2 + topOffset, y1, z).writeTextureCoord(glyph.u2(), glyph.v1()).writeByte((byte) glyph.atlasIndex()).writeShort((short) textDataIndex).endVertex();
        vertexBufferBuilder.writeVector3f(positionMatrix, x1 + topOffset, y1, z).writeTextureCoord(glyph.u1(), glyph.v1()).writeByte((byte) glyph.atlasIndex()).writeShort((short) textDataIndex).endVertex();
    }

    private ArrayCache<AtlasGlyph> getAtlasGlyphs(final FontInstance font) {
        if (!this.fontAtlasGlyphs.containsKey(font)) {
            this.fontAtlasGlyphs.put(font, new ArrayCache<>(font.getFace().getGlyphCount(), glyphIndex -> this.loadAtlasGlyph(font, glyphIndex)));
        }
        return this.fontAtlasGlyphs.get(font);
    }

    private AtlasGlyph getAtlasGlyph(final FontInstance font, final int glyphIndex) {
        return this.getAtlasGlyphs(font).getOrLoad(glyphIndex);
    }

    private AtlasGlyph loadAtlasGlyph(final FontInstance font, final int glyphIndex) {
        if (font instanceof ScaledFontInstance scaledFont) {
            final AtlasGlyph baseAtlasGlyph = this.getAtlasGlyph(scaledFont.getBaseInstance(), glyphIndex);
            if (baseAtlasGlyph != AtlasGlyph.EMPTY) {
                return new AtlasGlyph(baseAtlasGlyph.atlasIndex(), baseAtlasGlyph.u1(), baseAtlasGlyph.v1(), baseAtlasGlyph.u2(), baseAtlasGlyph.v2(),
                        baseAtlasGlyph.xOffset() * scaledFont.getScale(),
                        baseAtlasGlyph.yOffset() * scaledFont.getScale(),
                        baseAtlasGlyph.width() * scaledFont.getScale(),
                        baseAtlasGlyph.height() * scaledFont.getScale()
                );
            } else {
                return AtlasGlyph.EMPTY;
            }
        }

        final FontInstance.GlyphBitmap glyphBitmap = font.createGlyphBitmap(glyphIndex, this.glyphRenderMode);
        if (glyphBitmap == null) {
            return AtlasGlyph.EMPTY;
        }

        Slot atlasSlot = null;
        StaticAtlasTexture atlas = null;
        for (int i = 0; i <= this.glyphAtlases.size(); i++) {
            if (i == this.glyphAtlases.size()) {
                atlas = switch (this.glyphRenderMode) {
                    case PIXELATED -> {
                        final StaticAtlasTexture atlasTexture = new StaticAtlasTexture(GL30C.GL_R8, ATLAS_SIZE, ATLAS_SIZE);
                        atlasTexture.setFilter(GL11C.GL_NEAREST);
                        atlasTexture.setParameterIntArray(GL33C.GL_TEXTURE_SWIZZLE_RGBA, new int[]{GL11C.GL_ONE, GL11C.GL_ONE, GL11C.GL_ONE, GL11C.GL_RED});
                        yield atlasTexture;
                    }
                    case COLORED_PIXELATED -> {
                        final StaticAtlasTexture atlasTexture = new StaticAtlasTexture(GL11C.GL_RGBA8, ATLAS_SIZE, ATLAS_SIZE);
                        atlasTexture.setFilter(GL11C.GL_NEAREST);
                        yield atlasTexture;
                    }
                    case ANTIALIASED -> {
                        final StaticAtlasTexture atlasTexture = new StaticAtlasTexture(GL30C.GL_R8, ATLAS_SIZE, ATLAS_SIZE);
                        atlasTexture.setParameterIntArray(GL33C.GL_TEXTURE_SWIZZLE_RGBA, new int[]{GL11C.GL_ONE, GL11C.GL_ONE, GL11C.GL_ONE, GL11C.GL_RED});
                        yield atlasTexture;
                    }
                    case COLORED_ANTIALIASED -> new StaticAtlasTexture(GL11C.GL_RGBA8, ATLAS_SIZE, ATLAS_SIZE);
                    case BSDF, SDF -> {
                        final StaticAtlasTexture atlasTexture = new StaticAtlasTexture(GL30C.GL_R8, ATLAS_SIZE, ATLAS_SIZE);
                        atlasTexture.setParameterIntArray(GL33C.GL_TEXTURE_SWIZZLE_RGBA, new int[]{GL11C.GL_RED, GL11C.GL_RED, GL11C.GL_RED, GL11C.GL_ONE});
                        yield atlasTexture;
                    }
                    case MSDF -> new StaticAtlasTexture(GL11C.GL_RGB8, ATLAS_SIZE, ATLAS_SIZE);
                };
            } else {
                atlas = this.glyphAtlases.get(i);
            }
            atlasSlot = atlas.addSlot(glyphBitmap.image());
            if (atlasSlot != null) {
                break;
            }
        }
        glyphBitmap.image().free();
        if (atlasSlot == null) { // Should never happen
            throw new IllegalStateException("Failed to find a free slot for glyph in atlas");
        }
        if (!this.glyphAtlases.contains(atlas)) {
            this.glyphAtlases.add(atlas);
        }

        return new AtlasGlyph(this.glyphAtlases.indexOf(atlas), atlasSlot.u1(), atlasSlot.v1(), atlasSlot.u2(), atlasSlot.v2(), glyphBitmap.xOffset(), glyphBitmap.yOffset(), atlasSlot.width(), atlasSlot.height());
    }

    private record AtlasGlyph(int atlasIndex, float u1, float v1, float u2, float v2, float xOffset, float yOffset, float width, float height) {

        private static final AtlasGlyph EMPTY = new AtlasGlyph(0, 0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F);

    }

}
