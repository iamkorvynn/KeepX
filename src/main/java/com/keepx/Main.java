package com.keepx;

import com.keepx.security.VaultManager;
import com.keepx.ui.layout.MainFrame;
import com.keepx.ui.theme.ThemeManager;

import javax.swing.*;

/**
 * Main — KeepX application entry point.
 * Sets up FlatLaf, detects theme preference, launches MainFrame on EDT.
 */
public class Main {

    public static void main(String[] args) {
        // Read saved theme preference before creating any UI
        boolean preferDark = VaultManager.getInstance().isDarkMode();

        // Init theme (applies FlatLaf) before any Swing component is created
        ThemeManager.getInstance().initialize(preferDark);

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);

            // Navigate to the appropriate first screen after frame is visible
            SwingUtilities.invokeLater(() -> {
                frame.syncLayeredPaneBounds();
                frame.navigateToInitialScreen();
            });
        });
    }
}
