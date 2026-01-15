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
package net.raphimc.thingl.gl.program;

import net.lenni0451.commons.lazy.Lazy;
import net.raphimc.thingl.gl.program.post.impl.*;
import net.raphimc.thingl.gl.resource.program.Program;
import net.raphimc.thingl.gl.resource.shader.Shader;
import net.raphimc.thingl.util.GlSlPreprocessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static net.raphimc.thingl.gl.resource.shader.Shader.Type.*;

public class Programs {

    protected final ShaderLoader shaderLoader = new ShaderLoader("thingl/shaders");

    private final Lazy<RegularProgram> color = Lazy.of(() -> {
        final RegularProgram program = new RegularProgram(this.shaderLoader.get("regular/color", VERTEX), this.shaderLoader.get("regular/color", FRAGMENT));
        program.setDebugName("color");
        return program;
    });

    private final Lazy<RegularProgram> texture = Lazy.of(() -> {
        final RegularProgram program = new RegularProgram(this.shaderLoader.get("regular/texture", VERTEX), this.shaderLoader.get("regular/texture", FRAGMENT));
        program.setDebugName("texture");
        return program;
    });

    private final Lazy<RegularProgram> textureArrayLayer = Lazy.of(() -> {
        final RegularProgram program = new RegularProgram(this.shaderLoader.get("regular/texture_array_layer", VERTEX), this.shaderLoader.get("regular/texture_array_layer", FRAGMENT));
        program.setDebugName("texture_array_layer");
        return program;
    });

    private final Lazy<RegularProgram> coloredTexture = Lazy.of(() -> {
        final RegularProgram program = new RegularProgram(this.shaderLoader.get("regular/colored_texture", VERTEX), this.shaderLoader.get("regular/colored_texture", FRAGMENT));
        program.setDebugName("colored_texture");
        return program;
    });

    private final Lazy<RegularProgram> colorizedTexture = Lazy.of(() -> {
        final RegularProgram program = new RegularProgram(this.shaderLoader.get("regular/colored_texture", VERTEX), this.shaderLoader.get("regular/colorized_texture", FRAGMENT));
        program.setDebugName("colorized_texture");
        return program;
    });

    private final Lazy<RegularProgram> line = Lazy.of(() -> {
        final RegularProgram program = new RegularProgram(this.shaderLoader.get("geometry/line", VERTEX), this.shaderLoader.get("geometry/line", GEOMETRY), this.shaderLoader.get("geometry/line", FRAGMENT));
        program.setDebugName("line");
        return program;
    });

    private final Lazy<RegularProgram> bitmapText = Lazy.of(() -> {
        final RegularProgram program = new RegularProgram(this.shaderLoader.get("regular/bitmap_text", VERTEX), this.shaderLoader.get("regular/bitmap_text", FRAGMENT));
        program.setDebugName("bitmap_text");
        return program;
    });

    private final Lazy<RegularProgram> sdfText = Lazy.of(() -> {
        final RegularProgram program = new RegularProgram(this.shaderLoader.get("regular/sdf_text", VERTEX), this.shaderLoader.get("regular/sdf_text", FRAGMENT));
        program.setDebugName("sdf_text");
        return program;
    });

    private final Lazy<RegularProgram> skyBox = Lazy.of(() -> {
        final RegularProgram program = new RegularProgram(this.shaderLoader.get("regular/sky_box", VERTEX), this.shaderLoader.get("regular/sky_box", FRAGMENT));
        program.setDebugName("sky_box");
        return program;
    });

    private final Lazy<GaussianBlurProgram> gaussianBlur = Lazy.of(() -> {
        final GaussianBlurProgram program = new GaussianBlurProgram(this.shaderLoader.get("post/post_processing", VERTEX), this.shaderLoader.get("post/gaussian_blur", FRAGMENT));
        program.setDebugName("gaussian_blur");
        return program;
    });

    private final Lazy<KawaseBlurProgram> kawaseBlur = Lazy.of(() -> {
        final KawaseBlurProgram program = new KawaseBlurProgram(this.shaderLoader.get("post/post_processing", VERTEX), this.shaderLoader.get("post/kawase_blur", FRAGMENT));
        program.setDebugName("kawase_blur");
        return program;
    });

    private final Lazy<ColorTweakProgram> colorTweak = Lazy.of(() -> {
        final ColorTweakProgram program = new ColorTweakProgram(this.shaderLoader.get("post/post_processing", VERTEX), this.shaderLoader.get("post/color_tweak", FRAGMENT));
        program.setDebugName("color_tweak");
        return program;
    });

    private final Lazy<SingleColorProgram> singleColor = Lazy.of(() -> {
        final SingleColorProgram program = new SingleColorProgram(this.shaderLoader.get("post/post_processing", VERTEX), this.shaderLoader.get("post/single_color", FRAGMENT));
        program.setDebugName("single_color");
        return program;
    });

    private final Lazy<RainbowColorProgram> rainbowColor = Lazy.of(() -> {
        final RainbowColorProgram program = new RainbowColorProgram(this.shaderLoader.get("post/post_processing", VERTEX), this.shaderLoader.get("post/rainbow_color", FRAGMENT));
        program.setDebugName("rainbow_color");
        return program;
    });

    private final Lazy<OutlineProgram> outline = Lazy.of(() -> {
        final OutlineProgram program = new OutlineProgram(this.shaderLoader.get("post/post_processing", VERTEX), this.shaderLoader.get("post/outline", FRAGMENT));
        program.setDebugName("outline");
        return program;
    });

    private final Lazy<MSAAProgram> msaa = Lazy.of(() -> {
        final MSAAProgram program = new MSAAProgram(this.shaderLoader.get("post/post_processing", VERTEX), this.shaderLoader.get("post/msaa", FRAGMENT), 4);
        program.setDebugName("msaa");
        return program;
    });

    private final Lazy<RegularProgram> instancedColor = Lazy.of(() -> {
        final RegularProgram program = new RegularProgram(this.shaderLoader.get("instancing/color", VERTEX), this.shaderLoader.get("regular/color", FRAGMENT));
        program.setDebugName("instanced_color");
        return program;
    });

    private final Lazy<RegularProgram> multidrawColor = Lazy.of(() -> {
        final RegularProgram program = new RegularProgram(this.shaderLoader.get("multidraw/color", VERTEX), this.shaderLoader.get("regular/color", FRAGMENT));
        program.setDebugName("multidraw_color");
        return program;
    });

    private final Lazy<RegularProgram> multidrawLine = Lazy.of(() -> {
        final RegularProgram program = new RegularProgram(this.shaderLoader.get("multidraw/line", VERTEX), this.shaderLoader.get("geometry/line", GEOMETRY), this.shaderLoader.get("geometry/line", FRAGMENT));
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

    @Deprecated(forRemoval = true)
    protected Shader getShader(final String name, final Shader.Type type) {
        return this.shaderLoader.get(name, type);
    }

    @Deprecated(forRemoval = true)
    protected Shader getShader(final String name, final Shader.Type type, final Map<String, Object> defines) {
        return this.shaderLoader.get(name, type, defines);
    }

    protected static class ShaderLoader {

        private final String basePath;
        private final Map<String, Shader> shaders = new HashMap<>();

        public ShaderLoader(final String basePath) {
            if (basePath.endsWith("/")) {
                this.basePath = basePath;
            } else {
                this.basePath = basePath + "/";
            }
        }

        public Shader get(final String name, final Shader.Type type) {
            return this.get(name, type, Map.of());
        }

        public Shader get(final String name, final Shader.Type type, final Map<String, Object> defines) {
            return this.shaders.computeIfAbsent(name + "." + type.getFileExtension(), path -> {
                try {
                    try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream(this.basePath + path)) {
                        if (stream == null) {
                            throw new IOException("Shader " + name + " not found");
                        }
                        final String source = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                        final GlSlPreprocessor preprocessor = new GlSlPreprocessor();
                        preprocessor.addDefines(defines);
                        preprocessor.setIncludeResolver(includePath -> {
                            final Path basePath = Paths.get(this.basePath + path);
                            final Path relativePath = Paths.get(includePath);
                            final Path resolvedPath = basePath.getParent().resolve(relativePath).normalize();
                            try (InputStream includeStream = this.getClass().getClassLoader().getResourceAsStream(resolvedPath.toString())) {
                                if (includeStream == null) {
                                    throw new IOException("Included shader file " + includePath + " not found");
                                }
                                return new String(includeStream.readAllBytes(), StandardCharsets.UTF_8);
                            } catch (IOException e) {
                                throw new UncheckedIOException("Failed to load included shader file: " + includePath, e);
                            }
                        });
                        final Shader shader = new Shader(type, preprocessor.process(source));
                        shader.setDebugName(name);
                        return shader;
                    }
                } catch (Throwable e) {
                    throw new RuntimeException("Failed to load shader " + name, e);
                }
            });
        }

    }

}
