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
package net.raphimc.thingl.text.markup.parser;

import it.unimi.dsi.fastutil.chars.CharPredicate;

import java.util.LinkedHashMap;
import java.util.Map;

public class MarkupParser {

    private static final CharPredicate NAME_CHARS = c -> Character.isLetterOrDigit(c) || c == '_' || c == ':' || c == '-' || c == '.';
    private static final CharPredicate UNQUOTED_VALUE_CHARS = c -> !Character.isWhitespace(c) && c != '>';
    private static final CharPredicate TEXT_STOP_CHARS = c -> c == '<' || c == '&';

    private final StringReader reader;

    public MarkupParser(final String input) {
        this.reader = new StringReader(input);
    }

    public void process(final MarkupEventHandler eventHandler) {
        this.reader.reset();
        try {
            while (this.reader.isReadable()) {
                if (this.reader.isNext('<')) { // tag
                    this.reader.skip(); // skip '<'
                    if (!this.reader.isNext('/')) { // start tag
                        final String elementName = this.readName();
                        final Map<String, String> attributes = new LinkedHashMap<>(4);
                        while (!this.reader.isNext('>') && !this.reader.isNext('/')) { // attributes
                            final String attributeName = this.readName();
                            this.reader.require('=');
                            attributes.put(attributeName, this.readValue());
                        }
                        final boolean selfClosing = this.reader.isNext('/');
                        if (selfClosing) {
                            this.reader.skip(); // skip '/'
                        }
                        this.reader.require('>');

                        eventHandler.handleStartElement(elementName, attributes);
                        if (selfClosing) {
                            eventHandler.handleEndElement(elementName);
                        }
                    } else { // end tag
                        this.reader.skip(); // skip '/'
                        eventHandler.handleEndElement(this.readName());
                        this.reader.require('>');
                    }
                } else { // text
                    final StringBuilder textBuilder = new StringBuilder();
                    while (this.reader.isReadable()) {
                        textBuilder.append(this.reader.readUntilEndOfInputOr(TEXT_STOP_CHARS));
                        if (!this.reader.isReadable()) { // end of input
                            break;
                        } else if (this.reader.isNext('<')) { // start of a tag
                            break;
                        } else if (this.reader.isNext('&')) { // entity
                            this.reader.skip(); // skip '&'
                            final String entity = this.reader.readUntil(';');
                            this.reader.skip(); // skip ';'
                            if (entity.startsWith("#")) { // numeric entity
                                if (entity.startsWith("#x") || entity.startsWith("#X")) { // hex
                                    textBuilder.appendCodePoint(Integer.parseInt(entity.substring(2), 16));
                                } else { // decimal
                                    textBuilder.appendCodePoint(Integer.parseInt(entity.substring(1)));
                                }
                            } else { // named entity
                                switch (entity) {
                                    case "lt" -> textBuilder.append('<');
                                    case "gt" -> textBuilder.append('>');
                                    case "amp" -> textBuilder.append('&');
                                    case "quot" -> textBuilder.append('"');
                                    case "apos" -> textBuilder.append('\'');
                                    default -> throw new RuntimeException("Unknown named entity: &" + entity + ";");
                                }
                            }
                        } else {
                            throw new RuntimeException("Unexpected character: " + this.reader.peek());
                        }
                    }
                    eventHandler.handleText(textBuilder.toString());
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage() + ", at index " + this.reader.getPosition() + ", context: '" + this.reader.getContext() + "'", e);
        }
    }

    private String readName() {
        this.reader.skipWhitespace();
        final String name = this.reader.readWhile(NAME_CHARS, 1);
        this.reader.skipWhitespace();
        return name;
    }

    private String readValue() {
        this.reader.skipWhitespace();
        final String value;
        if (this.reader.peek() == '"' || this.reader.peek() == '\'') { // quoted value
            final char quote = this.reader.read();
            value = this.reader.readUntil(quote);
            this.reader.skip(); // skip closing quote
        } else { // unquoted value
            value = this.reader.readWhile(UNQUOTED_VALUE_CHARS);
        }
        this.reader.skipWhitespace();
        return value;
    }

}
