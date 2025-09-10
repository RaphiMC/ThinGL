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
package net.raphimc.thingl.awt.text;

import it.unimi.dsi.fastutil.ints.IntObjectPair;
import net.raphimc.thingl.awt.AwtUtil;
import net.raphimc.thingl.text.font.Font;
import net.raphimc.thingl.util.ImageUtil;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AwtFont extends Font {

    private final java.awt.Font font;
    private final BufferedImage drawImage;
    private final Graphics2D graphics;
    private final LineMetrics fontMetrics;
    private final String postScriptName;
    private final String familyName;

    public AwtFont(final java.awt.Font font) {
        this(font, false);
    }

    public AwtFont(final java.awt.Font font, final boolean useHinting) {
        super(font.getSize());
        this.font = font;
        this.drawImage = new BufferedImage(font.getSize() * 2, font.getSize() * 2, BufferedImage.TYPE_INT_ARGB);
        this.graphics = this.drawImage.createGraphics();
        AwtUtil.configureGraphics2DForMaximumQuality(this.graphics);
        if (useHinting) {
            this.graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        } else {
            this.graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        }
        this.graphics.setFont(font);
        this.graphics.setColor(Color.WHITE);
        this.fontMetrics = this.font.getLineMetrics("", this.graphics.getFontRenderContext());
        this.postScriptName = font.getPSName();
        this.familyName = font.getFamily();
    }

    @Override
    public GlyphBitmap createGlyphBitmap(final Glyph glyph, final GlyphBitmap.RenderMode renderMode) {
        final GlyphVector glyphVector = this.font.createGlyphVector(this.graphics.getFontRenderContext(), new int[]{glyph.glyphIndex()});
        if (glyphVector.getNumGlyphs() != 1) {
            throw new IllegalStateException("Glyph vector for glyph index " + glyph.glyphIndex() + " does not map to exactly one glyph");
        }

        switch (renderMode) {
            case PIXELATED, COLORED_PIXELATED -> this.graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            case ANTIALIASED, COLORED_ANTIALIASED -> this.graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            default -> throw new IllegalArgumentException("Unsupported render mode: " + renderMode);
        }
        this.graphics.setComposite(AlphaComposite.Clear);
        this.graphics.fillRect(0, 0, this.drawImage.getWidth(), this.drawImage.getHeight());
        this.graphics.setComposite(AlphaComposite.Src);
        this.graphics.drawGlyphVector(glyphVector, -glyph.bearingX(), -glyph.bearingY());
        final int width = (int) Math.ceil(glyph.width());
        final int height = (int) Math.ceil(glyph.height());
        final int[] pixels = new int[width * height];
        this.drawImage.getRGB(0, 0, width, height, pixels, 0, width);

        final IntObjectPair<ByteBuffer> pixelData = switch (renderMode) {
            case PIXELATED, ANTIALIASED -> {
                final ByteBuffer buffer = ByteBuffer.allocate(pixels.length * Integer.BYTES);
                buffer.order(ByteOrder.BIG_ENDIAN).asIntBuffer().put(pixels).flip();
                yield IntObjectPair.of(GL11C.GL_RED, ImageUtil.convertColorToGrayscale(buffer, width, height, 0, false, true));
            }
            case COLORED_PIXELATED, COLORED_ANTIALIASED -> {
                final ByteBuffer buffer = MemoryUtil.memAlloc(pixels.length * Integer.BYTES);
                buffer.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().put(pixels).flip();
                yield IntObjectPair.of(GL12C.GL_BGRA, buffer);
            }
            default -> throw new IllegalArgumentException("Unsupported render mode: " + renderMode);
        };

        return new GlyphBitmap(width, height, glyph.bearingX(), glyph.bearingY(), pixelData.leftInt(), pixelData.right());
    }

    @Override
    public void free() {
        super.free();
        this.graphics.dispose();
    }

    public java.awt.Font getFont() {
        return this.font;
    }

    @Override
    public float getAscent() {
        return this.fontMetrics.getAscent();
    }

    @Override
    public float getDescent() {
        return this.fontMetrics.getDescent();
    }

    @Override
    public float getHeight() {
        return this.fontMetrics.getHeight();
    }

    @Override
    public float getUnderlinePosition() {
        return this.fontMetrics.getUnderlineOffset() + this.fontMetrics.getUnderlineThickness() / 2F;
    }

    @Override
    public float getUnderlineThickness() {
        return this.fontMetrics.getUnderlineThickness();
    }

    @Override
    public float getStrikethroughPosition() {
        return this.fontMetrics.getStrikethroughOffset() + this.fontMetrics.getStrikethroughThickness() / 2F;
    }

    @Override
    public float getStrikethroughThickness() {
        return this.fontMetrics.getStrikethroughThickness();
    }

    @Override
    public String getPostScriptName() {
        return this.postScriptName;
    }

    @Override
    public String getFamilyName() {
        return this.familyName;
    }

    @Override
    public String getSubFamilyName() {
        return null;
    }

    @Override
    protected Glyph loadGlyphByIndex(final int glyphIndex) {
        final GlyphVector glyphVector = this.font.createGlyphVector(this.graphics.getFontRenderContext(), new int[]{glyphIndex});
        if (glyphVector.getNumGlyphs() != 1) {
            throw new IllegalStateException("Glyph vector for glyph index " + glyphIndex + " does not map to exactly one glyph");
        }

        final GlyphMetrics glyphMetrics = glyphVector.getGlyphMetrics(0);
        final Rectangle2D bounds = glyphMetrics.getBounds2D();
        if (!this.graphics.getFontRenderContext().usesFractionalMetrics()) {
            bounds.setRect(bounds.getBounds());
        }

        final float width = (float) bounds.getWidth();
        final float height = (float) bounds.getHeight();
        final float xAdvance = glyphMetrics.getAdvanceX();
        final float bearingX = (float) bounds.getMinX();
        final float bearingY = (float) bounds.getMinY();
        return new Glyph(this, glyphIndex, width, height, xAdvance, bearingX, bearingY);
    }

    @Override
    protected Glyph loadGlyphByCodePoint(final int codePoint) {
        final GlyphVector glyphVector = this.font.createGlyphVector(this.graphics.getFontRenderContext(), Character.toChars(codePoint));
        if (glyphVector.getNumGlyphs() != 1) {
            throw new IllegalStateException("Glyph vector for code point " + codePoint + " does not map to exactly one glyph");
        }
        return this.getGlyphByIndex(glyphVector.getGlyphCode(0));
    }

    @Override
    protected long createHarfBuzzInstance() {
        throw new UnsupportedOperationException();
    }

}
