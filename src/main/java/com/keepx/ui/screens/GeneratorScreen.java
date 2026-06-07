package com.keepx.ui.screens;

import com.keepx.ui.components.*;
import com.keepx.ui.layout.ScreenRouter;
import com.keepx.ui.theme.ColorTokens;
import com.keepx.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * GeneratorScreen — password generator with length slider, character toggles,
 * live preview, strength meter, and session history.
 */
public class GeneratorScreen extends JPanel
        implements ThemeManager.ThemeChangeListener, ScreenRouter.ScreenLifecycle {
    private static final long serialVersionUID = 1L;

    private final JLabel            previewLabel;
    private final JSlider           lengthSlider;
    private final JLabel            lengthLabel;
    private final JCheckBox         upperBox;
    private final JCheckBox         lowerBox;
    private final JCheckBox         numbersBox;
    private final JCheckBox         symbolsBox;
    private final NeoStrengthMeter  strengthMeter;
    private final JPanel            historyPanel;
    private final NeoButton         generateBtn;
    private final NeoButton         copyBtn;

    private final List<String> history = new ArrayList<>();
    private Consumer<String>   pickCallback = null; // set when opened from EntryFormScreen

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String UPPER   = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER   = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+[]{}|;:,.<>?";

    public GeneratorScreen() {
        setOpaque(false);
        setLayout(new BorderLayout(0, ColorTokens.VERTICAL_GAP));
        setBorder(BorderFactory.createEmptyBorder(
            ColorTokens.SCREEN_PADDING, ColorTokens.SCREEN_PADDING,
            80 + ColorTokens.SCREEN_PADDING, ColorTokens.SCREEN_PADDING));

        // ── Title ─────────────────────────────────────────────────────────────────
        JLabel title = label("🎲 Password Generator", 26, Font.BOLD, ThemeManager.getInstance().getTextPrimary());
        add(title, BorderLayout.NORTH);

        // ── Main content ──────────────────────────────────────────────────────────
        JPanel center = transparent();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        // Generated password preview card
        NeoCard previewCard = new NeoCard();
        previewCard.setLayout(new BorderLayout());
        previewCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        previewCard.setBorder(BorderFactory.createEmptyBorder(12, 18, 12 + ColorTokens.SHADOW_OFFSET, 18 + ColorTokens.SHADOW_OFFSET));

        previewLabel = new JLabel("Click Generate →");
        previewLabel.setFont(new Font("Monospaced", Font.BOLD, 22));
        previewLabel.setForeground(ThemeManager.getInstance().getTextPrimary());
        previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        previewCard.add(previewLabel, BorderLayout.CENTER);
        center.add(previewCard);
        center.add(Box.createVerticalStrut(8));

        // Strength meter
        strengthMeter = new NeoStrengthMeter();
        strengthMeter.setAlignmentX(Component.LEFT_ALIGNMENT);
        strengthMeter.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        center.add(strengthMeter);
        center.add(Box.createVerticalStrut(16));

        // Length slider
        JPanel lengthRow = transparent();
        lengthRow.setLayout(new BoxLayout(lengthRow, BoxLayout.X_AXIS));
        lengthRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel lengthLbl = label("Length:", 14, Font.BOLD, ThemeManager.getInstance().getTextPrimary());
        lengthLbl.setPreferredSize(new Dimension(70, 30));
        lengthLabel = label("20", 14, Font.BOLD, ThemeManager.getInstance().getAccent());
        lengthLabel.setPreferredSize(new Dimension(36, 30));
        lengthSlider = new JSlider(8, 64, 20);
        lengthSlider.setOpaque(false);
        lengthSlider.addChangeListener(e -> {
            lengthLabel.setText(String.valueOf(lengthSlider.getValue()));
            regenerate();
        });

        lengthRow.add(lengthLbl);
        lengthRow.add(Box.createHorizontalStrut(8));
        lengthRow.add(lengthSlider);
        lengthRow.add(Box.createHorizontalStrut(8));
        lengthRow.add(lengthLabel);
        center.add(lengthRow);
        center.add(Box.createVerticalStrut(12));

        // Toggles
        upperBox   = styledCheck("Uppercase (A-Z)", true);
        lowerBox   = styledCheck("Lowercase (a-z)", true);
        numbersBox = styledCheck("Numbers (0-9)",   true);
        symbolsBox = styledCheck("Symbols (!@#…)",  true);

        JPanel toggleRow = transparent();
        toggleRow.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 0));
        toggleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        toggleRow.add(upperBox); toggleRow.add(lowerBox);
        toggleRow.add(numbersBox); toggleRow.add(symbolsBox);
        center.add(toggleRow);
        center.add(Box.createVerticalStrut(16));

        // Buttons
        JPanel btnRow = transparent();
        btnRow.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        generateBtn = new NeoButton("Generate", NeoButton.Variant.PRIMARY);
        copyBtn     = new NeoButton("Copy", NeoButton.Variant.SECONDARY);
        NeoButton useBtn = new NeoButton("Use This Password", NeoButton.Variant.SECONDARY);

        generateBtn.addActionListener(e -> regenerate());
        copyBtn.addActionListener(e -> copyToClipboard());
        useBtn.addActionListener(e -> {
            if (pickCallback != null && previewLabel.getText() != null && !previewLabel.getText().contains("→")) {
                pickCallback.accept(previewLabel.getText());
                pickCallback = null;
            }
        });

        btnRow.add(generateBtn);
        btnRow.add(copyBtn);
        btnRow.add(useBtn);
        center.add(btnRow);
        center.add(Box.createVerticalStrut(20));

        // History
        JLabel histTitle = label("Recent (session only)", 13, Font.BOLD, ThemeManager.getInstance().getTextSecondary());
        center.add(histTitle);
        center.add(Box.createVerticalStrut(8));
        historyPanel = transparent();
        historyPanel.setLayout(new BoxLayout(historyPanel, BoxLayout.Y_AXIS));
        center.add(historyPanel);

        add(center, BorderLayout.CENTER);
        ThemeManager.getInstance().addThemeChangeListener(this);
    }

    private void regenerate() {
        StringBuilder charset = new StringBuilder();
        if (upperBox.isSelected())   charset.append(UPPER);
        if (lowerBox.isSelected())   charset.append(LOWER);
        if (numbersBox.isSelected()) charset.append(NUMBERS);
        if (symbolsBox.isSelected()) charset.append(SYMBOLS);

        if (charset.length() == 0) {
            previewLabel.setText("Select at least one option");
            strengthMeter.setStrength(0);
            return;
        }

        int length = lengthSlider.getValue();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(charset.charAt(RANDOM.nextInt(charset.length())));
        }

        String pw = sb.toString();
        previewLabel.setText(pw);
        strengthMeter.setStrength(NeoStrengthMeter.evaluate(pw));

        // Update history (keep last 5)
        history.add(0, pw);
        if (history.size() > 5) history.remove(history.size() - 1);
        refreshHistory();
    }

    private void copyToClipboard() {
        String pw = previewLabel.getText();
        if (pw == null || pw.contains("→")) return;
        Toolkit.getDefaultToolkit().getSystemClipboard()
            .setContents(new StringSelection(pw), null);
        JLayeredPane lp = ScreenRouter.getInstance().getMainFrame().getLayeredPane2();
        NeoToast.show(lp, "Copied! Clears in 30s", NeoToast.Type.SUCCESS, 3500);

        // Auto-clear clipboard after 30 seconds
        new javax.swing.Timer(30_000, ev -> {
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            try {
                String current = (String) cb.getData(DataFlavor.stringFlavor);
                if (pw.equals(current)) {
                    cb.setContents(new StringSelection(""), null);
                }
            } catch (Exception ignored) {}
            ((javax.swing.Timer) ev.getSource()).stop();
        }).start();
    }

    private void refreshHistory() {
        historyPanel.removeAll();
        for (String pw : history) {
            JPanel row = transparent();
            row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            JLabel l = new JLabel(pw);
            l.setFont(new Font("Monospaced", Font.PLAIN, 13));
            l.setForeground(ThemeManager.getInstance().getTextSecondary());
            NeoButton copy = new NeoButton("Copy", NeoButton.Variant.SECONDARY);
            copy.setFont(new Font("SansSerif", Font.BOLD, 11));
            copy.setPreferredSize(new Dimension(50, 26));
            copy.addActionListener(e -> {
                Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(pw), null);
                JLayeredPane lp = ScreenRouter.getInstance().getMainFrame().getLayeredPane2();
                NeoToast.show(lp, "Copied! Clears in 30s", NeoToast.Type.SUCCESS, 3500);

                // Auto-clear clipboard after 30 seconds
                new javax.swing.Timer(30_000, ev -> {
                    Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                    try {
                        String current = (String) cb.getData(DataFlavor.stringFlavor);
                        if (pw.equals(current)) {
                            cb.setContents(new StringSelection(""), null);
                        }
                    } catch (Exception ignored) {}
                    ((javax.swing.Timer) ev.getSource()).stop();
                }).start();
            });
            row.add(l);
            row.add(Box.createHorizontalGlue());
            row.add(copy);
            historyPanel.add(row);
            historyPanel.add(Box.createVerticalStrut(4));
        }
        historyPanel.revalidate(); historyPanel.repaint();
    }

    /** Called by EntryFormScreen to enable pick mode. */
    public void setPickMode(Consumer<String> callback) {
        this.pickCallback = callback;
    }

    private JCheckBox styledCheck(String text, boolean selected) {
        JCheckBox cb = new JCheckBox(text, selected);
        cb.setOpaque(false);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cb.setForeground(ThemeManager.getInstance().getTextPrimary());
        cb.addActionListener(e -> regenerate());
        return cb;
    }

    private JPanel transparent() {
        JPanel p = new JPanel(); p.setOpaque(false); return p;
    }

    private JLabel label(String text, int size, int style, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", style, size));
        l.setForeground(color);
        return l;
    }

    @Override public void onThemeChanged(boolean isDark) { repaint(); }
    @Override public void onScreenShown() { if (history.isEmpty()) regenerate(); }
    @Override public void removeNotify() {
        super.removeNotify();
        ThemeManager.getInstance().removeThemeChangeListener(this);
    }
}
