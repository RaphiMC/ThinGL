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
package net.raphimc.thingl.text.markup.element;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public abstract class Element<T> {

    protected final Map<String, BiFunction<T, String, T>> attributes = new HashMap<>();

    public T apply(T target, final Map<String, String> attributes) {
        if (!attributes.isEmpty()) {
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                final BiFunction<T, String, T> handler = this.attributes.get(entry.getKey());
                if (handler != null) {
                    target = handler.apply(target, entry.getValue());
                } else {
                    throw new IllegalArgumentException("Unknown attribute: '" + entry.getKey() + "', expected one of: " + this.attributes.keySet());
                }
            }
        } else {
            final BiFunction<T, String, T> handler = this.attributes.get(null);
            if (handler != null) {
                target = handler.apply(target, null);
            } else {
                throw new IllegalArgumentException("Element requires at least one attribute: " + this.attributes.keySet());
            }
        }
        return target;
    }

}
