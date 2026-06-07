package com.keepx.security;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.keepx.model.VaultEntry;

import javax.crypto.SecretKey;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.security.*;
import java.util.*;

/**
 * VaultManager — singleton managing all vault file I/O.
 *
 * Vault location:  ~/.keepx/vault.kpx   (AES-256-GCM encrypted JSON)
 * Config location: ~/.keepx/config.json  (salt, password hash, preferences)
 *
 * Lifecycle:
 *   createVault(password)  → first-time setup
 *   unlock(password)       → derive key, decrypt vault into memory
 *   lock()                 → clear key + entries from memory
 *   save()                 → re-encrypt and write vault.kpx
 */
public class VaultManager {

    private static VaultManager instance;

    // ── File locations ────────────────────────────────────────────────────────────
    private static final String DIR_NAME    = ".keepx";
    private static final String VAULT_FILE  = "vault.kpx";
    private static final String CONFIG_FILE = "config.json";

    private final Path vaultDir;
    private final Path vaultPath;
    private final Path configPath;

    // ── Runtime state ─────────────────────────────────────────────────────────────
    private SecretKey       activeKey   = null;
    private List<VaultEntry> entries    = new ArrayList<>();
    private boolean          unlocked   = false;
    private Config           config     = null;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // ── Config POJO ───────────────────────────────────────────────────────────────
    private static class Config {
        String passwordHash;   // SHA-256 hex of master password
        String saltBase64;     // Base64-encoded PBKDF2 salt
        String lastBackupDate; // ISO date string
        boolean isDarkMode;
    }

    private VaultManager() {
        String home   = System.getProperty("user.home");
        vaultDir      = Paths.get(home, DIR_NAME);
        vaultPath     = vaultDir.resolve(VAULT_FILE);
        configPath    = vaultDir.resolve(CONFIG_FILE);
    }

    public static VaultManager getInstance() {
        if (instance == null) instance = new VaultManager();
        return instance;
    }

    // ── Setup ─────────────────────────────────────────────────────────────────────

    public boolean vaultExists() {
        return Files.exists(vaultPath) && Files.exists(configPath);
    }

    /**
     * First-time setup: generate salt, hash password, create empty vault.
     */
    public void createVault(char[] masterPassword) throws Exception {
        Files.createDirectories(vaultDir);

        byte[] salt = EncryptionManager.generateSalt();
        String hash = EncryptionManager.hashPassword(masterPassword);

        config = new Config();
        config.saltBase64  = EncryptionManager.encodeSalt(salt);
        config.passwordHash = hash;
        config.isDarkMode   = false;

        writeConfig();

        // Derive key and create empty vault
        activeKey = EncryptionManager.deriveKey(masterPassword, salt);
        entries   = new ArrayList<>();
        unlocked  = true;

        saveVault();
    }

    // ── Unlock / Lock ─────────────────────────────────────────────────────────────

    /**
     * Attempts to unlock the vault. Returns true if password is correct.
     */
    public boolean unlock(char[] masterPassword) throws Exception {
        if (!vaultExists()) return false;

        loadConfig();

        String providedHash = EncryptionManager.hashPassword(masterPassword);
        if (!providedHash.equals(config.passwordHash)) {
            return false;
        }

        byte[] salt = EncryptionManager.decodeSalt(config.saltBase64);
        activeKey   = EncryptionManager.deriveKey(masterPassword, salt);

        loadVault();
        unlocked = true;
        return true;
    }

    /**
     * Locks the vault — clears key and entries from memory.
     */
    public void lock() {
        activeKey = null;
        entries.clear();
        unlocked = false;
    }

    public boolean isUnlocked() { return unlocked; }

    // ── CRUD ──────────────────────────────────────────────────────────────────────

    public List<VaultEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public void addEntry(VaultEntry entry) throws Exception {
        requireUnlocked();
        entries.add(entry);
        saveVault();
    }

    public void updateEntry(VaultEntry entry) throws Exception {
        requireUnlocked();
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getId().equals(entry.getId())) {
                entries.set(i, entry);
                saveVault();
                return;
            }
        }
        throw new IllegalArgumentException("Entry not found: " + entry.getId());
    }

    public void deleteEntry(String id) throws Exception {
        requireUnlocked();
        entries.removeIf(e -> e.getId().equals(id));
        saveVault();
    }

    // ── Password encryption helpers ───────────────────────────────────────────────

    /** Encrypts a plain-text password and stores it inside the entry. */
    public void encryptEntryPassword(VaultEntry entry, String plainPassword) throws Exception {
        requireUnlocked();
        String encrypted = EncryptionManager.encrypt(plainPassword, activeKey);
        entry.setEncryptedPassword(encrypted);
    }

    /** Decrypts the password stored inside an entry. Returns plain text. */
    public String decryptEntryPassword(VaultEntry entry) throws Exception {
        requireUnlocked();
        return EncryptionManager.decryptString(entry.getEncryptedPassword(), activeKey);
    }

    // ── Master Password Change ────────────────────────────────────────────────────

    public void changeMasterPassword(char[] currentPassword, char[] newPassword) throws Exception {
        requireUnlocked();

        // Verify current password
        String currentHash = EncryptionManager.hashPassword(currentPassword);
        if (!currentHash.equals(config.passwordHash)) {
            throw new SecurityException("Current password is incorrect.");
        }

        // Re-derive with new password and new salt
        byte[] newSalt = EncryptionManager.generateSalt();
        SecretKey newKey  = EncryptionManager.deriveKey(newPassword, newSalt);
        String newHash    = EncryptionManager.hashPassword(newPassword);

        // Re-encrypt all entry passwords with new key
        List<VaultEntry> reEncrypted = new ArrayList<>();
        for (VaultEntry e : entries) {
            String plain = EncryptionManager.decryptString(e.getEncryptedPassword(), activeKey);
            String newEnc = EncryptionManager.encrypt(plain, newKey);
            e.setEncryptedPassword(newEnc);
            reEncrypted.add(e);
        }

        // Commit
        activeKey           = newKey;
        entries             = reEncrypted;
        config.saltBase64   = EncryptionManager.encodeSalt(newSalt);
        config.passwordHash = newHash;

        writeConfig();
        saveVault();
    }

    // ── Backup / Restore ──────────────────────────────────────────────────────────

    public void backupTo(Path destination) throws Exception {
        Files.copy(vaultPath, destination, StandardCopyOption.REPLACE_EXISTING);
        config.lastBackupDate = java.time.LocalDate.now().toString();
        writeConfig();
    }

    public void restoreFrom(Path source) throws IOException {
        Files.copy(source, vaultPath, StandardCopyOption.REPLACE_EXISTING);
        // Force reload on next unlock
        lock();
    }

    public String getLastBackupDate() {
        if (config == null) { try { loadConfig(); } catch (Exception ignored) {} }
        return config != null ? config.lastBackupDate : "Never";
    }

    // ── CSV Import ────────────────────────────────────────────────────────────────

    /**
     * Imports Chrome/Firefox CSV format: name,url,username,password
     * Returns [imported, skipped] counts.
     */
    public int[] importCSV(File csvFile) throws Exception {
        requireUnlocked();
        int imported = 0, skipped = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; } // skip header
                String[] cols = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (cols.length < 4) { skipped++; continue; }

                String name     = strip(cols[0]);
                String url      = strip(cols[1]);
                String username = strip(cols[2]);
                String password = strip(cols[3]);

                // Duplicate check
                boolean dup = entries.stream().anyMatch(e ->
                    name.equalsIgnoreCase(e.getSiteName()) &&
                    username.equalsIgnoreCase(e.getUsername()));

                if (dup) { skipped++; continue; }

                VaultEntry entry = new VaultEntry();
                entry.setSiteName(name);
                entry.setUrl(url);
                entry.setUsername(username);
                encryptEntryPassword(entry, password);
                entries.add(entry);
                imported++;
            }
        }

        saveVault();
        return new int[]{imported, skipped};
    }

    private String strip(String s) {
        return s.trim().replaceAll("^\"|\"$", "");
    }

    // ── Theme preference ──────────────────────────────────────────────────────────

    public boolean isDarkMode() {
        if (config == null) { try { loadConfig(); } catch (Exception ignored) {} }
        return config != null && config.isDarkMode;
    }

    public void saveDarkMode(boolean dark) {
        if (config == null) config = new Config();
        config.isDarkMode = dark;
        try { writeConfig(); } catch (Exception ignored) {}
    }

    // ── Internal I/O ──────────────────────────────────────────────────────────────

    private void saveVault() throws Exception {
        requireUnlocked();
        String json      = gson.toJson(entries);
        String encrypted = EncryptionManager.encrypt(json, activeKey);
        Files.writeString(vaultPath, encrypted);
    }

    private void loadVault() throws Exception {
        String encrypted = Files.readString(vaultPath);
        if (encrypted.isBlank()) {
            entries = new ArrayList<>();
            return;
        }
        String json = EncryptionManager.decryptString(encrypted, activeKey);
        Type   type = new TypeToken<List<VaultEntry>>(){}.getType();
        entries = gson.fromJson(json, type);
        if (entries == null) entries = new ArrayList<>();
    }

    private void writeConfig() throws Exception {
        Files.createDirectories(vaultDir);
        String json = gson.toJson(config);
        Files.writeString(configPath, json);
    }

    private void loadConfig() throws Exception {
        String json = Files.readString(configPath);
        config = gson.fromJson(json, Config.class);
    }

    private void requireUnlocked() {
        if (!unlocked || activeKey == null)
            throw new IllegalStateException("Vault is locked.");
    }
}
