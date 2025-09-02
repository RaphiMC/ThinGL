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
package net.raphimc.thingl.text.shaping.impl;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.text.TextRun;
import net.raphimc.thingl.text.TextSegment;
import net.raphimc.thingl.text.font.Font;
import net.raphimc.thingl.text.shaping.ShapedTextRun;
import net.raphimc.thingl.text.shaping.ShapedTextSegment;
import net.raphimc.thingl.text.shaping.TextShaper;
import org.lwjgl.util.harfbuzz.HarfBuzz;
import org.lwjgl.util.harfbuzz.hb_glyph_info_t;
import org.lwjgl.util.harfbuzz.hb_glyph_position_t;

import java.util.ArrayList;
import java.util.List;

public class HarfBuzzTextShaper extends TextShaper {

    public static final HarfBuzzTextShaper INSTANCE = new HarfBuzzTextShaper();

    @Override
    public ShapedTextRun shape(final TextRun textRun) {
        ThinGL.capabilities().ensureHarfBuzzPresent();
        final long hbBuffer = HarfBuzz.hb_buffer_create();
        if (!HarfBuzz.hb_buffer_allocation_successful(hbBuffer)) {
            throw new IllegalStateException("Failed to allocate buffer");
        }
        HarfBuzz.hb_buffer_reset(hbBuffer);
        HarfBuzz.hb_buffer_set_cluster_level(hbBuffer, HarfBuzz.HB_BUFFER_CLUSTER_LEVEL_CHARACTERS);

        for (int segmentIdx = 0; segmentIdx < textRun.segments().size(); segmentIdx++) {
            final TextSegment textSegment = textRun.segments().get(segmentIdx);
            final String text = textSegment.text();
            for (int i = 0; i < text.length(); i++) {
                final int codePoint = text.codePointAt(i);
                if (codePoint >= Character.MIN_SUPPLEMENTARY_CODE_POINT) {
                    i++;
                }
                HarfBuzz.hb_buffer_add(hbBuffer, codePoint, segmentIdx);
            }
        }

        HarfBuzz.hb_buffer_guess_segment_properties(hbBuffer);
        // HarfBuzz.hb_buffer_set_direction(hbBuffer, HarfBuzz.HB_DIRECTION_LTR);
        // HarfBuzz.hb_buffer_set_script(hbBuffer, HarfBuzz.HB_SCRIPT_LATIN);
        // HarfBuzz.hb_buffer_set_language(hbBuffer, HarfBuzz.hb_language_from_string("en"));
        HarfBuzz.hb_shape(textRun.font().getHarfBuzzInstance(), hbBuffer, null);

        final hb_glyph_info_t.Buffer infos = HarfBuzz.hb_buffer_get_glyph_infos(hbBuffer);
        final hb_glyph_position_t.Buffer positions = HarfBuzz.hb_buffer_get_glyph_positions(hbBuffer);
        final int length = infos.remaining();
        if (length != positions.remaining()) {
            throw new IllegalStateException("Glyph info and position buffers have different lengths");
        }
        final List<ShapedTextSegment> shapedTextSegments = new ArrayList<>(textRun.segments().size());
        for (TextSegment textSegment : textRun.segments()) {
            shapedTextSegments.add(new ShapedTextSegment(new ArrayList<>(textSegment.text().length()), textSegment.color(), textSegment.styleFlags(), textSegment.outlineColor(), textSegment.visualOffset()));
        }
        float x = 0F;
        float y = 0F;
        for (int i = 0; i < length; i++) {
            final hb_glyph_info_t info = infos.get(i);
            final hb_glyph_position_t position = positions.get(i);
            final Font.Glyph fontGlyph = textRun.font().getGlyphByIndex(info.codepoint());
            shapedTextSegments.get(info.cluster()).glyphs().add(new Glyph(fontGlyph, x + position.x_offset() / 64F, y - position.y_offset() / 64F));
            x += position.x_advance() / 64F;
            y -= position.y_advance() / 64F;
        }
        for (ShapedTextSegment textSegment : shapedTextSegments) {
            textSegment.calculateBounds();
        }

        HarfBuzz.hb_buffer_destroy(hbBuffer);
        return new ShapedTextRun(textRun.font(), shapedTextSegments);
    }

}
