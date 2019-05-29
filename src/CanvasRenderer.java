import components.Tool;
import components.ToolState;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import primitives.Vec2;
import primitives.Vec3;
import shapes.Polygon;
import shapes.Shape;
import shapes.*;
import util.Transfrom;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.LinkedList;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class CanvasRenderer {

    private static final int W_INITIAL_WIDTH = 800;
    private static final int W_INITIAL_HEIGHT = 600;
    private static final String W_TITLE = "Graphics Editor";

    //Modifier keys status
    private boolean kSpace = false;
    private boolean kAlt = false;
    private boolean kCtrl = false;

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

    Shape selected = null;
    //General variables
    private LinkedList<Shape> shapes = new LinkedList<>();
    private static final float VERTEX_RADIUS = 5;

    private UIRenderer ui;
    private ToolState state;
    private long window;
    private boolean grabbed = false;
    private ShapeHandle shapeHandle = null;
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
        if ((key == GLFW_KEY_LEFT_CONTROL || key == GLFW_KEY_RIGHT_CONTROL) && action == GLFW_PRESS) {
            kCtrl = true;
        }
        if ((key == GLFW_KEY_LEFT_CONTROL || key == GLFW_KEY_RIGHT_CONTROL) && action == GLFW_RELEASE) {
            kCtrl = false;
        }
        if (key == GLFW_KEY_V && action == GLFW_PRESS) {
            state.current = Tool.SELECT;
            ui.updateTool();
        }
        if (key == GLFW_KEY_R && action == GLFW_PRESS) {
            selected = null;
            state.current = Tool.RECT;
            ui.updateTool();
        }
        if (key == GLFW_KEY_T && action == GLFW_PRESS) {
            selected = null;
            state.current = Tool.POLYGON;
            ui.updateTool();
        }
        if (key == GLFW_KEY_L && action == GLFW_PRESS) {
            selected = null;
            state.current = Tool.ELLIPSE;
            ui.updateTool();
        }
        if (key == GLFW_KEY_P && action == GLFW_PRESS) {
            selected = null;
            state.current = Tool.PEN;
            ui.updateTool();
        }
        if ((key == GLFW_KEY_DELETE || key == GLFW_KEY_BACKSPACE) && action == GLFW_RELEASE && selected != null) {
            shapes.remove(selected);
            selected = null;
        }
        if (key == GLFW_KEY_S && action == GLFW_PRESS && kCtrl) {
            save();
        }
        if (key == GLFW_KEY_O && action == GLFW_PRESS && kCtrl) {
            load();
        }
        if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
            selected = null;
        }
    };
    private final GLFWWindowSizeCallbackI resizeCallback = (window, width, height) -> {
        glViewport(0, 0, width, height);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        glOrtho(0.0f, width, height, 0, 1.0f, -1.0f);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
    };
    private final GLFWMouseButtonCallbackI mbCallback = (window, button, action, mods) -> {
        if (!kSpace && state.current == Tool.SELECT && button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS) {
            Vec2<Float> worldPos = Transfrom.viewportToWorldSpace(cursorPos(), translation, scale);
            if (selected != null) {
                shapeHandle = selected.getHandle(worldPos, VERTEX_RADIUS / scale);
                if (shapeHandle != null) return;
            }
            selected = null;
            for (Shape shape : shapes) {
                if (shape.collider(scale).contains(worldPos)) {
                    selected = shape;
                }
            }
            if (selected != null) {
                state.color = selected.color().copy();
                ui.updateColor();
                ui.updateSliders();
            }
        }
        if (selected instanceof Polygon && button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS) {
            selected = null;
        }
    };
    private final GLFWScrollCallbackI scrollCallback = (window, xoffset, yoffset) -> {
        if (kAlt) {
            scrollSpeed = SCROLL_RATE * yoffset;
            shapes.forEach(shape -> shape.updateForScale(
                    (float) (scale + scrollSpeed * scrollSpeed / DECCEL_RATE - scrollSpeed * scrollSpeed / 2 / DECCEL_RATE)));
        }
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

    private Vec2<Integer> windowSize() {
        int[] width = new int[1];
        int[] height = new int[1];
        glfwGetWindowSize(window, width, height);
        return new Vec2<>(width[0], height[0]);
    }

    private Vec2<Double> cursorPos() {
        double[] x = new double[1];
        double[] y = new double[1];
        glfwGetCursorPos(window, x, y);
        return new Vec2<>(x[0], y[0]);
    }

    private void processMouse() {
        boolean mousePressed = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS;
        if (kSpace) {
            if (mousePressed) {
                if (tracked) {
                    Vec2<Double> currPos = cursorPos();
                    mouseSpeed.x = currPos.x - prevPos.x;
                    mouseSpeed.y = currPos.y - prevPos.y;

                    translation.x += mouseSpeed.x.floatValue();
                    translation.y += mouseSpeed.y.floatValue();

                    prevPos = currPos;
                } else {
                    tracked = true;
                    prevPos = cursorPos();
                }
            } else {
                tracked = false;
            }
        } else switch (state.current) {
            case RECT:
                if (mousePressed) {
                    if (selected == null) {
                        Vec2<Double> curPos = cursorPos();
                        selected = new Rect(
                                (curPos.x.floatValue() - translation.x) / scale,
                                (curPos.y.floatValue() - translation.y) / scale,
                                0,
                                0,
                                state.color.copy());
                        shapes.add(selected);
                    } else {
                        Vec2<Double> curPos = cursorPos();
                        Rect selectedRect = (Rect) selected;
                        selectedRect.width = (curPos.x.floatValue() - translation.x) / scale - selectedRect.x;
                        selectedRect.height = (curPos.y.floatValue() - translation.y) / scale - selectedRect.y;
                        selectedRect.recalc();
                    }
                } else {
                    selected = null;
                }
                break;
            case SELECT:
                if (selected != null && mousePressed) {
                    Vec2<Double> mousePos = cursorPos();
                    Vec2<Float> worldPos = Transfrom.viewportToWorldSpace(mousePos, translation, scale);
                    if (shapeHandle != null) {
                        if (tracked) {
                            Vec2<Float> vec = new Vec2<>(
                                    (float) (mousePos.x - prevPos.x) / scale,
                                    (float) (mousePos.y - prevPos.y) / scale);
                            shapeHandle.translate(vec);
                            prevPos = mousePos;
                        } else {
                            tracked = true;
                            prevPos = mousePos;
                        }
                    } else if (grabbed) {
                        if (tracked) {
                            selected.translate(new Vec2<>(
                                    (float) (mousePos.x - prevPos.x) / scale,
                                    (float) (mousePos.y - prevPos.y) / scale));
                            prevPos = mousePos;
                        } else {
                            tracked = true;
                            prevPos = mousePos;
                        }
                    } else {
                        grabbed = selected.collider(scale).contains(worldPos);
                    }
                }
                break;
            case POLYGON:
                if (mousePressed) {
                    if (selected == null) {
                        Polygon poly = new Polygon();
                        prevPos = cursorPos();
                        Vec2<Float> worldPos = Transfrom.viewportToWorldSpace(prevPos, translation, scale);
                        poly.add(worldPos.x, worldPos.y);
                        selected = poly;
                        shapes.add(selected);
                    } else {
                        Polygon poly = (Polygon) selected;
                        Vec2<Double> mousePos = cursorPos();
                        Vec2<Float> worldPos = Transfrom.viewportToWorldSpace(mousePos, translation, scale);
                        if (Transfrom.length(mousePos, prevPos) > VERTEX_RADIUS) poly.add(worldPos.x, worldPos.y);
                        prevPos = mousePos;
                    }
                }
                break;
            case ELLIPSE:
                if (mousePressed) {
                    if (selected == null) {
                        Vec2<Double> curPos = cursorPos();
                        selected = new Ellipse(
                                (curPos.x.floatValue() - translation.x) / scale,
                                (curPos.y.floatValue() - translation.y) / scale,
                                0,
                                0,
                                scale,
                                state.color.copy());
                        shapes.add(selected);
                    } else {
                        Vec2<Double> curPos = cursorPos();
                        Ellipse selectedEllipse = (Ellipse) selected;
                        selectedEllipse.width = (curPos.x.floatValue() - translation.x) / scale - selectedEllipse.x;
                        selectedEllipse.height = (curPos.y.floatValue() - translation.y) / scale - selectedEllipse.y;
                        selectedEllipse.updateForScale(scale);
                    }
                } else {
                    selected = null;
                }
                break;
            case PEN:
                if (mousePressed) {
                    if (selected == null) {
                        Pen pen = new Pen();
                        prevPos = cursorPos();
                        Vec2<Float> worldPos = Transfrom.viewportToWorldSpace(prevPos, translation, scale);
                        pen.add(worldPos.x, worldPos.y);
                        selected = pen;
                        shapes.add(selected);
                    } else {
                        Pen pen = (Pen) selected;
                        Vec2<Double> mousePos = cursorPos();
                        Vec2<Float> worldPos = Transfrom.viewportToWorldSpace(mousePos, translation, scale);
                        if (Transfrom.length(mousePos, prevPos) > VERTEX_RADIUS) pen.add(worldPos.x, worldPos.y);
                        prevPos = mousePos;
                    }
                }
                break;
        }

        if (!mousePressed) {
            mouseSpeed.x *= DECCEL_RATE;
            mouseSpeed.y *= DECCEL_RATE;
            translation.x += mouseSpeed.x.floatValue();
            translation.y += mouseSpeed.y.floatValue();
            tracked = false;
            grabbed = false;
        }
        scale *= 1 + scrollSpeed;
        Vec2<Double> mousePos = cursorPos();

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
                Vec2<Integer> winSize = windowSize();
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
                    glColor3f(0.9f, 0.9f, 0.9f);
                    float[] ver = selected.handles();
                    if (selected.mode() != GL_LINE_STRIP) {
                        glBegin(GL_LINE_LOOP);
                        for (int i = 0; i < ver.length; i += 2) {
                            glVertex2f(ver[i] * scale + translation.x, ver[i + 1] * scale + translation.y);
                        }
                        glEnd();
                    }
                    glBegin(GL_QUADS);
                        for (int i=0; i<ver.length; i+=2) {
                            float x = ver[i]*scale+translation.x;
                            float y = ver[i+1]*scale + translation.y;
                            glVertex2f(x-VERTEX_RADIUS, y-VERTEX_RADIUS);
                            glVertex2f(x+VERTEX_RADIUS, y-VERTEX_RADIUS);
                            glVertex2f(x+VERTEX_RADIUS, y+VERTEX_RADIUS);
                            glVertex2f(x-VERTEX_RADIUS, y+VERTEX_RADIUS);
                        }
                    glEnd();
                }

                glfwSwapBuffers(window);

                processMouse();
                glfwPollEvents();
            }
        }

        ui.dispose();
        terminate();
    }

    void terminate() {
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
        System.exit(0);
    }

    void save() {
        FileDialog fileDialog = new FileDialog(ui, "Select where to save drawing", FileDialog.SAVE);
        fileDialog.setFile("drawing.shape");
        fileDialog.setVisible(true);
        if (fileDialog.getFile() != null) {
            try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(fileDialog.getFile()))) {
                outputStream.writeObject(shapes);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                        null,
                        "File " + fileDialog.getFile() + " can't be accessed\n" + ex.getLocalizedMessage(),
                        "Trouble with saving to file",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    void load() {
        FileDialog fileDialog = new FileDialog(ui, "Select file to load", FileDialog.LOAD);
        fileDialog.setVisible(true);
        if (fileDialog.getFile() != null) {
            try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(fileDialog.getFile()))) {
                shapes = (LinkedList<shapes.Shape>) inputStream.readObject();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                        null,
                        "File " + fileDialog.getFile() + " can't be read\n" + ex.getLocalizedMessage(),
                        "Trouble reading file",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } catch (ClassNotFoundException ex) {
                JOptionPane.showMessageDialog(
                        null,
                        "File " + fileDialog.getFile() + " is invalid or corrupted\n" + ex.getLocalizedMessage(),
                        "Trouble interpreting file",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}
