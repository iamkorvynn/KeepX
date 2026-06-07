package com.keepx.ui.screens;

import com.keepx.security.VaultManager;
import com.keepx.ui.components.*;
import com.keepx.ui.layout.MainFrame;
import com.keepx.ui.layout.ScreenRouter;
import com.keepx.ui.theme.ColorTokens;
import com.keepx.ui.theme.ThemeManager;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;

/**
 * SetupScreen — first-run screen for creating the master password and vault.
 */
public class SetupScreen extends JPanel
        implements ThemeManager.ThemeChangeListener, ScreenRouter.ScreenLifecycle {

    private final NeoPasswordField passwordField;
    private final NeoPasswordField confirmField;
    private final NeoStrengthMeter strengthMeter;
    private final JLabel           errorLabel;
    private final NeoButton        createBtn;

    // Labels / text areas whose foreground must update on theme change
    private final JLabel    logoLabel;
    private final JLabel    taglineLabel;
    private final JLabel    headingLabel;
    private final JTextArea disclaimer;

    public SetupScreen() {
        setOpaque(false);
        setLayout(new GridBagLayout());

        NeoCard card = new NeoCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(480, 580));
        card.setMaximumSize(new Dimension(480, 580));

        int p = 32;
        card.setBorder(BorderFactory.createEmptyBorder(
            p, p, p + ColorTokens.SHADOW_OFFSET, p + ColorTokens.SHADOW_OFFSET));

        // Logo + tagline
        logoLabel    = styledLabel("🔐 KeepX", 38, Font.BOLD, ThemeManager.getInstance().getAccent());
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        taglineLabel = styledLabel("Your secure offline vault", 14, Font.PLAIN, ThemeManager.getInstance().getTextSecondary());
        taglineLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headingLabel = styledLabel("Create Master Password", 20, Font.BOLD, ThemeManager.getInstance().getTextPrimary());
        headingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Fields
        passwordField = new NeoPasswordField("Enter master password");
        confirmField  = new NeoPasswordField("Confirm master password");
        styleField(passwordField);
        styleField(confirmField);

        strengthMeter = new NeoStrengthMeter();
        strengthMeter.setAlignmentX(Component.CENTER_ALIGNMENT);
        strengthMeter.setPreferredSize(new Dimension(400, 30));
        strengthMeter.setMaximumSize(new Dimension(400, 30));

        // Live strength
        passwordField.getPasswordField().getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { updateStrength(); }
            public void removeUpdate(DocumentEvent e)  { updateStrength(); }
            public void changedUpdate(DocumentEvent e) { updateStrength(); }
        });

        // Disclaimer
        disclaimer = new JTextArea(
            "⚠️  Your master password is never stored. If forgotten, " +
            "your vault cannot be recovered — by design.");
        disclaimer.setEditable(false);
        disclaimer.setOpaque(false);
        disclaimer.setFont(new Font("SansSerif", Font.PLAIN, 12));
        disclaimer.setForeground(ThemeManager.getInstance().getTextSecondary());
        disclaimer.setLineWrap(true);
        disclaimer.setWrapStyleWord(true);
        disclaimer.setMaximumSize(new Dimension(400, 60));
        disclaimer.setAlignmentX(Component.CENTER_ALIGNMENT);

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(ColorTokens.DANGER);
        errorLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        createBtn = new NeoButton("Create Vault", NeoButton.Variant.PRIMARY);
        createBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        createBtn.setMaximumSize(new Dimension(400, ColorTokens.BUTTON_HEIGHT + 4));
        createBtn.addActionListener(e -> handleCreate());

        // Assemble
        card.add(Box.createVerticalStrut(4));
        card.add(logoLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(taglineLabel);
        card.add(Box.createVerticalStrut(24));
        card.add(headingLabel);
        card.add(Box.createVerticalStrut(16));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(6));
        card.add(strengthMeter);
        card.add(Box.createVerticalStrut(12));
        card.add(confirmField);
        card.add(Box.createVerticalStrut(16));
        card.add(disclaimer);
        card.add(Box.createVerticalStrut(8));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(14));
        card.add(createBtn);

        add(card, new GridBagConstraints());
        ThemeManager.getInstance().addThemeChangeListener(this);
    }

    private void updateStrength() {
        String pw = passwordField.getText();
        strengthMeter.setStrength(pw.isEmpty() ? 0 : NeoStrengthMeter.evaluate(pw));
    }

    private void handleCreate() {
        String pass    = passwordField.getText();
        String confirm = confirmField.getText();
        errorLabel.setText(" ");

        if (pass.length() < 8) { showError("Password must be at least 8 characters."); return; }
        if (NeoStrengthMeter.evaluate(pass) < 2) { showError("Password is too weak. Add uppercase, numbers or symbols."); return; }
        if (!pass.equals(confirm)) { showError("Passwords do not match."); return; }

        createBtn.setEnabled(false);
        createBtn.setText("Creating vault…");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                VaultManager.getInstance().createVault(pass.toCharArray());
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    ScreenRouter.getInstance().getMainFrame().startAutoLockTimer();
                    ScreenRouter.getInstance().navigate(ScreenRouter.VAULT);
                } catch (Exception ex) {
                    showError("Error: " + ex.getCause().getMessage());
                    createBtn.setEnabled(true);
                    createBtn.setText("Create Vault");
                }
            }
        };
        worker.execute();
    }

    private void showError(String msg) { errorLabel.setText(msg); }

    private JLabel styledLabel(String text, int size, int style, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", style, size));
        l.setForeground(color);
        return l;
    }

    private void styleField(JComponent c) {
        c.setAlignmentX(Component.CENTER_ALIGNMENT);
        c.setMaximumSize(new Dimension(400, ColorTokens.INPUT_HEIGHT + ColorTokens.SHADOW_OFFSET));
    }

    @Override
    public void onThemeChanged(boolean isDark) {
        ThemeManager tm = ThemeManager.getInstance();
        headingLabel.setForeground(tm.getTextPrimary());
        taglineLabel.setForeground(tm.getTextSecondary());
        disclaimer.setForeground(tm.getTextSecondary());
        repaint();
    }

    @Override public void onScreenShown() { errorLabel.setText(" "); }

    @Override public void removeNotify() {
        super.removeNotify();
        ThemeManager.getInstance().removeThemeChangeListener(this);
    }
}
