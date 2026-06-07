package com.keepx.ui.layout;

import com.keepx.ui.components.NeoNavBar;
import com.keepx.ui.screens.*;
import com.keepx.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * ScreenRouter — singleton that manages CardLayout screen switching.
 * Screens are registered at startup. navigate(id) shows the target card
 * and updates the NeoNavBar active state.
 */
public class ScreenRouter {

    private static ScreenRouter instance;

    private JPanel cardPanel;
    private CardLayout cardLayout;
    private NeoNavBar navBar;
    private MainFrame mainFrame;

    private final Map<String, JPanel> screens = new HashMap<>();
    private String currentScreen = "";

    // Screen ID constants
    public static final String SETUP     = "SETUP";
    public static final String LOGIN     = "LOGIN";
    public static final String VAULT     = "VAULT";
    public static final String ADD_ENTRY = "ADD_ENTRY";
    public static final String GENERATOR = "GENERATOR";
    public static final String AUDIT     = "AUDIT";
    public static final String SETTINGS  = "SETTINGS";

    private static final java.util.Set<String> NO_NAV_SCREENS =
        java.util.Set.of(SETUP, LOGIN);

    private ScreenRouter() {}

    public static ScreenRouter getInstance() {
        if (instance == null) instance = new ScreenRouter();
        return instance;
    }

    public void init(JPanel cardPanel, CardLayout cardLayout, NeoNavBar navBar, MainFrame mainFrame) {
        this.cardPanel  = cardPanel;
        this.cardLayout = cardLayout;
        this.navBar     = navBar;
        this.mainFrame  = mainFrame;
    }

    public void registerScreen(String id, JPanel screen) {
        screens.put(id, screen);
        cardPanel.add(screen, id);
    }

    public void navigate(String screenId) {
        if (!screens.containsKey(screenId)) {
            System.err.println("[ScreenRouter] Unknown screen: " + screenId);
            return;
        }

        currentScreen = screenId;
        cardLayout.show(cardPanel, screenId);

        // Show/hide nav
        boolean showNav = !NO_NAV_SCREENS.contains(screenId);
        navBar.setVisible(showNav);
        if (showNav) {
            navBar.setActiveScreen(screenId);
        } else {
            mainFrame.stopAutoLockTimer();
        }

        // Re-position nav after visibility change
        mainFrame.repositionNav();

        // Notify screen it's being shown
        JPanel screen = screens.get(screenId);
        if (screen instanceof ScreenLifecycle) {
            ((ScreenLifecycle) screen).onScreenShown();
        }

        // Update background
        mainFrame.getContentPane().setBackground(ThemeManager.getInstance().getBackground());
        mainFrame.repaint();
    }

    public String getCurrentScreen() { return currentScreen; }
    public JPanel getScreen(String id) { return screens.get(id); }
    public MainFrame getMainFrame() { return mainFrame; }

    /** Screens implementing this are notified when they become active. */
    public interface ScreenLifecycle {
        void onScreenShown();
    }
}
