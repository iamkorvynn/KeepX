package com.keepx.ui.components;

import com.keepx.ui.theme.ColorTokens;
import com.keepx.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * NeoPasswordField — Neo-Brutalist styled password input with built-in eye toggle.
 * Eye icon is painted inside the right margin of the outer JPanel.
 * The eye hit-test mouse listener is attached to the passField directly,
 * with coordinates translated back to the outer panel's space, so clicks are
 * never swallowed by the underlying JPasswordField.
 */
public class NeoPasswordField extends JPanel implements ThemeManager.ThemeChangeListener {
    private static final long serialVersionUID = 1L;

    private final JPasswordField passField;
    private final char           originalEcho;
    private boolean showPassword = false;
    private boolean focused      = false;
    private boolean error        = false;
    private String  placeholder  = "";

    // Eye button bounds for hit-testing (in outer panel coords)
    private Rectangle eyeBounds = new Rectangle();

    public NeoPasswordField(String placeholder) {
        this.placeholder = placeholder;
        setOpaque(false);
        setLayout(null); // manual layout
        int s = ColorTokens.SHADOW_OFFSET;
        if (ThemeManager.getInstance().isDark()) {
            setBorder(BorderFactory.createEmptyBorder(0, s, s, 0));
        } else {
            setBorder(BorderFactory.createEmptyBorder(0, 0, s, s));
        }

        passField = new JPasswordField();
        passField.setOpaque(false);
        passField.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 44)); // right margin for eye
        passField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        passField.setCaretColor(ColorTokens.PRIMARY_ACCENT);
        passField.setEchoChar('\u25CF');
        originalEcho = passField.getEchoChar(); // capture echo char as original

        passField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { focused = true;  repaint(); }
            @Override public void focusLost(FocusEvent e)   { focused = false; repaint(); }
        });

        // CRITICAL FIX: attach mouse listener to passField and translate coords
        // to outer panel space for the eye-bounds hit test. This prevents the
        // JPasswordField from swallowing the click before the outer panel sees it.
        passField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Translate from passField coords → outer panel coords
                Point translated = SwingUtilities.convertPoint(passField, e.getPoint(), NeoPasswordField.this);
                if (eyeBounds.contains(translated)) {
                    toggleVisibility();
                }
            }
        });

        // Also keep mouse listener on the outer panel itself for edge cases
        // (e.g. user clicks exactly on the painted eye but not on passField)
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (eyeBounds.contains(e.getPoint())) {
                    toggleVisibility();
                }
            }
        });

        add(passField);
        ThemeManager.getInstance().addThemeChangeListener(this);
    }

    public NeoPasswordField() {
        this("");
    }

    private void toggleVisibility() {
        showPassword = !showPassword;
        if (showPassword) {
            passField.setEchoChar((char) 0); // 0 = show plain text
        } else {
            passField.setEchoChar(originalEcho); // restore echo char
        }
        repaint();
    }

    @Override
    public void doLayout() {
        int w = getWidth();
        int h = getHeight();
        int s = ColorTokens.SHADOW_OFFSET;
        int fieldH = Math.max(h - s, ColorTokens.INPUT_HEIGHT);
        // passField fills the full visible area (shadow is painted outside)
        int fieldX = ThemeManager.getInstance().isDark() ? s : 0;
        passField.setBounds(fieldX, 0, w - s, fieldH);
    }

    @Override
    public void onThemeChanged(boolean isDark) {
        ThemeManager tm = ThemeManager.getInstance();
        passField.setForeground(tm.getTextPrimary());
        passField.setCaretColor(ColorTokens.PRIMARY_ACCENT);
        int s = ColorTokens.SHADOW_OFFSET;
        if (isDark) {
            setBorder(BorderFactory.createEmptyBorder(0, s, s, 0));
        } else {
            setBorder(BorderFactory.createEmptyBorder(0, 0, s, s));
        }
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

    /** Forward key listeners to the inner JPasswordField so callers can attach them. */
    @Override
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

        // Keep foreground in sync every paint cycle (FlatLaf updateComponentTreeUI can reset it)
        passField.setForeground(tm.getTextPrimary());
        passField.setBackground(new Color(0, 0, 0, 0)); // keep transparent

        int shadowX = tm.isDark() ? 0 : s;
        int fillX   = tm.isDark() ? s : 0;

        // 1. Shadow
        g2.setColor(tm.getShadow());
        g2.fillRoundRect(shadowX, s, w - s, h, r, r);

        // 2. Fill
        g2.setColor(tm.getInputFill());
        g2.fillRoundRect(fillX, 0, w - s, h, r, r);

        // 3. Border
        Color borderColor = error        ? ColorTokens.DANGER
                          : focused      ? ColorTokens.PRIMARY_ACCENT
                          :                tm.getBorder();
        int sw = focused ? ColorTokens.HEAVY_BORDER : ColorTokens.BORDER_THICKNESS;
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(sw));
        g2.drawRoundRect(fillX, 0, w - s - 1, h - 1, r, r);

        // 4. Placeholder
        if (passField.getPassword().length == 0 && !placeholder.isEmpty()) {
            g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g2.setColor(tm.getTextSecondary());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(placeholder, fillX + 13, (h - fm.getHeight()) / 2 + fm.getAscent());
        }

        // 5. Eye icon (right side) — computed fresh every paint so coords are always correct
        int eyeSize = 22;
        int eyeX    = fillX + (w - s - eyeSize - 10);
        int eyeY    = (h - eyeSize) / 2;
        eyeBounds   = new Rectangle(eyeX, eyeY, eyeSize, eyeSize);

        Color eyeColor = showPassword ? ColorTokens.PRIMARY_ACCENT : tm.getTextSecondary();
        g2.setColor(eyeColor);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Outer eye shape (diamond polygon)
        int[] xPts = { eyeX, eyeX + eyeSize / 2, eyeX + eyeSize, eyeX + eyeSize / 2 };
        int[] yPts = { eyeY + eyeSize / 2, eyeY + 3, eyeY + eyeSize / 2, eyeY + eyeSize - 3 };
        g2.drawPolygon(xPts, yPts, 4);

        // Pupil
        g2.drawOval(eyeX + eyeSize / 2 - 4, eyeY + eyeSize / 2 - 4, 8, 8);

        // Strikethrough when hidden
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
