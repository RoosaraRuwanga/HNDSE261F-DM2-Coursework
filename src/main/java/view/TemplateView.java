package view;

import javax.swing.*;

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

    public static void main(String[] args){
        new TemplateView();
    }
}
