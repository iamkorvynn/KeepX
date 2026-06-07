package com.keepx.ui.screens;

import com.keepx.security.VaultManager;
import com.keepx.ui.components.*;
import com.keepx.ui.layout.ScreenRouter;
import com.keepx.ui.theme.ColorTokens;
import com.keepx.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * LoginScreen — unlocks the existing vault with the master password.
 * Shows attempt counter and 30-second lockout after 5 failures.
 */
public class LoginScreen extends JPanel
        implements ThemeManager.ThemeChangeListener, ScreenRouter.ScreenLifecycle {

    private final NeoPasswordField passField;
    private final NeoButton        unlockBtn;
    private final JLabel           errorLabel;
    private final JLabel           attemptsLabel;
    private final JLabel           lockLabel;
    private final JLabel           brandLabel;
    private final JLabel           subtitleLabel;

    private int     failedAttempts = 0;
    private boolean lockedOut      = false;
    private Timer   lockoutTimer;
    private int     lockoutSecondsLeft = 30;

    public LoginScreen() {
        setOpaque(false);
        setLayout(new GridBagLayout());

        NeoCard card = new NeoCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(440, 440));
        card.setMaximumSize(new Dimension(440, 440));
        int p = 36;
        card.setBorder(BorderFactory.createEmptyBorder(
            p, p, p + ColorTokens.SHADOW_OFFSET, p + ColorTokens.SHADOW_OFFSET));

        // Lock icon + brand
        lockLabel = label("🔐", 52, Font.BOLD, ThemeManager.getInstance().getAccent());
        lockLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        brandLabel = label("KeepX", 32, Font.BOLD, ThemeManager.getInstance().getTextPrimary());
        brandLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        subtitleLabel = label("Enter your master password to unlock", 13, Font.PLAIN,
                ThemeManager.getInstance().getTextSecondary());
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Password field
        passField = new NeoPasswordField("Master password");
        passField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passField.setMaximumSize(new Dimension(360, ColorTokens.INPUT_HEIGHT + ColorTokens.SHADOW_OFFSET));

        // Enter key submits
        passField.getPasswordField().addActionListener(e -> handleUnlock());

        // Error + attempts
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(ColorTokens.DANGER);
        errorLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        attemptsLabel = new JLabel(" ");
        attemptsLabel.setForeground(ThemeManager.getInstance().getTextSecondary());
        attemptsLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        attemptsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Unlock button
        unlockBtn = new NeoButton("Unlock Vault", NeoButton.Variant.PRIMARY);
        unlockBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        unlockBtn.setMaximumSize(new Dimension(360, ColorTokens.BUTTON_HEIGHT + 4));
        unlockBtn.addActionListener(e -> handleUnlock());

        // Assemble
        card.add(Box.createVerticalStrut(8));
        card.add(lockLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(brandLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitleLabel);
        card.add(Box.createVerticalStrut(28));
        card.add(passField);
        card.add(Box.createVerticalStrut(8));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(attemptsLabel);
        card.add(Box.createVerticalStrut(16));
        card.add(unlockBtn);

        add(card, new GridBagConstraints());
        ThemeManager.getInstance().addThemeChangeListener(this);
    }

    private void handleUnlock() {
        if (lockedOut) return;

        String pw = passField.getText();
        if (pw.isEmpty()) { errorLabel.setText("Please enter your master password."); return; }

        unlockBtn.setEnabled(false);
        unlockBtn.setText("Unlocking…");
        errorLabel.setText(" ");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override protected Boolean doInBackground() throws Exception {
                return VaultManager.getInstance().unlock(pw.toCharArray());
            }
            @Override protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        failedAttempts = 0;
                        passField.setText("");
                        errorLabel.setText(" ");
                        attemptsLabel.setText(" ");
                        ScreenRouter.getInstance().getMainFrame().startAutoLockTimer();
                        ScreenRouter.getInstance().navigate(ScreenRouter.VAULT);
                    } else {
                        failedAttempts++;
                        passField.setError(true);
                        if (failedAttempts >= 5) {
                            startLockout();
                        } else {
                            errorLabel.setText("Incorrect password. Attempt " + failedAttempts + " of 5.");
                        }
                    }
                } catch (Exception ex) {
                    errorLabel.setText("Error: " + ex.getMessage());
                } finally {
                    if (!lockedOut) {
                        unlockBtn.setEnabled(true);
                        unlockBtn.setText("Unlock Vault");
                    }
                }
            }
        };
        worker.execute();
    }

    private void startLockout() {
        lockedOut = true;
        lockoutSecondsLeft = 30;
        unlockBtn.setEnabled(false);
        passField.setError(true);

        lockoutTimer = new Timer(1000, null);
        lockoutTimer.addActionListener(e -> {
            lockoutSecondsLeft--;
            errorLabel.setText("Too many attempts. Wait " + lockoutSecondsLeft + "s…");
            if (lockoutSecondsLeft <= 0) {
                lockoutTimer.stop();
                lockedOut = false;
                failedAttempts = 0;
                unlockBtn.setEnabled(true);
                unlockBtn.setText("Unlock Vault");
                passField.setError(false);
                errorLabel.setText(" ");
                attemptsLabel.setText(" ");
            }
        });
        lockoutTimer.start();
        errorLabel.setText("Too many attempts. Wait 30s…");
    }

    private JLabel label(String text, int size, int style, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", style, size));
        l.setForeground(color);
        return l;
    }

    @Override
    public void onThemeChanged(boolean isDark) {
        ThemeManager tm = ThemeManager.getInstance();
        brandLabel.setForeground(tm.getTextPrimary());
        subtitleLabel.setForeground(tm.getTextSecondary());
        attemptsLabel.setForeground(tm.getTextSecondary());
        repaint();
    }

    @Override public void onScreenShown() {
        passField.setText("");
        passField.setError(false);
        errorLabel.setText(" ");
        attemptsLabel.setText(" ");
        unlockBtn.setEnabled(true);
        unlockBtn.setText("Unlock Vault");
        SwingUtilities.invokeLater(() -> passField.getPasswordField().requestFocusInWindow());
    }

    @Override public void removeNotify() {
        super.removeNotify();
        ThemeManager.getInstance().removeThemeChangeListener(this);
        if (lockoutTimer != null) lockoutTimer.stop();
    }
}
