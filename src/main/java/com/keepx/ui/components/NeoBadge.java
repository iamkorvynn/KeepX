package com.keepx.ui.components;

import com.keepx.ui.theme.ColorTokens;
import com.keepx.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;

/**
 * NeoBadge — small Neo-Brutalist pill label.
 * Used for category labels, issue labels, and status indicators.
 * Color is specified per instance; border always uses theme border.
 */
public class NeoBadge extends JPanel implements ThemeManager.ThemeChangeListener {

    private String text;
    private Color fillColor;

    // Category → color mapping
    public static Color colorForCategory(String category) {
        if (category == null) return new Color(0xB388FF);
        switch (category.toLowerCase()) {
            case "social":        return new Color(0x7C4DFF);
            case "banking":       return new Color(0x00B0FF);
            case "work":          return new Color(0xFF6D00);
            case "shopping":      return new Color(0x00C853);
            case "entertainment": return new Color(0xFF4081);
            case "other":         return new Color(0x90A4AE);
            default:              return new Color(0xB388FF);
        }
    }

    public NeoBadge(String text, Color fillColor) {
        this.text = text;
        this.fillColor = fillColor;
        setOpaque(false);
        ThemeManager.getInstance().addThemeChangeListener(this);
    }

    public NeoBadge(String text) {
        this(text, colorForCategory(text));
    }

    public void setText(String text) { this.text = text; repaint(); }
    public void setFillColor(Color c) { this.fillColor = c; repaint(); }

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
        int r = h / 2;

        // Fill
        g2.setColor(fillColor);
        g2.fillRoundRect(0, 0, w, h, r, r);

        // Border
        g2.setColor(tm.getBorder());
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, r, r);

        // Text
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(text)) / 2;
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, tx, ty);

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(new Font("SansSerif", Font.BOLD, 11));
        int tw = (fm != null && text != null) ? fm.stringWidth(text) : 40;
        return new Dimension(tw + 20, 22);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ThemeManager.getInstance().removeThemeChangeListener(this);
    }
}
