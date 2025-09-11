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
package net.raphimc.thingl.awt;

import net.raphimc.thingl.resource.image.texture.Texture2D;
import net.raphimc.thingl.resource.image.texture.Texture2DArray;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL12C;

import java.awt.*;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class AwtUtil {

    public static void configureGraphics2DForMaximumQuality(final Graphics2D graphics) {
        // General
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

        // Shapes
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // Text
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    public static void uploadBufferedImageToTexture2D(final Texture2D texture, final int x, final int y, final BufferedImage image) {
        final int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        texture.uploadPixels(x, y, image.getWidth(), image.getHeight(), GL12C.GL_BGRA, pixels, false);
    }

    public static void uploadBufferedImageToTexture2DArray(final Texture2DArray texture, final int x, final int y, final int layer, final BufferedImage image) {
        final int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        texture.uploadPixels(x, y, layer, image.getWidth(), image.getHeight(), GL12C.GL_BGRA, pixels, false);
    }

    public static List<List<Vector2f>> convertShapeToPolyLines(final Shape shape) {
        return convertShapeToPolyLines(shape, 1F);
    }

    public static List<List<Vector2f>> convertShapeToPolyLines(final Shape shape, final float flatness) {
        final PathIterator pathIterator = shape.getPathIterator(null, flatness);
        final List<List<Vector2f>> polyLines = new ArrayList<>();
        final List<Vector2f> pathPoints = new ArrayList<>();
        final Vector2f pathStart = new Vector2f();
        final float[] coords = new float[6];
        while (!pathIterator.isDone()) {
            switch (pathIterator.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO -> {
                    if (!pathPoints.isEmpty()) {
                        polyLines.add(new ArrayList<>(pathPoints));
                        pathPoints.clear();
                    }
                    pathPoints.add(new Vector2f(coords[0], coords[1]));
                    pathStart.set(coords[0], coords[1]);
                }
                case PathIterator.SEG_LINETO -> pathPoints.add(new Vector2f(coords[0], coords[1]));
                case PathIterator.SEG_CLOSE -> pathPoints.add(new Vector2f(pathStart));
                default -> throw new IllegalStateException("Unexpected segment type");
            }
            pathIterator.next();
        }
        if (!pathPoints.isEmpty()) {
            polyLines.add(pathPoints);
        }
        return polyLines;
    }

}
