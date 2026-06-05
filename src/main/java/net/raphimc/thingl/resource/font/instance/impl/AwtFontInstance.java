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
package net.raphimc.thingl.resource.font.instance.impl;

import net.raphimc.thingl.resource.font.face.impl.AwtFontFace;
import net.raphimc.thingl.resource.font.instance.FontInstance;
import net.raphimc.thingl.resource.image.impl.AwtByteImage2D;
import net.raphimc.thingl.resource.image.impl.ByteImage2D;
import net.raphimc.thingl.util.AwtUtil;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class AwtFontInstance extends FontInstance {

    private final Font font;
    private final BufferedImage drawImage;
    private final Graphics2D graphics;
    private final LineMetrics fontMetrics;

    public AwtFontInstance(final AwtFontFace face, final int size) {
        super(face, size);
        this.font = face.getFont().deriveFont((float) size);
        this.drawImage = new BufferedImage(this.font.getSize() * 2, this.font.getSize() * 2, BufferedImage.TYPE_INT_ARGB);
        this.graphics = this.drawImage.createGraphics();
        AwtUtil.configureGraphics2DForMaximumQuality(this.graphics);
        this.graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        this.graphics.setFont(this.font);
        this.graphics.setColor(Color.WHITE);
        this.fontMetrics = this.font.getLineMetrics("", this.graphics.getFontRenderContext());
    }

    @Override
    public GlyphBitmap createGlyphBitmap(final int glyphIndex, final GlyphBitmap.RenderMode renderMode) {
        final GlyphMetrics metrics = this.getGlyphMetrics(glyphIndex);
        if (metrics.width() <= 0 || metrics.height() <= 0) {
            return null;
        }
        switch (renderMode) {
            case PIXELATED, COLORED_PIXELATED -> this.graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            case ANTIALIASED, COLORED_ANTIALIASED -> this.graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            default -> throw new IllegalArgumentException("Unsupported render mode: " + renderMode);
        }
        final GlyphVector glyphVector = this.font.createGlyphVector(this.graphics.getFontRenderContext(), new int[]{glyphIndex});
        if (glyphVector.getNumGlyphs() != 1) {
            throw new IllegalStateException("Glyph vector for glyph index " + glyphIndex + " does not map to exactly one glyph");
        }
        final int xOffset = (int) Math.floor(metrics.bearingX());
        final int yOffset = (int) Math.floor(metrics.bearingY());
        final int width = (int) Math.ceil(metrics.width()) + 1;
        final int height = (int) Math.ceil(metrics.height()) + 1;
        this.graphics.setComposite(AlphaComposite.Clear);
        this.graphics.fillRect(0, 0, this.drawImage.getWidth(), this.drawImage.getHeight());
        this.graphics.setComposite(AlphaComposite.Src);
        this.graphics.drawGlyphVector(glyphVector, -xOffset, -yOffset);
        ByteImage2D image = new AwtByteImage2D(this.drawImage.getSubimage(0, 0, width, height));
        if (renderMode == GlyphBitmap.RenderMode.PIXELATED || renderMode == GlyphBitmap.RenderMode.ANTIALIASED) {
            image = image.convertToSingleChannel(3);
        }
        return new GlyphBitmap(image, xOffset, yOffset);
    }

    @Override
    public AwtFontFace getFace() {
        return (AwtFontFace) super.getFace();
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
    protected GlyphMetrics loadGlyphMetrics(final int glyphIndex) {
        final GlyphVector glyphVector = this.font.createGlyphVector(this.graphics.getFontRenderContext(), new int[]{glyphIndex});
        if (glyphVector.getNumGlyphs() != 1) {
            throw new IllegalStateException("Glyph vector for glyph index " + glyphIndex + " does not map to exactly one glyph");
        }
        final java.awt.font.GlyphMetrics glyphMetrics = glyphVector.getGlyphMetrics(0);
        final Rectangle2D bounds = glyphMetrics.getBounds2D();
        return new GlyphMetrics((float) bounds.getWidth(), (float) bounds.getHeight(), glyphMetrics.getAdvanceX(), (float) bounds.getMinX(), (float) bounds.getMinY());
    }

    protected void free0() {
        super.free0();
        this.graphics.dispose();
    }

}
