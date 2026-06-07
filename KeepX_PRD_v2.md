# KeepX — Secure Password Manager
### Product Requirements Document (PRD) · v2.0

| Field | Details |
|---|---|
| **Project Name** | KeepX — Secure Password Manager |
| **Platform** | Desktop (Java Swing) |
| **Version** | v2.0 |
| **Timeline** | 5 Days |
| **Team Size** | 4 Members |
| **IDE** | Antigravity (VS Code fork) |
| **Build Tool** | Maven |

---

## 1. Project Overview

### 1.1 What is KeepX?

KeepX is an offline desktop password manager built with Java Swing that allows users to securely store, organize, and manage all their passwords in one place. All data is encrypted locally on the user's device using AES-256 encryption, ensuring complete privacy with zero cloud dependency. The app runs entirely on the user's machine — no internet connection is ever required.

### 1.2 Problem Statement

People today manage dozens of online accounts across banking, social media, shopping, work, and entertainment. Most people either reuse the same weak password everywhere or forget passwords constantly. Existing solutions either require cloud access (LastPass, Bitwarden) or are too complex for everyday users. There is no safe, simple, fully offline tool for a regular person to manage passwords without trusting a third-party server.

### 1.3 Solution

KeepX provides a secure local vault protected by a single master password. Users only need to remember one password — KeepX remembers the rest, encrypted and safe on their own machine. The vault file is completely unreadable without the correct master password.

### 1.4 Target Users

- Students managing college portals, email, and social accounts
- Working professionals with multiple work and personal accounts
- Families managing shared subscriptions and services
- Anyone who wants passwords safe without trusting a cloud service

### 1.5 Key Differentiators

- 100% offline — no internet required, ever
- AES-256 encryption — military-grade, industry standard
- Zero knowledge — master password is never stored, only its hash
- Portable — runs as a single JAR or EXE, no installation needed

---

## 2. Goals & Success Criteria

### 2.1 Primary Goals

- Build a fully functional, secure local password manager as a Java Swing desktop app
- Implement AES-256 + PBKDF2 encryption so all stored data is protected at rest
- Deliver a clean, modern UI (using FlatLaf) that any user can navigate without a tutorial
- Complete and deliver within 5 days as a group of 4

### 2.2 Success Criteria

| Criteria | Target |
|---|---|
| App opens and locks with master password | Working on all Windows 10+ machines |
| Passwords stored and retrieved correctly | Zero data loss on save/load |
| AES-256 encryption active | Vault file unreadable without master password |
| Password generator works | Generates strong passwords on demand |
| Security audit runs | Flags weak, duplicate, and old passwords |
| Auto-lock activates | Vault locks after 5 minutes of inactivity |
| Exportable as JAR/EXE | Runs without IDE on any Windows machine |
| UI loads in under 3 seconds | Verified on low-spec hardware |

---

## 3. Features & Requirements

### 3.1 Core Features

#### 3.1.1 Master Password & Login Screen

- First launch: user creates a master password with a strength indicator
- Subsequent launches: user enters master password to unlock vault
- Master password is never stored — only its SHA-256 hash is saved for verification
- Wrong password: show error with attempt counter (lock after 5 failed attempts for 30 seconds)
- Forgot password: vault cannot be recovered (by design — this is a security feature, displayed clearly to user on setup)
- Animated logo/splash on first load for a polished feel

#### 3.1.2 Password Vault Dashboard

- Displays all saved password entries in a clean card list
- Entries organized by category tabs: All, Social, Banking, Work, Shopping, Entertainment, Other
- Each entry card shows: site/app name, username, category badge, last modified date
- Password field hidden by default, shown only on toggle
- Pinned/favourite entries appear at the top of the list
- Entry count shown per category tab
- Empty state shown clearly when no entries exist in a category

#### 3.1.3 Add / Edit / Delete Entry

- Form fields: Site/App Name, URL (optional), Username/Email, Password, Category, Notes
- Real-time password strength meter shown while typing
- "Generate Password" button inline in the form — opens generator without leaving the screen
- Save button encrypts and stores entry to local vault file
- Edit opens pre-filled form for that entry
- Delete shows a confirmation dialog before removing
- Form validates all required fields before saving

#### 3.1.4 Search & Filter

- Search bar on dashboard filters entries in real time as user types
- Searches across site name, username, URL, and notes
- Category filter tabs work alongside search (search within a category)
- "No results" empty state shown with a clear message
- Search is case-insensitive

---

### 3.2 Convenience Features

#### 3.2.1 Copy to Clipboard

- One-click copy button next to username and password on each entry
- Password copied without being displayed on screen
- Auto-clears clipboard after 30 seconds for security
- Toast notification shown: "Copied! Clears in 30s" with a countdown

#### 3.2.2 Show / Hide Password Toggle

- Eye icon next to password field in entry view and add/edit form
- Toggles between hidden (bullet dots) and visible (plain text)
- Defaults to hidden every time the entry is opened

#### 3.2.3 Password Strength Meter

- Real-time strength indicator while typing a new password
- Four levels: Weak (red), Fair (orange), Good (yellow), Strong (green)
- Based on: length ≥ 12, uppercase letters, numbers, and symbols
- Shown both in the Add/Edit form and inside the Password Generator

#### 3.2.4 Favourite / Pin Entries

- Star icon on each entry card to mark as favourite
- Favourited entries pinned to top of the vault list
- Favourites persisted across sessions

#### 3.2.5 Notes Field

- Each entry has an optional multi-line notes field
- Useful for security questions, backup codes, and account recovery info
- Notes are encrypted along with the rest of the entry

#### 3.2.6 Last Modified Date

- Each entry automatically records and displays when it was last updated
- Used by the Security Audit to flag passwords older than 90 days

#### 3.2.7 URL Field & Quick Launch

- Each entry has an optional URL field
- "Open" button next to URL launches the website in the default browser
- Allows one-click access to the login page

---

### 3.3 Security Features

#### 3.3.1 AES-256 Encryption

- All vault data encrypted using AES-256 before writing to disk
- Master password used as key derivation input via PBKDF2WithHmacSHA256
- Salt stored separately from vault data to prevent rainbow table attacks
- Vault file uses `.kpx` extension and is fully unreadable without correct master password

#### 3.3.2 Auto Lock

- App automatically locks vault after 5 minutes of inactivity
- Inactivity is detected via mouse movement and keyboard events
- Countdown warning shown in the status bar at 1 minute remaining
- User must re-enter master password to unlock

#### 3.3.3 Login Attempt Throttling

- After 5 consecutive failed login attempts, app locks for 30 seconds
- Attempt counter and lock timer displayed clearly on login screen
- Prevents brute force attacks on the master password

#### 3.3.4 Security Audit

- Scans entire vault and flags:
  - Weak passwords (strength meter score: Weak or Fair)
  - Duplicate passwords (same password used for multiple entries)
  - Old passwords (not updated in 90+ days)
- Shows a summary report: total issues, breakdown by type
- Each flagged entry is clickable — opens the Edit form directly
- "Fix All Weak" shortcut opens all weak entries in tabs

#### 3.3.5 Change Master Password

- Available in Settings
- Requires current master password verification before allowing change
- Re-encrypts entire vault with the new master password
- Shows progress indicator during re-encryption

---

### 3.4 Tools

#### 3.4.1 Password Generator

- Accessible from the dashboard toolbar and from inside the Add/Edit form
- Options: password length slider (8–64), include uppercase, lowercase, numbers, symbols
- One-click generate with live preview of generated password
- One-click copy to clipboard
- Strength meter shown for generated password
- History of last 5 generated passwords shown (session only, not saved)

#### 3.4.2 Import from CSV

- Supports CSV exports from Chrome and Firefox
- CSV format: `name, url, username, password`
- App parses and bulk-imports all entries into vault
- Duplicate detection: warns user if an entry already exists (by site name + username)
- Import summary shown after completion: X imported, Y skipped

#### 3.4.3 Backup and Restore

- Export: saves encrypted `.kpx` vault file to user-chosen location
- Restore: loads a `.kpx` vault file from user-chosen location
- Backup file is encrypted — safe to store in cloud, USB, or email to self
- Last backup date shown in Settings

---

### 3.5 UI & Theme

#### 3.5.1 FlatLaf Modern Theme

- Uses FlatLaf library (`com.formdev:flatlaf:3.4`) for a modern Swing UI
- Replaces default ugly Swing look with a clean, flat, professional appearance
- One line of code: `FlatDarkLaf.setup()` or `FlatLightLaf.setup()`

#### 3.5.2 Dark Mode / Light Mode

- Toggle switch in the top navigation bar
- App re-renders immediately on toggle without restart
- Default: system preference detected on first launch
- Preference saved to a local config file between sessions

#### 3.5.3 Consistent Navigation Bar

- All screens share a top navigation bar with:
  - KeepX logo (left)
  - Current screen title (center)
  - Dark/light mode toggle, auto-lock timer, settings icon (right)

---

## 4. Screen Layouts

KeepX consists of 7 main screens. All screens share the consistent top navigation bar described above.

| Screen | Key Elements | Primary Action |
|---|---|---|
| **Splash / Setup** | App logo animation, first-time master password creation | Create master password |
| **Login Screen** | Master password input, attempt counter, app logo | Unlock vault |
| **Dashboard** | Search bar, category tabs, entry card list, add button | Browse & search passwords |
| **Add / Edit Entry** | Form fields, strength meter, inline generator, notes | Save new or updated entry |
| **Password Generator** | Options panel, generated password preview, history | Generate & copy password |
| **Security Audit** | Summary stats, flagged entries list, issue type labels, fix buttons | Review and fix weak passwords |
| **Settings** | Change master password, theme toggle, import CSV, backup/restore, last backup date | Manage app preferences |

---

## 5. Technical Stack

| Component | Technology | Purpose |
|---|---|---|
| UI Framework | Java Swing + FlatLaf 3.4 | All screens, modern flat theme, dark/light mode |
| Encryption | AES-256 + PBKDF2WithHmacSHA256 (Java built-in) | Encrypt/decrypt vault data |
| Local Storage | Encrypted JSON file (`.kpx`) | Store all password entries |
| JSON Parsing | Gson 2.10 | Read/write vault data as JSON |
| Password Hashing | SHA-256 (Java built-in) | Verify master password |
| Build & Dependency | Maven | Manage Gson + FlatLaf dependencies |
| Export | JAR via Maven + Launch4j | Package as runnable JAR and Windows EXE |
| IDE | Antigravity (VS Code fork) | Development environment with AI agent |
| Version Control | Git + GitHub | Collaboration, branching, and final submission |

### 5.1 Maven Dependencies (pom.xml)

```xml
<!-- FlatLaf — Modern Swing UI theme -->
<dependency>
    <groupId>com.formdev</groupId>
    <artifactId>flatlaf</artifactId>
    <version>3.4</version>
</dependency>

<!-- Gson — JSON parsing for vault file -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

---

## 6. Team Responsibilities

| Member | Area | Responsibilities |
|---|---|---|
| **Person 1** | Security & Auth | Login screen UI, master password setup flow, AES-256 encryption/decryption logic, PBKDF2 key derivation, salt generation, SHA-256 hash verification, auto-lock timer, login attempt throttling, change master password |
| **Person 2** | Dashboard & UI | Main dashboard layout, category tabs, entry card list, search bar real-time filtering, favourites/pin logic, FlatLaf theme setup, dark/light mode toggle, navigation bar component, empty states |
| **Person 3** | Entry Management | Add/edit/delete entry forms, URL field + quick launch, notes field, show/hide password toggle, password strength meter, last modified date tracking, local vault file read/write with Gson, splash screen |
| **Person 4** | Tools & Audit | Password generator screen + inline generator, security audit screen + logic, copy to clipboard + auto-clear timer, toast notifications, CSV import, backup and restore, settings screen |

---

## 7. 5-Day Development Timeline

| Day | Phase | Tasks |
|---|---|---|
| **Day 1** | Setup & Foundation | GitHub repo setup, Maven project structure, add FlatLaf + Gson to `pom.xml`, agree on color scheme, create shared UI constants file, build login screen + master password setup, implement AES-256 encryption skeleton, draw all screen wireframes |
| **Day 2** | Core Screens | Dashboard layout with category tabs, entry card component, vault file read/write (encrypted JSON), add/edit entry form, password strength meter |
| **Day 3** | Feature Layer | Search + real-time filter, show/hide toggle, copy to clipboard + auto-clear, password generator (standalone screen), favourites/pin, URL quick launch |
| **Day 4** | Advanced Features | Security audit screen + logic, auto-lock timer, login throttling, backup/restore, CSV import, settings screen, dark/light mode toggle, change master password |
| **Day 5** | Polish & Delivery | Bug fixes, UI polish (spacing, icons, colours), splash screen animation, README writing, JAR export via Maven, EXE packaging with Launch4j, final GitHub push, demo rehearsal |

### 7.1 GitHub Branching Strategy

- `main` — always working, demo-ready code only
- `dev` — integration branch, all members merge here first
- `feature/person1-auth`, `feature/person2-dashboard`, etc. — individual work branches
- Merge to `dev` at end of each day, merge `dev` → `main` on Day 5

---

## 8. Non-Functional Requirements

### 8.1 Security

- No passwords stored in plain text — ever
- Master password never stored, only its SHA-256 hash
- PBKDF2 salt regenerated on every vault creation and master password change
- Clipboard auto-cleared after 30 seconds
- Vault auto-locks after 5 minutes of inactivity
- Login throttled after 5 failed attempts

### 8.2 Performance

- App launches and shows login screen in under 3 seconds
- Search results appear within 100ms of each keystroke
- Vault save/load completes in under 1 second for up to 500 entries
- Theme switch (dark/light) completes without visible lag

### 8.3 Usability

- New user can add their first password within 2 minutes of opening the app
- All key actions accessible within 2 clicks from the dashboard
- Every error state shows a clear, human-readable message (never raw Java exceptions)
- All form fields have placeholder text explaining expected input

### 8.4 Compatibility

- Runs on Windows 10 and above
- Requires Java 11 or above (or bundled JRE in EXE version via Launch4j)
- No internet connection required for any feature — fully offline

---

## 9. Out of Scope (v1.0)

The following features are intentionally excluded from v1.0 to keep the project achievable within 5 days:

- Cloud sync or online backup
- Mobile version (Android/iOS)
- Browser extension integration
- Two-factor authentication (2FA)
- Multi-user or shared vaults
- Automatic password change for websites
- macOS or Linux native packaging
- Biometric unlock (fingerprint)

These are strong candidates for a v2.0 iteration after submission.

---

## 10. Risk Register

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| AES encryption bugs corrupt vault | Medium | High | Test encrypt/decrypt in isolation on Day 1 before integrating |
| Gson serialization issues with encrypted data | Low | High | Test save/load with dummy entries before building UI on top |
| FlatLaf conflicts with custom Swing components | Low | Medium | Test FlatLaf setup on Day 1 with a blank JFrame |
| CSV import edge cases break parser | Medium | Low | Mark as bonus feature — skip if running low on time Day 4 |
| Launch4j EXE packaging fails on deadline | Medium | Medium | Prioritise working JAR first — EXE is a bonus |
| Merge conflicts between team members | High | Medium | Use feature branches + merge to `dev` daily, not directly to `main` |

---

## 11. Glossary

| Term | Definition |
|---|---|
| **AES-256** | Advanced Encryption Standard with 256-bit key — industry standard symmetric encryption used by banks and governments |
| **PBKDF2** | Password-Based Key Derivation Function — converts master password into a secure encryption key, with salt to prevent precomputed attacks |
| **SHA-256** | Secure Hash Algorithm — stores a fingerprint of the master password without storing the password itself |
| **Salt** | Random data added to the master password before hashing — prevents identical passwords from producing identical hashes |
| **Vault** | The encrypted local `.kpx` file where all password entries are stored |
| **Master Password** | The single password the user must remember to unlock the entire vault |
| **FlatLaf** | A modern open-source look-and-feel library for Java Swing that replaces the default UI with a clean, flat appearance |
| **JAR** | Java ARchive — compiled Java application that runs on any machine with Java installed |
| **EXE** | Windows executable — JAR wrapped with Launch4j for one-click launch without needing Java knowledge |
| **Gson** | Google's Java library for converting Java objects to/from JSON format |
| **PBKDF2WithHmacSHA256** | The specific PBKDF2 algorithm variant used — combines PBKDF2 with HMAC-SHA256 for stronger key derivation |

---

*KeepX — Secure Password Manager | PRD v2.0 | Java Swing College Project*
