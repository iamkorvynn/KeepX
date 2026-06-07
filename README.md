# 🔐 KeepX — Secure Offline Password Manager

KeepX is a secure, 100% offline desktop password manager built in Java Swing. It protects and organizes all your passwords locally on your machine using military-grade encryption, ensuring absolute privacy with zero cloud dependencies.

---

## ✨ Features

### 🔒 Uncompromising Security
*   **Local AES-256 Encryption**: All data is encrypted at rest using AES-256 in GCM mode, derived from your master password using **PBKDF2** (65,536 iterations).
*   **Zero-Knowledge Architecture**: Your master password is never stored; only its SHA-256 hash is kept for validation.
*   **Brute-Force Lockout**: Restricts login attempts (lockout for 30 seconds after 5 failed entries).
*   **Global Auto-Lock**: Automatically locks the vault after 5 minutes of keyboard or mouse inactivity.
*   **Clipboard Auto-Clear**: Automatically wipes copied passwords from the system clipboard 30 seconds after copying.

### 🎨 Premium UI/UX (Neo-Brutalism)
*   **Neo-Brutalist Aesthetic**: Clean interfaces featuring high-contrast borders, solid shadows, and modern lavender/yellow color palettes.
*   **Instant Dark & Light Mode**: Real-time theme switching without app restarts.
*   **Dashboard & Category Chips**: Sort and search passwords instantly by category (Social, Banking, Work, Shopping, etc.).
*   **Star & Pin**: Pin favorite entries to the top of your vault.
*   **Satisfying Micro-Animations**: Buttons animate with realistic press depth shifts.

### 🛠️ Integrated Utility Tools
*   **Password Generator**: Accessible inline or standalone, supporting custom length sliders, complexity options, and a session history.
*   **Security Auditor**: Scans credentials and flags weak (Fair/Weak), duplicate, or outdated (>90 days) passwords.
*   **Encrypted Backups**: Export a timestamped encrypted backup (`.kpx` file) to any chosen folder on your system.
*   **CSV Import**: Bulk-import existing passwords from Google Chrome or Mozilla Firefox exports.

---

## 🛠️ Tech Stack

*   **Language**: Java 17
*   **UI Framework**: Java Swing + [FlatLaf 3.4](https://github.com/JFormDesigner/FlatLaf)
*   **Serialization**: [Gson 2.10](https://github.com/google/gson)
*   **Build Tool**: Apache Maven
*   **Packaging**: Launch4j (compiles native `.exe` binaries)

---

## 🚀 Getting Started

### Prerequisites
*   **Java Development Kit (JDK) 17** or higher installed.

### Clone and Navigate
```bash
git clone https://github.com/iamkorvynn/KeepX.git
cd KeepX
```

### Compiling and Running
To compile the source code:
```bash
mvn compile
```

To compile and package the application into a runnable fat JAR and wrapped Windows EXE:
```bash
mvn package
```

### Running the Application
Once packaged, you can run the application directly from the target folder:
*   Double-click the native executable: `target/KeepX.exe`
*   Or launch the JAR via command line:
    ```bash
    java -jar target/KeepX.jar
    ```

---

## 📦 Distribution & Sharing

### Zero-Dependency Portable Folder
To share KeepX with someone who does not have Java installed, distribute a folder structured like this:
```text
KeepX/
  ├── KeepX.exe
  └── jre/            (A Java 17 Runtime Environment folder next to the EXE)
```
*Launch4j will automatically prioritize the bundled JRE inside this folder on startup.*
