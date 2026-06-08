package com.keepx.ui.screens;

import com.keepx.model.VaultEntry;
import com.keepx.security.VaultManager;
import com.keepx.ui.components.*;
import com.keepx.ui.layout.ScreenRouter;
import com.keepx.ui.theme.ColorTokens;
import com.keepx.ui.theme.ThemeManager;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DashboardScreen — main vault view with search, category filter, pinned cards, and entry list.
 */
public class DashboardScreen extends JPanel
        implements ThemeManager.ThemeChangeListener, ScreenRouter.ScreenLifecycle {
    private static final long serialVersionUID = 1L;

    private static final String[] CATEGORIES = {"All", "Social", "Banking", "Work", "Shopping", "Entertainment", "Other"};

    private final NeoTextField searchField;
    private final JPanel       chipRow;
    private final JPanel       pinnedPanel;
    private final JPanel       entryListPanel;
    private final JScrollPane  scrollPane;
    private final JLabel       emptyState;
    private final JLabel       titleLabel; // stored for theme update

    private String activeCategory = "All";
    private String searchQuery    = "";

    private final Map<String, NeoButton> chipButtons = new LinkedHashMap<>();

    public DashboardScreen() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(
            ColorTokens.SCREEN_PADDING, ColorTokens.SCREEN_PADDING,
            80 + ColorTokens.SCREEN_PADDING, ColorTokens.SCREEN_PADDING)); // bottom clears nav

        // ── Header ───────────────────────────────────────────────────────────────
        JPanel header = transparent();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        titleLabel = label("\uD83D\uDD12 My Vault", 26, Font.BOLD, ThemeManager.getInstance().getTextPrimary());
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(titleLabel);
        header.add(Box.createVerticalStrut(12));

        // Search
        searchField = new NeoTextField("Search vault…");
        searchField.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, ColorTokens.INPUT_HEIGHT + ColorTokens.SHADOW_OFFSET));
        searchField.addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { onSearch(); }
            public void removeUpdate(DocumentEvent e)  { onSearch(); }
            public void changedUpdate(DocumentEvent e) { onSearch(); }
        });
        header.add(searchField);
        header.add(Box.createVerticalStrut(12));

        // Category chips
        chipRow = transparent();
        chipRow.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chipRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (String cat : CATEGORIES) {
            NeoButton chip = new NeoButton(cat, NeoButton.Variant.SECONDARY);
            chip.setFont(new Font("SansSerif", Font.BOLD, 12));
            chip.setPreferredSize(new Dimension(cat.length() * 9 + 32, 36));
            chip.addActionListener(e -> { activeCategory = cat; refreshChips(); refreshList(); });
            chipButtons.put(cat, chip);
            chipRow.add(chip);
        }
        header.add(chipRow);
        header.add(Box.createVerticalStrut(16));

        add(header, BorderLayout.NORTH);

        // ── Content (pinned + entry list) ─────────────────────────────────────────
        JPanel content = transparent();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // Pinned section
        pinnedPanel = transparent();
        pinnedPanel.setLayout(new BoxLayout(pinnedPanel, BoxLayout.Y_AXIS));
        content.add(pinnedPanel);

        // Entry list
        entryListPanel = transparent();
        entryListPanel.setLayout(new BoxLayout(entryListPanel, BoxLayout.Y_AXIS));

        emptyState = label("No entries found. Add your first password!", 16, Font.PLAIN,
                ThemeManager.getInstance().getTextSecondary());
        emptyState.setAlignmentX(Component.CENTER_ALIGNMENT);
        emptyState.setHorizontalAlignment(SwingConstants.CENTER);

        content.add(entryListPanel);
        content.add(emptyState);

        scrollPane = new JScrollPane(content);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        ThemeManager.getInstance().addThemeChangeListener(this);
    }

    @Override
    public void onScreenShown() {
        activeCategory = "All";
        searchQuery = "";
        searchField.setText("");
        refreshChips();
        refreshList();
    }

    private void onSearch() {
        searchQuery = searchField.getText().trim().toLowerCase();
        refreshList();
    }

    private void refreshChips() {
        for (Map.Entry<String, NeoButton> e : chipButtons.entrySet()) {
            e.getValue().setVariant(e.getKey().equals(activeCategory)
                    ? NeoButton.Variant.PRIMARY : NeoButton.Variant.SECONDARY);
        }
        chipRow.repaint();
    }

    private void refreshList() {
        if (!VaultManager.getInstance().isUnlocked()) return;

        List<VaultEntry> all = new ArrayList<>(VaultManager.getInstance().getEntries());

        // Filter by category
        List<VaultEntry> filtered = all.stream()
            .filter(e -> activeCategory.equals("All") || activeCategory.equalsIgnoreCase(e.getCategory()))
            .filter(e -> {
                if (searchQuery.isEmpty()) return true;
                return containsIgnoreCase(e.getSiteName(), searchQuery)
                    || containsIgnoreCase(e.getUsername(), searchQuery)
                    || containsIgnoreCase(e.getUrl(), searchQuery)
                    || containsIgnoreCase(e.getNotes(), searchQuery);
            })
            .collect(Collectors.toList());

        // Split into pinned + regular
        List<VaultEntry> pinned  = filtered.stream().filter(VaultEntry::isFavourite).collect(Collectors.toList());
        List<VaultEntry> regular = filtered.stream().filter(e -> !e.isFavourite()).collect(Collectors.toList());

        // Sort by lastModified desc
        Comparator<VaultEntry> byDate = Comparator.comparingLong(VaultEntry::getLastModified).reversed();
        pinned.sort(byDate);
        regular.sort(byDate);

        // Rebuild panels on EDT
        pinnedPanel.removeAll();
        entryListPanel.removeAll();

        if (!pinned.isEmpty()) {
            JLabel pinnedTitle = label("\u2B50 Pinned", 14, Font.BOLD, ThemeManager.getInstance().getAccent());
            pinnedTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
            pinnedPanel.add(pinnedTitle);
            for (VaultEntry e : pinned) pinnedPanel.add(buildEntryCard(e, true));
            pinnedPanel.add(Box.createVerticalStrut(12));
        }

        for (VaultEntry e : regular) entryListPanel.add(buildEntryCard(e, false));

        boolean empty = filtered.isEmpty();
        emptyState.setVisible(empty);

        pinnedPanel.revalidate(); pinnedPanel.repaint();
        entryListPanel.revalidate(); entryListPanel.repaint();
        scrollPane.revalidate(); scrollPane.repaint();
    }

    private JPanel buildEntryCard(VaultEntry entry, boolean pinned) {
        NeoCard card = new NeoCard(pinned);
        // Use BorderLayout: CENTER = info, EAST = buttons
        card.setLayout(new BorderLayout(16, 0));
        // No fixed max height — let content determine height naturally, just give padding room for shadow
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        card.setBorder(BorderFactory.createEmptyBorder(
            14, 18, 14 + ColorTokens.SHADOW_OFFSET, 18 + ColorTokens.SHADOW_OFFSET));

        // ── Left: info column ─────────────────────────────────────────────────
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel siteName = label(entry.getSiteName() != null ? entry.getSiteName() : "Untitled",
                15, Font.BOLD, ThemeManager.getInstance().getTextPrimary());
        siteName.setAlignmentX(Component.LEFT_ALIGNMENT); // CRITICAL: left-align in BoxLayout

        JLabel username = label(entry.getUsername() != null ? entry.getUsername() : "",
                13, Font.PLAIN, ThemeManager.getInstance().getTextSecondary());
        username.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Badge + date on one row
        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        badgeRow.setOpaque(false);
        badgeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        NeoBadge badge = new NeoBadge(entry.getCategory() != null ? entry.getCategory() : "Other");
        String date = new SimpleDateFormat("MMM d, yyyy").format(new Date(entry.getLastModified()));
        JLabel mod = label("· Modified " + date, 11, Font.PLAIN, ThemeManager.getInstance().getTextSecondary());
        badgeRow.add(badge);
        badgeRow.add(mod);

        info.add(Box.createVerticalGlue());
        info.add(siteName);
        info.add(Box.createVerticalStrut(3));
        info.add(username);
        info.add(Box.createVerticalStrut(6));
        info.add(badgeRow);
        info.add(Box.createVerticalGlue());

        card.add(info, BorderLayout.CENTER);

        // ── Right: action buttons ─────────────────────────────────────────────
        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));

        NeoButton starBtn = new NeoButton(entry.isFavourite() ? "★" : "☆", NeoButton.Variant.SECONDARY);
        starBtn.setFont(new Font("SansSerif", Font.PLAIN, 16));
        starBtn.setPreferredSize(new Dimension(42, 36));
        starBtn.setMaximumSize(new Dimension(42, 36));
        starBtn.setToolTipText(entry.isFavourite() ? "Unpin" : "Pin");
        starBtn.addActionListener(e -> {
            try {
                entry.setFavourite(!entry.isFavourite());
                VaultManager.getInstance().updateEntry(entry);
                refreshList();
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        NeoButton copyBtn = new NeoButton("Copy", NeoButton.Variant.SECONDARY);
        copyBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        copyBtn.setPreferredSize(new Dimension(60, 36));
        copyBtn.setMaximumSize(new Dimension(60, 36));
        copyBtn.addActionListener(e -> copyPassword(entry));

        NeoButton editBtn = new NeoButton("Edit", NeoButton.Variant.SECONDARY);
        editBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        editBtn.setPreferredSize(new Dimension(52, 36));
        editBtn.setMaximumSize(new Dimension(52, 36));
        editBtn.addActionListener(e -> openEdit(entry));

        actions.add(starBtn);
        actions.add(Box.createHorizontalStrut(6));
        actions.add(copyBtn);
        actions.add(Box.createHorizontalStrut(6));
        actions.add(editBtn);

        // Vertically center the button row within the card's EAST slot
        JPanel actionsWrap = new JPanel(new GridBagLayout());
        actionsWrap.setOpaque(false);
        actionsWrap.add(actions); // GridBagLayout centers its single child

        card.add(actionsWrap, BorderLayout.EAST);

        // ── Outer wrap: card + shadow gap ──────────────────────────────────────
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.add(card);
        wrap.add(Box.createVerticalStrut(8));
        return wrap;
    }


    private void copyPassword(VaultEntry entry) {
        SwingWorker<String, Void> w = new SwingWorker<>() {
            @Override protected String doInBackground() throws Exception {
                return VaultManager.getInstance().decryptEntryPassword(entry);
            }
            @Override protected void done() {
                try {
                    String plain = get();
                    Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new StringSelection(plain), null);

                    // Auto-clear clipboard after 30 seconds
                    new javax.swing.Timer(30_000, ev -> {
                        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                        try {
                            String current = (String) cb.getData(DataFlavor.stringFlavor);
                            if (plain.equals(current)) {
                                cb.setContents(new StringSelection(""), null);
                            }
                        } catch (Exception ignored) {}
                        ((javax.swing.Timer) ev.getSource()).stop();
                    }).start();

                    JLayeredPane lp = ScreenRouter.getInstance().getMainFrame().getLayeredPane2();
                    NeoToast.show(lp, "Copied! Clears in 30s", NeoToast.Type.SUCCESS, 3500);
                } catch (Exception ex) {
                    JLayeredPane lp = ScreenRouter.getInstance().getMainFrame().getLayeredPane2();
                    NeoToast.show(lp, "Failed to copy password", NeoToast.Type.DANGER, 3000);
                }
            }
        };
        w.execute();
    }

    private void openEdit(VaultEntry entry) {
        JPanel raw = ScreenRouter.getInstance().getScreen(ScreenRouter.ADD_ENTRY);
        if (raw instanceof EntryFormScreen) {
            ((EntryFormScreen) raw).loadEntry(entry);
        }
        ScreenRouter.getInstance().navigate(ScreenRouter.ADD_ENTRY);
    }

    private boolean containsIgnoreCase(String s, String q) {
        return s != null && s.toLowerCase().contains(q);
    }

    private JPanel transparent() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        return p;
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
        titleLabel.setForeground(tm.getTextPrimary());
        emptyState.setForeground(tm.getTextSecondary());
        repaint();
    }
    @Override public void removeNotify() {
        super.removeNotify();
        ThemeManager.getInstance().removeThemeChangeListener(this);
    }
}
