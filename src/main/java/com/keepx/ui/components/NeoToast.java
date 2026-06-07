package com.keepx.ui.components;

import com.keepx.ui.theme.ColorTokens;
import com.keepx.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


/**
 * NeoToast — floating notification panel.
 * Appears at bottom-center of the frame, slides up, self-dismisses.
 * Use NeoToast.show(parent, message, type, durationMs) to display.
 */
public class NeoToast extends JPanel implements ThemeManager.ThemeChangeListener {
    private static final long serialVersionUID = 1L;

    public enum Type { INFO, SUCCESS, WARNING, DANGER }

    private String message;
    private Type type;
    private Timer dismissTimer;
    private Timer slideTimer;
    private int targetY;
    private int currentY;

    public NeoToast(String message, Type type) {
        this.message = message;
        this.type = type;
        setOpaque(false);
        setPreferredSize(new Dimension(360, 54));
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
        int h = getHeight() - ColorTokens.SHADOW_OFFSET;
        int r = ColorTokens.LARGE_CORNER_RADIUS;
        int s = ColorTokens.SHADOW_OFFSET;

        Color fill = switch (type) {
            case SUCCESS -> ColorTokens.SUCCESS;
            case WARNING -> ColorTokens.WARNING;
            case DANGER  -> ColorTokens.DANGER;
            default      -> tm.getSurface();
        };

        // Shadow
        g2.setColor(tm.getShadow());
        g2.fillRoundRect(s, s, w - s, h, r, r);

        // Fill
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, w - s, h, r, r);

        // Border
        g2.setColor(tm.getBorder());
        g2.setStroke(new BasicStroke(ColorTokens.BORDER_THICKNESS));
        g2.drawRoundRect(0, 0, w - s - 1, h - 1, r, r);

        // Message
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        Color textColor = (type == Type.INFO) ? tm.getTextPrimary() : Color.WHITE;
        g2.setColor(textColor);
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - s - fm.stringWidth(message)) / 2;
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(message, tx, ty);

        g2.dispose();
    }

    /**
     * Static factory — shows a toast on the given JLayeredPane.
     */
    public static NeoToast show(JLayeredPane layeredPane, String message, Type type, int durationMs) {
        NeoToast toast = new NeoToast(message, type);
        Dimension size = toast.getPreferredSize();

        // Position at bottom center
        int lw = layeredPane.getWidth();
        int lh = layeredPane.getHeight();
        int x = (lw - size.width) / 2;
        int startY = lh + size.height;
        int endY = lh - size.height - 90; // above nav bar

        toast.setBounds(x, startY, size.width, size.height);
        layeredPane.add(toast, JLayeredPane.POPUP_LAYER);

        // Slide in
        int[] yRef = {startY};
        Timer slideIn = new Timer(12, null);
        slideIn.addActionListener(e -> {
            yRef[0] -= 10;
            if (yRef[0] <= endY) {
                yRef[0] = endY;
                ((Timer) e.getSource()).stop();
            }
            toast.setLocation(x, yRef[0]);
        });
        slideIn.start();

        // Auto-dismiss
        Timer dismiss = new Timer(durationMs, e -> {
            layeredPane.remove(toast);
            layeredPane.repaint();
        });
        dismiss.setRepeats(false);
        dismiss.start();

        return toast;
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ThemeManager.getInstance().removeThemeChangeListener(this);
    }
}
