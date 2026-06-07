package com.keepx.ui.components;

import com.keepx.ui.theme.ColorTokens;
import com.keepx.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * NeoPasswordField — Neo-Brutalist styled password input with built-in eye toggle.
 * Extends the NeoTextField visual system but uses JPasswordField internally.
 * Eye icon painted inside the right margin to show/hide password text.
 */
public class NeoPasswordField extends JPanel implements ThemeManager.ThemeChangeListener {

    private final JPasswordField passField;
    private boolean showPassword = false;
    private boolean focused = false;
    private boolean error   = false;
    private String placeholder = "";

    // Eye button bounds for hit-testing
    private Rectangle eyeBounds = new Rectangle();

    public NeoPasswordField(String placeholder) {
        this.placeholder = placeholder;
        setOpaque(false);
        setLayout(null); // manual layout for eye button
        int s = ColorTokens.SHADOW_OFFSET;
        // Extra space bottom/right for shadow
        setBorder(BorderFactory.createEmptyBorder(0, 0, s, s));

        passField = new JPasswordField();
        passField.setOpaque(false);
        passField.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 44)); // right margin for eye
        passField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        passField.setCaretColor(ColorTokens.PRIMARY_ACCENT);
        passField.setEchoChar('●');

        passField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { focused = true;  repaint(); }
            @Override public void focusLost(FocusEvent e)   { focused = false; repaint(); }
        });

        add(passField);

        // Eye toggle via mouse click
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (eyeBounds.contains(e.getPoint())) {
                    toggleVisibility();
                }
            }
        });

        ThemeManager.getInstance().addThemeChangeListener(this);
    }

    public NeoPasswordField() {
        this("");
    }

    private void toggleVisibility() {
        showPassword = !showPassword;
        passField.setEchoChar(showPassword ? (char) 0 : '●');
        repaint();
    }

    @Override
    public void doLayout() {
        int w = getWidth();
        int h = getHeight();
        int s = ColorTokens.SHADOW_OFFSET;
        int fieldH = Math.max(h - s, ColorTokens.INPUT_HEIGHT);
        passField.setBounds(0, 0, w - s, fieldH);
        // Eye sits on top painted in paintComponent; no separate component needed
    }

    @Override
    public void onThemeChanged(boolean isDark) {
        passField.setForeground(ThemeManager.getInstance().getTextPrimary());
        repaint();
    }

    public char[] getPassword()     { return passField.getPassword(); }
    public String getText()         { return new String(passField.getPassword()); }
    public void setText(String t)   { passField.setText(t); }
    public JPasswordField getPasswordField() { return passField; }

    public void setError(boolean error) {
        this.error = error;
        repaint();
    }

    public void addKeyListener(KeyListener l) { passField.addKeyListener(l); }

    @Override
    protected void paintComponent(Graphics g) {
        ThemeManager tm = ThemeManager.getInstance();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int w = getWidth();
        int h = getHeight() - ColorTokens.SHADOW_OFFSET;
        int r = ColorTokens.CORNER_RADIUS;
        int s = ColorTokens.SHADOW_OFFSET;

        passField.setForeground(tm.getTextPrimary());

        // 1. Shadow
        g2.setColor(tm.getShadow());
        g2.fillRoundRect(s, s, w - s, h, r, r);

        // 2. Fill
        g2.setColor(tm.getInputFill());
        g2.fillRoundRect(0, 0, w - s, h, r, r);

        // 3. Border
        Color borderColor = error ? ColorTokens.DANGER
                          : focused ? ColorTokens.PRIMARY_ACCENT
                          : tm.getBorder();
        int sw = focused ? ColorTokens.HEAVY_BORDER : ColorTokens.BORDER_THICKNESS;
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(sw));
        g2.drawRoundRect(0, 0, w - s - 1, h - 1, r, r);

        // 4. Placeholder
        if (passField.getPassword().length == 0 && !placeholder.isEmpty() && !passField.isFocusOwner()) {
            g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g2.setColor(tm.getTextSecondary());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(placeholder, 13, (h - fm.getHeight()) / 2 + fm.getAscent());
        }

        // 5. Eye icon (right side)
        int eyeSize = 22;
        int eyeX = w - s - eyeSize - 10;
        int eyeY = (h - eyeSize) / 2;
        eyeBounds = new Rectangle(eyeX, eyeY, eyeSize, eyeSize);

        g2.setColor(tm.getTextSecondary());
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Outer eye shape
        int[] xPoints = {eyeX, eyeX + eyeSize / 2, eyeX + eyeSize, eyeX + eyeSize / 2};
        int[] yPoints = {eyeY + eyeSize / 2, eyeY + 3, eyeY + eyeSize / 2, eyeY + eyeSize - 3};
        g2.drawPolygon(xPoints, yPoints, 4);

        // Pupil
        g2.drawOval(eyeX + eyeSize / 2 - 4, eyeY + eyeSize / 2 - 4, 8, 8);

        // Strikethrough if password hidden
        if (!showPassword) {
            g2.setColor(tm.getTextSecondary());
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(eyeX + 3, eyeY + eyeSize - 3, eyeX + eyeSize - 3, eyeY + 3);
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
