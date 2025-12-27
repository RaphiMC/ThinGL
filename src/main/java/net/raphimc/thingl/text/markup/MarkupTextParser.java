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
package net.raphimc.thingl.text.markup;

import net.raphimc.thingl.resource.font.Font;
import net.raphimc.thingl.text.TextLine;
import net.raphimc.thingl.text.TextRun;
import net.raphimc.thingl.text.TextStyle;
import net.raphimc.thingl.text.font.FontSet;
import net.raphimc.thingl.text.markup.handler.TextLineMarkupHandler;
import net.raphimc.thingl.text.markup.handler.TextRunMarkupHandler;
import net.raphimc.thingl.text.markup.parser.MarkupParser;

/**
 * <h1>Markup Format Documentation</h1>
 * <p>The markup text format allows styling text with an XML-like syntax.</p>
 * <p>The parser uses a stack-based approach to apply styles. Each opening tag pushes a new style onto the stack,
 * and each closing tag pops the style off the stack, reverting to the previous style.
 * The parser can have a base style set that is applied to the entire text unless overridden by markup elements.</p>
 *
 * <h2>Examples</h2>
 * <h3>Basic Usage</h3>
 * <code>&lt;bold&gt;This is bold text&lt;/bold&gt;</code><br>
 * Renders as: <b>This is bold text</b>
 * <h3>Nested Styles</h3>
 * <code>&lt;color rgb=#FF0000&gt;This is red &lt;italic&gt;and italic&lt;/italic&gt; text&lt;/color&gt;</code><br>
 * Renders as: <span style="color:#FF0000;">This is red <i>and italic</i> text</span>
 * <h3>Combining Multiple Styles</h3>
 * <code>&lt;bold&gt;&lt;color rgb=(0,0,255)&gt;Bold and Blue Text&lt;/color&gt;&lt;/bold&gt;</code><br>
 * Renders as: <b><span style="color:#0000FF;">Bold and Blue Text</span></b>
 * <h3>Optional closing tags</h3>
 * If a closing tag is omitted, the style will apply until the end of the text or until another style overrides it.<br>
 * Example:<br>
 * <code>&lt;bold&gt;This is bold text. &lt;italic&gt;This is bold and italic text.</code><br>
 * Renders as: <b>This is bold text. <i>This is bold and italic text.</i></b>
 *
 * <h2>Elements</h2>
 * <h3>bold</h3>
 * <b>Description</b>: Makes the text bold.<br>
 * <b>Alias</b>: <code>b</code><br>
 * <b>Attributes</b>:
 * <ul>
 *     <li>
 *         <code>enabled</code> (optional): <code>true</code> or <code>false</code> to enable or disable bold formatting. Default: <code>true</code>.
 *     </li>
 * </ul>
 *
 * <h3>color</h3>
 * <b>Description</b>: Sets the text color.<br>
 * <b>Aliases</b>: <code>col</code>, <code>c</code><br>
 * <b>Attributes</b>:
 * <ul>
 *     <li>
 *         <code>rgb</code> (required): Hexadecimal (<code>#RRGGBB</code>) or RGB tuple (<code>(R,G,B)</code>) specifying the color.
 *     </li>
 *     <li>
 *         <code>rgba</code> (required): Hexadecimal (<code>#RRGGBBAA</code>) or RGBA tuple (<code>(R,G,B,A)</code>) specifying the color.
 *     </li>
 *     <li>
 *         <code>argb</code> (required): Hexadecimal (<code>#AARRGGBB</code>) or ARGB tuple (<code>(A,R,G,B)</code>) specifying the color.
 *     </li>
 *     <li>
 *         <code>value</code> (required): A color constant name (e.g., <code>red</code>, <code>blue</code>, <code>green</code>, etc.).
 *     </li>
 * </ul>
 * <b>Notes</b>: Only one of the attributes is required. If multiple are provided, the last one takes precedence.
 *
 * <h3>italic</h3>
 * <b>Description</b>: Makes the text italic.<br>
 * <b>Alias</b>: <code>i</code><br>
 * <b>Attributes</b>:
 * <ul>
 *     <li>
 *         <code>enabled</code> (optional): <code>true</code> or <code>false</code> to enable or disable italic formatting. Default: <code>true</code>.
 *     </li>
 * </ul>
 *
 * <h3>outline</h3>
 * <b>Description</b>: Adds an outline to the text.<br>
 * <b>Attributes</b>:
 * <ul>
 *     <li>
 *         <code>rgb</code> (required): Hexadecimal (<code>#RRGGBB</code>) or RGB tuple (<code>(R,G,B)</code>) specifying the color.
 *     </li>
 *     <li>
 *         <code>rgba</code> (required): Hexadecimal (<code>#RRGGBBAA</code>) or RGBA tuple (<code>(R,G,B,A)</code>) specifying the color.
 *     </li>
 *     <li>
 *         <code>argb</code> (required): Hexadecimal (<code>#AARRGGBB</code>) or ARGB tuple (<code>(A,R,G,B)</code>) specifying the color.
 *     </li>
 *     <li>
 *         <code>value</code> (required): A color constant name (e.g., <code>red</code>, <code>blue</code>, <code>green</code>, etc.).
 *     </li>
 * </ul>
 * <b>Notes</b>: Only one of the attributes is required. If multiple are provided, the last one takes precedence.
 *
 * <h3>shadow</h3>
 * <b>Description</b>: Adds a shadow to the text.<br>
 * <b>Attributes</b>:
 * <ul>
 *     <li>
 *         <code>enabled</code> (optional): <code>true</code> or <code>false</code> to enable or disable text shadow. Default: <code>true</code>.
 *     </li>
 * </ul>
 *
 * <h3>strikethrough</h3>
 * <b>Description</b>: Adds a strikethrough line to the text.<br>
 * <b>Alias</b>: <code>s</code><br>
 * <b>Attributes</b>:
 * <ul>
 *     <li>
 *         <code>enabled</code> (optional): <code>true</code> or <code>false</code> to enable or disable strikethrough formatting. Default: <code>true</code>.
 *     </li>
 * </ul>
 *
 * <h3>underline</h3>
 * <b>Description</b>: Adds an underline to the text.<br>
 * <b>Alias</b>: <code>u</code><br>
 * <b>Attributes</b>:
 * <ul>
 *     <li>
 *         <code>enabled</code> (optional): <code>true</code> or <code>false</code> to enable or disable underline formatting. Default: <code>true</code>.
 *     </li>
 * </ul>
 */
public class MarkupTextParser {

    private static final TextStyle DEFAULT_STYLE = new TextStyle();

    public static TextRun parse(final Font font, final String markupText) {
        return parse(font, markupText, DEFAULT_STYLE);
    }

    public static TextRun parse(final Font font, final String markupText, final TextStyle baseStyle) {
        final MarkupParser markupParser = new MarkupParser(markupText);
        final TextRunMarkupHandler textRunMarkupHandler = new TextRunMarkupHandler(font, baseStyle);
        markupParser.process(textRunMarkupHandler);
        return textRunMarkupHandler.getTextRun();
    }

    public static TextLine parse(final FontSet fontSet, final String markupText) {
        return parse(fontSet, markupText, DEFAULT_STYLE);
    }

    public static TextLine parse(final FontSet fontSet, final String markupText, final TextStyle baseStyle) {
        final MarkupParser markupParser = new MarkupParser(markupText);
        final TextLineMarkupHandler textLineMarkupHandler = new TextLineMarkupHandler(fontSet, baseStyle);
        markupParser.process(textLineMarkupHandler);
        return textLineMarkupHandler.getTextLine();
    }

}
