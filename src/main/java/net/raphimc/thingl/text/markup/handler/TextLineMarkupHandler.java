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
package net.raphimc.thingl.text.markup.handler;

import net.raphimc.thingl.text.TextLine;
import net.raphimc.thingl.text.TextRun;
import net.raphimc.thingl.text.TextStyle;
import net.raphimc.thingl.text.font.FontSet;

public class TextLineMarkupHandler extends TextMarkupHandler {

    private final FontSet fontSet;
    private final TextLine textLine;

    public TextLineMarkupHandler(final FontSet fontSet, final TextStyle baseStyle) {
        super(baseStyle);
        this.fontSet = fontSet;
        this.textLine = new TextLine();
    }

    @Override
    protected void handleStyledText(final String text, final TextStyle style) {
        final TextLine textLine = TextLine.fromString(this.fontSet, text, style);
        for (TextRun textRun : textLine.runs()) {
            this.textLine.addRun(textRun);
        }
    }

    public TextLine getTextLine() {
        return this.textLine;
    }

}
