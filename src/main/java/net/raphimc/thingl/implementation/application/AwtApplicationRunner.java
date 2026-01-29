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
package net.raphimc.thingl.implementation.application;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.implementation.window.AwtWindowInterface;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;
import org.lwjgl.opengl.awt.PlatformWin32GLCanvas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public abstract class AwtApplicationRunner extends ApplicationRunner {

    protected GLData glData;
    protected JFrame frame;
    protected GLCanvas canvas;

    public AwtApplicationRunner(final Configuration configuration) {
        super(configuration);
    }

    @Override
    protected void launchWindowSystem() {
        this.setGLAttributes();
        this.createWindow();
        this.windowInterface = new AwtWindowInterface(this.canvas);
    }

    protected void setGLAttributes() {
        this.glData = new GLData();
        this.glData.majorVersion = this.configuration.getOpenGLMajorVersion();
        this.glData.minorVersion = this.configuration.getOpenGLMinorVersion();
        this.glData.profile = GLData.Profile.CORE;
        this.glData.forwardCompatible = true;
        this.glData.swapInterval = this.configuration.shouldUseVSync() ? 1 : 0;
    }

    protected void createWindow() {
        this.canvas = new GLCanvas(this.glData);
        this.frame = new JFrame(this.configuration.getWindowTitle());
        this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                AwtApplicationRunner.this.thinGL.getRenderThread().interrupt();
            }

            @Override
            public void windowClosed(final WindowEvent e) {
                if (AwtApplicationRunner.this.thinGL != null) { // Window has been closed externally using dispose()
                    ThinGL.LOGGER.error("Window was closed incorrectly. Use 'frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING))' to close the window correctly.");
                    System.exit(-1);
                }
            }
        });
        this.frame.setPreferredSize(new Dimension(this.configuration.getWindowWidth(), this.configuration.getWindowHeight()));
        this.frame.add(this.canvas);
        this.frame.pack();
        this.frame.setVisible(true);
    }

    @Override
    protected void configureGLContext() {
        this.canvas.createAndBindContext();
    }

    @Override
    protected void pollWindowEvents() {
    }

    @Override
    protected void swapWindowBuffers() {
        this.canvas.swapBuffers();
    }

    @Override
    protected void freeGL() {
        super.freeGL();
        this.canvas.removeNotify();
        this.canvas = null;
    }

    @Override
    protected void freeWindowSystem() {
        if (this.windowInterface != null) {
            this.windowInterface.free();
            this.windowInterface = null;
        }
        this.freeWindow();
    }

    protected void freeWindow() {
        if (this.frame != null) {
            final boolean wasInterrupted = Thread.interrupted();
            try {
                this.frame.dispose();
            } finally {
                if (wasInterrupted) {
                    Thread.currentThread().interrupt();
                }
            }
            this.frame = null;
        }
    }

    public static class GLCanvas extends AWTGLCanvas {

        public GLCanvas(final GLData data) {
            super(data);
        }

        @Override
        public void initGL() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void paintGL() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeNotify() {
            if (this.context != 0L) {
                this.platformCanvas.deleteContext(this.context);
            }
            super.removeNotify();
        }

        @Override
        public void disposeCanvas() {
            if (this.platformCanvas instanceof PlatformWin32GLCanvas canvas && canvas.ds == null) {
                return; // Prevent a crash on Windows when disposing after the device context has already been destroyed
            }
            super.disposeCanvas();
        }

        public void createAndBindContext() {
            super.beforeRender();
            try {
                this.platformCanvas.unlock();
            } catch (AWTException e) {
                throw new RuntimeException("Failed to unlock Canvas", e);
            }
        }

    }

}
