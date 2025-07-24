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

import net.lenni0451.commons.lazy.Lazy;
import net.raphimc.thingl.program.post.impl.*;
import net.raphimc.thingl.resource.program.Program;
import net.raphimc.thingl.resource.shader.Shader;
import net.raphimc.thingl.util.GlSlPreprocessor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static net.raphimc.thingl.resource.shader.Shader.Type.*;

public class Programs {

    private final Map<String, Shader> shaders = new HashMap<>();

    private final Lazy<RegularProgram> color = Lazy.of(() -> {
        final RegularProgram program = new RegularProgram(this.getShader("regular/color", VERTEX), this.getShader("regular/color", FRAGMENT));
        program.setDebugName("color");
        return program;
    });

    private final Lazy<RegularProgram> texture = Lazy.of(() -> {
        final RegularProgram program = new RegularProgram(this.getShader("regular/texture", VERTEX), this.getShader("regular/texture", FRAGMENT));
        program.setDebugName("texture");
        return program;
    });

    private final Lazy<RegularProgram> textureArrayLayer = Lazy.of(() -> {
        final RegularProgram program = new RegularProgram(this.getShader("regular/texture_array_layer", VERTEX), this.getShader("regular/texture_array_layer", FRAGMENT));
        program.setDebugName("texture_array_layer");
        return program;
    });

    private final Lazy<RegularProgram> coloredTexture = Lazy.of(() -> {
        final RegularProgram program = new RegularProgram(this.getShader("regular/colored_texture", VERTEX), this.getShader("regular/colored_texture", FRAGMENT));
        program.setDebugName("colored_texture");
        return program;
    });

    private final Lazy<RegularProgram> colorizedTexture = Lazy.of(() -> {
        final RegularProgram program = new RegularProgram(this.getShader("regular/colored_texture", VERTEX), this.getShader("regular/colorized_texture", FRAGMENT));
        program.setDebugName("colorized_texture");
        return program;
    });

    private final Lazy<RegularProgram> line = Lazy.of(() -> {
        final RegularProgram program = new RegularProgram(this.getShader("geometry/line", VERTEX), this.getShader("geometry/line", GEOMETRY), this.getShader("geometry/line", FRAGMENT));
        program.setDebugName("line");
        return program;
    });

    private final Lazy<RegularProgram> bitmapText = Lazy.of(() -> {
        final RegularProgram program = new RegularProgram(this.getShader("regular/bitmap_text", VERTEX), this.getShader("regular/bitmap_text", FRAGMENT));
        program.setDebugName("bitmap_text");
        return program;
    });

    private final Lazy<RegularProgram> sdfText = Lazy.of(() -> {
        final RegularProgram program = new RegularProgram(this.getShader("regular/sdf_text", VERTEX), this.getShader("regular/sdf_text", FRAGMENT));
        program.setDebugName("sdf_text");
        return program;
    });

    private final Lazy<RegularProgram> skyBox = Lazy.of(() -> {
        final RegularProgram program = new RegularProgram(this.getShader("regular/sky_box", VERTEX), this.getShader("regular/sky_box", FRAGMENT));
        program.setDebugName("sky_box");
        return program;
    });

    private final Lazy<GaussianBlurProgram> gaussianBlur = Lazy.of(() -> {
        final GaussianBlurProgram program = new GaussianBlurProgram(this.getShader("post/post_processing", Shader.Type.VERTEX), this.getShader("post/gaussian_blur", Shader.Type.FRAGMENT));
        program.setDebugName("gaussian_blur");
        return program;
    });

    private final Lazy<KawaseBlurProgram> kawaseBlur = Lazy.of(() -> {
        final KawaseBlurProgram program = new KawaseBlurProgram(this.getShader("post/post_processing", Shader.Type.VERTEX), this.getShader("post/kawase_blur", Shader.Type.FRAGMENT));
        program.setDebugName("kawase_blur");
        return program;
    });

    private final Lazy<ColorTweakProgram> colorTweak = Lazy.of(() -> {
        final ColorTweakProgram program = new ColorTweakProgram(this.getShader("post/post_processing", Shader.Type.VERTEX), this.getShader("post/color_tweak", Shader.Type.FRAGMENT));
        program.setDebugName("color_tweak");
        return program;
    });

    private final Lazy<SingleColorProgram> singleColor = Lazy.of(() -> {
        final SingleColorProgram program = new SingleColorProgram(this.getShader("post/post_processing", Shader.Type.VERTEX), this.getShader("post/single_color", Shader.Type.FRAGMENT));
        program.setDebugName("single_color");
        return program;
    });

    private final Lazy<RainbowColorProgram> rainbowColor = Lazy.of(() -> {
        final RainbowColorProgram program = new RainbowColorProgram(this.getShader("post/post_processing", Shader.Type.VERTEX), this.getShader("post/rainbow_color", Shader.Type.FRAGMENT));
        program.setDebugName("rainbow_color");
        return program;
    });

    private final Lazy<OutlineProgram> outline = Lazy.of(() -> {
        final OutlineProgram program = new OutlineProgram(this.getShader("post/post_processing", Shader.Type.VERTEX), this.getShader("post/outline", Shader.Type.FRAGMENT));
        program.setDebugName("outline");
        return program;
    });

    private final Lazy<MSAAProgram> msaa = Lazy.of(() -> {
        final MSAAProgram program = new MSAAProgram(this.getShader("post/post_processing", Shader.Type.VERTEX), this.getShader("post/msaa", Shader.Type.FRAGMENT), 4);
        program.setDebugName("msaa");
        return program;
    });

    private final Lazy<RegularProgram> instancedColor = Lazy.of(() -> {
        final RegularProgram program = new RegularProgram(this.getShader("instancing/color", VERTEX), this.getShader("regular/color", FRAGMENT));
        program.setDebugName("instanced_color");
        return program;
    });

    private final Lazy<RegularProgram> multidrawColor = Lazy.of(() -> {
        final RegularProgram program = new RegularProgram(this.getShader("multidraw/color", VERTEX), this.getShader("regular/color", FRAGMENT));
        program.setDebugName("multidraw_color");
        return program;
    });

    private final Lazy<RegularProgram> multidrawLine = Lazy.of(() -> {
        final RegularProgram program = new RegularProgram(this.getShader("multidraw/line", VERTEX), this.getShader("geometry/line", GEOMETRY), this.getShader("geometry/line", FRAGMENT));
        program.setDebugName("multidraw_line");
        return program;
    });

    public RegularProgram getColor() {
        return this.color.get();
    }

    public RegularProgram getTexture() {
        return this.texture.get();
    }

    public RegularProgram getTextureArrayLayer() {
        return this.textureArrayLayer.get();
    }

    public RegularProgram getColoredTexture() {
        return this.coloredTexture.get();
    }

    public RegularProgram getColorizedTexture() {
        return this.colorizedTexture.get();
    }

    public RegularProgram getLine() {
        return this.line.get();
    }

    public RegularProgram getBitmapText() {
        return this.bitmapText.get();
    }

    public RegularProgram getSdfText() {
        return this.sdfText.get();
    }

    public RegularProgram getSkyBox() {
        return this.skyBox.get();
    }

    public GaussianBlurProgram getGaussianBlur() {
        return this.gaussianBlur.get();
    }

    public KawaseBlurProgram getKawaseBlur() {
        return this.kawaseBlur.get();
    }

    public ColorTweakProgram getColorTweak() {
        return this.colorTweak.get();
    }

    public SingleColorProgram getSingleColor() {
        return this.singleColor.get();
    }

    public RainbowColorProgram getRainbowColor() {
        return this.rainbowColor.get();
    }

    public OutlineProgram getOutline() {
        return this.outline.get();
    }

    public MSAAProgram getMsaa() {
        return this.msaa.get();
    }

    public RegularProgram getInstancedColor() {
        return this.instancedColor.get();
    }

    public RegularProgram getMultidrawColor() {
        return this.multidrawColor.get();
    }

    public RegularProgram getMultidrawLine() {
        return this.multidrawLine.get();
    }

    protected Shader getShader(final String name, final Shader.Type type) {
        return this.getShader(name, type, Map.of());
    }

    protected Shader getShader(final String name, final Shader.Type type, final Map<String, Object> defines) {
        return this.shaders.computeIfAbsent(name + "." + type.getFileExtension(), path -> {
            try {
                final InputStream stream = this.getClass().getClassLoader().getResourceAsStream("thingl/shaders/" + path);
                if (stream == null) {
                    throw new IOException("Shader " + name + " not found");
                }
                String source = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                source = GlSlPreprocessor.addDefines(source, defines);
                final Shader shader = new Shader(type, source);
                shader.setDebugName(name);
                return shader;
            } catch (Throwable e) {
                throw new RuntimeException("Failed to load shader " + name, e);
            }
        });
    }

    public void free() {
        Class<?> clazz = this.getClass();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getType() == Lazy.class) {
                    try {
                        final Lazy<? extends Program> lazy = (Lazy<? extends Program>) field.get(this);
                        if (lazy.isInitialized()) {
                            lazy.get().freeFully();
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Failed to free program", e);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

}
