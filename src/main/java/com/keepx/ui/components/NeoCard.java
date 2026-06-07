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

    private boolean pinned = false;
    private boolean hovered = false;

    public NeoCard() {
        init();
    }

    public NeoCard(boolean pinned) {
        this.pinned = pinned;
        init();
    }

    private void init() {
        setOpaque(false);
        setLayout(new BorderLayout());
        int p = ColorTokens.CARD_PADDING;
        int extra = ColorTokens.SHADOW_OFFSET;
        setBorder(BorderFactory.createEmptyBorder(p, p, p + extra, p + extra));
        ThemeManager.getInstance().addThemeChangeListener(this);
    }

    @Override
    public void onThemeChanged(boolean isDark) { repaint(); }

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

        Color fill = pinned
                ? (tm.isDark() ? new Color(0x2E2448) : ColorTokens.SECONDARY_ACCENT.brighter().brighter())
                : tm.getSurface();

        // 1. Shadow
        g2.setColor(tm.getShadow());
        g2.fillRoundRect(s, s, w - s, h - s, r, r);

        // 2. Fill
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, w - s, h - s, r, r);

        // 3. Border
        g2.setColor(tm.getBorder());
        g2.setStroke(new BasicStroke(ColorTokens.BORDER_THICKNESS));
        g2.drawRoundRect(0, 0, w - s - 1, h - s - 1, r, r);

        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ThemeManager.getInstance().removeThemeChangeListener(this);
    }
}
