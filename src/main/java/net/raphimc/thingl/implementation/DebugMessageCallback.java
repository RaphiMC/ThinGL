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
package net.raphimc.thingl.implementation;

import net.raphimc.thingl.ThinGL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Callback;

import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DebugMessageCallback {

    public static Callback install(final boolean appendStackTrace) {
        final Callback callback = GLUtil.setupDebugMessageCallback(new PrintStream(APIUtil.DEBUG_STREAM) {
            private final AtomicInteger messagesPerSecond = new AtomicInteger();
            private final AtomicLong lastMessageTime = new AtomicLong();

            @Override
            public void print(Object obj) {
                super.print(obj);

                if (appendStackTrace) {
                    final long currentTime = System.nanoTime();
                    if (currentTime - this.lastMessageTime.get() > 1_000_000_000L) {
                        this.messagesPerSecond.set(0);
                    }
                    this.messagesPerSecond.incrementAndGet();
                    this.lastMessageTime.set(currentTime);
                    if (this.messagesPerSecond.get() <= 20) {
                        new Exception().printStackTrace(this);
                    }
                }
            }
        });
        if (callback == null) {
            ThinGL.LOGGER.warn("Failed to set up OpenGL debug message callback");
            return null;
        }

        if (appendStackTrace) {
            ThinGL.glStateManager().enable(GL43C.GL_DEBUG_OUTPUT_SYNCHRONOUS);
        }
        GL43C.glDebugMessageControl(GL11C.GL_DONT_CARE, GL11C.GL_DONT_CARE, GL11C.GL_DONT_CARE, (int[]) null, true);
        GL43C.glDebugMessageControl(GL11C.GL_DONT_CARE, GL11C.GL_DONT_CARE, GL43C.GL_DEBUG_SEVERITY_NOTIFICATION, (int[]) null, false);

        // NVIDIA: Texture state usage warning: The texture object (0) bound to texture image unit 1 does not have a defined base level and cannot be used for texture mapping.
        GL43C.glDebugMessageControl(GL43C.GL_DEBUG_SOURCE_API, GL43C.GL_DEBUG_TYPE_OTHER, GL11C.GL_DONT_CARE, 0x20084, false);
        // NVIDIA: Framebuffer detailed info: The driver allocated storage for renderbuffer 1.
        GL43C.glDebugMessageControl(GL43C.GL_DEBUG_SOURCE_API, GL43C.GL_DEBUG_TYPE_OTHER, GL11C.GL_DONT_CARE, 0x20061, false);

        return callback;
    }

}
