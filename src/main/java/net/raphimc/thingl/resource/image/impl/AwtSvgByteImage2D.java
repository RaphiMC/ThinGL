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
package net.raphimc.thingl.resource.image.impl;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.DocumentLimits;
import com.github.weisj.jsvg.parser.LoaderContext;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.github.weisj.jsvg.renderer.SVGRenderingHints;
import com.github.weisj.jsvg.view.ViewBox;
import net.raphimc.thingl.implementation.Capabilities;
import net.raphimc.thingl.util.AwtUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class AwtSvgByteImage2D extends AwtByteImage2D {

    static {
        Capabilities.assertJsvgAvailable();
    }

    private static final LoaderContext LOADER_CONTEXT = LoaderContext.builder()
            .documentLimits(new DocumentLimits(DocumentLimits.DEFAULT_MAX_USE_NESTING_DEPTH, DocumentLimits.DEFAULT_MAX_NESTING_DEPTH, Integer.MAX_VALUE))
            .build();

    public AwtSvgByteImage2D(final byte[] documentBytes, final int width, final int height) {
        this(new ByteArrayInputStream(documentBytes), width, height);
    }

    public AwtSvgByteImage2D(final byte[] documentBytes, final float scale) {
        this(new ByteArrayInputStream(documentBytes), scale);
    }

    public AwtSvgByteImage2D(final InputStream documentStream, final int width, final int height) {
        final SVGDocument document = new SVGLoader().load(documentStream, null, LOADER_CONTEXT);
        if (document == null) {
            throw new RuntimeException("Failed to load SVG document");
        }
        this(document, width, height);
    }

    public AwtSvgByteImage2D(final InputStream documentStream, final float scale) {
        final SVGDocument document = new SVGLoader().load(documentStream, null, LOADER_CONTEXT);
        if (document == null) {
            throw new RuntimeException("Failed to load SVG document");
        }
        this(document, Math.round(document.size().width * scale), Math.round(document.size().height * scale));
    }

    private AwtSvgByteImage2D(final SVGDocument document, final int width, final int height) {
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = image.createGraphics();
        AwtUtil.configureGraphics2DForMaximumQuality(graphics);
        graphics.setRenderingHint(SVGRenderingHints.KEY_IMAGE_ANTIALIASING, SVGRenderingHints.VALUE_IMAGE_ANTIALIASING_ON);
        graphics.setRenderingHint(SVGRenderingHints.KEY_CACHE_OFFSCREEN_IMAGE, SVGRenderingHints.VALUE_NO_CACHE);
        document.render(null, graphics, new ViewBox(image.getWidth(), image.getHeight()));
        graphics.dispose();
        super(image);
    }

}
