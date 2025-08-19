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

import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.implementation.application.GLFWApplicationRunner;
import net.raphimc.thingl.implementation.instance.ThreadLocalInstanceManager;
import net.raphimc.thingl.implementation.window.GLFWWindowInterface;
import net.raphimc.thingl.text.TextRun;
import net.raphimc.thingl.text.font.Font;
import org.joml.Matrix4fStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MultiWindowExample extends GLFWApplicationRunner {

    public static void main(String[] args) {
        new MultiWindowExample().launch();
    }

    public MultiWindowExample() {
        super(null);
    }

    @Override
    protected void launch() {
        ThinGL.setInstanceManager(new ThreadLocalInstanceManager());

        this.initGLFW();
        this.setWindowHints();

        final List<Window> windows = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            windows.add(new Window(i));
        }

        while (!windows.isEmpty()) {
            final Iterator<Window> it = windows.iterator();
            while (it.hasNext()) {
                final Window window = it.next();
                if (window.isRenderThreadAlive()) {
                    window.tickWindow();
                } else {
                    window.freeWindow();
                    it.remove();
                }
            }
            if (!windows.isEmpty()) {
                windows.get(0).getWindowInterface().responsiveSleep(1F);
            }
        }
    }

    @Override
    protected void render(final Matrix4fStack positionMatrix) {
        throw new UnsupportedOperationException();
    }


    private static class Window extends GLFWApplicationRunner {

        private final int num;
        private Font robotoRegular;

        public Window(final int num) {
            super(new Configuration().setWindowTitle("ThinGL Example - Multi Window Example | Window #" + num).setExtendedDebugMode(true));
            this.num = num;
            this.createWindow();
            this.windowInterface = new GLFWWindowInterface(this.window);
            new Thread(() -> {
                try {
                    this.launchGL();
                    this.launchFuture.complete(null);
                    try {
                        this.runRenderLoop();
                    } finally {
                        this.freeGL();
                    }
                } catch (Throwable e) {
                    this.launchFuture.completeExceptionally(e);
                    this.freeFuture.completeExceptionally(e);
                    throw e;
                }
            }, "Window #" + num + " Render Thread").start();
            this.launchFuture.join();
        }

        @Override
        protected void init() {
            super.init();
            try {
                final byte[] fontData = MultiWindowExample.class.getResourceAsStream("/fonts/Roboto-Regular.ttf").readAllBytes();
                this.robotoRegular = new Font(fontData, 32);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void render(final Matrix4fStack positionMatrix) {
            ThinGL.rendererText().textRun(positionMatrix, TextRun.fromString(this.robotoRegular, "Window #" + this.num, Color.WHITE), 5, 5);
        }

        public GLFWWindowInterface getWindowInterface() {
            return (GLFWWindowInterface) this.windowInterface;
        }

        public boolean isRenderThreadAlive() {
            return this.thinGL != null && this.thinGL.getRenderThread().isAlive();
        }

        @Override
        public void tickWindow() {
            super.tickWindow();
        }

        @Override
        public void freeWindow() {
            super.freeWindow();
        }

    }

}
