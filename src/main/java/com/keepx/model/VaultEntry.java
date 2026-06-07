package com.keepx.model;

import java.util.UUID;

/**
 * VaultEntry — data model for a single password record.
 * All fields are serialized to JSON by Gson and stored encrypted.
 * encryptedPassword stores the AES-encrypted ciphertext (base64).
 */
public class VaultEntry {

    private String  id;
    private String  siteName;
    private String  username;
    private String  encryptedPassword; // base64-encoded AES-GCM ciphertext
    private String  category;
    private String  notes;
    private String  url;
    private boolean favourite;
    private long    createdAt;
    private long    lastModified;

    public VaultEntry() {
        this.id           = UUID.randomUUID().toString();
        this.createdAt    = System.currentTimeMillis();
        this.lastModified = createdAt;
        this.favourite    = false;
        this.category     = "Other";
    }

    // ── Getters ──────────────────────────────────────────────────────────────────
    public String  getId()                { return id; }
    public String  getSiteName()          { return siteName; }
    public String  getUsername()          { return username; }
    public String  getEncryptedPassword() { return encryptedPassword; }
    public String  getCategory()          { return category; }
    public String  getNotes()             { return notes; }
    public String  getUrl()               { return url; }
    public boolean isFavourite()          { return favourite; }
    public long    getCreatedAt()         { return createdAt; }
    public long    getLastModified()      { return lastModified; }

    // ── Setters ──────────────────────────────────────────────────────────────────
    public void setId(String id)                              { this.id = id; }
    public void setSiteName(String siteName)                  { this.siteName = siteName; touch(); }
    public void setUsername(String username)                  { this.username = username; touch(); }
    public void setEncryptedPassword(String encryptedPassword){ this.encryptedPassword = encryptedPassword; touch(); }
    public void setCategory(String category)                  { this.category = category; touch(); }
    public void setNotes(String notes)                        { this.notes = notes; touch(); }
    public void setUrl(String url)                            { this.url = url; touch(); }
    public void setFavourite(boolean favourite)               { this.favourite = favourite; touch(); }
    public void setCreatedAt(long createdAt)                  { this.createdAt = createdAt; }
    public void setLastModified(long lastModified)            { this.lastModified = lastModified; }

    /** Updates lastModified timestamp to now. */
    private void touch() {
        this.lastModified = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "VaultEntry{id='" + id + "', siteName='" + siteName + "', username='" + username + "'}";
    }
}
