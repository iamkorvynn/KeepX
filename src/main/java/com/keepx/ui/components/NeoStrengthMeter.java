package com.keepx.ui.components;

import com.keepx.ui.theme.ColorTokens;
import com.keepx.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


/**
 * NeoStrengthMeter — 4-segment password strength bar.
 * Weak=red, Fair=orange, Good=yellow, Strong=green.
 * setStrength(0-4) updates segments; 0 = all empty.
 */
public class NeoStrengthMeter extends JPanel implements ThemeManager.ThemeChangeListener {
    private static final long serialVersionUID = 1L;

    private int strength = 0; // 0=none, 1=Weak, 2=Fair, 3=Good, 4=Strong

    private static final String[] LABELS = {"", "Weak", "Fair", "Good", "Strong"};
    private static final Color[] COLORS  = {
        null,
        ColorTokens.STRENGTH_WEAK,
        ColorTokens.STRENGTH_FAIR,
        ColorTokens.STRENGTH_GOOD,
        ColorTokens.STRENGTH_STRONG
    };

    public NeoStrengthMeter() {
        setOpaque(false);
        setPreferredSize(new Dimension(200, 30));
        ThemeManager.getInstance().addThemeChangeListener(this);
    }

    public void setStrength(int strength) {
        this.strength = Math.max(0, Math.min(4, strength));
        repaint();
    }

    public int getStrength() { return strength; }

    /**
     * Evaluates password strength and calls setStrength().
     * Returns 0–4.
     */
    public static int evaluate(String password) {
        if (password == null || password.isEmpty()) return 0;
        int score = 0;
        if (password.length() >= 8)  score++;
        if (password.length() >= 12) score++;
        if (password.matches(".*[A-Z].*") && password.matches(".*[a-z].*")) score++;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[^A-Za-z0-9].*")) score++;
        // Map 0-5 → 0-4
        if (score <= 1) return 1;
        if (score == 2) return 2;
        if (score == 3) return 3;
        return 4;
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
        int segCount = 4;
        int gap = 6;
        int totalGap = gap * (segCount - 1);
        int segH = 10;
        int segW = (w - totalGap) / segCount;
        int r = 5;
        int segY = 2;

        for (int i = 0; i < segCount; i++) {
            int x = i * (segW + gap);
            boolean active = strength > i;
            Color fill = active && strength > 0 ? COLORS[strength] : tm.getMutedSurface();

            // Segment fill
            g2.setColor(fill);
            g2.fillRoundRect(x, segY, segW, segH, r, r);

            // Segment border
            g2.setColor(tm.getBorder());
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(x, segY, segW, segH, r, r);
        }

        // Strength label
        if (strength > 0) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.setColor(COLORS[strength]);
            g2.drawString(LABELS[strength], 0, h - 1);
        }

        g2.dispose();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ThemeManager.getInstance().removeThemeChangeListener(this);
    }
}
