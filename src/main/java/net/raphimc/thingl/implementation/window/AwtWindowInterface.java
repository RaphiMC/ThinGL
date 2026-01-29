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
package net.raphimc.thingl.implementation.window;

import org.lwjgl.opengl.awt.AWTGLCanvas;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class AwtWindowInterface extends WindowInterface {

    private final AWTGLCanvas canvas;
    private final ComponentListener resizeListener = new ComponentAdapter() {
        @Override
        public void componentResized(final ComponentEvent e) {
            AwtWindowInterface.this.callFramebufferResizeCallbacks(AwtWindowInterface.this.canvas.getFramebufferWidth(), AwtWindowInterface.this.canvas.getFramebufferHeight());
        }
    };

    public AwtWindowInterface(final AWTGLCanvas canvas) {
        this.canvas = canvas;
        this.callFramebufferResizeCallbacks(canvas.getFramebufferWidth(), canvas.getFramebufferHeight());
        canvas.addComponentListener(this.resizeListener);
    }

    public AWTGLCanvas getCanvas() {
        return this.canvas;
    }

    @Override
    public void free() {
        this.canvas.removeComponentListener(this.resizeListener);
    }

}
