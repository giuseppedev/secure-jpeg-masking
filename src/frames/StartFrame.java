package frames;

import test.Test;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class StartFrame extends JFrame {

    private final int FRAME_WIDTH = 240;
    private final int FRAME_HEIGHT = 320;

    public StartFrame(){
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setResizable(false);
        createMainPanel();
    }

    public void createMainPanel(){
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(7, 1, 0, 14));

        JPanel title = createTitlePanel();
        JPanel project = createProjectPanel();
        JPanel compression = createCompressionPanel();
        JPanel decompression = createDecompressionPanel();
        JPanel test = createTestPanel();
        JPanel exit = createExitPanel();
        JPanel authors = createAuthorsPanel();

        panel.add(title);
        panel.add(project);
        panel.add(compression);
        panel.add(decompression);
        panel.add(test);
        panel.add(exit);
        panel.add(authors);

        add(panel);
    }

    public JPanel createTitlePanel(){
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Data Compression Project");
        label.setBorder(BorderFactory.createEmptyBorder(9, 5, 0, 0));
        label.setFont(new Font("SansSerif", Font.PLAIN , 14));
        panel.add(label);
        return panel;
    }

    public JPanel createProjectPanel(){
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Secure JPEG Compression");
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        label.setFont(new Font("SansSerif", Font.BOLD , 16));
        panel.add(label);
        return panel;
    }

    public JPanel createCompressionPanel(){
        JPanel panel = new JPanel();
        JButton btn = new JButton("Compression");
        //btn.setPreferredSize(new Dimension(150, 31));
        panel.add(btn);

        class clickButton implements ActionListener{
            public void actionPerformed(ActionEvent e) {
                CompressionFrame frame = new CompressionFrame();
                frame.setLocationRelativeTo(null);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
                StartFrame.this.dispose();
            }
        }

        ActionListener listener = new clickButton();
        btn.addActionListener(listener);

        return panel;
    }

    public JPanel createDecompressionPanel(){
        JPanel panel = new JPanel();
        JButton btn = new JButton("Decompression");
        //btn.setPreferredSize(new Dimension(150, 31));
        panel.add(btn);

        class clickButton implements ActionListener{
            public void actionPerformed(ActionEvent e) {
                DecompressionFrame frame = new DecompressionFrame();
                frame.setLocationRelativeTo(null);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
                StartFrame.this.dispose();
            }
        }

        ActionListener listener = new clickButton();
        btn.addActionListener(listener);

        return panel;
    }

    public JPanel createTestPanel(){
        JPanel panel = new JPanel();
        JButton btn = new JButton("Test");
        //btn.setPreferredSize(new Dimension(150, 31));
        panel.add(btn);

        class clickButton implements ActionListener{
            public void actionPerformed(ActionEvent e) {
                Test test = new Test();
            }
        }

        ActionListener listener = new clickButton();
        btn.addActionListener(listener);

        return panel;
    }

    public JPanel createExitPanel(){
        JPanel panel = new JPanel();
        JButton btn = new JButton("Exit");
        //btn.setPreferredSize(new Dimension(150, 31));
        panel.add(btn);

        class clickButton implements ActionListener{
            public void actionPerformed(ActionEvent e) {
                StartFrame.this.dispose();
            }
        }

        ActionListener listener = new clickButton();
        btn.addActionListener(listener);

        return panel;
    }

    public JPanel createAuthorsPanel(){
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Altobelli, Citro, Galiano, Vitale");
        label.setFont(new Font("SansSerif", Font.PLAIN , 14));
        panel.add(label);

        return panel;
    }

}
