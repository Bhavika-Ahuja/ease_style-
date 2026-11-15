package com.stylesync.model;

import org.bson.types.ObjectId;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * WearHistory model class to track when clothing items or outfits are worn
 * Useful for analytics and recommendations
 */
public class WearHistory implements Serializable, Comparable<WearHistory> {
    private static final long serialVersionUID = 1L;

    private ObjectId id;
    private String historyId;
    private String userId;
    private String itemId; // Can be clothingId or outfitId
    private ItemType itemType;
    private LocalDateTime wornOn;
    private String notes;

    public enum ItemType {
        CLOTHING("Clothing"),
        OUTFIT("Outfit");

        private final String displayName;

        ItemType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Default constructor
    public WearHistory() {
        this.wornOn = LocalDateTime.now();
    }

    // Parameterized constructor
    public WearHistory(String userId, String itemId, ItemType itemType) {
        this();
        this.userId = userId;
        this.itemId = itemId;
        this.itemType = itemType;
    }

    // Full constructor
    public WearHistory(String userId, String itemId, ItemType itemType, 
                      LocalDateTime wornOn, String notes) {
        this.userId = userId;
        this.itemId = itemId;
        this.itemType = itemType;
        this.wornOn = wornOn != null ? wornOn : LocalDateTime.now();
        this.notes = notes;
    }

    // Getters and Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
        this.historyId = id != null ? id.toHexString() : null;
    }

    public String getHistoryId() {
        return historyId;
    }

    public void setHistoryId(String historyId) {
        this.historyId = historyId;
        if (historyId != null && !historyId.isEmpty()) {
            this.id = new ObjectId(historyId);
        }
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public LocalDateTime getWornOn() {
        return wornOn;
    }

    public void setWornOn(LocalDateTime wornOn) {
        this.wornOn = wornOn;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Comparable implementation - sort by date (most recent first)
    @Override
    public int compareTo(WearHistory other) {
        return other.wornOn.compareTo(this.wornOn);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WearHistory that = (WearHistory) o;
        return Objects.equals(historyId, that.historyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(historyId);
    }

    @Override
    public String toString() {
        return "WearHistory{" +
                "userId='" + userId + '\'' +
                ", itemId='" + itemId + '\'' +
                ", itemType=" + itemType +
                ", wornOn=" + wornOn +
                '}';
    }
}
