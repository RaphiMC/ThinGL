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
package net.raphimc.thingl.gl.texture.animated;

import net.lenni0451.commons.threading.ThreadUtils;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.resource.image.texture.impl.Texture2D;
import net.raphimc.thingl.image.animated.AnimatedImage;
import net.raphimc.thingl.util.TimerHack;
import org.lwjgl.opengl.GL11C;

import java.util.concurrent.CompletableFuture;

public class AnimatedTexture extends Texture2D {

    private final ThinGL thinGL;
    private final AnimatedImage animatedImage;
    private Thread loadingThread;

    public AnimatedTexture(final AnimatedImage animatedImage) {
        this(GL11C.GL_RGBA8, animatedImage);
    }

    public AnimatedTexture(final int internalFormat, final AnimatedImage animatedImage) {
        super(internalFormat, animatedImage.getWidth(), animatedImage.getHeight());
        this.thinGL = ThinGL.get();
        this.animatedImage = animatedImage;
    }

    public AnimatedTexture start() {
        if (this.loadingThread != null) {
            throw new IllegalStateException("Animation was already started");
        }

        this.loadingThread = new Thread(() -> {
            TimerHack.ensureRunning();
            try {
                while (this.thinGL.isAllocated() && !Thread.currentThread().isInterrupted() && this.animatedImage.hasMoreFrames()) {
                    final long startTime = System.nanoTime();
                    final int delay = this.animatedImage.loadNextFrame();
                    if (!this.thinGL.isAllocated() || Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    final CompletableFuture<Void> uploadFuture = new CompletableFuture<>();
                    this.thinGL.runOnRenderThread(() -> {
                        try {
                            this.uploadImage(0, 0, this.animatedImage);
                        } finally {
                            uploadFuture.complete(null);
                        }
                    });
                    uploadFuture.join();
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
            } catch (InterruptedException _) {
            }
        }
        this.animatedImage.free();
        super.free0();
    }

}
