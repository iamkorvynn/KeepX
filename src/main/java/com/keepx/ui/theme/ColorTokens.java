package com.keepx.ui.theme;

import java.awt.Color;

/**
 * ColorTokens — centralized color palette for KeepX Neo-Brutalist UI.
 * All colors sourced from the Frontend UI/UX PRD v1.0.
 * Components must NEVER hardcode colors — always use ThemeManager.get*() methods.
 */
public final class ColorTokens {

    private ColorTokens() {} // utility class

    // ── Light Mode Palette ──────────────────────────────────────────────────────
    public static final Color LIGHT_BACKGROUND    = new Color(0xEDE7F6);
    public static final Color LIGHT_SURFACE       = new Color(0xF8F5FF);
    public static final Color LIGHT_BORDER        = new Color(0x000000);
    public static final Color LIGHT_SHADOW        = new Color(0x000000);
    public static final Color LIGHT_TEXT_PRIMARY   = new Color(0x0F0F0F);
    public static final Color LIGHT_TEXT_SECONDARY = new Color(0x5E4D7A);
    public static final Color LIGHT_INPUT_FILL    = new Color(0xFFFFFF);
    public static final Color LIGHT_MUTED_SURFACE = new Color(0xDED5F0);

    // ── Dark Mode Palette ───────────────────────────────────────────────────────
    public static final Color DARK_BACKGROUND     = new Color(0x1A1525);
    public static final Color DARK_SURFACE        = new Color(0x241D35);
    public static final Color DARK_BORDER         = new Color(0xFFFFFF);
    public static final Color DARK_SHADOW         = new Color(0x2D2040);
    public static final Color DARK_TEXT_PRIMARY    = new Color(0xF5F0FF);
    public static final Color DARK_TEXT_SECONDARY  = new Color(0xA89BC2);
    public static final Color DARK_INPUT_FILL     = new Color(0x2C2440);
    public static final Color DARK_MUTED_SURFACE  = new Color(0x302745);

    // ── Brand / Accent (same in both modes) ─────────────────────────────────────
    public static final Color PRIMARY_ACCENT      = new Color(0xFFE500); // Yellow
    public static final Color SECONDARY_ACCENT    = new Color(0xB388FF); // Lavender
    public static final Color DANGER              = new Color(0xFF3B3B); // Red
    public static final Color SUCCESS             = new Color(0x00C853); // Green
    public static final Color WARNING             = new Color(0xFF9F1C); // Orange

    // ── Strength Meter ──────────────────────────────────────────────────────────
    public static final Color STRENGTH_WEAK       = new Color(0xFF3B3B);
    public static final Color STRENGTH_FAIR       = new Color(0xFF9F1C);
    public static final Color STRENGTH_GOOD       = new Color(0xFFE500);
    public static final Color STRENGTH_STRONG     = new Color(0x00C853);
    public static final Color STRENGTH_EMPTY      = new Color(0x555555); // unused segment

    // ── Visual Tokens ───────────────────────────────────────────────────────────
    public static final int BORDER_THICKNESS      = 3;
    public static final int HEAVY_BORDER          = 4;
    public static final int SHADOW_OFFSET         = 6;
    public static final int CORNER_RADIUS         = 10;
    public static final int LARGE_CORNER_RADIUS   = 14;
    public static final int INPUT_HEIGHT          = 48;
    public static final int BUTTON_HEIGHT         = 46;
    public static final int CARD_PADDING          = 18;
    public static final int SCREEN_PADDING        = 28;
    public static final int VERTICAL_GAP          = 14;
    public static final int SECTION_GAP           = 22;
}
