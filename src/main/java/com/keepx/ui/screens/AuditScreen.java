package com.keepx.ui.screens;

import com.keepx.model.VaultEntry;
import com.keepx.security.VaultManager;
import com.keepx.ui.components.*;
import com.keepx.ui.layout.ScreenRouter;
import com.keepx.ui.theme.ColorTokens;
import com.keepx.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;


/**
 * AuditScreen — Security audit showing weak, duplicate, and old passwords.
 * Top: 3 stat cards. Below: flagged entry list with fix buttons.
 */
public class AuditScreen extends JPanel
        implements ThemeManager.ThemeChangeListener, ScreenRouter.ScreenLifecycle {
    private static final long serialVersionUID = 1L;

    private final JLabel weakCount;
    private final JLabel dupCount;
    private final JLabel oldCount;
    private final JPanel flaggedPanel;
    private final JLabel statusLabel;

    public AuditScreen() {
        setOpaque(false);
        setLayout(new BorderLayout(0, ColorTokens.VERTICAL_GAP));
        setBorder(BorderFactory.createEmptyBorder(
            ColorTokens.SCREEN_PADDING, ColorTokens.SCREEN_PADDING,
            80 + ColorTokens.SCREEN_PADDING, ColorTokens.SCREEN_PADDING));

        // ── Title ─────────────────────────────────────────────────────────────────
        JPanel titleRow = transparent(new FlowLayout(FlowLayout.LEFT));
        JLabel title = label("\uD83D\uDD0D Security Audit", 26, Font.BOLD, ThemeManager.getInstance().getTextPrimary());
        NeoButton rescanBtn = new NeoButton("Rescan", NeoButton.Variant.SECONDARY);
        rescanBtn.setPreferredSize(new Dimension(90, 36));
        rescanBtn.addActionListener(e -> runAudit());
        titleRow.add(title);
        titleRow.add(Box.createHorizontalStrut(16));
        titleRow.add(rescanBtn);
        add(titleRow, BorderLayout.NORTH);

        // ── Stat cards ────────────────────────────────────────────────────────────
        JPanel statsRow = transparent(new GridLayout(1, 3, 16, 0));
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        weakCount = bigStatLabel("0", ColorTokens.DANGER);
        dupCount  = bigStatLabel("0", ColorTokens.WARNING);
        oldCount  = bigStatLabel("0", ColorTokens.SECONDARY_ACCENT);

        statsRow.add(statCard("Weak Passwords", weakCount, ColorTokens.DANGER));
        statsRow.add(statCard("Duplicates",     dupCount,  ColorTokens.WARNING));
        statsRow.add(statCard("Outdated (90d+)",oldCount,  ColorTokens.SECONDARY_ACCENT));

        // ── Flagged list ──────────────────────────────────────────────────────────
        flaggedPanel = transparent();
        flaggedPanel.setLayout(new BoxLayout(flaggedPanel, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(flaggedPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        statusLabel = label("Run audit to check your vault.", 14, Font.PLAIN,
                ThemeManager.getInstance().getTextSecondary());
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel center = transparent(new BorderLayout());
        center.add(statsRow, BorderLayout.NORTH);
        center.add(Box.createVerticalStrut(16), BorderLayout.CENTER);
        center.add(scroll, BorderLayout.SOUTH);

        JPanel main = transparent(new BorderLayout(0, 12));
        main.add(statsRow, BorderLayout.NORTH);
        JPanel listWrap = transparent(new BorderLayout());
        listWrap.add(scroll, BorderLayout.CENTER);
        main.add(listWrap, BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        ThemeManager.getInstance().addThemeChangeListener(this);
    }

    @Override
    public void onScreenShown() { runAudit(); }

    private void runAudit() {
        if (!VaultManager.getInstance().isUnlocked()) {
            statusLabel.setText("Vault is locked.");
            return;
        }

        statusLabel.setText("Scanning…");
        flaggedPanel.removeAll();

        SwingWorker<AuditResult, Void> worker = new SwingWorker<>() {
            @Override protected AuditResult doInBackground() throws Exception {
                List<VaultEntry> entries = new ArrayList<>(VaultManager.getInstance().getEntries());
                AuditResult result = new AuditResult();
                Map<String, List<VaultEntry>> byPassword = new HashMap<>();
                long ninetyDaysAgo = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000);

                for (VaultEntry e : entries) {
                    try {
                        String plain = VaultManager.getInstance().decryptEntryPassword(e);
                        int strength = NeoStrengthMeter.evaluate(plain);

                        if (strength <= 2) {
                            result.weak.add(new FlaggedEntry(e, "Weak", "Strength: " +
                                (strength == 1 ? "Weak" : "Fair")));
                        }

                        byPassword.computeIfAbsent(plain, k -> new ArrayList<>()).add(e);

                        if (e.getLastModified() < ninetyDaysAgo) {
                            result.old.add(new FlaggedEntry(e, "Outdated", "Not changed in 90+ days"));
                        }
                    } catch (Exception ignored) {}
                }

                // Duplicates
                for (Map.Entry<String, List<VaultEntry>> me : byPassword.entrySet()) {
                    if (me.getValue().size() > 1) {
                        for (VaultEntry e : me.getValue()) {
                            result.duplicates.add(new FlaggedEntry(e, "Duplicate",
                                "Same password used " + me.getValue().size() + "×"));
                        }
                    }
                }

                return result;
            }

            @Override protected void done() {
                try {
                    AuditResult r = get();
                    weakCount.setText(String.valueOf(r.weak.size()));
                    dupCount.setText(String.valueOf(r.duplicates.size()));
                    oldCount.setText(String.valueOf(r.old.size()));

                    flaggedPanel.removeAll();
                    List<FlaggedEntry> all = new ArrayList<>();
                    all.addAll(r.weak); all.addAll(r.duplicates); all.addAll(r.old);

                    if (all.isEmpty()) {
                        statusLabel.setText("\u2705 All passwords look good!");
                    } else {
                        statusLabel.setText(all.size() + " issue(s) found.");
                        for (FlaggedEntry f : all) flaggedPanel.add(buildFlaggedRow(f));
                    }

                    flaggedPanel.revalidate(); flaggedPanel.repaint();
                } catch (Exception ex) {
                    statusLabel.setText("Audit error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private JPanel buildFlaggedRow(FlaggedEntry f) {
        NeoCard card = new NeoCard();
        card.setLayout(new BorderLayout(10, 0));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        card.setBorder(BorderFactory.createEmptyBorder(
            10, 14, 10 + ColorTokens.SHADOW_OFFSET, 14 + ColorTokens.SHADOW_OFFSET));

        JPanel info = transparent();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel site = label(f.entry.getSiteName(), 15, Font.BOLD, ThemeManager.getInstance().getTextPrimary());
        JLabel detail = label(f.detail, 12, Font.PLAIN, ThemeManager.getInstance().getTextSecondary());
        info.add(site); info.add(Box.createVerticalStrut(2)); info.add(detail);
        card.add(info, BorderLayout.CENTER);

        JPanel right = transparent(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        Color badgeColor = switch (f.issueType) {
            case "Weak"      -> ColorTokens.DANGER;
            case "Duplicate" -> ColorTokens.WARNING;
            default          -> ColorTokens.SECONDARY_ACCENT;
        };
        NeoBadge badge = new NeoBadge(f.issueType, badgeColor);
        NeoButton fixBtn = new NeoButton("Fix", NeoButton.Variant.PRIMARY);
        fixBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        fixBtn.setPreferredSize(new Dimension(52, 34));
        fixBtn.addActionListener(e -> {
            JPanel raw = ScreenRouter.getInstance().getScreen(ScreenRouter.ADD_ENTRY);
            if (raw instanceof EntryFormScreen) ((EntryFormScreen) raw).loadEntry(f.entry);
            ScreenRouter.getInstance().navigate(ScreenRouter.ADD_ENTRY);
        });
        right.add(badge); right.add(fixBtn);
        card.add(right, BorderLayout.EAST);

        JPanel wrap = transparent();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.add(card);
        wrap.add(Box.createVerticalStrut(8));
        return wrap;
    }

    private JPanel statCard(String title, JLabel count, Color accentColor) {
        NeoCard card = new NeoCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        int p = 14;
        card.setBorder(BorderFactory.createEmptyBorder(p, p, p + ColorTokens.SHADOW_OFFSET, p + ColorTokens.SHADOW_OFFSET));
        count.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel tl = label(title, 12, Font.BOLD, ThemeManager.getInstance().getTextSecondary());
        tl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(count);
        card.add(Box.createVerticalStrut(4));
        card.add(tl);
        return card;
    }

    private JLabel bigStatLabel(String text, Color color) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", Font.BOLD, 36));
        l.setForeground(color);
        return l;
    }

    private JPanel transparent() { JPanel p = new JPanel(); p.setOpaque(false); return p; }
    private JPanel transparent(LayoutManager lm) { JPanel p = new JPanel(lm); p.setOpaque(false); return p; }
    private JLabel label(String t, int s, int st, Color c) {
        JLabel l = new JLabel(t); l.setFont(new Font("SansSerif", st, s)); l.setForeground(c); return l;
    }

    @Override public void onThemeChanged(boolean isDark) { repaint(); }
    @Override public void removeNotify() {
        super.removeNotify();
        ThemeManager.getInstance().removeThemeChangeListener(this);
    }

    // ── Inner types ───────────────────────────────────────────────────────────────
    private static class AuditResult {
        List<FlaggedEntry> weak       = new ArrayList<>();
        List<FlaggedEntry> duplicates = new ArrayList<>();
        List<FlaggedEntry> old        = new ArrayList<>();
    }

    private static class FlaggedEntry {
        VaultEntry entry;
        String issueType;
        String detail;
        FlaggedEntry(VaultEntry e, String type, String detail) {
            this.entry = e; this.issueType = type; this.detail = detail;
        }
    }
}
