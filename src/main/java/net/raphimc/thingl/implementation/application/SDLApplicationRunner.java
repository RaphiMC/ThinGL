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

import net.raphimc.thingl.implementation.window.SDLWindowInterface;
import net.raphimc.thingl.util.SDLErrorUtil;
import org.lwjgl.sdl.*;
import org.lwjgl.system.MemoryStack;

public abstract class SDLApplicationRunner extends ApplicationRunner {

    protected long window;
    protected int windowId;
    protected long openGLContext;

    public SDLApplicationRunner(final Configuration configuration) {
        super(configuration);
    }

    @Override
    protected void launchWindowSystem() {
        this.initSDL();
        this.setGLAttributes();
        this.createWindow();
        this.windowInterface = new SDLWindowInterface(this.window);
    }

    protected void initSDL() {
        SDLErrorUtil.checkError(SDLInit.SDL_Init(SDLInit.SDL_INIT_VIDEO | SDLInit.SDL_INIT_EVENTS));
    }

    protected void createWindow() {
        final int properties = SDLProperties.SDL_CreateProperties();
        SDLErrorUtil.checkError(properties, "Failed to create SDL properties");
        try {
            this.setWindowProperties(properties);
            this.window = SDLVideo.SDL_CreateWindowWithProperties(properties);
            SDLErrorUtil.checkError(this.window, "Failed to create window");
            this.windowId = SDLVideo.SDL_GetWindowID(this.window);
            SDLErrorUtil.checkError(this.windowId, "Failed to get window id");
        } finally {
            SDLProperties.SDL_DestroyProperties(properties);
        }
    }

    protected void setGLAttributes() {
        SDLErrorUtil.checkError(SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_CONTEXT_MAJOR_VERSION, this.configuration.getOpenGLMajorVersion()));
        SDLErrorUtil.checkError(SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_CONTEXT_MINOR_VERSION, this.configuration.getOpenGLMinorVersion()));
        SDLErrorUtil.checkError(SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_CONTEXT_PROFILE_MASK, SDLVideo.SDL_GL_CONTEXT_PROFILE_CORE));
        SDLErrorUtil.checkError(SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_CONTEXT_FLAGS, SDLVideo.SDL_GL_CONTEXT_FORWARD_COMPATIBLE_FLAG));
    }

    protected void setWindowProperties(final int properties) {
        SDLErrorUtil.checkError(SDLProperties.SDL_SetBooleanProperty(properties, SDLVideo.SDL_PROP_WINDOW_CREATE_OPENGL_BOOLEAN, true));
        SDLErrorUtil.checkError(SDLProperties.SDL_SetBooleanProperty(properties, SDLVideo.SDL_PROP_WINDOW_CREATE_RESIZABLE_BOOLEAN, true));
        SDLErrorUtil.checkError(SDLProperties.SDL_SetStringProperty(properties, SDLVideo.SDL_PROP_WINDOW_CREATE_TITLE_STRING, this.configuration.getWindowTitle()));
        SDLErrorUtil.checkError(SDLProperties.SDL_SetNumberProperty(properties, SDLVideo.SDL_PROP_WINDOW_CREATE_WIDTH_NUMBER, this.configuration.getWindowWidth()));
        SDLErrorUtil.checkError(SDLProperties.SDL_SetNumberProperty(properties, SDLVideo.SDL_PROP_WINDOW_CREATE_HEIGHT_NUMBER, this.configuration.getWindowHeight()));
        if (this.configuration.isWindowCentered()) {
            SDLErrorUtil.checkError(SDLProperties.SDL_SetNumberProperty(properties, SDLVideo.SDL_PROP_WINDOW_CREATE_X_NUMBER, SDLVideo.SDL_WINDOWPOS_CENTERED));
            SDLErrorUtil.checkError(SDLProperties.SDL_SetNumberProperty(properties, SDLVideo.SDL_PROP_WINDOW_CREATE_Y_NUMBER, SDLVideo.SDL_WINDOWPOS_CENTERED));
        }
    }

    @Override
    protected void configureGLContext() {
        this.openGLContext = SDLVideo.SDL_GL_CreateContext(this.window);
        SDLErrorUtil.checkError(this.openGLContext, "Failed to create OpenGL context");
        SDLErrorUtil.checkError(SDLVideo.SDL_GL_SetSwapInterval(this.configuration.shouldUseVSync() ? 1 : 0), "Failed to set swap interval");
    }

    @Override
    protected void pollWindowEvents() {
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            final SDL_Event event = SDL_Event.calloc(memoryStack);
            while (SDLEvents.SDL_PollEvent(event)) {
                this.handleWindowEvent(event);
            }
        }
    }

    protected void handleWindowEvent(final SDL_Event event) {
        if (event.type() == SDLEvents.SDL_EVENT_WINDOW_CLOSE_REQUESTED && event.window().windowID() == this.windowId) {
            this.thinGL.getRenderThread().interrupt();
        }
    }

    @Override
    protected void swapWindowBuffers() {
        SDLErrorUtil.checkError(SDLVideo.SDL_GL_SwapWindow(this.window), "Failed to swap buffers");
    }

    @Override
    protected void freeWindowSystem() {
        this.freeWindow();
        this.freeSDL();
    }

    protected void freeWindow() {
        if (this.windowInterface != null) {
            this.windowInterface.free();
            this.windowInterface = null;
        }
        if (this.openGLContext != 0L) {
            SDLVideo.SDL_GL_DestroyContext(this.openGLContext);
            this.openGLContext = 0L;
        }
        if (this.window != 0L) {
            SDLVideo.SDL_DestroyWindow(this.window);
            this.window = 0L;
        }
    }

    protected void freeSDL() {
        SDLInit.SDL_Quit();
    }

}
