package com.keepx.security;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * EncryptionManager — all cryptographic operations for KeepX.
 *
 * Key derivation:  PBKDF2WithHmacSHA256 (65536 iterations, 256-bit key)
 * Encryption:      AES/GCM/NoPadding (128-bit IV prepended to ciphertext)
 * Password hash:   SHA-256 hex (for master password verification only)
 * Salt:            16-byte SecureRandom (stored separately in config)
 */
public final class EncryptionManager {

    private static final String  KDF_ALGO       = "PBKDF2WithHmacSHA256";
    private static final String  CIPHER_ALGO    = "AES/GCM/NoPadding";
    private static final String  KEY_ALGO       = "AES";
    private static final int     KDF_ITERATIONS = 65_536;
    private static final int     KEY_BITS       = 256;
    private static final int     IV_BYTES       = 12;   // GCM standard IV
    private static final int     GCM_TAG_BITS   = 128;
    private static final int     SALT_BYTES     = 16;

    private EncryptionManager() {}

    // ── Salt ──────────────────────────────────────────────────────────────────────

    /** Generates a cryptographically random 16-byte salt. */
    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    /** Encodes salt to Base64 for storage in config file. */
    public static String encodeSalt(byte[] salt) {
        return Base64.getEncoder().encodeToString(salt);
    }

    /** Decodes Base64 salt from config file. */
    public static byte[] decodeSalt(String encoded) {
        return Base64.getDecoder().decode(encoded);
    }

    // ── Key Derivation ────────────────────────────────────────────────────────────

    /**
     * Derives a 256-bit AES SecretKey from the master password and salt
     * using PBKDF2WithHmacSHA256 with 65536 iterations.
     */
    public static SecretKey deriveKey(char[] password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, KDF_ITERATIONS, KEY_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(KDF_ALGO);
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(keyBytes, KEY_ALGO);
        } finally {
            spec.clearPassword();
        }
    }

    // ── Encryption ────────────────────────────────────────────────────────────────

    /**
     * Encrypts plaintext bytes using AES-GCM.
     * Output format: [12-byte IV] + [ciphertext + 16-byte GCM tag]
     * Returns Base64-encoded result.
     */
    public static String encrypt(byte[] plaintext, SecretKey key)
            throws GeneralSecurityException {
        byte[] iv = new byte[IV_BYTES];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_BITS, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);

        byte[] ciphertext = cipher.doFinal(plaintext);

        // Prepend IV to ciphertext
        byte[] combined = new byte[IV_BYTES + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, IV_BYTES);
        System.arraycopy(ciphertext, 0, combined, IV_BYTES, ciphertext.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    public static String encrypt(String plaintext, SecretKey key) throws GeneralSecurityException {
        return encrypt(plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8), key);
    }

    // ── Decryption ────────────────────────────────────────────────────────────────

    /**
     * Decrypts a Base64-encoded AES-GCM ciphertext.
     * Expects: [12-byte IV] + [ciphertext + GCM tag] as produced by encrypt().
     */
    public static byte[] decrypt(String base64Data, SecretKey key)
            throws GeneralSecurityException {
        byte[] combined = Base64.getDecoder().decode(base64Data);

        byte[] iv         = new byte[IV_BYTES];
        byte[] ciphertext = new byte[combined.length - IV_BYTES];
        System.arraycopy(combined, 0, iv, 0, IV_BYTES);
        System.arraycopy(combined, IV_BYTES, ciphertext, 0, ciphertext.length);

        Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_BITS, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        return cipher.doFinal(ciphertext);
    }

    public static String decryptString(String base64Data, SecretKey key)
            throws GeneralSecurityException {
        return new String(decrypt(base64Data, key), java.nio.charset.StandardCharsets.UTF_8);
    }

    // ── Password Hashing ──────────────────────────────────────────────────────────

    /**
     * Returns the SHA-256 hex digest of the master password.
     * Used ONLY for verification; the actual key is derived via PBKDF2.
     */
    public static String hashPassword(char[] password)
            throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        // Convert char[] to bytes without creating a long-lived String
        byte[] passBytes = charArrayToBytes(password);
        byte[] digest = md.digest(passBytes);
        java.util.Arrays.fill(passBytes, (byte) 0); // clear

        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    /** Secure char[] → byte[] conversion using standard UTF-8 encoding. */
    private static byte[] charArrayToBytes(char[] chars) {
        java.nio.ByteBuffer bb = java.nio.charset.StandardCharsets.UTF_8
                .encode(java.nio.CharBuffer.wrap(chars));
        byte[] bytes = new byte[bb.remaining()];
        bb.get(bytes);
        java.util.Arrays.fill(bb.array(), (byte) 0);
        return bytes;
    }
}
