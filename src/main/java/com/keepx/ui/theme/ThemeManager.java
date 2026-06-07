package com.keepx.ui.theme;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ThemeManager — singleton that controls light/dark theme state for KeepX.
 *
 * KEY DESIGN NOTE:
 * We intentionally do NOT call SwingUtilities.updateComponentTreeUI() after
 * toggling the theme.  That call resets every Swing component's foreground /
 * background back to FlatLaf defaults, wiping our custom KeepX palette.
 *
 * Instead:
 *  1. FlatLaf LAF is applied once at startup (or on the first toggle).
 *  2. KeepX's own FlatLaf UI-defaults are injected into UIManager so that
 *     native Swing components (JScrollBar, JComboBox, etc.) inherit the
 *     KeepX palette rather than FlatLaf's blue tones.
 *  3. Every custom-painted NeoComponent registers as a ThemeChangeListener
 *     and repaints itself in onThemeChanged().
 *  4. Standard Swing labels/text areas created at screen-construction time
 *     must be updated by their parent screen's onThemeChanged() method.
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

    public boolean isDark() { return isDark; }

    public void setDark(boolean dark) {
        this.isDark = dark;
        applyFlatLafDefaults();
        notifyListeners();
    }

    public void toggle() { setDark(!isDark); }

    /**
     * Apply FlatLaf and inject KeepX palette overrides into UIManager.
     * Called once at startup. For subsequent toggles we only update UIManager
     * defaults and notify listeners — no updateComponentTreeUI.
     */
    private void applyFlatLafDefaults() {
        try {
            // Apply LAF only on startup (when no windows exist yet) or when explicitly toggling.
            // We do NOT call updateComponentTreeUI because it wipes custom component colors.
            if (Window.getWindows().length == 0) {
                if (isDark) {
                    UIManager.setLookAndFeel(new FlatDarkLaf());
                } else {
                    UIManager.setLookAndFeel(new FlatLightLaf());
                }
            }
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("[ThemeManager] Failed to apply FlatLaf: " + e.getMessage());
        }

        // Inject KeepX palette into UIManager so Swing internals pick up our colors.
        // This overrides FlatLaf's default blue tones with our purple/lavender palette.
        injectUIManagerDefaults();
    }

    /**
     * Push KeepX palette into UIManager so native Swing widgets
     * (JScrollBar, JComboBox popup, JScrollPane viewport, etc.) use our colors.
     */
    private void injectUIManagerDefaults() {
        Color bg      = getBackground();
        Color surface = getSurface();
        Color text    = getTextPrimary();
        Color textSec = getTextSecondary();
        Color accent  = ColorTokens.PRIMARY_ACCENT;
        Color input   = getInputFill();
        Color border  = getBorder();

        // Panel / container backgrounds
        UIManager.put("Panel.background",           bg);
        UIManager.put("RootPane.background",        bg);
        UIManager.put("Frame.background",           bg);
        UIManager.put("ContentPane.background",     bg);
        UIManager.put("OptionPane.background",      surface);
        UIManager.put("Dialog.background",          surface);

        // Text colors
        UIManager.put("Label.foreground",           text);
        UIManager.put("TextArea.foreground",        text);
        UIManager.put("TextArea.background",        surface);
        UIManager.put("TextArea.caretForeground",   accent);
        UIManager.put("TextField.foreground",       text);
        UIManager.put("TextField.background",       input);
        UIManager.put("TextField.caretForeground",  accent);
        UIManager.put("PasswordField.foreground",   text);
        UIManager.put("PasswordField.background",   input);
        UIManager.put("PasswordField.caretForeground", accent);
        UIManager.put("TextComponent.arc",          ColorTokens.CORNER_RADIUS);

        // ComboBox
        UIManager.put("ComboBox.background",        input);
        UIManager.put("ComboBox.foreground",        text);
        UIManager.put("ComboBox.selectionBackground", accent);
        UIManager.put("ComboBox.selectionForeground", new Color(0x0F0F0F));
        UIManager.put("ComboBox.buttonBackground",  input);

        // ScrollBar — hide visual clutter; match bg
        UIManager.put("ScrollBar.background",       bg);
        UIManager.put("ScrollBar.thumb",            textSec);
        UIManager.put("ScrollBar.thumbDarkShadow",  bg);
        UIManager.put("ScrollBar.thumbHighlight",   surface);
        UIManager.put("ScrollBar.track",            bg);
        UIManager.put("ScrollBar.width",            8);

        // ScrollPane
        UIManager.put("ScrollPane.background",      bg);
        UIManager.put("Viewport.background",        bg);

        // Button / ToggleButton (only for non-NeoButton native widgets)
        UIManager.put("Button.background",          surface);
        UIManager.put("Button.foreground",          text);
        UIManager.put("ToggleButton.background",    isDark ? new Color(0x3A2E55) : new Color(0xD4C8ED));
        UIManager.put("ToggleButton.foreground",    text);
        UIManager.put("ToggleButton.selectedBackground", accent);
        UIManager.put("ToggleButton.selectedForeground", new Color(0x0F0F0F));

        // Focus ring — use accent
        UIManager.put("Component.focusColor",       accent);
        UIManager.put("Component.focusWidth",       2);

        // PopupMenu (ComboBox dropdown, context menus)
        UIManager.put("PopupMenu.background",       surface);
        UIManager.put("PopupMenu.foreground",       text);
        UIManager.put("MenuItem.background",        surface);
        UIManager.put("MenuItem.foreground",        text);
        UIManager.put("MenuItem.selectionBackground", accent);
        UIManager.put("MenuItem.selectionForeground", new Color(0x0F0F0F));

        // List (used by ComboBox popup)
        UIManager.put("List.background",            surface);
        UIManager.put("List.foreground",            text);
        UIManager.put("List.selectionBackground",   accent);
        UIManager.put("List.selectionForeground",   new Color(0x0F0F0F));

        // Table
        UIManager.put("Table.background",           surface);
        UIManager.put("Table.foreground",           text);
        UIManager.put("Table.selectionBackground",  accent);
        UIManager.put("Table.selectionForeground",  new Color(0x0F0F0F));
        UIManager.put("TableHeader.background",     surface);
        UIManager.put("TableHeader.foreground",     textSec);

        // Separator
        UIManager.put("Separator.foreground",       border);

        // ToolTip
        UIManager.put("ToolTip.background",         surface);
        UIManager.put("ToolTip.foreground",         text);

        // FileChooser
        UIManager.put("FileChooser.background",     bg);
        UIManager.put("FileView.directoryIcon",     null);

        // OptionPane
        UIManager.put("OptionPane.messageForeground", text);
    }

    // ── Listener Management ───────────────────────────────────────────────────────

    public void addThemeChangeListener(ThemeChangeListener l) {
        if (!listeners.contains(l)) listeners.add(l);
    }

    public void removeThemeChangeListener(ThemeChangeListener l) {
        listeners.remove(l);
    }

    private void notifyListeners() {
        // Snapshot to avoid ConcurrentModificationException if a listener removes itself
        List<ThemeChangeListener> snapshot = new ArrayList<>(listeners);
        for (ThemeChangeListener l : snapshot) {
            l.onThemeChanged(isDark);
        }
    }

    // ── Color Accessors ────────────────────────────────────────────────────────────

    public Color getBackground()     { return isDark ? ColorTokens.DARK_BACKGROUND    : ColorTokens.LIGHT_BACKGROUND;    }
    public Color getSurface()        { return isDark ? ColorTokens.DARK_SURFACE       : ColorTokens.LIGHT_SURFACE;       }
    public Color getBorder()         { return isDark ? ColorTokens.DARK_BORDER        : ColorTokens.LIGHT_BORDER;        }
    public Color getShadow()         { return isDark ? ColorTokens.DARK_SHADOW        : ColorTokens.LIGHT_SHADOW;        }
    public Color getTextPrimary()    { return isDark ? ColorTokens.DARK_TEXT_PRIMARY   : ColorTokens.LIGHT_TEXT_PRIMARY;  }
    public Color getTextSecondary()  { return isDark ? ColorTokens.DARK_TEXT_SECONDARY : ColorTokens.LIGHT_TEXT_SECONDARY;}
    public Color getInputFill()      { return isDark ? ColorTokens.DARK_INPUT_FILL    : ColorTokens.LIGHT_INPUT_FILL;    }
    public Color getMutedSurface()   { return isDark ? ColorTokens.DARK_MUTED_SURFACE : ColorTokens.LIGHT_MUTED_SURFACE; }

    // Accent colors are the same in both modes
    public Color getAccent()          { return ColorTokens.PRIMARY_ACCENT;   }
    public Color getSecondaryAccent() { return ColorTokens.SECONDARY_ACCENT; }
    public Color getDanger()          { return ColorTokens.DANGER;           }
    public Color getSuccess()         { return ColorTokens.SUCCESS;          }
    public Color getWarning()         { return ColorTokens.WARNING;          }

    // ── Init on startup ────────────────────────────────────────────────────────────

    /**
     * Call once from Main.java before creating the JFrame.
     * Detects saved dark-mode preference and applies FlatLaf + UIManager palette.
     */
    public void initialize(boolean preferDark) {
        this.isDark = preferDark;
        applyFlatLafDefaults();
    }
}
