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
package net.raphimc.thingl.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlSlPreprocessor {

    private final Map<String, Object> defines = new HashMap<>();

    public void addDefines(final Map<String, Object> defines) {
        if (defines == null) {
            throw new IllegalArgumentException("Defines map cannot be null");
        }
        for (Map.Entry<String, Object> entry : defines.entrySet()) {
            this.addDefine(entry.getKey(), entry.getValue());
        }
    }

    public void addDefine(final String name, final Object value) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Define name cannot be null or empty");
        }
        this.defines.put(name, value);
    }

    public String process(final String code) {
        final List<String> codeLines = new ArrayList<>(code.lines().toList());
        if (codeLines.isEmpty()) {
            throw new IllegalArgumentException("Shader code cannot be empty");
        }
        if (!codeLines.get(0).startsWith("#version")) {
            throw new IllegalArgumentException("Shader code must start with a #version directive");
        }

        if (!this.defines.isEmpty()) {
            final List<String> defineLines = new ArrayList<>();
            defineLines.add("#line 2 1");
            for (Map.Entry<String, Object> entry : this.defines.entrySet()) {
                final StringBuilder sb = new StringBuilder();
                sb.append("#define ").append(entry.getKey());
                if (entry.getValue() != null) {
                    sb.append(' ').append(entry.getValue());
                }
                defineLines.add(sb.toString());
            }
            defineLines.add("#line 2 0");

            codeLines.addAll(1, defineLines);
        }

        return String.join("\n", codeLines);
    }

}
