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

import net.raphimc.thingl.implementation.util.sdl.SdlException;
import org.lwjgl.sdl.*;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

public class SDLWindowInterface extends WindowInterface {

    private final long windowHandle;
    private final int windowId;
    private final SDL_EventFilter eventWatch;

    public SDLWindowInterface() {
        this(SdlException.check(SDLVideo.SDL_GL_GetCurrentWindow(), "Failed to get OpenGL context window handle"));
    }

    public SDLWindowInterface(final long windowHandle) {
        this.windowHandle = windowHandle;
        this.windowId = SdlException.check(SDLVideo.SDL_GetWindowID(windowHandle), "Failed to get window id");

        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            final IntBuffer width = memoryStack.mallocInt(1);
            final IntBuffer height = memoryStack.mallocInt(1);
            SdlException.check(SDLVideo.SDL_GetWindowSizeInPixels(windowHandle, width, height), "Failed to get window size");
            this.callFramebufferResizeCallbacks(width.get(0), height.get(0));
        }

        this.eventWatch = SDL_EventFilter.create(this::onEvent);
        SdlException.check(SDLEvents.SDL_AddEventWatch(this.eventWatch, 0L), "Failed to add event watch");
    }

    public long getWindowHandle() {
        return this.windowHandle;
    }

    public int getWindowId() {
        return this.windowId;
    }

    @Override
    public void free() {
        this.assertOnWindowThread();
        SDLEvents.SDL_RemoveEventWatch(this.eventWatch, 0L);
        this.eventWatch.free();
    }

    private boolean onEvent(final long userData, final long eventPtr) {
        final SDL_Event event = SDL_Event.create(eventPtr);
        if (event.type() == SDLEvents.SDL_EVENT_WINDOW_PIXEL_SIZE_CHANGED) {
            final SDL_WindowEvent windowEvent = event.window();
            if (windowEvent.windowID() == this.windowId) {
                this.callFramebufferResizeCallbacks(windowEvent.data1(), windowEvent.data2());
            }
        }
        return true;
    }

}
