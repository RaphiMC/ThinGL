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
package net.raphimc.thingl.resource.font.face;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.raphimc.thingl.resource.font.instance.FontInstance;
import net.raphimc.thingl.resource.font.instance.FontInstanceSet;
import net.raphimc.thingl.text.util.GlyphPredicate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;

public class FontFaceSet {

    private final FontFace mainFace;
    private final SequencedMap<FontFace, GlyphPredicate> faces = new LinkedHashMap<>();
    private final Int2ObjectMap<FontInstanceSet> instanceSets = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>());

    public FontFaceSet(final SequencedMap<FontFace, GlyphPredicate> faces) {
        if (faces.isEmpty()) {
            throw new IllegalArgumentException("Font face set must contain at least one face");
        }
        this.mainFace = faces.firstEntry().getKey();
        this.faces.putAll(faces);
    }

    public FontInstanceSet getInstanceSet(final int size) {
        return this.instanceSets.computeIfAbsent(size, s -> {
            final SequencedMap<FontInstance, GlyphPredicate> newFaces = new LinkedHashMap<>(this.faces.size());
            for (Map.Entry<FontFace, GlyphPredicate> entry : this.faces.entrySet()) {
                newFaces.put(entry.getKey().getInstance(s), entry.getValue());
            }
            return new FontInstanceSet(newFaces);
        });
    }

    public FontFace getMainFace() {
        return this.mainFace;
    }

    public FontFace getFace(final int codePoint) {
        for (Map.Entry<FontFace, GlyphPredicate> entry : this.faces.entrySet()) {
            final FontFace face = entry.getKey();
            final GlyphPredicate predicate = entry.getValue();
            if (predicate.test(codePoint) && face.getGlyphIndex(codePoint) != 0) {
                return face;
            }
        }
        return this.mainFace;
    }

    public void free() {
        this.faces.keySet().forEach(FontFace::free);
        this.faces.clear();
    }

}
