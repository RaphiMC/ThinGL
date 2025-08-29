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
package net.raphimc.thingl.texture.animated;

import net.lenni0451.commons.threading.ThreadUtils;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.resource.image.texture.Texture2D;
import net.raphimc.thingl.texture.animated.frameprovider.FrameProvider;
import net.raphimc.thingl.util.TimerHack;
import org.lwjgl.opengl.GL11C;

public class AnimatedTexture extends Texture2D {

    private final ThinGL thinGL;
    private final FrameProvider frameProvider;
    private Thread loadingThread;

    public AnimatedTexture(final FrameProvider frameProvider) {
        this(GL11C.GL_RGBA8, frameProvider);
    }

    public AnimatedTexture(final int internalFormat, final FrameProvider frameProvider) {
        super(internalFormat, frameProvider.getWidth(), frameProvider.getHeight());
        this.thinGL = ThinGL.get();
        this.frameProvider = frameProvider;
    }

    public AnimatedTexture start() {
        if (this.loadingThread != null) {
            throw new IllegalStateException("Animation was already started");
        }

        this.loadingThread = new Thread(() -> {
            TimerHack.ensureRunning();
            try {
                while (this.thinGL.isAllocated() && !Thread.currentThread().isInterrupted()) {
                    final long startTime = System.nanoTime();
                    final int delay = this.frameProvider.loadNextFrame(this);
                    if (delay < 0) {
                        return;
                    }

                    final float timeToSleep = delay - (System.nanoTime() - startTime) / 1_000_000F;
                    if (timeToSleep > 0) {
                        ThreadUtils.hybridSleep(timeToSleep);
                    }
                }
            } catch (Throwable e) {
                if (e.getCause() instanceof InterruptedException) {
                    return;
                }

                ThinGL.LOGGER.error("Failed to load animated texture frame", e);
            }
        }, "AnimatedTexture-LoadThread");
        this.loadingThread.setDaemon(true);
        this.loadingThread.start();
        return this;
    }

    public boolean isRunning() {
        return this.loadingThread != null && this.loadingThread.isAlive();
    }

    public boolean isFinished() {
        return this.loadingThread != null && !this.loadingThread.isAlive();
    }

    @Override
    protected void free0() {
        if (this.isRunning()) {
            this.loadingThread.interrupt();
            try {
                this.loadingThread.join(1000);
            } catch (InterruptedException ignored) {
            }
        }
        this.frameProvider.free();
        super.free0();
    }

}
