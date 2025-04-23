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
package net.raphimc.thingl.implementation;

import net.lenni0451.commons.threading.ThreadUtils;
import net.raphimc.thingl.ThinGL;

public class UnmanagedGLFWWindowInterface extends GLFWWindowInterface {

    private final Thread windowThread;

    public UnmanagedGLFWWindowInterface(final long windowHandle) {
        super(windowHandle);
        this.windowThread = Thread.currentThread();
    }

    @Override
    public void responsiveSleep(final float millis) {
        if (Thread.currentThread() == this.windowThread) {
            super.responsiveSleep(millis);
        } else {
            final long ms = (long) millis;
            final long ns = (long) ((millis - ms) * 1_000_000);
            ThreadUtils.hybridSleep(ms, ns);
        }
    }

    public Thread getWindowThread() {
        return this.windowThread;
    }

    @Override
    public void free() {
        if (Thread.currentThread() == this.windowThread) {
            super.free();
        }
    }

    @Override
    protected void callFramebufferResizeCallbacks(final int width, final int height) {
        if (ThinGL.isInitialized()) {
            ThinGL.get().runOnRenderThread(() -> super.callFramebufferResizeCallbacks(width, height));
        } else {
            super.callFramebufferResizeCallbacks(width, height);
        }
    }

}
