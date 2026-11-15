package com.stylesync.model;

import org.bson.types.ObjectId;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Clothing model class representing a wardrobe item
 * Demonstrates ENCAPSULATION and proper domain modeling
 */
public class Clothing implements Serializable, Comparable<Clothing> {
    private static final long serialVersionUID = 1L;

    private ObjectId id;
    private String clothingId; // String representation
    private String userId; // Owner of this clothing
    private String name;
    private Category category;
    private String color;
    private Season season;
    private Occasion occasion;
    private Style style;
    private String imageUrl;
    private int wearCount;
    private boolean inLaundry;
    private boolean isFavorite;
    private LocalDateTime dateAdded;
    private LocalDateTime lastWorn;

    // Enums for type safety
    public enum Category {
        TOPS("Tops"),
        BOTTOMS("Bottoms"),
        DRESSES("Dresses"),
        SHOES("Shoes"),
        ACCESSORIES("Accessories"),
        OUTERWEAR("Outerwear");

        private final String displayName;

        Category(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Season {
        SUMMER("Summer"),
        WINTER("Winter"),
        MONSOON("Monsoon"),
        ALL_SEASON("All Season");

        private final String displayName;

        Season(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Occasion {
        COLLEGE("College"),
        OFFICE("Office"),
        CASUAL("Casual"),
        PARTY("Party"),
        WEDDING("Wedding"),
        FORMAL_EVENTS("Formal Events");

        private final String displayName;

        Occasion(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Style {
        CASUAL("Casual"),
        FORMAL("Formal"),
        ETHNIC("Ethnic"),
        WESTERN("Western"),
        SPORTY("Sporty"),
        MINIMALIST("Minimalist");

        private final String displayName;

        Style(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Default constructor
    public Clothing() {
        this.dateAdded = LocalDateTime.now();
        this.wearCount = 0;
        this.inLaundry = false;
        this.isFavorite = false;
    }

    // Parameterized constructor
    public Clothing(String userId, String name, Category category, String color,
                    Season season, Occasion occasion, Style style, String imageUrl) {
        this();
        this.userId = userId;
        this.name = name;
        this.category = category;
        this.color = color;
        this.season = season;
        this.occasion = occasion;
        this.style = style;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
        this.clothingId = id != null ? id.toHexString() : null;
    }

    public String getClothingId() {
        return clothingId;
    }

    public void setClothingId(String clothingId) {
        this.clothingId = clothingId;
        if (clothingId != null && !clothingId.isEmpty()) {
            this.id = new ObjectId(clothingId);
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Season getSeason() {
        return season;
    }

    public void setSeason(Season season) {
        this.season = season;
    }

    public Occasion getOccasion() {
        return occasion;
    }

    public void setOccasion(Occasion occasion) {
        this.occasion = occasion;
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getWearCount() {
        return wearCount;
    }

    public void setWearCount(int wearCount) {
        this.wearCount = wearCount;
    }

    public boolean isInLaundry() {
        return inLaundry;
    }

    public void setInLaundry(boolean inLaundry) {
        this.inLaundry = inLaundry;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public LocalDateTime getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(LocalDateTime dateAdded) {
        this.dateAdded = dateAdded;
    }

    public LocalDateTime getLastWorn() {
        return lastWorn;
    }

    public void setLastWorn(LocalDateTime lastWorn) {
        this.lastWorn = lastWorn;
    }

    // Business methods
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

    public void sendToLaundry() {
        this.inLaundry = true;
    }

    public void returnFromLaundry() {
        this.inLaundry = false;
    }

    // Comparable implementation for natural ordering (by name)
    @Override
    public int compareTo(Clothing other) {
        return this.name.compareToIgnoreCase(other.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Clothing clothing = (Clothing) o;
        return Objects.equals(clothingId, clothing.clothingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clothingId);
    }

    @Override
    public String toString() {
        return "Clothing{" +
                "name='" + name + '\'' +
                ", category=" + category +
                ", color='" + color + '\'' +
                ", wearCount=" + wearCount +
                ", inLaundry=" + inLaundry +
                '}';
    }
}
