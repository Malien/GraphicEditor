import components.Tool;
import components.ToolState;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import primitives.*;
import shapes.Rect;
import shapes.Shape;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class CanvasRenderer {

    private static final int W_INITIAL_WIDTH = 600;
    private static final int W_INITIAL_HEIGHT = 400;
    private static final String W_TITLE = "Graphics Editor";

    //Modifier keys status
    private boolean kSpace = false;
    private boolean kAlt = false;

    //Mouse stats
    private Vec2<Double> prevPos = new Vec2<>(0.0,0.0);
    private Vec2<Double> mouseSpeed = new Vec2<>(0.0, 0.0);
    private boolean tracked = false;
    private double scrollSpeed = 0;
    private static final double DECCEL_RATE = 0.8;
    private static final double SCROLL_RATE = 0.04;

    //Transformations
    private Vec2<Float> translation = new Vec2<>(0f, 0f);
    private float scale = 1;

    //General variables
    private LinkedList<Shape> shapes = new LinkedList<>();
    private Shape selected = null;

    private UIRenderer ui;
    private ToolState state;
    private long window;

    //Callbacks
    private final GLFWKeyCallbackI keyCallback = (window, key, scancode, action, mods) -> {
        if (key == GLFW_KEY_SPACE && action == GLFW_PRESS) {
            kSpace = true;
        }
        if (key == GLFW_KEY_SPACE && action == GLFW_RELEASE) {
            kSpace = false;
        }
        if ((key == GLFW_KEY_LEFT_ALT || key == GLFW_KEY_RIGHT_ALT) && action == GLFW_PRESS) {
            kAlt = true;
        }
        if ((key == GLFW_KEY_LEFT_ALT || key == GLFW_KEY_RIGHT_ALT) && action == GLFW_RELEASE) {
            kAlt = false;
        }
    };
    private final GLFWMouseButtonCallbackI mbCallback = (window, button, action, mods) -> {
        if (!kSpace && state.current == Tool.SELECT && button == GLFW_MOUSE_BUTTON_1 && action == GLFW_RELEASE) {
            Vec2<Double> dpos = cursorPos(window);
            Vec2<Float> pos = new Vec2<>(
                    (dpos.x.floatValue() - translation.x)/scale,
                    (dpos.y.floatValue() - translation.y)/scale);
            selected = null;
            for (Shape shape : shapes) {
                if (shape.collider().contains(pos)) {
                    selected = shape;
                }
            }
            if (selected != null) {
                state.color = selected.color().copy();
                ui.updateColor();
                ui.updateSliders();
            }
        }
    };
    private final GLFWWindowSizeCallbackI resizeCallback = (window, width, height) -> {
        glViewport(0,0,width,height);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        glOrtho(0.0f,width,height,0,1.0f,-1.0f);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
    };
    private final GLFWScrollCallbackI scrollCallback = (window, xoffset, yoffset) -> {
        if (kAlt) scrollSpeed = SCROLL_RATE * yoffset;
    };
    private final GLFWDropCallbackI dropCallback = (window, count, names) -> {};

    public static void main(String[] args) {
        ToolState state = new ToolState();
        CanvasRenderer canvas = new CanvasRenderer(state);
        canvas.ui = new UIRenderer(canvas, state);
        canvas.ui.setVisible(true);
        canvas.run();
    }

    public CanvasRenderer(ToolState state) {
        this.state = state;
    }

    private Vec2<Integer> windowSize(long window) {
        int[] width = new int[1];
        int[] height = new int[1];
        glfwGetWindowPos(window, width, height);
        return new Vec2<>(width[0], height[0]);
    }
    private Vec2<Integer> windowPos(long window) {
        int[] x = new int[1];
        int[] y = new int[1];
        glfwGetWindowPos(window, x, y);
        return new Vec2<>(x[0], y[0]);
    }
    private Vec2<Double> cursorPos(long window) {
        double[] x = new double[1];
        double[] y = new double[1];
        glfwGetCursorPos(window, x, y);
        return new Vec2<>(x[0], y[0]);
    }

    private int createShader(int type, String sourceFile) throws IOException, RuntimeException{
        String code = new String(Files.readAllBytes(Paths.get(sourceFile)));
        int shader = glCreateShader(type);
        glShaderSource(shader, code);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Can't compile shader: " +  glGetShaderInfoLog(shader));
        }
        return shader;
    }

    private void processMouse(long window) {
        boolean mousePressed = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS;
        if (kSpace) {
            if (mousePressed) {
                if (tracked) {
                    Vec2<Double> currPos = cursorPos(window);
                    mouseSpeed.x = currPos.x - prevPos.x;
                    mouseSpeed.y = currPos.y - prevPos.y;

                    translation.x += mouseSpeed.x.floatValue();
                    translation.y += mouseSpeed.y.floatValue();

                    prevPos = currPos;
                } else {
                    tracked = true;
                    prevPos = cursorPos(window);
                }
            } else {
                tracked = false;
            }
        } else if (state.current == Tool.RECT) {
            if (mousePressed) {
                if (selected == null) {
                    Vec2<Double> curPos = cursorPos(window);
                    selected = new Rect(
                            (curPos.x.floatValue() - translation.x) / scale,
                            (curPos.y.floatValue() - translation.y) / scale,
                            0,
                            0,
                            state.color.copy());
                    shapes.add(selected);
                } else {
                    Vec2<Double> curPos = cursorPos(window);
                    Rect selectedRect = (Rect) selected;
                    selectedRect.width = (curPos.x.floatValue() - translation.x) / scale - selectedRect.x;
                    selectedRect.height = (curPos.y.floatValue() - translation.y) / scale - selectedRect.y;
                    selectedRect.recalc();
                }
            } else {
                selected = null;
            }
        }

        if (!mousePressed) {
            mouseSpeed.x *= DECCEL_RATE;
            mouseSpeed.y *= DECCEL_RATE;
            translation.x += mouseSpeed.x.floatValue();
            translation.y += mouseSpeed.y.floatValue();
        }
        scale *= 1 + scrollSpeed;
        Vec2<Double> mousePos = cursorPos(window);

        double mx = translation.x - mousePos.x;
        double my = translation.y - mousePos.y;

        double dx = mx*(scrollSpeed);
        double dy = my*(scrollSpeed);

        translation.x += (float) dx;
        translation.y += (float) dy;

        scrollSpeed *= DECCEL_RATE;
    }

    private void run() {
        //GLFW initialization
        {
            GLFWErrorCallback.createPrint(System.err).set();

            if (!glfwInit()) {
                throw new RuntimeException("Can't initialize GLFW");
            }
        }
        //Window creation
        {
            glfwDefaultWindowHints();
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
            glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);

            window = glfwCreateWindow(W_INITIAL_WIDTH, W_INITIAL_HEIGHT, W_TITLE, NULL, NULL);
            if (window == NULL) {
                throw new RuntimeException("Can't create window");
            }

            //Callbacks
            glfwSetKeyCallback(window, keyCallback);
            glfwSetScrollCallback(window, scrollCallback);
            glfwSetDropCallback(window, dropCallback);
            glfwSetWindowSizeCallback(window, resizeCallback);
            glfwSetMouseButtonCallback(window, mbCallback);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            if (vidmode != null) {
                Vec2<Integer> winSize = windowSize(window);
                glfwSetWindowPos(
                        window,
                        (vidmode.width() - winSize.x) / 2,
                        (vidmode.height() - winSize.y) / 2
                );
            }

            glfwMakeContextCurrent(window);
            glfwSwapInterval(1);

            glfwShowWindow(window);
        }
        //GL initialization
        {
            GL.createCapabilities();

            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glOrtho(0, W_INITIAL_WIDTH, W_INITIAL_HEIGHT, 0, 1, -1);
            glMatrixMode(GL_MODELVIEW);

            glClearColor(0.8f, 0.8f, 0.8f, 1.0f);
        }
        //Program loop
        {
            while (!glfwWindowShouldClose(window)) {

                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

                for (Shape shape : shapes) {
                    glBegin(shape.mode());
                        Vec3<Float> color = shape.color();
                        glColor3f(color.x, color.y, color.z);
                        float[] ver = shape.vertices();
                        for (int i=0; i<ver.length; i+=2) {
                            glVertex2f(ver[i]*scale + translation.x, ver[i+1]*scale + translation.y);
                        }
                    glEnd();
                }

                //Draw border around selected target
                if (selected != null) {
                    selected.setColor(state.color.copy());
                    glBegin(GL_LINE_LOOP);
                        glColor3f(0.9f, 0.9f, 0.9f);
                        float[] ver = selected.vertices();
                        for (int i=0; i<ver.length; i+=2) {
                            glVertex2f(ver[i]*scale+translation.x, ver[i+1]*scale + translation.y);
                        }
                    glEnd();
                }

                glfwSwapBuffers(window);

                processMouse(window);
                glfwPollEvents();
            }
        }

        ui.dispose();
        terminate();
    }


    public void terminate() {
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
        System.exit(0);
    }
}
