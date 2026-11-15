package com.stylesync.model;

import org.bson.types.ObjectId;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Outfit model class representing a combination of clothing items
 * Uses ArrayList to store clothing IDs - demonstrates Java Collection Framework
 */
public class Outfit implements Serializable, Comparable<Outfit> {
    private static final long serialVersionUID = 1L;

    private ObjectId id;
    private String outfitId;
    private String userId;
    private String name;
    private List<String> clothingIds; // ArrayList - stores IDs of clothing items in the outfit
    private Clothing.Season season;
    private Clothing.Occasion occasion;
    private Clothing.Style style;
    private int wearCount;
    private boolean isFavorite;
    private LocalDateTime dateCreated;
    private LocalDateTime lastWorn;
    private String thumbnailUrl; // URL of the first clothing item or custom image

    // Default constructor
    public Outfit() {
        this.clothingIds = new ArrayList<>(); // ArrayList for dynamic clothing list
        this.dateCreated = LocalDateTime.now();
        this.wearCount = 0;
        this.isFavorite = false;
    }

    // Parameterized constructor
    public Outfit(String userId, String name, Clothing.Season season, 
                  Clothing.Occasion occasion, Clothing.Style style) {
        this();
        this.userId = userId;
        this.name = name;
        this.season = season;
        this.occasion = occasion;
        this.style = style;
    }

    // Getters and Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
        this.outfitId = id != null ? id.toHexString() : null;
    }

    public String getOutfitId() {
        return outfitId;
    }

    public void setOutfitId(String outfitId) {
        this.outfitId = outfitId;
        if (outfitId != null && !outfitId.isEmpty()) {
            this.id = new ObjectId(outfitId);
        }
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getClothingIds() {
        return clothingIds;
    }

    public void setClothingIds(List<String> clothingIds) {
        this.clothingIds = clothingIds != null ? clothingIds : new ArrayList<>();
    }

    public Clothing.Season getSeason() {
        return season;
    }

    public void setSeason(Clothing.Season season) {
        this.season = season;
    }

    public Clothing.Occasion getOccasion() {
        return occasion;
    }

    public void setOccasion(Clothing.Occasion occasion) {
        this.occasion = occasion;
    }

    public Clothing.Style getStyle() {
        return style;
    }

    public void setStyle(Clothing.Style style) {
        this.style = style;
    }

    public int getWearCount() {
        return wearCount;
    }

    public void setWearCount(int wearCount) {
        this.wearCount = wearCount;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public LocalDateTime getLastWorn() {
        return lastWorn;
    }

    public void setLastWorn(LocalDateTime lastWorn) {
        this.lastWorn = lastWorn;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    // Business methods
    public void addClothing(String clothingId) {
        if (clothingId != null && !clothingId.isEmpty() && !this.clothingIds.contains(clothingId)) {
            this.clothingIds.add(clothingId);
        }
    }

    public void removeClothing(String clothingId) {
        this.clothingIds.remove(clothingId);
    }

    public void incrementWearCount() {
        this.wearCount++;
        this.lastWorn = LocalDateTime.now();
    }

    public void markAsWorn() {
        incrementWearCount();
    }

    public void toggleFavorite() {
        this.isFavorite = !this.isFavorite;
    }

    public int getClothingCount() {
        return clothingIds.size();
    }

    public boolean isEmpty() {
        return clothingIds.isEmpty();
    }

    // Comparable implementation - sort by wear count (descending) then by name
    @Override
    public int compareTo(Outfit other) {
        int wearCompare = Integer.compare(other.wearCount, this.wearCount);
        if (wearCompare != 0) {
            return wearCompare;
        }
        return this.name.compareToIgnoreCase(other.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Outfit outfit = (Outfit) o;
        return Objects.equals(outfitId, outfit.outfitId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(outfitId);
    }

    @Override
    public String toString() {
        return "Outfit{" +
                "name='" + name + '\'' +
                ", clothingCount=" + clothingIds.size() +
                ", season=" + season +
                ", occasion=" + occasion +
                ", wearCount=" + wearCount +
                ", isFavorite=" + isFavorite +
                '}';
    }
}
