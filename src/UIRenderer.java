import components.Tool;
import components.ToolState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.LinkedList;

import static javax.swing.border.BevelBorder.LOWERED;

public class UIRenderer extends JFrame {
    private JPanel contentPane;
    private JButton buttonSave;
    private JButton buttonLoad;
    private JRadioButton selectToolRadioButton;
    private JRadioButton rectangleRadioButton;
    private JRadioButton polygonRadioButton;
    private JSlider rSlider;
    private JSlider gSlider;
    private JSlider bSlider;
    private JSlider aSlider;
    private JTextField hexColorCodeField;
    private JPanel colorDisplay;

    private ToolState state;
    private String oldHex;

    private CanvasRenderer canvas;

    public UIRenderer(CanvasRenderer canvas, ToolState state) {
        setContentPane(contentPane);
        setTitle("Tool pallet");

        this.canvas = canvas;
        this.state = state;

        ButtonGroup toolGroup = new ButtonGroup();
        toolGroup.add(selectToolRadioButton);
        toolGroup.add(rectangleRadioButton);
        toolGroup.add(polygonRadioButton);

        colorDisplay.setBorder(BorderFactory.createBevelBorder(LOWERED));
        colorDisplay.setBackground(Color.ORANGE);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
                canvas.terminate();
            }
        });

        selectToolRadioButton.addActionListener(e -> {
            state.current = Tool.SELECT;
        });
        rectangleRadioButton.addActionListener(e -> {
            state.current = Tool.RECT;
        });
        polygonRadioButton.addActionListener(e -> {
            state.current = Tool.POLYGON;
        });

        rSlider.addChangeListener(e -> {
            state.color.x = rSlider.getValue() / 255f;
            updateColor();
        });
        gSlider.addChangeListener(e -> {
            state.color.y = gSlider.getValue() / 255f;
            updateColor();
        });
        bSlider.addChangeListener(e -> {
            state.color.z = bSlider.getValue() / 255f;
            updateColor();
        });
        aSlider.addChangeListener(e -> {
//            state.color.x = rSlider.getValue() / 255f;
            updateColor();
        });
        hexColorCodeField.addActionListener(e -> {
            try {
                Color color = Color.decode('#' + hexColorCodeField.getText());
                state.color.x = color.getRed() / 255f;
                state.color.y = color.getGreen() / 255f;
                state.color.z = color.getBlue() / 255f;
            } catch (NumberFormatException ex) {
                hexColorCodeField.setText(oldHex);
            }
            updateSliders();
            updateColor();
        });

        buttonSave.addActionListener(e -> {save();});

        buttonLoad.addActionListener(e -> {load();});

        updateColor();
        updateSliders();

        pack();
    }

    void save() {
        FileDialog fileDialog = new FileDialog(this, "Select where to save drawing", FileDialog.SAVE);
        fileDialog.setFile("drawing.shape");
        fileDialog.setVisible(true);
        if (fileDialog.getFile() != null) {
            try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(fileDialog.getFile()))) {
                outputStream.writeObject(canvas.shapes);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "File " + fileDialog.getFile() + " can't be accessed\n" + ex.getLocalizedMessage(),
                        "Trouble with saving to file",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    void load() {
        FileDialog fileDialog = new FileDialog(this, "Select file to load", FileDialog.LOAD);
        fileDialog.setVisible(true);
        if (fileDialog.getFile() != null) {
            try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(fileDialog.getFile()))) {
                canvas.shapes = (LinkedList<shapes.Shape>) inputStream.readObject();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "File " + fileDialog.getFile() + " can't be read\n" + ex.getLocalizedMessage(),
                        "Trouble reading file",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } catch (ClassNotFoundException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "File " + fileDialog.getFile() + " is invalid or corrupted\n" + ex.getLocalizedMessage(),
                        "Trouble interpreting file",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    void updateTool() {
        switch (state.current) {
            case SELECT:
                selectToolRadioButton.setSelected(true);
                break;
            case RECT:
                rectangleRadioButton.setSelected(true);
                break;
            case POLYGON:
                polygonRadioButton.setSelected(true);
                break;
        }
    }

    void updateColor() {
        Color color = new Color(state.color.x, state.color.y, state.color.z);
        colorDisplay.setBackground(color);
        hexColorCodeField.setText(Integer.toHexString(color.getRGB()).substring(2).toUpperCase());
        oldHex = hexColorCodeField.getText();
    }

    void updateSliders() {
        rSlider.setValue((int) (state.color.x * 255));
        gSlider.setValue((int) (state.color.y * 255));
        bSlider.setValue((int) (state.color.z * 255));
//        aSlider.setValue((int) (state.color.x * 255));
    }
}
