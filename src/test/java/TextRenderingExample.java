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

import base.ExampleBase;
import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.drawbuilder.drawbatchdataholder.ImmediateMultiDrawBatchDataHolder;
import net.raphimc.thingl.drawbuilder.drawbatchdataholder.MultiDrawBatchDataHolder;
import net.raphimc.thingl.renderer.text.BSDFTextRenderer;
import net.raphimc.thingl.renderer.text.BitmapTextRenderer;
import net.raphimc.thingl.renderer.text.SDFTextRenderer;
import net.raphimc.thingl.renderer.text.TextRenderer;
import net.raphimc.thingl.util.font.Font;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.io.IOException;

public class TextRenderingExample extends ExampleBase {

    public static void main(String[] args) {
        new TextRenderingExample().run();
    }

    private BitmapTextRenderer bitmapTextRenderer;
    private SDFTextRenderer sdfTextRenderer;

    @Override
    protected void init() {
        final Font font;
        try {
            final byte[] fontData = TextRenderingExample.class.getResourceAsStream("/fonts/Roboto-Regular.ttf").readAllBytes();
            font = new Font(fontData, 32);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Example to select a custom charmap to use for resolving unicode characters
        // FreeTypeInstance.checkError(FreeType.FT_Select_Charmap(font.getFontFace(), FreeType.FT_ENCODING_MS_SYMBOL), "Failed to select charmap");

        this.bitmapTextRenderer = new BitmapTextRenderer(font);
        this.sdfTextRenderer = new BSDFTextRenderer(font);
    }

    @Override
    protected void render(final Matrix4fStack positionMatrix) {
        final MultiDrawBatchDataHolder multiDrawBatchDataHolder = new ImmediateMultiDrawBatchDataHolder();

        positionMatrix.pushMatrix();
        { // Multi color text
            positionMatrix.translate(ThinGL.windowInterface().getFramebufferWidth() / 2F, 10, 0);
            float xOffset = this.sdfTextRenderer.renderString(positionMatrix, multiDrawBatchDataHolder, "Multi", 0, 0, 0, Color.RED, 0, Color.TRANSPARENT);
            xOffset += this.sdfTextRenderer.renderString(positionMatrix, multiDrawBatchDataHolder, "color ", xOffset, 0, 0, Color.GREEN, TextRenderer.INTERNAL_NO_BEARING_BIT, Color.TRANSPARENT);
            this.sdfTextRenderer.renderString(positionMatrix, multiDrawBatchDataHolder, "Text", xOffset, 0, 0, Color.BLUE, TextRenderer.INTERNAL_NO_BEARING_BIT, Color.TRANSPARENT);
        }
        { // Text outline
            positionMatrix.translate(0, 32, 0);
            this.sdfTextRenderer.renderString(positionMatrix, multiDrawBatchDataHolder, "Outlined Text", 0, 0, 0, Color.WHITE, 0, Color.RED);
        }
        { // Bold text
            positionMatrix.translate(0, 32, 0);
            this.sdfTextRenderer.renderString(positionMatrix, multiDrawBatchDataHolder, "Bold Text", 0, 0, 0, Color.WHITE, TextRenderer.STYLE_BOLD_BIT, Color.TRANSPARENT);
        }
        { // Shadowed text
            positionMatrix.translate(0, 32, 0);
            this.sdfTextRenderer.renderString(positionMatrix, multiDrawBatchDataHolder, "Shadowed Text", 0, 0, 0, Color.WHITE, TextRenderer.STYLE_SHADOW_BIT, Color.TRANSPARENT);
        }
        { // Italic text
            positionMatrix.translate(0, 32, 0);
            this.sdfTextRenderer.renderString(positionMatrix, multiDrawBatchDataHolder, "Italic Text", 0, 0, 0, Color.WHITE, TextRenderer.STYLE_ITALIC_BIT, Color.TRANSPARENT);
        }
        { // Underline text
            positionMatrix.translate(0, 32, 0);
            this.sdfTextRenderer.renderString(positionMatrix, multiDrawBatchDataHolder, "Underline Text", 0, 0, 0, Color.WHITE, TextRenderer.STYLE_UNDERLINE_BIT, Color.TRANSPARENT);
        }
        { // Strikethrough text
            positionMatrix.translate(0, 32, 0);
            this.sdfTextRenderer.renderString(positionMatrix, multiDrawBatchDataHolder, "Strikethrough Text", 0, 0, 0, Color.WHITE, TextRenderer.STYLE_STRIKETHROUGH_BIT, Color.TRANSPARENT);
        }
        { // Multiple styles text
            positionMatrix.translate(0, 32, 0);
            this.sdfTextRenderer.renderString(positionMatrix, multiDrawBatchDataHolder, "Multiple Styles", 0, 0, 0, Color.WHITE, TextRenderer.STYLE_SHADOW_BIT | TextRenderer.STYLE_BOLD_BIT | TextRenderer.STYLE_ITALIC_BIT, Color.BLUE);
        }
        positionMatrix.popMatrix();

        { // Bitmap text renderer
            positionMatrix.pushMatrix();
            this.animatedScale(positionMatrix);
            this.bitmapTextRenderer.renderString(positionMatrix, multiDrawBatchDataHolder, "Bitmap Rendering!", 0, 0, 0, Color.WHITE);
            positionMatrix.popMatrix();
        }

        positionMatrix.translate(0, ThinGL.windowInterface().getFramebufferHeight() / 2F, 0);

        { // SDF text renderer
            positionMatrix.pushMatrix();
            this.animatedScale(positionMatrix);
            this.sdfTextRenderer.renderString(positionMatrix, multiDrawBatchDataHolder, "SDF Rendering!", 0, 0, 0, Color.WHITE);
            positionMatrix.popMatrix();
        }

        multiDrawBatchDataHolder.draw();
        multiDrawBatchDataHolder.free();
    }

    private void animatedScale(final Matrix4f positionMatrix) {
        final float elapsedTime = (System.currentTimeMillis() % 20000) / 10000F;
        final float scale = 0.2F + 4.8F * Math.abs((float) Math.sin(Math.PI * elapsedTime));
        positionMatrix.scaleXY(scale, scale);
    }

}
