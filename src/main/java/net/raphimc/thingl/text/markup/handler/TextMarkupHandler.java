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

import net.raphimc.thingl.text.TextStyle;
import net.raphimc.thingl.text.markup.element.Element;
import net.raphimc.thingl.text.markup.element.style.*;
import net.raphimc.thingl.text.markup.parser.MarkupEventHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public abstract class TextMarkupHandler implements MarkupEventHandler {

    private static final Map<String, Element<TextStyle>> STYLE_ELEMENTS = new HashMap<>();

    static {
        STYLE_ELEMENTS.put("color", new ColorElement());
        STYLE_ELEMENTS.put("col", STYLE_ELEMENTS.get("color"));
        STYLE_ELEMENTS.put("c", STYLE_ELEMENTS.get("color"));

        STYLE_ELEMENTS.put("shadow", new ShadowElement());

        STYLE_ELEMENTS.put("bold", new BoldElement());
        STYLE_ELEMENTS.put("b", STYLE_ELEMENTS.get("bold"));

        STYLE_ELEMENTS.put("italic", new ItalicElement());
        STYLE_ELEMENTS.put("i", STYLE_ELEMENTS.get("italic"));

        STYLE_ELEMENTS.put("underline", new UnderlineElement());
        STYLE_ELEMENTS.put("u", STYLE_ELEMENTS.get("underline"));

        STYLE_ELEMENTS.put("strikethrough", new StrikethroughElement());
        STYLE_ELEMENTS.put("s", STYLE_ELEMENTS.get("strikethrough"));

        STYLE_ELEMENTS.put("outline", new OutlineElement());
    }

    private final Stack<String> elementStack = new Stack<>();
    private final Stack<TextStyle> styleStack = new Stack<>();

    public TextMarkupHandler(final TextStyle baseStyle) {
        this.styleStack.push(baseStyle);
    }

    @Override
    public void handleStartElement(final String name, final Map<String, String> attributes) {
        try {
            final Element<TextStyle> styleElement = STYLE_ELEMENTS.get(name);
            if (styleElement != null) {
                this.styleStack.push(styleElement.apply(this.styleStack.peek(), attributes));
            } else {
                throw new IllegalArgumentException("Unknown element: '" + name + "'");
            }
        } catch (Throwable e) {
            throw new RuntimeException("Error handling '" + name + "' element: " + e.getMessage(), e);
        }

        this.elementStack.push(name);
    }

    @Override
    public void handleEndElement(final String name) {
        if (this.elementStack.isEmpty()) {
            throw new IllegalStateException("No element to end for: '" + name + "'");
        }
        if (!this.elementStack.peek().equals(name)) {
            throw new IllegalStateException("Mismatched end element: '" + name + "', expected: '" + this.elementStack.peek() + "'");
        }
        this.elementStack.pop();

        if (STYLE_ELEMENTS.containsKey(name)) {
            this.styleStack.pop();
        }
    }

    @Override
    public void handleText(final String text) {
        this.handleStyledText(text, this.styleStack.peek());
    }

    protected abstract void handleStyledText(final String text, final TextStyle style);

}
