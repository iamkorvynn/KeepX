package com.keepx.ui.components;

import com.keepx.ui.theme.ColorTokens;
import com.keepx.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;

/**
 * NeoCard — Neo-Brutalist styled card panel.
 * Surface fill + 3px border + 6px hard shadow.
 * Optional "pinned" mode applies lavender tint.
 * Use as a container; add child components normally.
 */
public class NeoCard extends JPanel implements ThemeManager.ThemeChangeListener {
    private static final long serialVersionUID = 1L;

    private boolean pinned = false;
    private int vPadding = ColorTokens.CARD_PADDING;
    private int hPadding = ColorTokens.CARD_PADDING;

    public NeoCard() {
        init();
    }

    public NeoCard(boolean pinned) {
        this.pinned = pinned;
        init();
    }

    public NeoCard(int padding) {
        this.vPadding = padding;
        this.hPadding = padding;
        init();
    }

    public NeoCard(int vPadding, int hPadding) {
        this.vPadding = vPadding;
        this.hPadding = hPadding;
        init();
    }

    private void updateBorder() {
        int extra = ColorTokens.SHADOW_OFFSET;
        if (ThemeManager.getInstance().isDark()) {
            setBorder(BorderFactory.createEmptyBorder(vPadding, hPadding + extra, vPadding + extra, hPadding));
        } else {
            setBorder(BorderFactory.createEmptyBorder(vPadding, hPadding, vPadding + extra, hPadding + extra));
        }
    }

    private void init() {
        setOpaque(false);
        setLayout(new BorderLayout());
        updateBorder();
        ThemeManager.getInstance().addThemeChangeListener(this);
    }

    @Override
    public void onThemeChanged(boolean isDark) {
        updateBorder();
        repaint();
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
        repaint();
    }

    public boolean isPinned() { return pinned; }

    @Override
    protected void paintComponent(Graphics g) {
        ThemeManager tm = ThemeManager.getInstance();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int r = ColorTokens.CORNER_RADIUS;
        int s = ColorTokens.SHADOW_OFFSET;

        // Pinned: subtle lavender tint in dark, light lavender in light mode
        Color fill = pinned
                ? (tm.isDark() ? new Color(0x222230) : new Color(0xEAE0FF))
                : tm.getSurface();

        int shadowX = tm.isDark() ? 0 : s;
        int fillX   = tm.isDark() ? s : 0;

        // 1. Shadow
        g2.setColor(tm.getShadow());
        g2.fillRoundRect(shadowX, s, w - s, h - s, r, r);

        // 2. Fill
        g2.setColor(fill);
        g2.fillRoundRect(fillX, 0, w - s, h - s, r, r);

        // 3. Border
        g2.setColor(tm.getBorder());
        g2.setStroke(new BasicStroke(ColorTokens.BORDER_THICKNESS));
        g2.drawRoundRect(fillX, 0, w - s - 1, h - s - 1, r, r);

        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ThemeManager.getInstance().removeThemeChangeListener(this);
    }
}
