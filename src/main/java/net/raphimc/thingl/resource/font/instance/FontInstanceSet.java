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
package net.raphimc.thingl.resource.font.instance;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.raphimc.thingl.text.util.GlyphPredicate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;

public class FontInstanceSet {

    private final FontInstance mainInstance;
    private final SequencedMap<FontInstance, GlyphPredicate> instances = new LinkedHashMap<>();
    private final Int2ObjectMap<FontInstanceSet> scaledInstanceSets = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>());

    public FontInstanceSet(final SequencedMap<FontInstance, GlyphPredicate> instances) {
        if (instances.isEmpty()) {
            throw new IllegalArgumentException("Font instance set must contain at least one instance");
        }
        this.mainInstance = instances.firstEntry().getKey();
        this.instances.putAll(instances);
    }

    public FontInstanceSet getScaledInstanceSet(final int size) {
        return this.scaledInstanceSets.computeIfAbsent(size, s -> {
            final SequencedMap<FontInstance, GlyphPredicate> newInstances = new LinkedHashMap<>(this.instances.size());
            for (Map.Entry<FontInstance, GlyphPredicate> entry : this.instances.entrySet()) {
                newInstances.put(entry.getKey().getScaledInstance(s), entry.getValue());
            }
            return new FontInstanceSet(newInstances);
        });
    }

    public FontInstance getMainInstance() {
        return this.mainInstance;
    }

    public FontInstance getInstance(final int codePoint) {
        for (Map.Entry<FontInstance, GlyphPredicate> entry : this.instances.entrySet()) {
            final FontInstance instance = entry.getKey();
            final GlyphPredicate predicate = entry.getValue();
            if (predicate.test(codePoint) && instance.getFace().getGlyphIndex(codePoint) != 0) {
                return instance;
            }
        }
        return this.mainInstance;
    }

    public void free() {
        this.instances.keySet().forEach(FontInstance::free);
        this.instances.clear();
    }

}
