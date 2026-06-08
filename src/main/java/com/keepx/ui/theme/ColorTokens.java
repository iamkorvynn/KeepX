package com.keepx.ui.theme;

import java.awt.Color;

/**
 * ColorTokens — centralized color palette for KeepX Neo-Brutalist UI.
 *
 * DARK PALETTE (v2 — proper neo-brutalist dark):
 *  Background : #1A1525  deep purple-black
 *  Surface    : #241D35  card / panel surface
 *  Border     : #FFFFFF  white — bold outlines on dark surfaces
 *  Shadow     : #2D2040  hard purple shadow
 *  Text Pri   : #F5F0FF  cream / soft lavender
 *  Text Sec   : #A89BC2  muted lavender
 *  Input fill : #1E1730  dark input background
 *  Muted surf : #302745  slightly lighter surface for hover/muted states
 *
 * LIGHT PALETTE (unchanged soft lavender):
 *  Background : #EDE7F6  soft lavender
 *  Surface    : #F8F5FF  near-white
 *  Border     : #000000  black
 *  Shadow     : #000000  black
 *  Text Pri   : #0F0F0F  near-black
 *  Text Sec   : #5E4D7A  muted purple
 *  Input fill : #FFFFFF  white
 *  Muted surf : #DED5F0  lavender tint
 * PRIMARY ACCENT  : #C4B5FD  soft lavender — buttons, active nav, toggle ON, focus rings
 * SECONDARY ACCENT: #9B7BFF  deeper lavender — badges, highlights
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

    // ── Dark Mode Palette — bold neo-brutalist dark ──────────────────────────────
    public static final Color DARK_BACKGROUND     = new Color(0x1A, 0x15, 0x25); // #1A1525 deep purple-black
    public static final Color DARK_SURFACE        = new Color(0x24, 0x1D, 0x35); // #241D35 card surface
    public static final Color DARK_BORDER         = new Color(0xFF, 0xFF, 0xFF); // #FFFFFF white outlines
    public static final Color DARK_SHADOW         = new Color(0x73, 0x56, 0xD6); // #7356D6 one shade darker than lavender shadow
    public static final Color DARK_TEXT_PRIMARY    = new Color(0xF5, 0xF0, 0xFF); // #F5F0FF cream lavender
    public static final Color DARK_TEXT_SECONDARY  = new Color(0xA8, 0x9B, 0xC2); // #A89BC2 muted lavender
    public static final Color DARK_INPUT_FILL     = new Color(0x1E, 0x17, 0x30); // #1E1730 dark input
    public static final Color DARK_MUTED_SURFACE  = new Color(0x30, 0x27, 0x45); // #302745 hover/muted

    // ── Brand / Accent ─────────────────────────────────────────────────────────
    // Lavender is PRIMARY — used for active nav, primary buttons, focus rings
    public static final Color PRIMARY_ACCENT      = new Color(0xC4, 0xB5, 0xFD); // #C4B5FD soft lavender
    public static final Color SECONDARY_ACCENT    = new Color(0x9B, 0x7B, 0xFF); // #9B7BFF deeper lavender
    public static final Color DANGER              = new Color(0xFF, 0x4D, 0x4D); // red
    public static final Color SUCCESS             = new Color(0x4A, 0xDE, 0x80); // green
    public static final Color WARNING             = new Color(0xFB, 0xBF, 0x24); // amber

    // ── Strength Meter ──────────────────────────────────────────────────────────
    public static final Color STRENGTH_WEAK       = new Color(0xFF4D4D);
    public static final Color STRENGTH_FAIR       = new Color(0xFBBF24);
    public static final Color STRENGTH_GOOD       = new Color(0xA3E635);
    public static final Color STRENGTH_STRONG     = new Color(0x4ADE80);
    public static final Color STRENGTH_EMPTY      = new Color(0x3A2E55); // dark purple-gray

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
