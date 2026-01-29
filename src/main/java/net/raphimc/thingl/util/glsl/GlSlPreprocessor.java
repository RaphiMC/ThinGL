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
package net.raphimc.thingl.util.glsl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class GlSlPreprocessor {

    private final List<String> codeLines = new ArrayList<>();

    public GlSlPreprocessor(final String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Shader code cannot be null or empty");
        }
        this.codeLines.addAll(code.lines().toList());
    }

    public void prependDefines(final Map<String, String> defines) {
        if (defines == null) {
            throw new IllegalArgumentException("Defines map cannot be null");
        }

        for (Map.Entry<String, String> entry : defines.entrySet()) {
            this.prependDefine(entry.getKey(), entry.getValue());
        }
    }

    public void prependDefine(final String name, final String value) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Define name cannot be null or empty");
        }

        final StringBuilder defineBuilder = new StringBuilder();
        defineBuilder.append("#define ").append(name);
        if (value != null) {
            defineBuilder.append(' ').append(value);
        }
        final String defineLine = defineBuilder.toString();

        final int index = this.getIndex(line -> line.startsWith("#version")) + 1;
        this.insertLines(defineLine, index, List.of(defineLine));
    }

    public void appendDefines(final Map<String, String> defines) {
        if (defines == null) {
            throw new IllegalArgumentException("Defines map cannot be null");
        }

        for (Map.Entry<String, String> entry : defines.entrySet()) {
            this.appendDefine(entry.getKey(), entry.getValue());
        }
    }

    public void appendDefine(final String name, final String value) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Define name cannot be null or empty");
        }

        final StringBuilder defineBuilder = new StringBuilder();
        defineBuilder.append("#define ").append(name);
        if (value != null) {
            defineBuilder.append(' ').append(value);
        }
        final String defineLine = defineBuilder.toString();

        final int index = this.getIndex(line -> !line.startsWith("#"));
        this.insertLines(defineLine, index, List.of(defineLine));
    }

    public void resolveIncludes(final Function<String, String> includeResolver) {
        if (includeResolver == null) {
            throw new IllegalArgumentException("Include resolver cannot be null");
        }

        for (int i = 0; i < this.codeLines.size(); i++) {
            final String line = this.codeLines.get(i).trim();
            if (line.startsWith("#include ")) {
                final String includePath = line.substring(9).trim().replaceAll("^\"|\"$", "");
                final String includeCode = includeResolver.apply(includePath);
                this.codeLines.remove(i);
                i += this.insertLines(includePath, i, includeCode.lines().toList()) - 1;
            }
        }
    }

    public void addIncludeGuard(final String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Include guard name cannot be null or empty");
        }

        final String includeGuard = name.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "_") + "_GUARD";
        this.insertLines(includeGuard, 0, List.of("#ifndef " + includeGuard, "#define " + includeGuard));
        this.insertLines(includeGuard, 3, this.codeLines.size(), List.of("#endif // " + includeGuard));
    }

    public String getCode() {
        return String.join("\n", this.codeLines);
    }

    private int getIndex(final Predicate<String> condition) {
        for (int i = 0; i < this.codeLines.size(); i++) {
            final String line = this.codeLines.get(i).trim();
            if (condition.test(line)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Could not find line matching the given condition");
    }

    private int insertLines(final Object identifier, final int index, final List<String> lines) {
        return this.insertLines(identifier, 1, index, lines);
    }

    private int insertLines(final Object identifier, final int startLine, final int index, final List<String> lines) {
        final String sourceStringNumber = String.valueOf(Math.abs(identifier.hashCode()));
        final List<String> linesToInsert = new ArrayList<>();
        linesToInsert.add("#line " + startLine + " " + sourceStringNumber + " // Begin: " + identifier);
        for (String line : lines) {
            linesToInsert.add(line.replaceAll("(^\\s*#line \\d+ )0", "$1" + sourceStringNumber));
        }
        if (index < this.codeLines.size()) {
            linesToInsert.add(this.getEffectiveLineAndSourceHash(index) + " // End: " + identifier);
            this.codeLines.addAll(index, linesToInsert);
        } else if (index == this.codeLines.size()) {
            linesToInsert.add(this.getEffectiveLineAndSourceHash(index) + " // End: " + identifier);
            this.codeLines.addAll(linesToInsert);
        } else {
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for code lines of size " + this.codeLines.size());
        }
        return linesToInsert.size();
    }

    private String getEffectiveLineAndSourceHash(final int absoluteLineNumber) {
        for (int i = Math.min(absoluteLineNumber, this.codeLines.size() - 1); i >= 0; i--) {
            final String line = this.codeLines.get(i).trim();
            if (line.startsWith("#line ")) {
                if (i == absoluteLineNumber) {
                    return line;
                }

                final String[] parts = line.split(" ");
                if (parts.length >= 3) {
                    try {
                        final int relativeLineNumber = absoluteLineNumber - (i + 1);
                        final int lineNumber = Integer.parseInt(parts[1]);
                        final int sourceHash = Integer.parseInt(parts[2]);
                        return "#line " + (lineNumber + relativeLineNumber) + " " + sourceHash;
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid #line directive format at line " + (i + 1));
                    }
                }
            }
        }
        return "#line " + (absoluteLineNumber + 1) + " 0";
    }

}
