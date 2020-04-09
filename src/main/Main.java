package main;

import frames.StartFrame;
import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        StartFrame start = new StartFrame();

        start.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        start.setLocationRelativeTo(null);
        start.setVisible(true);

    }
}
