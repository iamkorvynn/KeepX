package com.keepx.ui.theme;

import java.awt.Color;

/**
 * ColorTokens — centralized color palette for KeepX Neo-Brutalist UI.
 * Components must NEVER hardcode colors — always use ThemeManager.get*() methods.
 *
 * UPDATED PALETTE:
 *  - Dark mode: true near-black (#0D0D0D bg, #1A1A1A surface)
 *  - Primary accent: Lavender (#C4B5FD) — replaces yellow
 *  - Secondary accent: Deeper lavender (#9B7BFF)
 */
public final class ColorTokens {

    private ColorTokens() {} // utility class

    // ── Light Mode Palette ──────────────────────────────────────────────────────
    public static final Color LIGHT_BACKGROUND    = new Color(0xEDE7F6); // soft lavender
    public static final Color LIGHT_SURFACE       = new Color(0xF8F5FF); // near-white
    public static final Color LIGHT_BORDER        = new Color(0x000000); // black border
    public static final Color LIGHT_SHADOW        = new Color(0x000000); // black shadow
    public static final Color LIGHT_TEXT_PRIMARY   = new Color(0x0F0F0F); // near-black
    public static final Color LIGHT_TEXT_SECONDARY = new Color(0x5E4D7A); // muted purple
    public static final Color LIGHT_INPUT_FILL    = new Color(0xFFFFFF); // white
    public static final Color LIGHT_MUTED_SURFACE = new Color(0xDED5F0); // lavender tint

    // ── Dark Mode Palette ───────────────────────────────────────────────────────
    // TRUE BLACK theme — no purple, no blue bleeding through
    public static final Color DARK_BACKGROUND     = new Color(0x0D0D0D); // near-black
    public static final Color DARK_SURFACE        = new Color(0x1A1A1A); // dark gray card
    public static final Color DARK_BORDER         = new Color(0xFFFFFF); // white border
    public static final Color DARK_SHADOW         = new Color(0x050505); // deep black shadow
    public static final Color DARK_TEXT_PRIMARY    = new Color(0xF0EAFF); // soft lavender-white
    public static final Color DARK_TEXT_SECONDARY  = new Color(0x9B8FBB); // muted lavender-gray
    public static final Color DARK_INPUT_FILL     = new Color(0x242424); // dark input
    public static final Color DARK_MUTED_SURFACE  = new Color(0x1F1F1F); // slightly lighter black

    // ── Brand / Accent ─────────────────────────────────────────────────────────
    // Lavender replaces yellow as the primary accent
    public static final Color PRIMARY_ACCENT      = new Color(0xC4B5FD); // lavender
    public static final Color SECONDARY_ACCENT    = new Color(0x9B7BFF); // deeper lavender
    public static final Color DANGER              = new Color(0xFF4D4D); // red
    public static final Color SUCCESS             = new Color(0x4ADE80); // green
    public static final Color WARNING             = new Color(0xFBBF24); // amber

    // ── Strength Meter ──────────────────────────────────────────────────────────
    public static final Color STRENGTH_WEAK       = new Color(0xFF4D4D); // red
    public static final Color STRENGTH_FAIR       = new Color(0xFBBF24); // amber
    public static final Color STRENGTH_GOOD       = new Color(0xA3E635); // yellow-green
    public static final Color STRENGTH_STRONG     = new Color(0x4ADE80); // green
    public static final Color STRENGTH_EMPTY      = new Color(0x3A3A3A); // dark gray

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
