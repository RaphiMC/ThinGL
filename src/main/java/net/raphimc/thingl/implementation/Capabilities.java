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
package net.raphimc.thingl.implementation;

import net.raphimc.thingl.ThinGL;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.Configuration;
import org.lwjgl.util.freetype.FreeType;

public class Capabilities {

    private static final boolean STB_AVAILABLE;
    private static final boolean FREE_TYPE_AVAILABLE;
    private static final boolean HARF_BUZZ_AVAILABLE;
    private static final boolean MESH_OPTIMIZER_AVAILABLE;
    private static final boolean EARCUT4J_AVAILABLE;
    private static final boolean GIF_READER_AVAILABLE;
    private static final boolean TWELVE_MONKEYS_WEBP_READER_AVAILABLE;
    private static final boolean JSVG_AVAILABLE;

    static {
        STB_AVAILABLE = isClassPresent("org.lwjgl.stb.LibSTB");
        FREE_TYPE_AVAILABLE = isClassPresent("org.lwjgl.util.freetype.FreeType");
        HARF_BUZZ_AVAILABLE = isClassPresent("org.lwjgl.util.harfbuzz.HarfBuzz");
        MESH_OPTIMIZER_AVAILABLE = isClassPresent("org.lwjgl.util.meshoptimizer.LibMeshOptimizer");
        EARCUT4J_AVAILABLE = isClassPresent("earcut4j.Earcut");
        GIF_READER_AVAILABLE = isClassPresent("com.ibasco.image.gif.GifImageReader");
        TWELVE_MONKEYS_WEBP_READER_AVAILABLE = isClassPresent("com.twelvemonkeys.imageio.plugins.webp.WebPImageReader");
        JSVG_AVAILABLE = isClassPresent("com.github.weisj.jsvg.SVGDocument");

        if (FREE_TYPE_AVAILABLE && HARF_BUZZ_AVAILABLE) {
            Configuration.HARFBUZZ_LIBRARY_NAME.set(FreeType.getLibrary());
        }
        if (System.getProperty("os.name").startsWith("Mac")) {
            System.setProperty("joml.nounsafe", "true");
        }
    }

    public static void assertStbAvailable() {
        if (!STB_AVAILABLE) {
            throw new IllegalStateException("STB is not available. Please add the LWJGL STB module to your project.");
        }
    }

    public static void assertFreeTypeAvailable() {
        if (!FREE_TYPE_AVAILABLE) {
            throw new IllegalStateException("FreeType is not available. Please add the LWJGL FreeType module to your project.");
        }
    }

    public static void assertHarfBuzzAvailable() {
        if (!HARF_BUZZ_AVAILABLE) {
            throw new IllegalStateException("HarfBuzz is not available. Please add the LWJGL HarfBuzz module to your project.");
        }
    }

    public static void assertMeshOptimizerAvailable() {
        if (!MESH_OPTIMIZER_AVAILABLE) {
            throw new IllegalStateException("MeshOptimizer is not available. Please add the LWJGL MeshOptimizer module to your project.");
        }
    }

    public static void assertEarcut4jAvailable() {
        if (!EARCUT4J_AVAILABLE) {
            throw new IllegalStateException("Earcut4j is not available. Please add https://github.com/earcut4j/earcut4j to your project.");
        }
    }

    public static void assertGifReaderAvailable() {
        if (!GIF_READER_AVAILABLE) {
            throw new IllegalStateException("GIF Reader is not available. Please add https://github.com/RaphiMC/gif-reader to your project.");
        }
    }

    public static void assertTwelveMonkeysWebpReaderAvailable() {
        if (!TWELVE_MONKEYS_WEBP_READER_AVAILABLE) {
            throw new IllegalStateException("TwelveMonkeys WebP Reader is not available. Please add https://github.com/haraldk/TwelveMonkeys to your project.");
        }
    }

    public static void assertJsvgAvailable() {
        if (!JSVG_AVAILABLE) {
            throw new IllegalStateException("JSVG is not available. Please add https://github.com/weisJ/jsvg to your project.");
        }
    }

    public static boolean isStbAvailable() {
        return STB_AVAILABLE;
    }

    public static boolean isFreeTypeAvailable() {
        return FREE_TYPE_AVAILABLE;
    }

    public static boolean isHarfBuzzAvailable() {
        return HARF_BUZZ_AVAILABLE;
    }

    public static boolean isMeshOptimizerAvailable() {
        return MESH_OPTIMIZER_AVAILABLE;
    }

    public static boolean isEarcut4jAvailable() {
        return EARCUT4J_AVAILABLE;
    }

    public static boolean isGifReaderAvailable() {
        return GIF_READER_AVAILABLE;
    }

    public static boolean isTwelveMonkeysWebpReaderAvailable() {
        return TWELVE_MONKEYS_WEBP_READER_AVAILABLE;
    }

    public static boolean isJsvgAvailable() {
        return JSVG_AVAILABLE;
    }

    private final int maxSamples;
    private final int maxColorAttachments;
    private final int maxArrayTextureLayers;

    public Capabilities() {
        this.maxSamples = ThinGL.glBackend().getInteger(GL30C.GL_MAX_SAMPLES);
        this.maxColorAttachments = ThinGL.glBackend().getInteger(GL30C.GL_MAX_COLOR_ATTACHMENTS);
        this.maxArrayTextureLayers = ThinGL.glBackend().getInteger(GL30C.GL_MAX_ARRAY_TEXTURE_LAYERS);
    }

    public int getMaxSamples() {
        return this.maxSamples;
    }

    public int getMaxColorAttachments() {
        return this.maxColorAttachments;
    }

    public int getMaxArrayTextureLayers() {
        return this.maxArrayTextureLayers;
    }

    private static boolean isClassPresent(final String className) {
        try {
            Class.forName(className, false, Capabilities.class.getClassLoader());
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    private static boolean tryRun(final Runnable runnable) {
        try {
            runnable.run();
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

}
