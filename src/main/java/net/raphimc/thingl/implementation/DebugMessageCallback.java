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

import net.raphimc.thingl.ThinGL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GLDebugMessageCallback;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DebugMessageCallback {

    private static final AtomicInteger MESSAGES_PER_SECOND = new AtomicInteger();
    private static final AtomicLong LAST_MESSAGE_TIME = new AtomicLong();

    public static void install(final boolean sync) {
        GL11C.glEnable(GL43C.GL_DEBUG_OUTPUT);
        if (sync) {
            GL11C.glEnable(GL43C.GL_DEBUG_OUTPUT_SYNCHRONOUS);
        }
        GL43C.glDebugMessageControl(GL11C.GL_DONT_CARE, GL11C.GL_DONT_CARE, GL11C.GL_DONT_CARE, (int[]) null, true);
        GL43C.glDebugMessageControl(GL11C.GL_DONT_CARE, GL11C.GL_DONT_CARE, GL43C.GL_DEBUG_SEVERITY_NOTIFICATION, (int[]) null, false);
        GL43C.glDebugMessageCallback(GLDebugMessageCallback.create(DebugMessageCallback::onMessage), 0L);
    }

    private static void onMessage(final int sourceId, final int typeId, final int id, final int severityId, final int messageLength, final long messagePtr, final long userParam) {
        final String message = GLDebugMessageCallback.getMessage(messageLength, messagePtr);
        final MessageSource source = MessageSource.fromGlSource(sourceId);
        final MessageType type = MessageType.fromGlType(typeId);
        final MessageSeverity severity = MessageSeverity.fromGlSeverity(severityId);

        final long currentTime = System.currentTimeMillis();
        if (currentTime - LAST_MESSAGE_TIME.get() > 1000) {
            MESSAGES_PER_SECOND.set(0);
        }
        MESSAGES_PER_SECOND.incrementAndGet();
        LAST_MESSAGE_TIME.set(currentTime);

        if (MESSAGES_PER_SECOND.get() <= 20) {
            ThinGL.LOGGER.error("OpenGL Debug Message (ID: {}): [{}] [{}] [{}] {}", id, source.getDisplayName().toUpperCase(), type.getDisplayName().toUpperCase(), severity.getDisplayName().toUpperCase(), message, new Exception());
        } else {
            ThinGL.LOGGER.error("OpenGL Debug Message (ID: {}): [{}] [{}] [{}] {}", id, source.getDisplayName().toUpperCase(), type.getDisplayName().toUpperCase(), severity.getDisplayName().toUpperCase(), message);
        }
    }

    private enum MessageSource {

        API(GL43C.GL_DEBUG_SOURCE_API, "API"),
        WINDOW_SYSTEM(GL43C.GL_DEBUG_SOURCE_WINDOW_SYSTEM, "Window System"),
        SHADER_COMPILER(GL43C.GL_DEBUG_SOURCE_SHADER_COMPILER, "Shader Compiler"),
        THIRD_PARTY(GL43C.GL_DEBUG_SOURCE_THIRD_PARTY, "Third Party"),
        APPLICATION(GL43C.GL_DEBUG_SOURCE_APPLICATION, "Application"),
        OTHER(GL43C.GL_DEBUG_SOURCE_OTHER, "Application"),
        ;

        public static MessageSource fromGlSource(final int glSource) {
            for (MessageSource source : values()) {
                if (source.glSource == glSource) {
                    return source;
                }
            }

            throw new IllegalArgumentException("Unknown message source: " + glSource);
        }

        private final int glSource;
        private final String displayName;

        MessageSource(final int glSource, final String displayName) {
            this.glSource = glSource;
            this.displayName = displayName;
        }

        public int getGlSource() {
            return this.glSource;
        }

        public String getDisplayName() {
            return this.displayName;
        }

    }

    private enum MessageType {

        ERROR(GL43C.GL_DEBUG_TYPE_ERROR, "Error"),
        DEPRECATED_BEHAVIOR(GL43C.GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR, "Deprecated Behavior"),
        UNDEFINED_BEHAVIOR(GL43C.GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR, "Undefined Behavior"),
        PORTABILITY(GL43C.GL_DEBUG_TYPE_PORTABILITY, "Portability"),
        PERFORMANCE(GL43C.GL_DEBUG_TYPE_PERFORMANCE, "Performance"),
        MARKER(GL43C.GL_DEBUG_TYPE_MARKER, "Marker"),
        PUSH_GROUP(GL43C.GL_DEBUG_TYPE_PUSH_GROUP, "Push Group"),
        POP_GROUP(GL43C.GL_DEBUG_TYPE_POP_GROUP, "Pop Group"),
        OTHER(GL43C.GL_DEBUG_TYPE_OTHER, "Other"),
        ;

        public static MessageType fromGlType(final int glType) {
            for (MessageType type : values()) {
                if (type.glType == glType) {
                    return type;
                }
            }

            throw new IllegalArgumentException("Unknown message type: " + glType);
        }

        private final int glType;
        private final String displayName;

        MessageType(final int glType, final String displayName) {
            this.glType = glType;
            this.displayName = displayName;
        }

        public int getGlType() {
            return this.glType;
        }

        public String getDisplayName() {
            return this.displayName;
        }

    }

    private enum MessageSeverity {

        HIGH(GL43C.GL_DEBUG_SEVERITY_HIGH, "High"),
        MEDIUM(GL43C.GL_DEBUG_SEVERITY_MEDIUM, "Medium"),
        LOW(GL43C.GL_DEBUG_SEVERITY_LOW, "Low"),
        NOTIFICATION(GL43C.GL_DEBUG_SEVERITY_NOTIFICATION, "Notification"),
        ;

        public static MessageSeverity fromGlSeverity(final int glSeverity) {
            for (MessageSeverity severity : values()) {
                if (severity.glSeverity == glSeverity) {
                    return severity;
                }
            }

            throw new IllegalArgumentException("Unknown message severity: " + glSeverity);
        }

        private final int glSeverity;
        private final String displayName;

        MessageSeverity(final int glSeverity, final String displayName) {
            this.glSeverity = glSeverity;
            this.displayName = displayName;
        }

        public int getGlSeverity() {
            return this.glSeverity;
        }

        public String getDisplayName() {
            return this.displayName;
        }

    }

}
