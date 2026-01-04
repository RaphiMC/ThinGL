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
package net.raphimc.thingl.text.markup.parser;

import it.unimi.dsi.fastutil.chars.CharPredicate;

public class StringReader {

    private static final int CONTEXT_LENGTH = 20;

    private final String input;
    private final int length;
    private int position;

    public StringReader(final String input) {
        this.input = input;
        this.length = input.length();
    }

    public boolean isNext(final char c) {
        return this.peek() == c;
    }

    public boolean isNext(final int offset, final char c) {
        return this.peek(offset) == c;
    }

    public boolean isNext(final String s) {
        if (this.position + s.length() <= this.length) {
            return this.input.regionMatches(this.position, s, 0, s.length());
        } else {
            return false;
        }
    }

    public char peek() {
        return peek(0);
    }

    public char peek(final int offset) {
        return this.input.charAt(this.position + offset);
    }

    public char read() {
        return this.input.charAt(this.position++);
    }

    public String readUntil(final char c) {
        final int start = this.position;
        while (this.input.charAt(this.position) != c) {
            this.position++;
        }
        return this.input.substring(start, this.position);
    }

    public String readUntil(final CharPredicate predicate) {
        final int start = this.position;
        while (!predicate.test(this.input.charAt(this.position))) {
            this.position++;
        }
        return this.input.substring(start, this.position);
    }

    public String readUntilEndOfInputOr(final CharPredicate predicate) {
        final int start = this.position;
        while (this.position < this.length && !predicate.test(this.input.charAt(this.position))) {
            this.position++;
        }
        return this.input.substring(start, this.position);
    }

    public String readWhile(final CharPredicate predicate) {
        return this.readWhile(predicate, 0);
    }

    public String readWhile(final CharPredicate predicate, final int minLength) {
        final int start = this.position;
        while (predicate.test(this.input.charAt(this.position))) {
            this.position++;
        }
        if (this.position - start < minLength) {
            throw new IllegalStateException("Expected at least " + minLength + " characters, but got " + (this.position - start));
        }
        return this.input.substring(start, this.position);
    }

    public void require(final char c) {
        if (this.read() != c) {
            throw new IllegalStateException("Expected '" + c + "' but got '" + this.peek() + "'");
        }
    }

    public void skip() {
        this.skip(1);
    }

    public void skip(final int count) {
        this.position += count;
    }

    public void skipWhitespace() {
        while (Character.isWhitespace(this.input.charAt(this.position))) {
            this.position++;
        }
    }

    public boolean isReadable() {
        return this.position < this.length;
    }

    public void reset() {
        this.position = 0;
    }

    public String getContext() {
        String context;
        if (this.position < CONTEXT_LENGTH) {
            context = this.input.substring(0, this.position);
        } else {
            context = "..." + this.input.substring(this.position - CONTEXT_LENGTH, this.position);
        }
        context += "<<<here>>>";
        if (this.length - this.position <= CONTEXT_LENGTH) {
            context += this.input.substring(this.position, this.length);
        } else {
            context += this.input.substring(this.position, this.position + CONTEXT_LENGTH) + "...";
        }
        return context;
    }

    public int getLength() {
        return this.length;
    }

    public int getPosition() {
        return this.position;
    }

    public void setPosition(final int position) {
        this.position = position;
    }

}
