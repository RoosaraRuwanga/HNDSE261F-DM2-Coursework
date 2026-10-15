package view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.util.Locale;

public class TemplateView extends JFrame {
    private JPanel MainPanel;
    private JLabel LabelTitle;
    private JButton btnRENAME;

    public TemplateView() {
        setTitle("SmartMove Transport Solutions");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 720);
        setContentPane(MainPanel);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        new TemplateView();
    }

}
