import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import primitives.*;
import shapes.Rect;
import shapes.Shape;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class CanvasRenderer {

    private static final int W_INITIAL_WIDTH = 600;
    private static final int W_INITIAL_HEIGHT = 400;
    private static final String W_TITLE = "Graphics Editor";

    //Callbacks
    private static final GLFWKeyCallbackI keyCallback = (window, key, scancode, action, mods) -> {};
    private static final GLFWMouseButtonCallbackI mbCallback = (window, button, action, mods) -> {};
    private static final GLFWWindowSizeCallbackI resizeCallback = (window, width, height) -> {};
    private static final GLFWScrollCallbackI scrollCallback = (window, xoffset, yoffset) -> {};
    private static final GLFWDropCallbackI dropCallback = (window, count, names) -> {};

    //Mouse stats
    private Vec2<Double> mouseAccel = new Vec2<>();
    private static final double DEACCEL_RATE = 0.1;

    private LinkedList<Shape> shapes = new LinkedList<>();

    public static void main(String[] args) {
        new CanvasRenderer().run();
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

    private void run() {
        long window;

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
                        (vidmode.width() - winSize.getX()) / 2,
                        (vidmode.height() - winSize.getY()) / 2
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
        }
        //Shader magic
//        int prog = 0;
//        int posLocation = 0;
//        int matTransformLocation = 0;
//        int colorLocation = 0;
//        try {
//            File dir = new File(".");
//            File[] files = dir.listFiles();
//            int vertexShader = createShader(GL_VERTEX_SHADER, "src/shaders/CanvasVertex.glsl");
//            int fragShader = createShader(GL_FRAGMENT_SHADER, "src/shaders/CanvasFrag.glsl");
//            prog = glCreateProgram();
//            glAttachShader(prog, vertexShader);
//            glAttachShader(prog, fragShader);
//
//            posLocation = glGetAttribLocation(prog, "a_position");
//            colorLocation = glGetAttribLocation(prog, "a_color");
//            matTransformLocation = glGetUniformLocation(prog, "u_matrix");
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            System.exit(1);
//        }
        //Program loop
        {
            shapes.add(new Rect(100,50,300,200, new Vec3<>(0.2f, 0.7f, 0.8f)));
            glColor3f(0.5f, 0.5f, 0.5f);
            glClearColor(0.9f, 0.9f, 0.9f, 1f);

            int vertexBuffer = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
            glBufferData(GL_ARRAY_BUFFER, shapes.get(0).vertices(), GL_DYNAMIC_DRAW);

            while (!glfwWindowShouldClose(window)) {

                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

//                glUseProgram(prog);
//                glEnableVertexAttribArray(posLocation);
//                glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
//                glVertexAttribPointer(posLocation, 2, GL_FLOAT, false, 0, 0);
//                float[] mat = new float[] {1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f};
//                glUniformMatrix3fv(matTransformLocation, false, mat);
//                glDrawArrays(GL_POLYGON, 0, 8);
//                glEnableClientState(GL_VERTEX_ARRAY);

                for (Shape shape : shapes) {
                    glBegin(shape.mode());
                        Vec3<Float> color = shape.color();
                        glColor3f(color.getX(), color.getY(), color.getZ());
                        float[] ver = shape.vertices();
                        for (int i=0; i<ver.length; i+=2) {
                            glVertex2f(ver[i], ver[i+1]);
                        }
                    glEnd();
                }
//                glDisableClientState(GL_VERTEX_ARRAY);

                glfwSwapBuffers(window);

                glfwPollEvents();
            }
        }
        //Termination
        {
            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);

            glfwTerminate();
            glfwSetErrorCallback(null).free();
//            System.exit(0);
        }
    }
}
