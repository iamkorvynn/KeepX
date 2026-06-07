package com.keepx.ui.theme;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ThemeManager — singleton that controls light/dark theme state.
 * All components register as ThemeChangeListeners to repaint on toggle.
 * Colors are retrieved via get*() methods so components never hardcode values.
 */
public final class ThemeManager {

    public interface ThemeChangeListener {
        void onThemeChanged(boolean isDark);
    }

    // ── Singleton ────────────────────────────────────────────────────────────────
    private static ThemeManager instance;
    private boolean isDark = false;
    private final List<ThemeChangeListener> listeners = new ArrayList<>();

    private ThemeManager() {}

    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    // ── Theme Control ────────────────────────────────────────────────────────────

    public boolean isDark() {
        return isDark;
    }

    public void setDark(boolean dark) {
        this.isDark = dark;
        applyFlatLaf();
        notifyListeners();
    }

    public void toggle() {
        setDark(!isDark);
    }

    private void applyFlatLaf() {
        try {
            if (isDark) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } else {
                UIManager.setLookAndFeel(new FlatLightLaf());
            }
            // Update all existing windows
            for (Window w : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(w);
            }
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("[ThemeManager] Failed to apply FlatLaf: " + e.getMessage());
        }
    }

    // ── Listener Management ───────────────────────────────────────────────────────

    public void addThemeChangeListener(ThemeChangeListener l) {
        if (!listeners.contains(l)) listeners.add(l);
    }

    public void removeThemeChangeListener(ThemeChangeListener l) {
        listeners.remove(l);
    }

    private void notifyListeners() {
        for (ThemeChangeListener l : listeners) {
            l.onThemeChanged(isDark);
        }
    }

    // ── Color Accessors ────────────────────────────────────────────────────────────

    public Color getBackground() {
        return isDark ? ColorTokens.DARK_BACKGROUND : ColorTokens.LIGHT_BACKGROUND;
    }

    public Color getSurface() {
        return isDark ? ColorTokens.DARK_SURFACE : ColorTokens.LIGHT_SURFACE;
    }

    public Color getBorder() {
        return isDark ? ColorTokens.DARK_BORDER : ColorTokens.LIGHT_BORDER;
    }

    public Color getShadow() {
        return isDark ? ColorTokens.DARK_SHADOW : ColorTokens.LIGHT_SHADOW;
    }

    public Color getTextPrimary() {
        return isDark ? ColorTokens.DARK_TEXT_PRIMARY : ColorTokens.LIGHT_TEXT_PRIMARY;
    }

    public Color getTextSecondary() {
        return isDark ? ColorTokens.DARK_TEXT_SECONDARY : ColorTokens.LIGHT_TEXT_SECONDARY;
    }

    public Color getInputFill() {
        return isDark ? ColorTokens.DARK_INPUT_FILL : ColorTokens.LIGHT_INPUT_FILL;
    }

    public Color getMutedSurface() {
        return isDark ? ColorTokens.DARK_MUTED_SURFACE : ColorTokens.LIGHT_MUTED_SURFACE;
    }

    // Accent colors are the same in both modes
    public Color getAccent()          { return ColorTokens.PRIMARY_ACCENT; }
    public Color getSecondaryAccent() { return ColorTokens.SECONDARY_ACCENT; }
    public Color getDanger()          { return ColorTokens.DANGER; }
    public Color getSuccess()         { return ColorTokens.SUCCESS; }
    public Color getWarning()         { return ColorTokens.WARNING; }

    // ── Init on startup ────────────────────────────────────────────────────────────

    /**
     * Call once from Main.java before creating the JFrame.
     * Detects system dark mode preference and applies FlatLaf.
     */
    public void initialize(boolean preferDark) {
        this.isDark = preferDark;
        applyFlatLaf();
    }
}
