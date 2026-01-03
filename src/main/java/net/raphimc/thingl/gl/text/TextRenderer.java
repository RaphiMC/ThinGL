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
import net.raphimc.thingl.rendering.dataholder.DrawBatchDataHolder;
import net.raphimc.thingl.rendering.dataholder.MultiDrawBatchDataHolder;
import net.raphimc.thingl.resource.font.Font;
import net.raphimc.thingl.text.TextStyle;
import net.raphimc.thingl.text.shaping.*;
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

    public static final float ITALIC_SHEAR_FACTOR = (float) Math.tan(Math.toRadians(14)); // 14 degrees
    public static final float SHADOW_OFFSET_FACTOR = 0.075F;
    public static final float BOLD_OFFSET_DIVIDER = 64F;

    private static final int ATLAS_SIZE = 1024;

    private final DrawBatch drawBatch;
    private final Font.GlyphBitmap.RenderMode glyphRenderMode;
    private final List<StaticAtlasTexture> glyphAtlases = new ArrayList<>();
    private final Reference2ObjectMap<Font.Glyph, AtlasGlyph> atlasGlyphs = new Reference2ObjectOpenHashMap<>();
    private float globalScale = 1F;

    protected TextRenderer(final Supplier<Program> program, final Font.GlyphBitmap.RenderMode glyphRenderMode) {
        this(program, glyphRenderMode, p -> {
        });
    }

    protected TextRenderer(final Supplier<Program> program, final Font.GlyphBitmap.RenderMode glyphRenderMode, final Consumer<Program> programSetup) {
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
            final TextStyle style = textSegment.style();
            final float xOffset = style.visualOffset().x * this.globalScale;
            final float yOffset = style.visualOffset().y * this.globalScale;

            if (style.isShadow()) {
                final Color shadowColor = style.color().multiply(0.25F);
                final int shadowStyleFlags = style.flags() & ~TextStyle.STYLE_STRIKETHROUGH_BIT;
                final Color shadowOutlineColor = style.color().withAlpha(style.outlineColor().getAlpha()).multiply(0.25F);

                final ShapedTextSegment shadowTextSegment = new ShapedTextSegment(textSegment.glyphs(), new TextStyle(shadowColor, shadowStyleFlags, shadowOutlineColor, style.visualOffset()), textSegment.visualBounds(), textSegment.logicalBounds());
                final ShapedTextSegment nonShadowTextSegment = new ShapedTextSegment(textSegment.glyphs(), style.withShadow(false), textSegment.visualBounds(), textSegment.logicalBounds());
                final float shadowOffset = SHADOW_OFFSET_FACTOR * textRun.font().getSize() * this.globalScale;
                this.renderTextSegment(positionMatrix, multiDrawBatchDataHolder, shadowTextSegment, x + xOffset + shadowOffset, y + yOffset + shadowOffset, z);
                this.renderTextDecorations(positionMatrix, multiDrawBatchDataHolder, shadowTextSegment, x + xOffset + shadowOffset, y + yOffset + shadowOffset, z, decorationFont);
                this.renderTextSegment(positionMatrix, multiDrawBatchDataHolder, nonShadowTextSegment, x + xOffset, y + yOffset, z);
                this.renderTextDecorations(positionMatrix, multiDrawBatchDataHolder, nonShadowTextSegment, x + xOffset, y + yOffset, z, decorationFont);
            } else {
                this.renderTextSegment(positionMatrix, multiDrawBatchDataHolder, textSegment, x + xOffset, y + yOffset, z);
                this.renderTextDecorations(positionMatrix, multiDrawBatchDataHolder, textSegment, x + xOffset, y + yOffset, z, decorationFont);
            }
        }
    }

    protected abstract void renderTextSegment(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final ShapedTextSegment textSegment, final float x, final float y, final float z);

    protected void renderTextSegment(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final ShapedTextSegment textSegment, final float x, final float y, final float z, final int textDataIndex) {
        final DrawBatchDataHolder drawBatchDataHolder = multiDrawBatchDataHolder.getDrawBatchDataHolder(this.drawBatch);
        final VertexBufferBuilder vertexBufferBuilder = drawBatchDataHolder.getVertexBufferBuilder();

        for (TextShaper.Glyph shapedGlyph : textSegment.glyphs()) {
            final Font.Glyph fontGlyph = shapedGlyph.fontGlyph();
            final AtlasGlyph atlasGlyph = this.getAtlasGlyph(fontGlyph);
            if (atlasGlyph != null) {
                final float glyphX = shapedGlyph.x() * this.globalScale;
                final float glyphY = shapedGlyph.y() * this.globalScale;
                this.renderGlyph(positionMatrix, vertexBufferBuilder, atlasGlyph, x + glyphX, y + glyphY, z, textSegment.style(), textDataIndex);
            }
        }
    }

    protected void renderTextDecorations(final Matrix4f positionMatrix, final MultiDrawBatchDataHolder multiDrawBatchDataHolder, final ShapedTextSegment textSegment, final float x, final float y, final float z, final Font font) {
        final TextStyle style = textSegment.style();
        if (style.isUnderline() || style.isStrikethrough()) {
            final int color = style.color().toABGR();
            if (style.isUnderline()) {
                float halfLineThickness = (font.getUnderlineThickness() * this.globalScale) / 2F;
                if (style.isBold()) {
                    halfLineThickness *= 1.5F;
                }
                final float lineY = y + font.getUnderlinePosition() * this.globalScale;
                Primitives.filledRectangle(positionMatrix, multiDrawBatchDataHolder, x + textSegment.logicalBounds().minX * this.globalScale, lineY - halfLineThickness, x + textSegment.logicalBounds().maxX * this.globalScale, lineY + halfLineThickness, z, color);
            }
            if (style.isStrikethrough()) {
                float halfLineThickness = (font.getStrikethroughThickness() * this.globalScale) / 2F;
                if (style.isBold()) {
                    halfLineThickness *= 1.5F;
                }
                final float lineY = y + font.getStrikethroughPosition() * this.globalScale;
                Primitives.filledRectangle(positionMatrix, multiDrawBatchDataHolder, x + textSegment.logicalBounds().minX * this.globalScale, lineY - halfLineThickness, x + textSegment.logicalBounds().maxX * this.globalScale, lineY + halfLineThickness, z, color);
            }
        }
    }

    private void renderGlyph(final Matrix4f positionMatrix, final VertexBufferBuilder vertexBufferBuilder, final AtlasGlyph glyph, final float x, final float y, final float z, final TextStyle textStyle, final int textDataIndex) {
        final float x1 = x + glyph.xOffset() * this.globalScale;
        final float x2 = x1 + glyph.width() * this.globalScale;
        final float y1 = y + glyph.yOffset() * this.globalScale;
        final float y2 = y1 + glyph.height() * this.globalScale;

        float topOffset = 0F;
        float bottomOffset = 0F;
        if (textStyle.isItalic()) {
            topOffset = ITALIC_SHEAR_FACTOR * -(y1 - y);
            bottomOffset = ITALIC_SHEAR_FACTOR * (y2 - y);
        }

        vertexBufferBuilder.writeVector3f(positionMatrix, x1 - bottomOffset, y2, z).writeTextureCoord(glyph.u1(), glyph.v2()).writeByte((byte) glyph.atlasIndex()).writeShort((short) textDataIndex).endVertex();
        vertexBufferBuilder.writeVector3f(positionMatrix, x2 - bottomOffset, y2, z).writeTextureCoord(glyph.u2(), glyph.v2()).writeByte((byte) glyph.atlasIndex()).writeShort((short) textDataIndex).endVertex();
        vertexBufferBuilder.writeVector3f(positionMatrix, x2 + topOffset, y1, z).writeTextureCoord(glyph.u2(), glyph.v1()).writeByte((byte) glyph.atlasIndex()).writeShort((short) textDataIndex).endVertex();
        vertexBufferBuilder.writeVector3f(positionMatrix, x1 + topOffset, y1, z).writeTextureCoord(glyph.u1(), glyph.v1()).writeByte((byte) glyph.atlasIndex()).writeShort((short) textDataIndex).endVertex();
    }

    private AtlasGlyph getAtlasGlyph(final Font.Glyph fontGlyph) {
        if (!this.atlasGlyphs.containsKey(fontGlyph)) {
            this.atlasGlyphs.put(fontGlyph, this.createAtlasGlyph(fontGlyph));
        }
        return this.atlasGlyphs.get(fontGlyph);
    }

    private AtlasGlyph createAtlasGlyph(final Font.Glyph fontGlyph) {
        final Font.GlyphBitmap glyphBitmap = fontGlyph.font().createGlyphBitmap(fontGlyph, this.glyphRenderMode);
        if (glyphBitmap == null) {
            return null;
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

        return new AtlasGlyph(this.glyphAtlases.indexOf(atlas), atlasSlot.u1(), atlasSlot.v1(), atlasSlot.u2(), atlasSlot.v2(), atlasSlot.width(), atlasSlot.height(), glyphBitmap.xOffset(), glyphBitmap.yOffset());
    }

    private record AtlasGlyph(int atlasIndex, float u1, float v1, float u2, float v2, float width, float height, float xOffset, float yOffset) {
    }

}
