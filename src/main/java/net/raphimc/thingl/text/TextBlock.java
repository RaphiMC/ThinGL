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
package net.raphimc.thingl.text;

import net.lenni0451.commons.collections.Lists;
import net.raphimc.thingl.text.shaping.ShapedTextBlock;
import net.raphimc.thingl.text.shaping.TextShaper;
import net.raphimc.thingl.text.shaping.impl.BasicTextShaper;

import java.util.List;

public record TextBlock(List<TextLine> lines) {

    public TextBlock(final TextLine... lines) {
        this(Lists.arrayList(lines));
    }

    public TextBlock addLine(final TextLine line) {
        this.lines.add(line);
        return this;
    }

    public TextBlock add(final TextLine line) {
        return this.addLine(line);
    }

    public ShapedTextBlock shape() {
        return this.shape(BasicTextShaper.INSTANCE);
    }

    public ShapedTextBlock shape(final TextShaper shaper) {
        return shaper.shape(this);
    }

}
