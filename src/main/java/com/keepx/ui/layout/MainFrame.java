package com.keepx.ui.layout;

import com.keepx.security.VaultManager;
import com.keepx.ui.components.NeoNavBar;
import com.keepx.ui.screens.*;
import com.keepx.ui.theme.ColorTokens;
import com.keepx.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * MainFrame — root JFrame using JLayeredPane for layered content.
 * Layer DEFAULT_LAYER: CardLayout panel with all screens.
 * Layer PALETTE_LAYER: floating NeoNavBar centered at bottom.
 * Auto-lock timer (5 min inactivity) implemented here.
 *
 * DARK MODE NOTE: The cardPanel is OPAQUE with an explicit background color
 * so the JLayeredPane behind it never shows FlatLaf's default color.
 * updateUI callbacks also re-apply the background directly.
 */
public class MainFrame extends JFrame implements ThemeManager.ThemeChangeListener {

    private final JLayeredPane layeredPane;
    private final JPanel       cardPanel;
    private final CardLayout   cardLayout;
    private final NeoNavBar    navBar;

    private Timer autoLockTimer;
    private static final int AUTO_LOCK_MS = 5 * 60 * 1000; // 5 minutes

    public MainFrame() {
        super("KeepX — Secure Password Manager");

        // ── Basic frame config ───────────────────────────────────────────────
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(new Dimension(1280, 800));
        setPreferredSize(new Dimension(1280, 800));
        setLocationRelativeTo(null);

        Color bg = ThemeManager.getInstance().getBackground();

        // ── Force JFrame background (eliminates white flash and FlatLaf default) ─
        getContentPane().setBackground(bg);
        getRootPane().setBackground(bg);
        setBackground(bg);

        // ── Layered pane ─────────────────────────────────────────────────────
        layeredPane = new JLayeredPane() {
            @Override
            protected void paintComponent(Graphics g) {
                // Paint our dark background under every layer
                g.setColor(ThemeManager.getInstance().getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        layeredPane.setOpaque(true);
        layeredPane.setBackground(bg);
        setContentPane(layeredPane);

        // ── Card panel (content layer) ────────────────────────────────────────
        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout) {
            @Override
            protected void paintComponent(Graphics g) {
                // Always fill with the current theme background
                g.setColor(ThemeManager.getInstance().getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        // OPAQUE = true so it fully covers the layered pane below
        cardPanel.setOpaque(true);
        cardPanel.setBackground(bg);
        layeredPane.add(cardPanel, JLayeredPane.DEFAULT_LAYER);

        // ── Nav bar (palette layer — floats above content) ────────────────────
        navBar = new NeoNavBar();
        navBar.setVisible(false);
        layeredPane.add(navBar, JLayeredPane.PALETTE_LAYER);

        // ── Router init ───────────────────────────────────────────────────────
        ScreenRouter.getInstance().init(cardPanel, cardLayout, navBar, this);

        // ── Build & register all screens ──────────────────────────────────────
        buildScreens();

        // ── Resize listener to keep layered pane + nav in sync ────────────────
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                syncLayeredPaneBounds();
                repositionNav();
            }
        });

        // ── Auto-lock ─────────────────────────────────────────────────────────
        setupAutoLock();

        // ── Theme ─────────────────────────────────────────────────────────────
        ThemeManager.getInstance().addThemeChangeListener(this);

        pack();
        setLocationRelativeTo(null);
    }

    private void buildScreens() {
        SetupScreen     setup     = new SetupScreen();
        LoginScreen     login     = new LoginScreen();
        DashboardScreen dashboard = new DashboardScreen();
        EntryFormScreen entryForm = new EntryFormScreen();
        GeneratorScreen generator = new GeneratorScreen();
        AuditScreen     audit     = new AuditScreen();
        SettingsScreen  settings  = new SettingsScreen();

        ScreenRouter.getInstance().registerScreen(ScreenRouter.SETUP,     setup);
        ScreenRouter.getInstance().registerScreen(ScreenRouter.LOGIN,     login);
        ScreenRouter.getInstance().registerScreen(ScreenRouter.VAULT,     dashboard);
        ScreenRouter.getInstance().registerScreen(ScreenRouter.ADD_ENTRY, entryForm);
        ScreenRouter.getInstance().registerScreen(ScreenRouter.GENERATOR, generator);
        ScreenRouter.getInstance().registerScreen(ScreenRouter.AUDIT,     audit);
        ScreenRouter.getInstance().registerScreen(ScreenRouter.SETTINGS,  settings);
    }

    /** Sync CardLayout panel + layered pane to fill entire frame. */
    public void syncLayeredPaneBounds() {
        int w = getContentPane().getWidth();
        int h = getContentPane().getHeight();
        layeredPane.setBounds(0, 0, w, h);
        cardPanel.setBounds(0, 0, w, h);
    }

    /** Reposition nav bar at bottom center. */
    public void repositionNav() {
        if (!navBar.isVisible()) return;
        int lw = layeredPane.getWidth();
        int lh = layeredPane.getHeight();
        Dimension navPref = navBar.getPreferredSize();
        int nx = (lw - navPref.width) / 2;
        int ny = lh - navPref.height - 20;
        navBar.setBounds(nx, ny, navPref.width, navPref.height);
        navBar.repaint();
    }

    private void setupAutoLock() {
        autoLockTimer = new Timer(AUTO_LOCK_MS, e -> lockVault());
        autoLockTimer.setRepeats(false);

        AWTEventListener resetListener = event -> {
            if (autoLockTimer.isRunning()) autoLockTimer.restart();
        };

        Toolkit.getDefaultToolkit().addAWTEventListener(
            resetListener,
            AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK
        );
    }

    public void startAutoLockTimer() { autoLockTimer.restart(); }
    public void stopAutoLockTimer()  { autoLockTimer.stop();    }

    private void lockVault() {
        VaultManager.getInstance().lock();
        ScreenRouter.getInstance().navigate(ScreenRouter.LOGIN);
        stopAutoLockTimer();
    }

    public JLayeredPane getLayeredPane2() { return layeredPane; }
    public NeoNavBar getNavBar() { return navBar; }

    public void navigateToInitialScreen() {
        syncLayeredPaneBounds();
        if (VaultManager.getInstance().vaultExists()) {
            ScreenRouter.getInstance().navigate(ScreenRouter.LOGIN);
        } else {
            ScreenRouter.getInstance().navigate(ScreenRouter.SETUP);
        }
    }

    @Override
    public void onThemeChanged(boolean isDark) {
        Color bg = ThemeManager.getInstance().getBackground();

        // Direct color forcing — belt + suspenders
        setBackground(bg);
        getContentPane().setBackground(bg);
        getRootPane().setBackground(bg);
        layeredPane.setBackground(bg);
        cardPanel.setBackground(bg);

        // Trigger full repaint cascade
        layeredPane.repaint();
        cardPanel.repaint();
        repaint();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ThemeManager.getInstance().removeThemeChangeListener(this);
    }
}
