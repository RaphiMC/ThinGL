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

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.HashMap;

public class DefinesMap extends HashMap<String, String> {

    public void putBoolean(final String key, final boolean value) {
        this.put(key, Boolean.toString(value));
    }

    public void putInt(final String key, final int value) {
        this.put(key, Integer.toString(value));
    }

    public void putUnsignedInt(final String key, final int value) {
        this.put(key, Integer.toUnsignedString(value) + 'u');
    }

    public void putFloat(final String key, final float value) {
        this.put(key, Float.toString(value));
    }

    public void putVector2f(final String key, final Vector2f vector) {
        this.put(key, "vec2(" + vector.x + ", " + vector.y + ')');
    }

    public void putVector2f(final String key, final float x, final float y) {
        this.put(key, "vec2(" + x + ", " + y + ')');
    }

    public void putVector3f(final String key, final Vector3f vector) {
        this.put(key, "vec3(" + vector.x + ", " + vector.y + ", " + vector.z + ')');
    }

    public void putVector3f(final String key, final float x, final float y, final float z) {
        this.put(key, "vec3(" + x + ", " + y + ", " + z + ')');
    }

    public void putVector4f(final String key, final Vector4f vector) {
        this.put(key, "vec4(" + vector.x + ", " + vector.y + ", " + vector.z + ", " + vector.w + ')');
    }

    public void putVector4f(final String key, final float x, final float y, final float z, final float w) {
        this.put(key, "vec4(" + x + ", " + y + ", " + z + ", " + w + ')');
    }

}
