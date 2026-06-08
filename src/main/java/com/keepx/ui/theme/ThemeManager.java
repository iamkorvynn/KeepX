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
 * RENDERING ORDER (critical for correct dark mode):
 *  1. Inject KeepX palette into UIManager (override FlatLaf blue with our dark purple).
 *  2. Apply FlatLaf LAF so it reads from UIManager.
 *  3. Call updateComponentTreeUI so every Swing component inherits UIManager values.
 *  4. Explicitly set background on JFrame, JLayeredPane, content pane (belt + suspenders).
 *  5. Fire ThemeChangeListeners so custom-painted NeoComponents repaint.
 *
 * Steps 1-2-3 together mean FlatLaf never has a chance to paint its own blue —
 * our UIManager overrides are already in place before Swing does any layout/paint.
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
        if (instance == null) instance = new ThemeManager();
        return instance;
    }

    // ── Theme Control ────────────────────────────────────────────────────────────

    public boolean isDark() { return isDark; }

    public void setDark(boolean dark) {
        this.isDark = dark;
        applyTheme();
        notifyListeners();
    }

    public void toggle() { setDark(!isDark); }

    /**
     * Full theme application:
     * 1. Inject UIManager palette (KeepX colors override FlatLaf defaults).
     * 2. Set FlatLaf LAF.
     * 3. updateComponentTreeUI so all windows pick up UIManager values.
     * 4. Force JFrame / content pane backgrounds directly.
     */
    private void applyTheme() {
        // Step 1 — inject BEFORE setting LAF so FlatLaf reads our overrides
        injectUIManagerDefaults();

        // Step 2 — set LAF (only needed once; subsequent toggles just re-inject + repaint)
        try {
            UIManager.setLookAndFeel(isDark ? new FlatDarkLaf() : new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("[ThemeManager] LAF error: " + e.getMessage());
        }

        // Step 3 — inject again AFTER LAF so our overrides survive FlatLaf's install
        injectUIManagerDefaults();

        // Step 4 — force-update all Swing components to read from UIManager
        for (Window w : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(w);
            // Step 5 — directly set every frame/panel background so Swing never
            // falls back to a cached FlatLaf color
            forceWindowBackground(w);
        }
    }

    /** Recursively force background/foreground on containers that Swing might miss. */
    private void forceWindowBackground(Window w) {
        Color bg      = getBackground();
        Color text    = getTextPrimary();

        w.setBackground(bg);
        if (w instanceof JFrame frame) {
            frame.getContentPane().setBackground(bg);
            frame.getRootPane().setBackground(bg);
            // Walk the layered pane
            forcePanel(frame.getLayeredPane(), bg, text);
        }
        w.repaint();
    }

    private void forcePanel(Container c, Color bg, Color text) {
        if (c == null) return;
        // Only force opaque containers that are plain JPanels (not our custom Neo* components)
        if (c.getClass() == JPanel.class) {
            c.setBackground(bg);
        }
        // JTextArea that is NOT inside a NeoComponent — force colors
        if (c instanceof JTextArea ta && !(c.getParent() instanceof JScrollPane sp
                && sp.getParent() != null && sp.getParent().getClass().getName().contains("Neo"))) {
            ta.setForeground(text);
        }
        for (Component child : c.getComponents()) {
            if (child instanceof Container cont) forcePanel(cont, bg, text);
        }
    }

    /**
     * Push the full KeepX palette into UIManager.
     * Called both BEFORE and AFTER setLookAndFeel to ensure our values survive.
     */
    private void injectUIManagerDefaults() {
        Color bg      = getBackground();
        Color surface = getSurface();
        Color text    = getTextPrimary();
        Color textSec = getTextSecondary();
        Color accent  = ColorTokens.PRIMARY_ACCENT;
        Color input   = getInputFill();
        Color border  = getBorder();
        Color muted   = getMutedSurface();

        // ── Window / container backgrounds ──────────────────────────────────────
        UIManager.put("Panel.background",               bg);
        UIManager.put("RootPane.background",            bg);
        UIManager.put("Frame.background",               bg);
        UIManager.put("Window.background",              bg);
        UIManager.put("ContentPane.background",         bg);
        UIManager.put("OptionPane.background",          surface);
        UIManager.put("Dialog.background",              surface);
        UIManager.put("FileChooser.background",         surface);

        // ── Text ─────────────────────────────────────────────────────────────────
        UIManager.put("Label.foreground",               text);
        UIManager.put("Label.background",               new Color(0, 0, 0, 0)); // transparent
        UIManager.put("TextArea.foreground",            text);
        UIManager.put("TextArea.background",            surface);
        UIManager.put("TextArea.inactiveForeground",    textSec);
        UIManager.put("TextArea.caretForeground",       accent);
        UIManager.put("TextField.foreground",           text);
        UIManager.put("TextField.background",           input);
        UIManager.put("TextField.inactiveForeground",   textSec);
        UIManager.put("TextField.caretForeground",      accent);
        UIManager.put("TextField.selectionBackground",  accent);
        UIManager.put("TextField.selectionForeground",  new Color(0x0F0F0F));
        UIManager.put("PasswordField.foreground",       text);
        UIManager.put("PasswordField.background",       input);
        UIManager.put("PasswordField.caretForeground",  accent);
        UIManager.put("PasswordField.selectionBackground", accent);
        UIManager.put("PasswordField.selectionForeground", new Color(0x0F0F0F));

        // ── ComboBox ─────────────────────────────────────────────────────────────
        UIManager.put("ComboBox.background",            input);
        UIManager.put("ComboBox.foreground",            text);
        UIManager.put("ComboBox.disabledBackground",    muted);
        UIManager.put("ComboBox.disabledForeground",    textSec);
        UIManager.put("ComboBox.selectionBackground",   accent);
        UIManager.put("ComboBox.selectionForeground",   new Color(0x0F0F0F));
        UIManager.put("ComboBox.buttonBackground",      input);
        UIManager.put("ComboBox.buttonArrowColor",      text);

        // ── ScrollBar ────────────────────────────────────────────────────────────
        UIManager.put("ScrollBar.background",           bg);
        UIManager.put("ScrollBar.thumb",                isDark ? new Color(0x3A2E55) : new Color(0xC4B8E0));
        UIManager.put("ScrollBar.thumbDarkShadow",      bg);
        UIManager.put("ScrollBar.thumbHighlight",       surface);
        UIManager.put("ScrollBar.track",                bg);
        UIManager.put("ScrollBar.trackHighlight",       bg);
        UIManager.put("ScrollBar.width",                8);

        // ── ScrollPane / Viewport ────────────────────────────────────────────────
        UIManager.put("ScrollPane.background",          bg);
        UIManager.put("Viewport.background",            bg);

        // ── Buttons (native Swing, not NeoButton) ────────────────────────────────
        UIManager.put("Button.background",              surface);
        UIManager.put("Button.foreground",              text);
        UIManager.put("Button.hoverBackground",         muted);
        UIManager.put("ToggleButton.background",        isDark ? new Color(0x2E2448) : new Color(0xD4C8ED));
        UIManager.put("ToggleButton.foreground",        text);
        UIManager.put("ToggleButton.selectedBackground",accent);
        UIManager.put("ToggleButton.selectedForeground",new Color(0x0F0F0F));

        // ── Focus ring ───────────────────────────────────────────────────────────
        UIManager.put("Component.focusColor",           accent);
        UIManager.put("Component.focusWidth",           2);
        UIManager.put("Component.innerFocusWidth",      0);

        // ── PopupMenu / DropDown ─────────────────────────────────────────────────
        UIManager.put("PopupMenu.background",           surface);
        UIManager.put("PopupMenu.foreground",           text);
        UIManager.put("PopupMenu.border",               BorderFactory.createLineBorder(border, 2));
        UIManager.put("MenuItem.background",            surface);
        UIManager.put("MenuItem.foreground",            text);
        UIManager.put("MenuItem.selectionBackground",   accent);
        UIManager.put("MenuItem.selectionForeground",   new Color(0x0F0F0F));
        UIManager.put("Menu.background",                surface);
        UIManager.put("Menu.foreground",                text);

        // ── List (ComboBox popup) ─────────────────────────────────────────────────
        UIManager.put("List.background",                surface);
        UIManager.put("List.foreground",                text);
        UIManager.put("List.selectionBackground",       accent);
        UIManager.put("List.selectionForeground",       new Color(0x0F0F0F));

        // ── Table ────────────────────────────────────────────────────────────────
        UIManager.put("Table.background",               surface);
        UIManager.put("Table.foreground",               text);
        UIManager.put("Table.gridColor",                border);
        UIManager.put("Table.selectionBackground",      accent);
        UIManager.put("Table.selectionForeground",      new Color(0x0F0F0F));
        UIManager.put("TableHeader.background",         muted);
        UIManager.put("TableHeader.foreground",         textSec);

        // ── ToolTip ──────────────────────────────────────────────────────────────
        UIManager.put("ToolTip.background",             surface);
        UIManager.put("ToolTip.foreground",             text);
        UIManager.put("ToolTip.border",                 BorderFactory.createLineBorder(border, 1));

        // ── Separator ────────────────────────────────────────────────────────────
        UIManager.put("Separator.foreground",           border);

        // ── OptionPane ───────────────────────────────────────────────────────────
        UIManager.put("OptionPane.messageForeground",   text);
        UIManager.put("OptionPane.background",          surface);

        // ── FlatLaf-specific: disable ALL accent/highlight colors that are blue ──
        UIManager.put("@accentColor",                   accent);
        UIManager.put("@accentBaseColor",               accent);
        UIManager.put("Component.accentColor",          accent);
        UIManager.put("TabbedPane.selectedBackground",  accent);
        UIManager.put("TabbedPane.underlineColor",      accent);
        UIManager.put("CheckBox.icon.selectedBackground", accent);
        UIManager.put("CheckBox.icon.checkmarkColor",   new Color(0x0F0F0F));
        UIManager.put("RadioButton.icon.selectedColor", accent);
        UIManager.put("Slider.thumbColor",              accent);
        UIManager.put("Slider.trackValueColor",         accent);
        UIManager.put("ProgressBar.foreground",         accent);
    }

    // ── Listener Management ───────────────────────────────────────────────────────

    public void addThemeChangeListener(ThemeChangeListener l) {
        if (!listeners.contains(l)) listeners.add(l);
    }

    public void removeThemeChangeListener(ThemeChangeListener l) {
        listeners.remove(l);
    }

    private void notifyListeners() {
        List<ThemeChangeListener> snapshot = new ArrayList<>(listeners);
        for (ThemeChangeListener l : snapshot) l.onThemeChanged(isDark);
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

    public Color getAccent()          { return ColorTokens.PRIMARY_ACCENT;   }
    public Color getSecondaryAccent() { return ColorTokens.SECONDARY_ACCENT; }
    public Color getDanger()          { return ColorTokens.DANGER;           }
    public Color getSuccess()         { return ColorTokens.SUCCESS;          }
    public Color getWarning()         { return ColorTokens.WARNING;          }

    // ── Init on startup ────────────────────────────────────────────────────────────

    /**
     * Call once from Main.java before creating any Swing component.
     */
    public void initialize(boolean preferDark) {
        this.isDark = preferDark;
        injectUIManagerDefaults(); // inject first
        try {
            UIManager.setLookAndFeel(isDark ? new FlatDarkLaf() : new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("[ThemeManager] LAF error: " + e.getMessage());
        }
        injectUIManagerDefaults(); // inject again after LAF install
    }
}
