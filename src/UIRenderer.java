import components.Tool;
import components.ToolState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
    private JTextField hexColorCodeField;
    private JPanel colorDisplay;
    private JRadioButton ellipseRadioButton;
    private JRadioButton penRadioButton;

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
        toolGroup.add(ellipseRadioButton);
        toolGroup.add(penRadioButton);

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
            canvas.selected = null;
            state.current = Tool.RECT;
        });
        polygonRadioButton.addActionListener(e -> {
            canvas.selected = null;
            state.current = Tool.POLYGON;
        });
        ellipseRadioButton.addActionListener(e -> {
            canvas.selected = null;
            state.current = Tool.ELLIPSE;
        });
        penRadioButton.addActionListener(e -> {
            canvas.selected = null;
            state.current = Tool.PEN;
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

        buttonSave.addActionListener(e -> {
            canvas.save();
        });

        buttonLoad.addActionListener(e -> {
            canvas.load();
        });

        updateColor();
        updateSliders();

        pack();
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
            case ELLIPSE:
                ellipseRadioButton.setSelected(true);
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
    }
}
