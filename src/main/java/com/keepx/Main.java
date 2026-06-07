package com.keepx;

import com.keepx.security.VaultManager;
import com.keepx.ui.layout.MainFrame;
import com.keepx.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;

/**
 * Main — KeepX application entry point.
 * Initializes theme BEFORE any Swing component is created,
 * then forces the background immediately on the JFrame.
 */
public class Main {

    public static void main(String[] args) {
        // Read saved theme preference before creating any UI
        boolean preferDark = VaultManager.getInstance().isDarkMode();

        // Init theme (applies FlatLaf + injects KeepX UIManager palette)
        // This must happen BEFORE any Swing component is instantiated.
        ThemeManager.getInstance().initialize(preferDark);

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();

            // Belt + suspenders: force background on the frame itself
            Color bg = ThemeManager.getInstance().getBackground();
            frame.setBackground(bg);
            frame.getContentPane().setBackground(bg);
            frame.getRootPane().setBackground(bg);

            frame.setVisible(true);

            // Navigate to the appropriate first screen after frame is visible
            SwingUtilities.invokeLater(() -> {
                frame.syncLayeredPaneBounds();
                frame.navigateToInitialScreen();
                frame.repaint();
            });
        });
    }
}
