package com.keepx.ui.components;

import com.keepx.ui.theme.ColorTokens;
import com.keepx.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * NeoToggle — Neo-Brutalist custom-painted pill toggle switch.
 *
 * ON  state: lavender accent track fill, thumb on the right.
 *
 * Draws: hard drop shadow → rounded pill track → circular thumb.
 * Size: 56px × 28px track,  22px thumb diameter.
 * Fires ActionListeners on every toggle (just like a JCheckBox).
 */
public class NeoToggle extends JPanel implements ThemeManager.ThemeChangeListener {
    private static final long serialVersionUID = 1L;

    // ── Visual constants ──────────────────────────────────────────────────────
    private static final int  TRACK_W      = 56;
    private static final int  TRACK_H      = 28;
    private static final int  THUMB_D      = 22;          // diameter
    private static final int  SHADOW_OFF   = 4;
    private static final int  BORDER_W     = 3;

    private boolean selected = false;

    // Optional action listeners (fire on toggle)
    private final java.util.List<java.awt.event.ActionListener> actionListeners =
            new java.util.ArrayList<>();

    // ── Constructor ───────────────────────────────────────────────────────────

    public NeoToggle() {
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText("Toggle dark / light mode");

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggle();
            }
        });

        ThemeManager.getInstance().addThemeChangeListener(this);
    }

    public NeoToggle(boolean selected) {
        this();
        this.selected = selected;
    }

    // ── State ─────────────────────────────────────────────────────────────────

    public boolean isSelected() { return selected; }

    public void setSelected(boolean selected) {
        if (this.selected != selected) {
            this.selected = selected;
            repaint();
        }
    }

    public void toggle() {
        selected = !selected;
        repaint();
        fireActionListeners();
    }

    // ── Listener support ──────────────────────────────────────────────────────

    public void addActionListener(java.awt.event.ActionListener l) {
        actionListeners.add(l);
    }

    public void removeActionListener(java.awt.event.ActionListener l) {
        actionListeners.remove(l);
    }

    private void fireActionListeners() {
        java.awt.event.ActionEvent evt = new java.awt.event.ActionEvent(
                this, java.awt.event.ActionEvent.ACTION_PERFORMED, "toggle");
        for (java.awt.event.ActionListener l : actionListeners) {
            l.actionPerformed(evt);
        }
    }

    // ── Painting ──────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        ThemeManager tm = ThemeManager.getInstance();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int trackX = 0;
        int trackY = 0; // Align to top-left to prevent shadow clipping at the bottom

        int shadowTrackX = tm.isDark() ? trackX : trackX + SHADOW_OFF;
        int fillTrackX   = tm.isDark() ? trackX + SHADOW_OFF : trackX;

        // ── 1. Hard drop shadow (solid rectangle, offset right+down or left+down) ──
        g2.setColor(tm.getShadow());
        g2.fillRoundRect(
                shadowTrackX,
                trackY + SHADOW_OFF,
                TRACK_W,
                TRACK_H,
                TRACK_H, TRACK_H  // full pill corners
        );

        // ── 2. Track fill ─────────────────────────────────────────────────────
        Color trackFill = selected
                ? ColorTokens.PRIMARY_ACCENT
                : (tm.isDark() ? new Color(0x30, 0x27, 0x45) : new Color(0xDE, 0xD5, 0xF0));
        g2.setColor(trackFill);
        g2.fillRoundRect(fillTrackX, trackY, TRACK_W, TRACK_H, TRACK_H, TRACK_H);

        // ── 3. Track border ───────────────────────────────────────────────────
        g2.setColor(tm.getBorder());
        g2.setStroke(new BasicStroke(BORDER_W, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawRoundRect(
                fillTrackX + 1, trackY + 1,
                TRACK_W - 2, TRACK_H - 2,
                TRACK_H, TRACK_H
        );

        // ── 4. Thumb ──────────────────────────────────────────────────────────
        int margin = (TRACK_H - THUMB_D) / 2;
        int thumbX = selected
                ? (fillTrackX + TRACK_W - THUMB_D - margin)   // right side = ON
                : (fillTrackX + margin);                       // left side  = OFF
        int thumbY = trackY + margin;

        // Thumb shadow
        g2.setColor(tm.getShadow());
        int thumbShadowX = tm.isDark() ? thumbX - 2 : thumbX + 2;
        g2.fillOval(thumbShadowX, thumbY + 2, THUMB_D, THUMB_D);

        // Thumb fill — white
        g2.setColor(Color.WHITE);
        g2.fillOval(thumbX, thumbY, THUMB_D, THUMB_D);

        // Thumb border (thin, 1.5px)
        g2.setColor(tm.getBorder());
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(thumbX, thumbY, THUMB_D - 1, THUMB_D - 1);


        g2.dispose();
    }

    // ── Size ──────────────────────────────────────────────────────────────────

    @Override
    public Dimension getPreferredSize() {
        // Extra right/bottom space absorbs the shadow so it paints within bounds
        return new Dimension(TRACK_W + SHADOW_OFF, TRACK_H + SHADOW_OFF);
    }

    @Override
    public Dimension getMinimumSize() { return getPreferredSize(); }

    @Override
    public Dimension getMaximumSize() { return getPreferredSize(); }

    // ── ThemeChangeListener ───────────────────────────────────────────────────

    @Override
    public void onThemeChanged(boolean isDark) { repaint(); }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ThemeManager.getInstance().removeThemeChangeListener(this);
    }
}
