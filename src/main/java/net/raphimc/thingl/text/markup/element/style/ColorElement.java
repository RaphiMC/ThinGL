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
package net.raphimc.thingl.text.markup.element.style;

import net.raphimc.thingl.text.TextStyle;
import net.raphimc.thingl.text.markup.element.Element;
import net.raphimc.thingl.text.markup.util.ConstantColorParser;
import net.raphimc.thingl.text.markup.util.IntegerColorParser;

public class ColorElement extends Element<TextStyle> {

    public ColorElement() {
        this.attributes.put("rgb", (style, value) -> style.withColor(IntegerColorParser.RGB.parse(value)));
        this.attributes.put("rgba", (style, value) -> style.withColor(IntegerColorParser.RGBA.parse(value)));
        this.attributes.put("argb", (style, value) -> style.withColor(IntegerColorParser.ARGB.parse(value)));
        this.attributes.put("value", (style, value) -> style.withColor(ConstantColorParser.parse(value)));
    }

}
