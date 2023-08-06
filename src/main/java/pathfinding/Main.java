package pathfinding;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static final JFrame FRAME = new JFrame("Pathfinding");
    public static final MapPanel MAP_PANEL = new MapPanel();
    public static final SettingsPanel SETTINGS_PANEL = new SettingsPanel();

    public static void main(String[] args) {
        // Close program when exiting
        // Usually it would hide itself
        FRAME.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Add panel to frame
        FRAME.add(MAP_PANEL, BorderLayout.CENTER);
        FRAME.add(SETTINGS_PANEL, BorderLayout.EAST);
        // ---------------------------

        // Pack the frame
        FRAME.pack();
        // Window size
        FRAME.setSize(640, 480);
        // Center window to center of the screen
        FRAME.setLocationRelativeTo(null);
        // Make the frame visible
        FRAME.setVisible(true);
    }
}
