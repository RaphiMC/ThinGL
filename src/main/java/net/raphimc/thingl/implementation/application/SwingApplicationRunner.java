package net.raphimc.thingl.implementation.application;

import net.raphimc.thingl.implementation.window.SwingWindowInterface;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public abstract class SwingApplicationRunner extends ApplicationRunner {

    protected JFrame frame;
    protected GLCanvas canvas;

    public SwingApplicationRunner(final Configuration configuration) {
        super(configuration);
    }

    @Override
    protected void launchWindowSystem() {
        this.initSwing();
        this.createWindow();
        this.windowInterface = new SwingWindowInterface(this.canvas, this.frame);
    }

    protected void initSwing() {
        this.frame = new JFrame(this.configuration.getWindowTitle());
        this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.frame.setLayout(new BorderLayout());
        this.frame.setMinimumSize(new Dimension(1, 1));
    }

    protected void createWindow() {
        GLData data = new GLData();
        setCanvasData(data);

        this.canvas = new GLCanvas(data);
        this.frame.add(this.canvas, BorderLayout.CENTER);

        this.frame.getContentPane().setPreferredSize(new Dimension(
                this.configuration.getWindowWidth(),
                this.configuration.getWindowHeight()
        ));

        this.frame.pack();
        this.frame.setLocationRelativeTo(null);

        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                if (thinGL != null) thinGL.getRenderThread().interrupt();
            }
        });

        this.frame.setVisible(true);
    }

    protected void setCanvasData(final GLData data) {
        data.majorVersion = this.configuration.getOpenGLMajorVersion();
        data.minorVersion = this.configuration.getOpenGLMinorVersion();
        if (this.configuration.getOpenGLMajorVersion() >= 3) {
            data.profile = GLData.Profile.CORE;
            data.forwardCompatible = true;
        }
        data.swapInterval = this.configuration.shouldUseVSync() ? 1 : 0;
    }

    @Override
    protected void configureGLContext() {
        this.canvas.beforeRender();
    }

    @Override
    protected void pollWindowEvents() { // Issues with multiwindow on macOS
        this.canvas.afterRender();
        this.canvas.beforeRender();
    }

    @Override
    protected void swapWindowBuffers() {
        this.canvas.swapBuffers();
    }

    @Override
    protected void freeWindowSystem() {
        this.freeWindow();
        this.freeSwing();
    }

    protected void freeWindow() {
        if (this.canvas != null && this.canvas.isDisplayable()) {
            this.canvas.afterRender();
            this.canvas.disposeCanvas();
        }

        if (this.windowInterface != null) {
            this.windowInterface.free();
            this.windowInterface = null;
        }
    }

    protected void freeSwing() {
        if (this.frame != null) {
            boolean wasInterrupted = Thread.interrupted();

            this.frame.setVisible(false);
            this.frame.dispose();
            this.frame = null;

            if (wasInterrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static class GLCanvas extends AWTGLCanvas {
        public GLCanvas(GLData data) {
            super(data);
        }

        @Override
        public void initGL() {}

        @Override
        public void paintGL() {}

        @Override
        public void beforeRender() {
            super.beforeRender();
        }

        @Override
        public void afterRender() {
            super.afterRender();
        }
    }
}
