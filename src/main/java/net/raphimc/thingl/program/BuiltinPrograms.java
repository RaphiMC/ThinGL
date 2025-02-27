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
package net.raphimc.thingl.program;

import net.raphimc.thingl.program.post.impl.*;
import net.raphimc.thingl.resource.program.Program;
import net.raphimc.thingl.resource.shader.Shader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static net.raphimc.thingl.resource.shader.Shader.Type.*;

public class BuiltinPrograms {

    private static final Map<String, Shader> SHADERS = new HashMap<>();

    // Regular
    public static final RegularProgram POSITION_COLOR = new RegularProgram(getShader("regular/position_color", VERTEX), getShader("regular/position_color", FRAGMENT));
    public static final RegularProgram POSITION_TEXTURE = new RegularProgram(getShader("regular/position_texture", VERTEX), getShader("regular/position_texture", FRAGMENT));
    public static final RegularProgram POSITION_COLOR_TEXTURE = new RegularProgram(getShader("regular/position_color_texture", VERTEX), getShader("regular/position_color_texture", FRAGMENT));
    public static final RegularProgram COLORIZED_TEXTURE = new RegularProgram(getShader("regular/position_color_texture", VERTEX), getShader("regular/colorized_texture", FRAGMENT));
    public static final RegularProgram BITMAP_TEXT = new RegularProgram(getShader("regular/bitmap_text", VERTEX), getShader("regular/bitmap_text", FRAGMENT));
    public static final RegularProgram SDF_TEXT = new RegularProgram(getShader("regular/sdf_text", VERTEX), getShader("regular/sdf_text", FRAGMENT));

    // Post Processing
    public static final GaussianBlurProgram GAUSSIAN_BLUR = new GaussianBlurProgram();
    public static final KawaseBlurProgram KAWASE_BLUR = new KawaseBlurProgram();
    public static final ColorTweakProgram COLOR_TWEAK = new ColorTweakProgram();
    public static final SingleColorProgram SINGLE_COLOR = new SingleColorProgram();
    public static final RainbowColorProgram RAINBOW_COLOR = new RainbowColorProgram();
    public static final OutlineProgram OUTLINE = new OutlineProgram();
    public static final MSAAProgram MSAA = new MSAAProgram(4);

    // Instancing
    public static final RegularProgram INSTANCED_POSITION_COLOR = new RegularProgram(getShader("instancing/position_color", VERTEX), getShader("regular/position_color", FRAGMENT));

    // Multidraw
    public static final RegularProgram MULTIDRAW_POSITION_COLOR = new RegularProgram(getShader("multidraw/position_color", VERTEX), getShader("regular/position_color", FRAGMENT));
    public static final RegularProgram MULTIDRAW_LINE = new RegularProgram(getShader("multidraw/line", VERTEX), getShader("geometry/line", GEOMETRY), getShader("geometry/line", FRAGMENT));

    // Geometry
    public static final RegularProgram LINE = new RegularProgram(getShader("geometry/line", VERTEX), getShader("geometry/line", GEOMETRY), getShader("geometry/line", FRAGMENT));

    static {
        for (Field field : BuiltinPrograms.class.getDeclaredFields()) {
            if (Program.class.isAssignableFrom(field.getType())) {
                try {
                    final Program program = (Program) field.get(null);
                    if (program != null) {
                        program.setDebugName(field.getName());
                    }
                } catch (Throwable ignored) {
                }
            }
        }
    }

    public static Shader getShader(final String name, final Shader.Type type) {
        return SHADERS.computeIfAbsent(name + "." + type.getFileExtension(), path -> {
            try {
                final InputStream stream = BuiltinPrograms.class.getClassLoader().getResourceAsStream("thingl/shaders/" + path);
                if (stream == null) {
                    throw new IOException("Shader " + name + " not found");
                }
                final String source = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                final Shader shader = new Shader(type, source);
                shader.setDebugName(name);
                return shader;
            } catch (Throwable e) {
                throw new RuntimeException("Failed to load shader " + name, e);
            }
        });
    }

}
