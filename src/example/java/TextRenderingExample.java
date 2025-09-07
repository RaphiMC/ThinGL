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

import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.implementation.application.GLFWApplicationRunner;
import net.raphimc.thingl.renderer.impl.RendererText;
import net.raphimc.thingl.text.TextRun;
import net.raphimc.thingl.text.TextSegment;
import net.raphimc.thingl.text.font.Font;
import net.raphimc.thingl.text.renderer.BitmapTextRenderer;
import net.raphimc.thingl.text.renderer.SDFTextRenderer;
import net.raphimc.thingl.text.shaping.ShapedTextRun;
import net.raphimc.thingl.text.shaping.impl.HarfBuzzTextShaper;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.io.IOException;

public class TextRenderingExample extends GLFWApplicationRunner {

    public static void main(String[] args) {
        new TextRenderingExample().launch();
    }

    public TextRenderingExample() {
        super(new Configuration().setWindowTitle("ThinGL Example - Text rendering").setExtendedDebugMode(true));
    }

    // Instead of creating a new RendererText instance, you can use ThinGL.rendererText() to get the default text renderer.
    private RendererText bitmapTextRenderer = new RendererText(new BitmapTextRenderer());
    private RendererText sdfTextRenderer = new RendererText(new SDFTextRenderer());
    private Font robotoRegular;
    private ShapedTextRun multiColoredText;

    @Override
    protected void init() {
        super.init();
        try {
            final byte[] fontData = TextRenderingExample.class.getResourceAsStream("/fonts/Roboto-Regular.ttf").readAllBytes();
            this.robotoRegular = new Font(fontData, 32);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Example to select a custom charmap to use for resolving unicode characters
        // FreeTypeLibrary.checkError(FreeType.FT_Select_Charmap(font.getFontFace(), FreeType.FT_ENCODING_MS_SYMBOL), "Failed to select charmap");

        // Text run with multiple differently styled segments
        this.multiColoredText = new TextRun(robotoRegular, new TextSegment("Multi", Color.RED), new TextSegment("color ", Color.GREEN), new TextSegment("Text", Color.BLUE)).shape();
        // Same as above, but using the add method instead of the constructor
        this.multiColoredText = new TextRun(robotoRegular)
                .add(new TextSegment("Multi", Color.RED))
                .add(new TextSegment("color ", Color.GREEN))
                .add(new TextSegment("Text", Color.BLUE)).shape();

        // Optionally you can use HarfBuzz for better text shaping (Complex scripts, ligatures, kerning, ...)
        TextRun.fromString(robotoRegular, "text here").shape(HarfBuzzTextShaper.INSTANCE);
    }

    @Override
    protected void render(final Matrix4fStack positionMatrix) {
        positionMatrix.pushMatrix();
        { // Multi color text
            positionMatrix.translate(ThinGL.windowInterface().getFramebufferWidth() / 2F, 10, 0);
            this.sdfTextRenderer.textRun(positionMatrix, this.multiColoredText, 0, 0);
        }
        { // Text outline
            positionMatrix.translate(0, this.robotoRegular.getSize(), 0);
            this.sdfTextRenderer.textRun(positionMatrix, new TextRun(robotoRegular, new TextSegment("Outlined Text", Color.WHITE, 0, Color.RED)), 0, 0);
        }
        { // Bold text
            positionMatrix.translate(0, this.robotoRegular.getSize(), 0);
            this.sdfTextRenderer.textRun(positionMatrix, TextRun.fromString(robotoRegular, "Bold Text", Color.WHITE, TextSegment.STYLE_BOLD_BIT), 0, 0);
        }
        { // Shadowed text
            positionMatrix.translate(0, this.robotoRegular.getSize(), 0);
            this.sdfTextRenderer.textRun(positionMatrix, TextRun.fromString(robotoRegular, "Shadowed Text", Color.WHITE, TextSegment.STYLE_SHADOW_BIT), 0, 0);
        }
        { // Italic text
            positionMatrix.translate(0, this.robotoRegular.getSize(), 0);
            this.sdfTextRenderer.textRun(positionMatrix, TextRun.fromString(robotoRegular, "Italic Text", Color.WHITE, TextSegment.STYLE_ITALIC_BIT), 0, 0);
        }
        { // Underline text
            positionMatrix.translate(0, this.robotoRegular.getSize(), 0);
            this.sdfTextRenderer.textRun(positionMatrix, TextRun.fromString(robotoRegular, "Underlined Text", Color.WHITE, TextSegment.STYLE_UNDERLINE_BIT), 0, 0);
        }
        { // Strikethrough text
            positionMatrix.translate(0, this.robotoRegular.getSize(), 0);
            this.sdfTextRenderer.textRun(positionMatrix, TextRun.fromString(robotoRegular, "Strikethrough Text", Color.WHITE, TextSegment.STYLE_STRIKETHROUGH_BIT), 0, 0);
        }
        { // Multiple styles text
            positionMatrix.translate(0, this.robotoRegular.getSize(), 0);
            final TextRun textRun = new TextRun(robotoRegular, new TextSegment("Multiple Styles", Color.WHITE, TextSegment.STYLE_SHADOW_BIT | TextSegment.STYLE_BOLD_BIT | TextSegment.STYLE_ITALIC_BIT, Color.BLUE));
            this.sdfTextRenderer.textRun(positionMatrix, textRun, 0, 0);
        }
        positionMatrix.popMatrix();

        { // Bitmap text renderer
            positionMatrix.pushMatrix();
            this.animatedScale(positionMatrix);
            this.bitmapTextRenderer.textRun(positionMatrix, TextRun.fromString(robotoRegular, "Bitmap Rendering!"), 0, 0);
            positionMatrix.popMatrix();
        }

        positionMatrix.translate(0, ThinGL.windowInterface().getFramebufferHeight() / 2F, 0);

        { // SDF text renderer
            positionMatrix.pushMatrix();
            this.animatedScale(positionMatrix);
            this.sdfTextRenderer.textRun(positionMatrix, TextRun.fromString(robotoRegular, "SDF Rendering!"), 0, 0);
            positionMatrix.popMatrix();
        }
    }

    private void animatedScale(final Matrix4f positionMatrix) {
        final float elapsedTime = (System.currentTimeMillis() % 20000) / 10000F;
        final float scale = 0.2F + 4.8F * Math.abs((float) Math.sin(Math.PI * elapsedTime));
        positionMatrix.scaleXY(scale, scale);
    }

}
