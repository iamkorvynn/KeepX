package com.keepx.ui.components;

import com.keepx.ui.theme.ColorTokens;
import com.keepx.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * NeoButton — Neo-Brutalist styled button with 3 variants.
 * Paints a hard offset shadow, then a filled rounded rect, then bold label.
 * Supports PRIMARY (yellow), SECONDARY (surface), DANGER (red) variants.
 */
public class NeoButton extends JButton implements ThemeManager.ThemeChangeListener {
    private static final long serialVersionUID = 1L;

    public enum Variant { PRIMARY, SECONDARY, DANGER }

    private Variant variant;
    private boolean hovered   = false;
    private boolean pressed   = false;

    public NeoButton(String text, Variant variant) {
        super(text);
        this.variant = variant;
        init();
    }

    public NeoButton(String text) {
        this(text, Variant.PRIMARY);
    }

    private void init() {
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(new Font("SansSerif", Font.BOLD, 14));
        setPreferredSize(new Dimension(getPreferredSize().width, ColorTokens.BUTTON_HEIGHT));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            @Override public void mousePressed(MouseEvent e) { pressed = true;  repaint(); }
            @Override public void mouseReleased(MouseEvent e){ pressed = false; repaint(); }
        });

        ThemeManager.getInstance().addThemeChangeListener(this);
    }

    @Override
    public void onThemeChanged(boolean isDark) { repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        ThemeManager tm = ThemeManager.getInstance();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int w = getWidth();
        int h = getHeight();
        int r = ColorTokens.CORNER_RADIUS;
        int shadowOff = ColorTokens.SHADOW_OFFSET;
        int translate = pressed ? 3 : 0;

        // Determine fill color
        Color fill;
        switch (variant) {
            case PRIMARY:   fill = hovered ? ColorTokens.PRIMARY_ACCENT.darker() : ColorTokens.PRIMARY_ACCENT; break;
            case DANGER:    fill = hovered ? ColorTokens.DANGER.darker()         : ColorTokens.DANGER;          break;
            default:        fill = hovered ? tm.getMutedSurface()                : tm.getSurface();             break;
        }

        Color border = tm.getBorder();
        Color shadow = tm.getShadow();
        Color textColor = (variant == Variant.PRIMARY) ? new Color(0x0F0F0F) : tm.getTextPrimary();
        if (variant == Variant.DANGER) textColor = Color.WHITE;

        // 1. Shadow rect (hard, no blur) - stays in place
        g2.setColor(shadow);
        g2.fillRoundRect(shadowOff, shadowOff, w - shadowOff, h - shadowOff, r, r);

        // 2. Main fill rect - translates/shifts when pressed
        g2.setColor(fill);
        g2.fillRoundRect(translate, translate, w - shadowOff, h - shadowOff, r, r);

        // 3. Border
        g2.setColor(border);
        g2.setStroke(new BasicStroke(ColorTokens.BORDER_THICKNESS));
        g2.drawRoundRect(translate, translate, w - shadowOff - 1, h - shadowOff - 1, r, r);

        // 4. Label
        g2.setFont(getFont());
        g2.setColor(textColor);
        FontMetrics fm = g2.getFontMetrics();
        String text = getText();
        int tx = translate + (w - shadowOff - fm.stringWidth(text)) / 2;
        int ty = translate + (h - shadowOff - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, tx, ty);

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont() != null ? getFont() : new Font("SansSerif", Font.BOLD, 14));
        int textW = (fm != null && getText() != null) ? fm.stringWidth(getText()) : 60;
        return new Dimension(textW + 48, ColorTokens.BUTTON_HEIGHT);
    }

    public void setVariant(Variant v) {
        this.variant = v;
        repaint();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ThemeManager.getInstance().removeThemeChangeListener(this);
    }
}
