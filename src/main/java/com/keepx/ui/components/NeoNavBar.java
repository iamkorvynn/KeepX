package com.keepx.ui.components;

import com.keepx.ui.theme.ColorTokens;
import com.keepx.ui.theme.ThemeManager;
import com.keepx.ui.layout.ScreenRouter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * NeoNavBar — floating bottom pill navigation bar.
 * Painted on JLayeredPane.PALETTE_LAYER, centered horizontally.
 * Five items: Vault | Add Entry | Generator | Audit | Settings.
 * Active item = yellow fill + 6px shadow. Inactive = surface fill.
 */
public class NeoNavBar extends JPanel implements ThemeManager.ThemeChangeListener {
    private static final long serialVersionUID = 1L;

    public static final String[] SCREEN_IDS = {"VAULT", "ADD_ENTRY", "GENERATOR", "AUDIT", "SETTINGS"};
    public static final String[] LABELS     = {"Vault", "Add Entry", "Generator", "Audit", "Settings"};
    public static final String[] ICONS      = {"\u229F", "\uFF0B", "\u2699", "\uD83D\uDEE1", "\u2699"}; // Unicode glyphs

    // Better icons using unicode
    private static final String[] NAV_ICONS = {
        "\uD83D\uDD12",  // Vault
        "\u271A",   // Add Entry
        "\uD83C\uDFB2",  // Generator
        "\uD83D\uDD0D",  // Audit
        "\u2699"    // Settings
    };

    private String activeScreen = "VAULT";
    private final Rectangle[] itemBounds;

    private static final int NAV_H    = 64;
    private static final int NAV_W    = 500;
    private static final int ITEM_W   = 90;
    private static final int ITEM_H   = 48;
    private static final int PAD_H    = 8;

    public NeoNavBar() {
        itemBounds = new Rectangle[SCREEN_IDS.length];
        setOpaque(false);
        setPreferredSize(new Dimension(NAV_W + ColorTokens.SHADOW_OFFSET,
                                       NAV_H + ColorTokens.SHADOW_OFFSET));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (int i = 0; i < itemBounds.length; i++) {
                    if (itemBounds[i] != null && itemBounds[i].contains(e.getPoint())) {
                        ScreenRouter.getInstance().navigate(SCREEN_IDS[i]);
                        break;
                    }
                }
            }
        });

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        ThemeManager.getInstance().addThemeChangeListener(this);
    }

    public void setActiveScreen(String screenId) {
        this.activeScreen = screenId;
        repaint();
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
        int s = ColorTokens.SHADOW_OFFSET;
        int r = 32; // pill radius

        // Pill total width = items * ITEM_W + padding
        int totalItems = SCREEN_IDS.length;
        int pillW = totalItems * ITEM_W + 24; // 12px side padding
        int pillH = NAV_H;
        int pillX = (w - s - pillW) / 2;
        int pillY = 0;

        // 1. Pill shadow
        g2.setColor(tm.getShadow());
        g2.fillRoundRect(pillX + s, pillY + s, pillW, pillH, r, r);

        // 2. Pill background
        g2.setColor(tm.getSurface());
        g2.fillRoundRect(pillX, pillY, pillW, pillH, r, r);

        // 3. Pill border
        g2.setColor(tm.getBorder());
        g2.setStroke(new BasicStroke(ColorTokens.BORDER_THICKNESS));
        g2.drawRoundRect(pillX, pillY, pillW - 1, pillH - 1, r, r);

        // 4. Nav items
        Font iconFont = new Font("SansSerif", Font.BOLD, 16);
        Font labelFont = new Font("SansSerif", Font.BOLD, 10);

        for (int i = 0; i < totalItems; i++) {
            int itemX = pillX + 12 + i * ITEM_W;
            int itemY = (pillH - ITEM_H) / 2;
            boolean active = SCREEN_IDS[i].equals(activeScreen);

            // Active item: yellow fill + shadow
            if (active) {
                int as = 3;
                g2.setColor(tm.getShadow());
                g2.fillRoundRect(itemX + as, itemY + as, ITEM_W - 4, ITEM_H, 12, 12);
                g2.setColor(ColorTokens.PRIMARY_ACCENT);
                g2.fillRoundRect(itemX, itemY, ITEM_W - 4, ITEM_H, 12, 12);
                g2.setColor(tm.getBorder());
                g2.setStroke(new BasicStroke(ColorTokens.BORDER_THICKNESS));
                g2.drawRoundRect(itemX, itemY, ITEM_W - 5, ITEM_H - 1, 12, 12);
            }

            itemBounds[i] = new Rectangle(itemX, itemY, ITEM_W - 4, ITEM_H);

            // Icon
            g2.setFont(iconFont);
            g2.setColor(active ? new Color(0x0F0F0F) : tm.getTextSecondary());
            FontMetrics ifm = g2.getFontMetrics();
            String icon = NAV_ICONS[i];
            int ix = itemX + (ITEM_W - 4 - ifm.stringWidth(icon)) / 2;
            g2.drawString(icon, ix, itemY + 22);

            // Label
            g2.setFont(labelFont);
            g2.setColor(active ? new Color(0x0F0F0F) : tm.getTextSecondary());
            FontMetrics lfm = g2.getFontMetrics();
            String label = LABELS[i];
            int lx = itemX + (ITEM_W - 4 - lfm.stringWidth(label)) / 2;
            g2.drawString(label, lx, itemY + 40);
        }

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(NAV_W + ColorTokens.SHADOW_OFFSET + 40,
                             NAV_H + ColorTokens.SHADOW_OFFSET);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ThemeManager.getInstance().removeThemeChangeListener(this);
    }
}
