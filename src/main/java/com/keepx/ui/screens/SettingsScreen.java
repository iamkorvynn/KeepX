package com.keepx.ui.screens;

import com.keepx.security.VaultManager;
import com.keepx.ui.components.*;
import com.keepx.ui.layout.ScreenRouter;
import com.keepx.ui.theme.ColorTokens;
import com.keepx.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.*;

/**
 * SettingsScreen — grouped settings sections:
 * Appearance | Security | Import/Export | Backup/Restore | About
 *
 * Dark-mode fix: all labels and the theme toggle button are stored as fields
 * and updated in onThemeChanged(). The dark/light toggle is a NeoButton
 * (custom-painted) so it always uses KeepX colors instead of FlatLaf blue.
 */
public class SettingsScreen extends JPanel
        implements ThemeManager.ThemeChangeListener, ScreenRouter.ScreenLifecycle {
    private static final long serialVersionUID = 1L;

    private final JLabel lastBackupLabel;
    private final JLabel titleLabel;

    // Theme toggle — stored as field so onThemeChanged can update its label text
    private final NeoButton darkModeBtn;

    // Labels that hold ThemeManager colors at construction time → must update on theme change
    private final List<JLabel>    themedLabels    = new ArrayList<>();

    public SettingsScreen() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(
            ColorTokens.SCREEN_PADDING, ColorTokens.SCREEN_PADDING,
            80 + ColorTokens.SCREEN_PADDING, ColorTokens.SCREEN_PADDING));

        titleLabel = label("\u2699 Settings", 26, Font.BOLD, ThemeManager.getInstance().getTextPrimary());
        trackLabel(titleLabel);
        add(titleLabel, BorderLayout.NORTH);

        JPanel content = transparent();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(Box.createVerticalStrut(16));

        // ── Dark Mode Toggle ───────────────────────────────────────────────────
        // Use NeoButton as a toggle so it follows KeepX palette, not FlatLaf blue
        darkModeBtn = new NeoButton(
            ThemeManager.getInstance().isDark() ? "\u2600 Switch to Light" : "\uD83C\uDF19 Switch to Dark",
            ThemeManager.getInstance().isDark() ? NeoButton.Variant.SECONDARY : NeoButton.Variant.PRIMARY
        );
        darkModeBtn.addActionListener(e -> {
            ThemeManager.getInstance().toggle();
            VaultManager.getInstance().saveDarkMode(ThemeManager.getInstance().isDark());
            // label / variant updated in onThemeChanged()
        });

        // ── Appearance ────────────────────────────────────────────────────────────
        content.add(sectionCard("Appearance", buildAppearanceSection()));
        content.add(Box.createVerticalStrut(ColorTokens.SECTION_GAP));

        // ── Security ──────────────────────────────────────────────────────────────
        content.add(sectionCard("Security", buildSecuritySection()));
        content.add(Box.createVerticalStrut(ColorTokens.SECTION_GAP));

        // ── Import / Export ───────────────────────────────────────────────────────
        content.add(sectionCard("Import / Export", buildImportSection()));
        content.add(Box.createVerticalStrut(ColorTokens.SECTION_GAP));

        // ── Backup / Restore ──────────────────────────────────────────────────────
        lastBackupLabel = label("Last backup: Never", 13, Font.PLAIN,
                ThemeManager.getInstance().getTextSecondary());
        trackLabel(lastBackupLabel);
        content.add(sectionCard("Backup / Restore", buildBackupSection()));
        content.add(Box.createVerticalStrut(ColorTokens.SECTION_GAP));

        // ── About ─────────────────────────────────────────────────────────────────
        content.add(sectionCard("About", buildAboutSection()));

        JScrollPane scroll = new JScrollPane(content);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        ThemeManager.getInstance().addThemeChangeListener(this);
    }

    private JPanel buildAppearanceSection() {
        JPanel p = row();
        JLabel lbl = label("Dark Mode", 14, Font.BOLD, ThemeManager.getInstance().getTextPrimary());
        trackLabel(lbl);
        p.add(lbl);
        p.add(Box.createHorizontalGlue());
        p.add(darkModeBtn);
        return p;
    }

    private JPanel buildSecuritySection() {
        JPanel p = col();
        NeoButton changePassBtn = new NeoButton("Change Master Password", NeoButton.Variant.SECONDARY);
        changePassBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        changePassBtn.setMaximumSize(new Dimension(280, ColorTokens.BUTTON_HEIGHT));
        changePassBtn.addActionListener(e -> showChangePwDialog());
        p.add(changePassBtn);
        return p;
    }

    private JPanel buildImportSection() {
        JPanel p = col();
        JLabel info = label("Import from Chrome / Firefox CSV export", 13, Font.PLAIN,
                ThemeManager.getInstance().getTextSecondary());
        trackLabel(info);
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        NeoButton importBtn = new NeoButton("Import CSV", NeoButton.Variant.SECONDARY);
        importBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        importBtn.setMaximumSize(new Dimension(160, ColorTokens.BUTTON_HEIGHT));
        importBtn.addActionListener(e -> handleImportCSV());
        p.add(info);
        p.add(Box.createVerticalStrut(10));
        p.add(importBtn);
        return p;
    }

    private JPanel buildBackupSection() {
        JPanel p = col();
        lastBackupLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lastBackupLabel);
        p.add(Box.createVerticalStrut(10));

        JPanel btnRow = transparent(new FlowLayout(FlowLayout.LEFT, 10, 0));
        NeoButton backupBtn  = new NeoButton("Backup Vault",  NeoButton.Variant.SECONDARY);
        NeoButton restoreBtn = new NeoButton("Restore Vault", NeoButton.Variant.SECONDARY);
        backupBtn.addActionListener(e  -> handleBackup());
        restoreBtn.addActionListener(e -> handleRestore());
        btnRow.add(backupBtn); btnRow.add(restoreBtn);
        p.add(btnRow);
        return p;
    }

    private JPanel buildAboutSection() {
        JPanel p = col();
        JLabel ver = label("KeepX v1.3.0", 15, Font.BOLD, ThemeManager.getInstance().getTextPrimary());
        JLabel desc1 = label("Offline AES-256 encrypted password manager", 13, Font.PLAIN,
                ThemeManager.getInstance().getTextSecondary());
        JLabel desc2 = label("Built with Java Swing + FlatLaf + Gson", 13, Font.PLAIN,
                ThemeManager.getInstance().getTextSecondary());
        trackLabel(ver); trackLabel(desc1); trackLabel(desc2);
        p.add(ver);
        p.add(Box.createVerticalStrut(4));
        p.add(desc1);
        p.add(Box.createVerticalStrut(4));
        p.add(desc2);
        return p;
    }

    private void showChangePwDialog() {
        JDialog dialog = new JDialog(ScreenRouter.getInstance().getMainFrame(),
                "Change Master Password", true);
        dialog.setSize(420, 380);
        dialog.setLocationRelativeTo(ScreenRouter.getInstance().getMainFrame());

        JPanel panel = new JPanel();
        panel.setBackground(ThemeManager.getInstance().getBackground());
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        NeoPasswordField currentField  = new NeoPasswordField("Current password");
        NeoPasswordField newField      = new NeoPasswordField("New password");
        NeoPasswordField confirmField  = new NeoPasswordField("Confirm new password");
        NeoStrengthMeter meter         = new NeoStrengthMeter();

        JLabel dialogTitle = new JLabel("Change Master Password");
        dialogTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        dialogTitle.setForeground(ThemeManager.getInstance().getTextPrimary());

        JLabel errLabel = new JLabel(" ");
        errLabel.setForeground(ColorTokens.DANGER);
        errLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        newField.getPasswordField().getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
            void update() {
                String pw = newField.getText();
                meter.setStrength(pw.isEmpty() ? 0 : NeoStrengthMeter.evaluate(pw));
            }
        });

        NeoButton saveBtn   = new NeoButton("Change Password", NeoButton.Variant.PRIMARY);
        NeoButton cancelBtn = new NeoButton("Cancel", NeoButton.Variant.SECONDARY);

        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, ColorTokens.BUTTON_HEIGHT));
        cancelBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, ColorTokens.BUTTON_HEIGHT));
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        cancelBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        saveBtn.addActionListener(e -> {
            String cur  = currentField.getText();
            String nw   = newField.getText();
            String conf = confirmField.getText();
            if (!nw.equals(conf)) { errLabel.setText("Passwords don't match."); return; }
            if (NeoStrengthMeter.evaluate(nw) < 2) { errLabel.setText("New password is too weak."); return; }
            try {
                VaultManager.getInstance().changeMasterPassword(cur.toCharArray(), nw.toCharArray());
                dialog.dispose();
                JLayeredPane lp = ScreenRouter.getInstance().getMainFrame().getLayeredPane2();
                NeoToast.show(lp, "Master password changed!", NeoToast.Type.SUCCESS, 3000);
            } catch (Exception ex) {
                errLabel.setText(ex.getMessage());
            }
        });
        cancelBtn.addActionListener(e -> dialog.dispose());

        panel.add(dialogTitle);
        panel.add(Box.createVerticalStrut(16));
        panel.add(currentField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(newField);
        panel.add(Box.createVerticalStrut(4));
        panel.add(meter);
        panel.add(Box.createVerticalStrut(10));
        panel.add(confirmField);
        panel.add(Box.createVerticalStrut(8));
        panel.add(errLabel);
        panel.add(Box.createVerticalStrut(12));
        panel.add(saveBtn);
        panel.add(Box.createVerticalStrut(8));
        panel.add(cancelBtn);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void handleImportCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Import from CSV");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV files", "csv"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            SwingWorker<int[], Void> w = new SwingWorker<>() {
                @Override protected int[] doInBackground() throws Exception {
                    return VaultManager.getInstance().importCSV(file);
                }
                @Override protected void done() {
                    try {
                        int[] counts = get();
                        JOptionPane.showMessageDialog(SettingsScreen.this,
                            counts[0] + " entries imported, " + counts[1] + " skipped.",
                            "Import Complete", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(SettingsScreen.this,
                            "Import failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            w.execute();
        }
    }

    private void handleBackup() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Choose Backup Destination Folder");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = fc.getSelectedFile();
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            String fileName = "keepx_backup_" + timestamp + ".kpx";
            Path dest = selectedFolder.toPath().resolve(fileName);
            try {
                VaultManager.getInstance().backupTo(dest);
                lastBackupLabel.setText("Last backup: " + VaultManager.getInstance().getLastBackupDate());
                JLayeredPane lp = ScreenRouter.getInstance().getMainFrame().getLayeredPane2();
                NeoToast.show(lp, "Backup saved to: " + fileName, NeoToast.Type.SUCCESS, 3500);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Backup failed: " + ex.getMessage());
            }
        }
    }

    private void handleRestore() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Restoring will replace your current vault. Are you sure?",
            "Confirm Restore", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Restore Vault from Backup");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("KPX vault files", "kpx"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                VaultManager.getInstance().restoreFrom(fc.getSelectedFile().toPath());
                JLayeredPane lp = ScreenRouter.getInstance().getMainFrame().getLayeredPane2();
                NeoToast.show(lp, "Vault restored. Please re-login.", NeoToast.Type.SUCCESS, 4000);
                ScreenRouter.getInstance().navigate(ScreenRouter.LOGIN);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Restore failed: " + ex.getMessage());
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────────

    private NeoCard sectionCard(String title, JPanel body) {
        NeoCard card = new NeoCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        int p = 18;
        card.setBorder(BorderFactory.createEmptyBorder(p, p, p + ColorTokens.SHADOW_OFFSET, p + ColorTokens.SHADOW_OFFSET));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        JLabel titleLabel = label(title, 15, Font.BOLD, ThemeManager.getInstance().getAccent());
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(12));
        body.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(body);
        return card;
    }

    private JPanel row() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        return p;
    }

    private JPanel col() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    private JPanel transparent() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        return p;
    }

    private JPanel transparent(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setOpaque(false);
        return p;
    }

    private JLabel label(String t, int s, int st, Color c) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("SansSerif", st, s));
        l.setForeground(c);
        return l;
    }

    /** Track a label so it gets its foreground refreshed on theme change. */
    private void trackLabel(JLabel l) { themedLabels.add(l); }

    @Override
    public void onThemeChanged(boolean isDark) {
        ThemeManager tm = ThemeManager.getInstance();

        // Refresh all tracked primary labels
        for (JLabel l : themedLabels) {
            // Distinguish primary vs secondary by looking at current color (rough heuristic).
            // Simpler approach: all tracked labels get primary; secondary ones override explicitly.
            l.setForeground(tm.getTextPrimary());
        }
        // Secondary-color labels
        lastBackupLabel.setForeground(tm.getTextSecondary());

        // Dark mode toggle button label / variant
        darkModeBtn.setText(isDark ? "\u2600 Switch to Light" : "\uD83C\uDF19 Switch to Dark");
        darkModeBtn.setVariant(isDark ? NeoButton.Variant.SECONDARY : NeoButton.Variant.PRIMARY);

        repaint();
    }

    @Override
    public void onScreenShown() {
        lastBackupLabel.setText("Last backup: " + VaultManager.getInstance().getLastBackupDate());
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ThemeManager.getInstance().removeThemeChangeListener(this);
    }
}
