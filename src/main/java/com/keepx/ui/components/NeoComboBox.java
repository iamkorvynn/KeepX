package com.keepx.ui.components;

import com.keepx.ui.theme.ColorTokens;
import com.keepx.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * NeoComboBox — Neo-Brutalist styled combo box wrapper.
 * Strong border, shadow, and custom layout.
 */
public class NeoComboBox<E> extends JPanel implements ThemeManager.ThemeChangeListener {
    private static final long serialVersionUID = 1L;

    private final JComboBox<E> comboBox;
    private boolean focused = false;

    public NeoComboBox(JComboBox<E> comboBox) {
        this.comboBox = comboBox;
        setOpaque(false);
        setLayout(new BorderLayout());
        int s = ColorTokens.SHADOW_OFFSET;
        if (ThemeManager.getInstance().isDark()) {
            setBorder(BorderFactory.createEmptyBorder(0, s, s, 0));
        } else {
            setBorder(BorderFactory.createEmptyBorder(0, 0, s, s));
        }

        comboBox.setOpaque(false);
        // Remove standard JComboBox border so our outer panel draws the border
        comboBox.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        // Listen for focus to repaint the border
        comboBox.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { focused = true;  repaint(); }
            @Override public void focusLost(FocusEvent e)   { focused = false; repaint(); }
        });

        // Forward focus listener from inner editor/button if editable (not in our case, but good practice)
        for (Component c : comboBox.getComponents()) {
            c.addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { focused = true;  repaint(); }
                @Override public void focusLost(FocusEvent e)   { focused = false; repaint(); }
            });
        }

        add(comboBox, BorderLayout.CENTER);
        ThemeManager.getInstance().addThemeChangeListener(this);
    }

    public JComboBox<E> getComboBox() { return comboBox; }

    @Override
    public void onThemeChanged(boolean isDark) {
        ThemeManager tm = ThemeManager.getInstance();
        comboBox.setBackground(new Color(0, 0, 0, 0));
        comboBox.setForeground(tm.getTextPrimary());
        int s = ColorTokens.SHADOW_OFFSET;
        if (isDark) {
            setBorder(BorderFactory.createEmptyBorder(0, s, s, 0));
        } else {
            setBorder(BorderFactory.createEmptyBorder(0, 0, s, s));
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        ThemeManager tm = ThemeManager.getInstance();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight() - ColorTokens.SHADOW_OFFSET;
        int r = ColorTokens.CORNER_RADIUS;
        int s = ColorTokens.SHADOW_OFFSET;

        // Keep foreground/background in sync
        comboBox.setForeground(tm.getTextPrimary());
        comboBox.setBackground(new Color(0, 0, 0, 0));

        int shadowX = tm.isDark() ? 0 : s;
        int fillX   = tm.isDark() ? s : 0;

        // 1. Shadow
        g2.setColor(tm.getShadow());
        g2.fillRoundRect(shadowX, s, w - s, h, r, r);

        // 2. Fill
        g2.setColor(tm.getInputFill());
        g2.fillRoundRect(fillX, 0, w - s, h, r, r);

        // 3. Border
        Color borderColor = focused ? ColorTokens.PRIMARY_ACCENT : tm.getBorder();
        g2.setColor(borderColor);
        int strokeW = focused ? ColorTokens.HEAVY_BORDER : ColorTokens.BORDER_THICKNESS;
        g2.setStroke(new BasicStroke(strokeW));
        g2.drawRoundRect(fillX, 0, w - s - 1, h - 1, r, r);

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, ColorTokens.INPUT_HEIGHT + ColorTokens.SHADOW_OFFSET);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ThemeManager.getInstance().removeThemeChangeListener(this);
    }
}
