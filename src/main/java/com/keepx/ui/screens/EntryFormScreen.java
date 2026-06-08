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

/**
 * EntryFormScreen — Add / Edit entry form with two-column layout.
 * Left: site, username, password, URL, category.
 * Right: notes, strength meter, generate password CTA.
 * Bottom: Save / Cancel / Delete (edit mode only).
 */
public class EntryFormScreen extends JPanel
        implements ThemeManager.ThemeChangeListener, ScreenRouter.ScreenLifecycle {
    private static final long serialVersionUID = 1L;

    private static final String[] CATEGORIES = {"Social", "Banking", "Work", "Shopping", "Entertainment", "Other"};

    private final NeoTextField     siteField;
    private final NeoTextField     usernameField;
    private final NeoPasswordField passwordField;
    private final NeoTextField     urlField;
    private final JComboBox<String> categoryBox;
    private final JTextArea        notesArea;
    private final JTextArea        tipsArea;     // need ref to update color on theme change
    private final JScrollPane      notesScroll;  // need ref to update border on theme change
    private JComponent             notesWrapper; // custom shadow panel wrapper
    private final NeoStrengthMeter strengthMeter;
    private final NeoButton        saveBtn;
    private final NeoButton        cancelBtn;
    private final NeoButton        deleteBtn;
    private final JLabel           errorLabel;
    private final JLabel           titleLabel;

    private VaultEntry editingEntry = null; // null = add mode

    public EntryFormScreen() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(
            ColorTokens.SCREEN_PADDING, ColorTokens.SCREEN_PADDING,
            80 + ColorTokens.SCREEN_PADDING, ColorTokens.SCREEN_PADDING));

        // ── Header ───────────────────────────────────────────────────────────────
        titleLabel = label("Add New Entry", 24, Font.BOLD, ThemeManager.getInstance().getTextPrimary());

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(ColorTokens.DANGER);
        errorLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        JPanel header = transparent(new FlowLayout(FlowLayout.LEFT));
        header.add(titleLabel);
        add(header, BorderLayout.NORTH);

        // ── Two-column form ───────────────────────────────────────────────────────
        JPanel form = transparent(new GridLayout(1, 2, 20, 0));

        // Left column
        JPanel left = transparent();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        siteField     = new NeoTextField("e.g. GitHub");
        usernameField = new NeoTextField("e.g. user@email.com");
        passwordField = new NeoPasswordField("Enter password");
        urlField      = new NeoTextField("https://");

        categoryBox = new JComboBox<>(CATEGORIES);
        categoryBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        categoryBox.setBackground(ThemeManager.getInstance().getInputFill());
        categoryBox.setForeground(ThemeManager.getInstance().getTextPrimary());
        categoryBox.setPreferredSize(new Dimension(0, ColorTokens.INPUT_HEIGHT));
        categoryBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, ColorTokens.INPUT_HEIGHT));
        
        // Wrap with NeoComboBox for border and shadow
        NeoComboBox<String> categoryBoxWrapper = new NeoComboBox<>(categoryBox);

        // Strength meter updates on password type
        passwordField.getPasswordField().getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { updateStrength(); }
            public void removeUpdate(DocumentEvent e)  { updateStrength(); }
            public void changedUpdate(DocumentEvent e) { updateStrength(); }
        });

        left.add(fieldGroup("Site / App Name *", siteField));
        left.add(Box.createVerticalStrut(ColorTokens.VERTICAL_GAP));
        left.add(fieldGroup("Username / Email *", usernameField));
        left.add(Box.createVerticalStrut(ColorTokens.VERTICAL_GAP));
        left.add(fieldGroup("Password *", passwordField));
        left.add(Box.createVerticalStrut(6));
        // Inline strength meter
        strengthMeter = new NeoStrengthMeter();
        strengthMeter.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        strengthMeter.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(strengthMeter);
        left.add(Box.createVerticalStrut(ColorTokens.VERTICAL_GAP));
        left.add(fieldGroup("URL (optional)", urlField));
        left.add(Box.createVerticalStrut(ColorTokens.VERTICAL_GAP));
        left.add(fieldGroup("Category", categoryBoxWrapper));

        // Right column
        JPanel right = transparent();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        notesArea = new JTextArea(6, 20);
        notesArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        notesArea.setForeground(ThemeManager.getInstance().getTextPrimary());
        notesArea.setBackground(ThemeManager.getInstance().getSurface());
        notesArea.setCaretColor(ColorTokens.PRIMARY_ACCENT);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        
        notesScroll = new JScrollPane(notesArea);
        notesScroll.setPreferredSize(new Dimension(0, 140));
        notesScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        notesScroll.setBorder(null); // border drawn by wrapper
        notesScroll.setOpaque(false);
        notesScroll.getViewport().setOpaque(false);

        // Wrap JScrollPane inside a Neo-Brutalist shadow panel
        notesWrapper = new JPanel(new BorderLayout()) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                ThemeManager tm = ThemeManager.getInstance();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight() - ColorTokens.SHADOW_OFFSET;
                int r = ColorTokens.CORNER_RADIUS;
                int s = ColorTokens.SHADOW_OFFSET;

                // 1. Shadow
                g2.setColor(tm.getShadow());
                g2.fillRoundRect(s, s, w - s, h, r, r);

                // 2. Fill
                g2.setColor(tm.getSurface());
                g2.fillRoundRect(0, 0, w - s, h, r, r);

                // 3. Border
                g2.setColor(tm.getBorder());
                g2.setStroke(new BasicStroke(ColorTokens.BORDER_THICKNESS));
                g2.drawRoundRect(0, 0, w - s - 1, h - 1, r, r);

                g2.dispose();
            }
        };
        notesWrapper.setOpaque(false);
        int shadowS = ColorTokens.SHADOW_OFFSET;
        notesWrapper.setBorder(BorderFactory.createEmptyBorder(6, 6, 6 + shadowS, 6 + shadowS));
        notesWrapper.setPreferredSize(new Dimension(0, 160));
        notesWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        notesWrapper.add(notesScroll, BorderLayout.CENTER);

        NeoButton generateBtn = new NeoButton("🎲 Generate Password", NeoButton.Variant.SECONDARY);
        generateBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        generateBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, ColorTokens.BUTTON_HEIGHT));
        generateBtn.addActionListener(e -> {
            JPanel gen = ScreenRouter.getInstance().getScreen(ScreenRouter.GENERATOR);
            if (gen instanceof GeneratorScreen) ((GeneratorScreen) gen).setPickMode(this::onPasswordPicked);
            ScreenRouter.getInstance().navigate(ScreenRouter.GENERATOR);
        });

        tipsArea = new JTextArea(
            "💡 Tips:\n• Use 12+ characters\n• Mix upper/lowercase\n• Add numbers & symbols\n• Never reuse passwords");
        tipsArea.setEditable(false);
        tipsArea.setOpaque(false);
        tipsArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tipsArea.setForeground(ThemeManager.getInstance().getTextSecondary());
        tipsArea.setLineWrap(true);
        tipsArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        right.add(fieldGroup("Notes (optional)", notesWrapper));
        right.add(Box.createVerticalStrut(ColorTokens.VERTICAL_GAP));
        right.add(generateBtn);
        right.add(Box.createVerticalStrut(16));
        right.add(tipsArea);

        form.add(left);
        form.add(right);
        add(form, BorderLayout.CENTER);

        // ── Bottom row ────────────────────────────────────────────────────────────
        JPanel bottom = transparent(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        saveBtn   = new NeoButton("Save Entry", NeoButton.Variant.PRIMARY);
        cancelBtn = new NeoButton("Cancel",     NeoButton.Variant.SECONDARY);
        deleteBtn = new NeoButton("Delete",     NeoButton.Variant.DANGER);

        saveBtn.addActionListener(e   -> handleSave());
        cancelBtn.addActionListener(e -> {
            clearForm();
            ScreenRouter.getInstance().navigate(ScreenRouter.VAULT);
        });
        deleteBtn.addActionListener(e -> handleDelete());
        deleteBtn.setVisible(false);

        bottom.add(errorLabel);
        bottom.add(deleteBtn);
        bottom.add(cancelBtn);
        bottom.add(saveBtn);
        add(bottom, BorderLayout.SOUTH);

        ThemeManager.getInstance().addThemeChangeListener(this);
    }

    private void updateStrength() {
        String pw = passwordField.getText();
        strengthMeter.setStrength(pw.isEmpty() ? 0 : NeoStrengthMeter.evaluate(pw));
    }

    private void onPasswordPicked(String pw) {
        passwordField.setText(pw);
        updateStrength();
        ScreenRouter.getInstance().navigate(ScreenRouter.ADD_ENTRY);
    }

    /** Called by DashboardScreen to load an entry for editing. */
    public void loadEntry(VaultEntry entry) {
        this.editingEntry = entry;
        titleLabel.setText("Edit Entry");
        deleteBtn.setVisible(true);

        siteField.setText(entry.getSiteName() != null ? entry.getSiteName() : "");
        usernameField.setText(entry.getUsername() != null ? entry.getUsername() : "");
        urlField.setText(entry.getUrl() != null ? entry.getUrl() : "");
        notesArea.setText(entry.getNotes() != null ? entry.getNotes() : "");

        // Category
        String cat = entry.getCategory();
        for (int i = 0; i < categoryBox.getItemCount(); i++) {
            if (categoryBox.getItemAt(i).equalsIgnoreCase(cat)) {
                categoryBox.setSelectedIndex(i); break;
            }
        }

        // Decrypt and show password
        SwingWorker<String, Void> w = new SwingWorker<>() {
            @Override protected String doInBackground() throws Exception {
                return VaultManager.getInstance().decryptEntryPassword(entry);
            }
            @Override protected void done() {
                try { passwordField.setText(get()); updateStrength(); } catch (Exception ignored) {}
            }
        };
        w.execute();
    }

    private void handleSave() {
        String site = siteField.getText().trim();
        String user = usernameField.getText().trim();
        String pass = passwordField.getText();
        String url  = urlField.getText().trim();
        String notes = notesArea.getText().trim();
        String cat  = (String) categoryBox.getSelectedItem();

        // Validate
        if (site.isEmpty()) { setError("Site / App Name is required."); return; }
        if (user.isEmpty()) { setError("Username / Email is required."); return; }
        if (pass.isEmpty()) { setError("Password is required."); return; }
        errorLabel.setText(" ");

        saveBtn.setEnabled(false);
        saveBtn.setText("Saving…");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                VaultEntry entry = editingEntry != null ? editingEntry : new VaultEntry();
                entry.setSiteName(site);
                entry.setUsername(user);
                entry.setUrl(url.isEmpty() ? null : url);
                entry.setNotes(notes.isEmpty() ? null : notes);
                entry.setCategory(cat);
                VaultManager.getInstance().encryptEntryPassword(entry, pass);

                if (editingEntry != null) {
                    VaultManager.getInstance().updateEntry(entry);
                } else {
                    VaultManager.getInstance().addEntry(entry);
                }
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    clearForm();
                    JLayeredPane lp = ScreenRouter.getInstance().getMainFrame().getLayeredPane2();
                    NeoToast.show(lp, editingEntry != null ? "Entry updated!" : "Entry saved!", NeoToast.Type.SUCCESS, 3000);
                    editingEntry = null;
                    ScreenRouter.getInstance().navigate(ScreenRouter.VAULT);
                } catch (Exception ex) {
                    setError("Error saving: " + ex.getMessage());
                } finally {
                    saveBtn.setEnabled(true);
                    saveBtn.setText("Save Entry");
                }
            }
        };
        worker.execute();
    }

    private void handleDelete() {
        if (editingEntry == null) return;
        int result = JOptionPane.showConfirmDialog(
            this,
            "Delete \"" + editingEntry.getSiteName() + "\"? This cannot be undone.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            try {
                VaultManager.getInstance().deleteEntry(editingEntry.getId());
                clearForm();
                editingEntry = null;
                JLayeredPane lp = ScreenRouter.getInstance().getMainFrame().getLayeredPane2();
                NeoToast.show(lp, "Entry deleted.", NeoToast.Type.DANGER, 3000);
                ScreenRouter.getInstance().navigate(ScreenRouter.VAULT);
            } catch (Exception ex) {
                setError("Delete failed: " + ex.getMessage());
            }
        }
    }

    private void clearForm() {
        siteField.setText(""); usernameField.setText(""); passwordField.setText("");
        urlField.setText(""); notesArea.setText("");
        strengthMeter.setStrength(0);
        titleLabel.setText("Add New Entry");
        deleteBtn.setVisible(false);
        errorLabel.setText(" ");
        editingEntry = null;
    }

    private void setError(String msg) { errorLabel.setText(msg); }

    private JPanel fieldGroup(String labelText, JComponent field) {
        JPanel p = transparent();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = label(labelText, 13, Font.BOLD, ThemeManager.getInstance().getTextPrimary());
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE,
            field.getPreferredSize().height > 0 ? field.getPreferredSize().height + 4
                                                 : ColorTokens.INPUT_HEIGHT + 10));
        p.add(l);
        p.add(field);
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
        tipsArea.setForeground(tm.getTextSecondary());
        notesArea.setForeground(tm.getTextPrimary());
        notesArea.setBackground(tm.getSurface());
        notesArea.setCaretColor(ColorTokens.PRIMARY_ACCENT);
        if (notesWrapper != null) {
            notesWrapper.repaint();
        }
        // ComboBox colors
        categoryBox.setBackground(tm.getInputFill());
        categoryBox.setForeground(tm.getTextPrimary());
        repaint();
    }
    @Override public void onScreenShown() {
        if (editingEntry == null) clearForm();
    }
    @Override public void removeNotify() {
        super.removeNotify();
        ThemeManager.getInstance().removeThemeChangeListener(this);
    }
}
