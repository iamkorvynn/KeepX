# KeepX — Frontend UI/UX PRD
### Java Swing-Friendly Frontend Product Requirements Document · v1.0

| Field | Details |
|---|---|
| **Project Name** | KeepX — Secure Password Manager |
| **Document Type** | Frontend UI/UX PRD |
| **Platform** | Desktop (Java Swing) |
| **Design Style** | Neo-Brutalism |
| **Version** | v1.0 |
| **Timeline** | 5 Days |
| **Team Size** | 4 Members |
| **Primary Goal** | Build a visually memorable, highly usable, fully Java Swing-based password manager interface |

---

## 1. Purpose

This document defines the frontend UI/UX direction for KeepX, a desktop password manager built in Java Swing. The goal is to create an interface that feels bold, modern, and memorable while remaining practical to implement within a short academic timeline and fully compatible with Java Swing component architecture. [file:16]

The UI must support all core product requirements already defined for KeepX, including master-password login, password vault dashboard, add/edit flows, password generator, security audit, settings, search, favourites, copy-to-clipboard, dark mode, and auto-lock feedback. [file:16]

---

## 2. Frontend Goals

### 2.1 Primary Goals

- Create a distinctive visual identity that stands out during project evaluation.
- Keep the interface fully implementable in pure Java Swing without requiring web views or Android components.
- Build reusable custom UI components so the team can move quickly and maintain consistency.
- Support both light mode and dark mode with one centralized theme system.
- Make security-related actions feel clear, trustworthy, and easy to understand.

### 2.2 UX Goals

- New users should understand the app structure within 30 seconds of launch.
- The main dashboard should expose the most frequent actions immediately: search, copy, show/hide, add entry, and filter by category. [file:16]
- All core actions should be accessible with minimal clicks and clear visual states. [file:16]
- The UI should feel polished enough to impress in a classroom demo while still being realistic to code in 5 days. [file:16]

---

## 3. Design Principles

### 3.1 Core Principles

1. **Bold over bland** — the app should look intentionally designed, not like default Swing.
2. **Structured over chaotic** — neo-brutalism should be expressive, but content must remain readable.
3. **Reusable over custom-per-screen** — use a component system instead of styling each screen separately.
4. **Feedback over ambiguity** — every important action should have a visible response.
5. **Security first** — hidden passwords, warning colors, and confirmation dialogs must always be clear. [file:16]

### 3.2 Java Swing Constraints

The visual system must respect Java Swing limitations. Instead of relying on CSS-like styling, the design should be implemented through custom-painted components, central color constants, consistent spacing rules, and reusable classes such as custom buttons, cards, panels, badges, and inputs.

The UI should avoid effects that are difficult or fragile in Swing, such as heavy blur, glassmorphism, animated gradients, complex vector morphing, or deeply nested responsive layouts. The final design should favor bold flat fills, strong borders, hard shadows, and stable panel-based composition.

---

## 4. Visual Direction

### 4.1 Style Summary

KeepX will use a neo-brutalist visual language with bold borders, hard shadows, strong color contrast, chunky components, and playful but controlled emphasis. The app should feel unique and memorable rather than corporate or generic.

The design direction combines:
- Lavender-led identity for uniqueness.
- Yellow accent for energy and emphasis.
- Black or white borders for strong contrast.
- Large cards and bold typography for hierarchy.
- Slightly rounded corners to keep the app friendly instead of harsh.

### 4.2 Mood

The interface should feel:
- Bold
- Youthful
- Secure
- Intentional
- Modern
- Demo-friendly

It should not feel:
- Too childish
- Too decorative
- Too corporate
- Too minimal to the point of looking unfinished

---

## 5. Theme System

### 5.1 Theme Strategy

KeepX will support both light mode and dark mode through a centralized `ThemeManager` class. All components must pull colors, borders, typography, and state tokens from the same theme object so that switching theme does not require screen-by-screen restyling.

### 5.2 Color Palette

| Role | Light Mode | Dark Mode |
|---|---|---|
| **Background** | `#EDE7F6` | `#1A1525` |
| **Surface / Card** | `#F8F5FF` | `#241D35` |
| **Border** | `#000000` | `#FFFFFF` |
| **Shadow** | `#000000` | `#2D2040` |
| **Primary Accent** | `#FFE500` | `#FFE500` |
| **Secondary Accent** | `#B388FF` | `#B388FF` |
| **Danger** | `#FF3B3B` | `#FF3B3B` |
| **Success** | `#00C853` | `#00C853` |
| **Warning** | `#FF9F1C` | `#FF9F1C` |
| **Text Primary** | `#0F0F0F` | `#F5F0FF` |
| **Text Secondary** | `#5E4D7A` | `#A89BC2` |
| **Input Fill** | `#FFFFFF` | `#2C2440` |
| **Muted Surface** | `#DED5F0` | `#302745` |

### 5.3 Color Usage Rules

- Yellow is reserved for primary actions, active navigation, and important highlights.
- Lavender is used for brand identity, surfaces, chips, and secondary emphasis.
- Red is reserved for destructive actions, weak-password states, and critical alerts. [file:16]
- Green is reserved for strong-password states, success messages, and healthy security signals. [file:16]
- Avoid placing more than three strong accent colors in the same component group.

### 5.4 Visual Tokens

| Token | Value |
|---|---|
| **Border Thickness** | 3 px |
| **Heavy Border** | 4 px |
| **Shadow Offset** | 6 px right, 6 px down |
| **Corner Radius** | 10 px |
| **Large Corner Radius** | 14 px |
| **Input Height** | 46–50 px |
| **Primary Button Height** | 46 px |
| **Card Padding** | 16–20 px |
| **Screen Padding** | 24–32 px |
| **Vertical Gap** | 12–16 px |
| **Section Gap** | 20–24 px |

---

## 6. Typography

### 6.1 Font Direction

The preferred typeface is **Space Grotesk** because it matches the geometric and bold personality of neo-brutalism while still being readable in a desktop application. If bundling a custom font becomes difficult, the fallback should be Java `SansSerif` with carefully chosen bold and plain weights.

### 6.2 Type Scale

| Use | Size | Weight |
|---|---|---|
| **App Branding** | 34–40 px | Bold |
| **Screen Title** | 24–28 px | Bold |
| **Section Title** | 18–20 px | Bold |
| **Card Title** | 16–18 px | Bold |
| **Body Text** | 14–16 px | Plain / Medium |
| **Metadata** | 12–13 px | Plain |
| **Button Label** | 14–15 px | Bold |

### 6.3 Typography Rules

- Keep headings short and strong.
- Use sentence case instead of all caps for most UI labels.
- Reserve larger type for one main title per screen.
- Metadata such as “Last modified” should be quieter than primary content. [file:16]

---

## 7. Layout Architecture

### 7.1 Technical Layout Strategy

The frontend should be built using:
- `JFrame` as the main application window.
- `JLayeredPane` for layered content and floating navigation.
- `CardLayout` for switching between major screens.
- Custom-painted Swing components for buttons, cards, input fields, tabs, and badges.
- Scrollable content panels where entry lists or settings sections can grow vertically.

This structure is Java Swing-friendly, easy to maintain, and well suited for custom UI styling.

### 7.2 Global Layout Model

Each major screen should follow this overall structure:
1. Background canvas.
2. Header area for title and contextual actions.
3. Main content panel.
4. Floating bottom navigation pill.
5. Optional toast layer or modal dialog layer.

### 7.3 Responsiveness

KeepX is a desktop-first app and does not need full responsive web behavior. However, the UI should still adapt gracefully to window resizing by:
- Maintaining minimum widths for cards and forms.
- Allowing content sections to scroll vertically.
- Re-centering the floating navigation on resize.
- Using layout managers for major sections instead of hardcoded absolute positions where possible.

Recommended default window size:
- 1280 x 800 for development and demo.
- Minimum supported size: 1100 x 700.

---

## 8. Navigation Model

### 8.1 Chosen Pattern

KeepX will use a **floating bottom pill navigation bar** centered horizontally near the bottom of the window. This is the primary navigation pattern and a major part of the product’s distinctive identity.

### 8.2 Navigation Items

The floating navigation contains five destinations:
- Vault
- Add Entry
- Generator
- Audit
- Settings

These destinations map directly to the core product flows in the main PRD. [file:16]

### 8.3 Navigation States

- **Active**: yellow fill, strong border, shadow, bold icon treatment.
- **Inactive**: surface fill, visible border, no emphasis.
- **Hover**: slight positional lift or stronger shadow.
- **Pressed**: reduced shadow offset to simulate click depth.
- **Disabled**: muted fill and muted text/icon color.

### 8.4 Navigation UX Rules

- Labels should remain visible under icons or beside icons if space allows.
- Active destination must be obvious at first glance.
- Navigation should never cover critical content; maintain bottom safe spacing.
- The nav bar remains visible across all post-login screens.
- Login and first-time setup screens do not show the floating nav.

---

## 9. Screen Specifications

### 9.1 Splash / First-Time Setup

**Purpose**  
Introduce the app on first launch and allow the user to create a master password. [file:16]

**Layout**  
- Centered main card.
- Large KeepX title and short supporting line.
- Two password fields: master password and confirm password.
- Strength meter below password field.
- Security note explaining that the vault cannot be recovered without the master password. [file:16]
- Primary button: “Create Vault”.

**UX Notes**  
- Show clear validation for mismatch or weak passwords.
- Use a calm but bold composition, not a busy one.
- This screen should feel trustworthy and important.

### 9.2 Login Screen

**Purpose**  
Unlock the existing vault using the master password. [file:16]

**Layout**  
- Centered authentication card.
- App logo or branded icon.
- Master password input.
- Show/hide password button.
- Unlock button.
- Error text area for wrong password feedback. [file:16]
- Optional failed-attempt counter if implemented.

**UX Notes**  
- Input focus should be obvious.
- Wrong password messages must be human-readable.
- The screen should remain minimal and fast to use.

### 9.3 Dashboard / Vault

**Purpose**  
Browse, search, filter, favorite, and interact with stored password entries. [file:16]

**Layout Zones**  
- Top header row with screen title and utility actions.
- Search bar near the top. [file:16]
- Category chips or tabs below search. [file:16]
- Pinned/favourite cards section at top. [file:16]
- Main vertical list of regular entries below.
- Floating Add button near upper-right content area.

**Entry Card Content**  
Each entry should include:
- Site/app name. [file:16]
- Username/email. [file:16]
- Category badge. [file:16]
- Last modified date. [file:16]
- Copy password action. [file:16]
- Show/hide password action. [file:16]
- Edit action. [file:16]
- Favourite action. [file:16]

**UX Notes**  
- Pinned entries should feel larger and more visual.
- Regular entries can be more compact for density.
- Empty states must be styled and intentional, not just blank space. [file:16]
- Search results should update in real time. [file:16]

### 9.4 Add / Edit Entry Screen

**Purpose**  
Create or update password records in the vault. [file:16]

**Layout**  
Use a two-column desktop form layout:
- Left column: Site/App Name, Username/Email, Password, URL, Category.
- Right column: Notes, Password Strength, Generate Password CTA, helpful tips.
- Bottom action row: Save, Cancel, Delete (for edit mode). [file:16]

**Required Fields**  
- Site/App Name [file:16]
- Username/Email [file:16]
- Password [file:16]
- Category [file:16]
- Notes optional [file:16]

**UX Notes**  
- Password is hidden by default. [file:16]
- Strength meter updates live. [file:16]
- Validation should appear inline, not only in blocking dialogs.
- Delete action should be visually separated and require confirmation. [file:16]

### 9.5 Password Generator

**Purpose**  
Help users generate strong passwords with clear controls and one-click copy. [file:16]

**Layout**  
- Large generated password display panel.
- Length slider.
- Toggle controls for uppercase, lowercase, numbers, symbols. [file:16]
- Generate button.
- Copy button. [file:16]
- Strength indicator below the generated output.

**UX Notes**  
- This screen should feel playful but controlled.
- Generated password should be easy to read and copy.
- Settings must be obvious even to non-technical users.

### 9.6 Security Audit

**Purpose**  
Review weak, duplicate, and old passwords and guide the user to fix them. [file:16]

**Layout**  
- Top summary stat cards: weak, duplicate, old. [file:16]
- List of flagged entries below. [file:16]
- Each row includes site/app, issue label, modified date, and fix action. [file:16]
- Use colored issue badges for visual scanning.

**UX Notes**  
- The screen should feel informative, not scary.
- Danger colors should be used precisely.
- Clicking “Fix” should open edit flow directly if implemented. [file:16]

### 9.7 Settings

**Purpose**  
Manage theme, backup/restore, import, and master-password settings. [file:16]

**Layout**  
Use grouped section cards:
- Appearance
- Security
- Import / Export
- Backup / Restore
- About

**Expected Actions**  
- Toggle dark/light mode. [file:16]
- Change master password. [file:16]
- Import from CSV. [file:16]
- Backup vault. [file:16]
- Restore vault. [file:16]

**UX Notes**  
- Group settings by purpose.
- Destructive or sensitive actions need clear confirmation.
- Avoid overloading this screen visually.

---

## 10. Component Library

### 10.1 Required Custom Components

The frontend should be built around a reusable component system. Recommended custom classes:
- `NeoPanel`
- `NeoCard`
- `NeoButton`
- `NeoIconButton`
- `NeoTextField`
- `NeoPasswordField`
- `NeoTextArea`
- `NeoBadge`
- `NeoChip`
- `NeoToggle`
- `NeoTabBar`
- `NeoNavBar`
- `NeoToast`
- `NeoDialog`
- `NeoStrengthMeter`

### 10.2 Common Component States

Every interactive component should support as relevant:
- Default
- Hover
- Pressed
- Focused
- Disabled
- Error
- Success
- Active / Selected

### 10.3 Styling Rules by Component

**Buttons**
- Primary buttons use yellow fill.
- Secondary buttons use surface or lavender fill.
- Destructive buttons use red fill.
- Buttons have strong borders and hard shadows.

**Inputs**
- Inputs should be tall and spacious.
- Default fill should contrast clearly with background.
- Focus state should strengthen border or add accent outline.
- Error state should show red border and helper text.

**Cards**
- Cards use surface color with border and shadow.
- Pinned cards can use a lavender-tinted or yellow-tinted variation.
- Card padding must remain consistent across screens.

**Chips / Tabs**
- Category chips must be easily clickable.
- Selected chip uses yellow fill or stronger border emphasis.
- Chip labels should remain readable at a glance.

**Badges**
- Use for category, password state, or issue labels.
- Keep badges short and color-consistent.

---

## 11. Interaction Design

### 11.1 Feedback Patterns

The app must provide visible feedback for key actions:
- Copy password → toast message with clipboard-clear note. [file:16]
- Save entry → success toast or inline confirmation.
- Delete entry → confirmation dialog. [file:16]
- Wrong master password → inline error message. [file:16]
- Auto-lock warning → visible timer or banner. [file:16]
- Search with no matches → designed empty state. [file:16]

### 11.2 Motion Guidance

Animations should be minimal because this is Java Swing and the build timeline is short. Use only lightweight motion where it improves usability:
- Button press depth effect.
- Soft toast entry/exit if easy to implement.
- Hover lift on cards or nav items.
- Theme switch without complex animation.

### 11.3 Dialog Rules

Use custom dialogs for:
- Delete confirmation. [file:16]
- Restore confirmation. [file:16]
- Change master password flow. [file:16]
- Error alerts for unrecoverable issues.

Dialogs should match the same visual system as the main app and avoid default Swing dialog styling where possible.

---

## 12. Accessibility and Usability

### 12.1 Readability

- Maintain strong contrast between text and background in both themes.
- Avoid placing yellow text on white or cream surfaces.
- Metadata text must remain readable, not too faint.

### 12.2 Input Usability

- Labels should be clear and consistent.
- Interactive controls must be large enough for comfortable desktop use.
- Keyboard focus states should be clearly visible.
- Primary form actions should be easy to discover.

### 12.3 Error Prevention

- Sensitive actions need confirmation.
- Required fields should be visually marked.
- Validation should happen early, not only after submit.
- Password fields should default to hidden. [file:16]

---

## 13. Java Swing Implementation Guidance

### 13.1 Friendly Patterns

To keep the design efficient and Java Swing-friendly, the frontend should prefer:
- Custom `paintComponent()` for shadows, fills, and borders.
- `BorderLayout`, `BoxLayout`, `GridBagLayout`, and `CardLayout` for stable composition.
- `JScrollPane` for vertically growing content areas.
- Simple icons or vector-like line icons instead of detailed illustrations.
- Centralized constants for spacing, radius, and color tokens.

### 13.2 Patterns to Avoid

Avoid these because they increase complexity or look fragile in Swing:
- Heavy absolute positioning for the full app.
- Deeply nested panels without layout discipline.
- Overuse of transparency and alpha overlays.
- Large image-based UI pieces for basic components.
- Very complex animations or transitions.
- Unstyled default `JTable` as the main vault view if a custom list/card approach is planned.

### 13.3 Suggested Package Structure

```text
ui/
  theme/
    Theme.java
    ThemeManager.java
    ColorTokens.java
    SpacingTokens.java
  components/
    NeoButton.java
    NeoTextField.java
    NeoCard.java
    NeoNavBar.java
    NeoToast.java
    NeoStrengthMeter.java
  screens/
    SetupScreen.java
    LoginScreen.java
    DashboardScreen.java
    EntryFormScreen.java
    GeneratorScreen.java
    AuditScreen.java
    SettingsScreen.java
  layout/
    MainFrame.java
    ScreenRouter.java
```

---

## 14. Frontend Scope for v1

### 14.1 Must-Have UI Elements

- Branded login/setup screen. [file:16]
- Floating bottom pill navigation.
- Dashboard with search, categories, pinned cards, and entry list. [file:16]
- Add/Edit form with strength meter and show/hide behavior. [file:16]
- Generator screen. [file:16]
- Audit screen. [file:16]
- Settings screen with theme toggle and security actions. [file:16]
- Light and dark theme support. [file:16]
- Toasts, dialogs, and validation states.

### 14.2 Nice-to-Have UI Enhancements

- Custom icon set.
- Animated splash screen.
- Lightweight hover transitions.
- Better empty states with illustrations or symbols.
- Keyboard shortcuts.

---

## 15. Acceptance Criteria

The frontend will be considered successful if:

- The app clearly looks custom-designed rather than default Swing.
- The neo-brutalist identity is visible in both light and dark mode.
- Navigation is obvious and consistent across all post-login screens.
- Users can search, browse, add, edit, and copy entries without confusion. [file:16]
- All required screens from the core product PRD are implemented in a visually consistent way. [file:16]
- Theme switching works without breaking readability or component contrast. [file:16]
- The design remains practical to implement using Java Swing within the given timeline. [file:16]

---

## 16. Final Direction

KeepX frontend will use a Java Swing-friendly neo-brutalist system built around lavender surfaces, yellow emphasis, strong borders, hard shadows, slightly rounded corners, and a floating bottom pill navigation model. This direction balances uniqueness, usability, and implementation feasibility while supporting the complete KeepX feature set defined in the product PRD. [file:16]

---

*KeepX — Frontend UI/UX PRD v1.0 | Java Swing College Project*
