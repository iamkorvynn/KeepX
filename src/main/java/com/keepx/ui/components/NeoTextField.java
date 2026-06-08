package com.keepx.ui.components;

import com.keepx.ui.theme.ColorTokens;
import com.keepx.ui.theme.ThemeManager;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;

/**
 * NeoTextField — Neo-Brutalist styled text input.
 * 48px tall, strong border, focus ring in accent color.
 * Placeholder text painted manually in secondary text color.
 */
public class NeoTextField extends JPanel implements ThemeManager.ThemeChangeListener {
    private static final long serialVersionUID = 1L;

    protected final JTextField field;
    private String placeholder = "";
    private boolean focused = false;
    private boolean error   = false;

    public NeoTextField() {
        this("");
    }

    public NeoTextField(String placeholder) {
        this.placeholder = placeholder;
        setOpaque(false);
        setLayout(new BorderLayout());
        int s = ColorTokens.SHADOW_OFFSET;
        if (ThemeManager.getInstance().isDark()) {
            setBorder(BorderFactory.createEmptyBorder(0, s, s, 0));
        } else {
            setBorder(BorderFactory.createEmptyBorder(0, 0, s, s));
        }

        field = new JTextField();
        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setCaretColor(ColorTokens.PRIMARY_ACCENT);
        field.setPreferredSize(new Dimension(0, ColorTokens.INPUT_HEIGHT));

        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { focused = true;  repaint(); }
            @Override public void focusLost(FocusEvent e)   { focused = false; repaint(); }
        });

        add(field, BorderLayout.CENTER);
        ThemeManager.getInstance().addThemeChangeListener(this);
    }

    @Override
    public void onThemeChanged(boolean isDark) {
        ThemeManager tm = ThemeManager.getInstance();
        field.setForeground(tm.getTextPrimary());
        field.setCaretColor(ColorTokens.PRIMARY_ACCENT);
        field.setBackground(new Color(0, 0, 0, 0));
        int s = ColorTokens.SHADOW_OFFSET;
        if (isDark) {
            setBorder(BorderFactory.createEmptyBorder(0, s, s, 0));
        } else {
            setBorder(BorderFactory.createEmptyBorder(0, 0, s, s));
        }
        repaint();
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    public String getText() { return field.getText(); }
    public void setText(String text) { field.setText(text); }

    public void setError(boolean error) {
        this.error = error;
        repaint();
    }

    public void addDocumentListener(DocumentListener l) {
        field.getDocument().addDocumentListener(l);
    }

    public JTextField getField() { return field; }

    @Override
    protected void paintComponent(Graphics g) {
        ThemeManager tm = ThemeManager.getInstance();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight() - ColorTokens.SHADOW_OFFSET;
        int r = ColorTokens.CORNER_RADIUS;
        int s = ColorTokens.SHADOW_OFFSET;

        // Update text color
        field.setForeground(tm.getTextPrimary());

        int shadowX = tm.isDark() ? 0 : s;
        int fillX   = tm.isDark() ? s : 0;

        // 1. Shadow
        g2.setColor(tm.getShadow());
        g2.fillRoundRect(shadowX, s, w - s, h, r, r);

        // 2. Fill
        g2.setColor(tm.getInputFill());
        g2.fillRoundRect(fillX, 0, w - s, h, r, r);

        // 3. Border — accent color when focused, red when error
        Color borderColor = error ? ColorTokens.DANGER
                          : focused ? ColorTokens.PRIMARY_ACCENT
                          : tm.getBorder();
        g2.setColor(borderColor);
        int strokeW = focused ? ColorTokens.HEAVY_BORDER : ColorTokens.BORDER_THICKNESS;
        g2.setStroke(new BasicStroke(strokeW));
        g2.drawRoundRect(fillX, 0, w - s - 1, h - 1, r, r);

        // 4. Placeholder
        if (field.getText().isEmpty() && !placeholder.isEmpty()) {
            g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g2.setColor(tm.getTextSecondary());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(placeholder, fillX + 13, (h - fm.getHeight()) / 2 + fm.getAscent());
        }

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
