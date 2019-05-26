import components.Tool;
import components.ToolState;
import primitives.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static javax.swing.border.BevelBorder.LOWERED;

public class UIRenderer extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
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

    public UIRenderer(CanvasRenderer canvas, ToolState state) {
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

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

        updateColor();
        updateSliders();

        pack();
    }

    void updateTool(Tool tool) {
        switch (tool) {
            case SELECT:
                selectToolRadioButton.setSelected(true);
                break;
            case RECT:
                rectangleRadioButton.setSelected(true);
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
