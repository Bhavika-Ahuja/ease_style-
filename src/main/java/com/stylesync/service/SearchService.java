package com.stylesync.service;

import com.stylesync.exception.DatabaseException;
import com.stylesync.model.Clothing;
import com.stylesync.repository.ClothingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * SearchService - implements advanced search and filtering
 * DEMONSTRATES EXTENSIVE JAVA COLLECTION FRAMEWORK USAGE:
 * - ArrayList for dynamic search results
 * - HashSet for unique values and deduplication
 * - TreeSet for sorted unique values
 * - HashMap for categorized grouping
 * - Comparator for custom sorting
 */
public class SearchService {
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    private final ClothingRepository clothingRepository;
    
    public SearchService() {
        this.clothingRepository = new ClothingRepository();
    }
    
    /**
     * Search clothing with multiple filters
     * Uses ArrayList to store dynamic search results
     */
    public List<Clothing> searchClothing(String userId, SearchCriteria criteria) 
            throws DatabaseException {
        
        logger.info("Searching clothing for user: {} with criteria: {}", userId, criteria);
        
        // Get all user's clothing - stored in ArrayList
        List<Clothing> allClothing = clothingRepository.findByUserId(userId);
        List<Clothing> results = new ArrayList<>(allClothing); // ArrayList for dynamic filtering
        
        // Apply filters
        if (criteria.getSearchQuery() != null && !criteria.getSearchQuery().isEmpty()) {
            String query = criteria.getSearchQuery().toLowerCase();
            results = results.stream()
                    .filter(c -> c.getName().toLowerCase().contains(query) ||
                                c.getColor().toLowerCase().contains(query))
                    .collect(Collectors.toList());
        }
        
        if (criteria.getCategory() != null) {
            results = results.stream()
                    .filter(c -> c.getCategory() == criteria.getCategory())
                    .collect(Collectors.toList());
        }
        
        if (criteria.getColor() != null && !criteria.getColor().isEmpty()) {
            String color = criteria.getColor().toLowerCase();
            results = results.stream()
                    .filter(c -> c.getColor().toLowerCase().equals(color))
                    .collect(Collectors.toList());
        }
        
        if (criteria.getSeason() != null) {
            results = results.stream()
                    .filter(c -> c.getSeason() == criteria.getSeason() || 
                                c.getSeason() == Clothing.Season.ALL_SEASON)
                    .collect(Collectors.toList());
        }
        
        if (criteria.getOccasion() != null) {
            results = results.stream()
                    .filter(c -> c.getOccasion() == criteria.getOccasion())
                    .collect(Collectors.toList());
        }
        
        if (criteria.getStyle() != null) {
            results = results.stream()
                    .filter(c -> c.getStyle() == criteria.getStyle())
                    .collect(Collectors.toList());
        }
        
        if (criteria.isOnlyFavorites()) {
            results = results.stream()
                    .filter(Clothing::isFavorite)
                    .collect(Collectors.toList());
        }
        
        if (criteria.isExcludeLaundry()) {
            results = results.stream()
                    .filter(c -> !c.isInLaundry())
                    .collect(Collectors.toList());
        }
        
        // Apply sorting using Comparator
        results = sortClothing(results, criteria.getSortBy(), criteria.isAscending());
        
        logger.info("Found {} clothing items matching criteria", results.size());
        return results;
    }
    
    /**
     * Sort clothing using Comparator - demonstrates custom comparators
     */
    private List<Clothing> sortClothing(List<Clothing> clothing, String sortBy, boolean ascending) {
        Comparator<Clothing> comparator;
        
        switch (sortBy != null ? sortBy.toUpperCase() : "NAME") {
            case "NAME":
                // Comparator for alphabetical sorting
                comparator = Comparator.comparing(Clothing::getName, String.CASE_INSENSITIVE_ORDER);
                break;
            case "CATEGORY":
                // Comparator for category sorting
                comparator = Comparator.comparing(c -> c.getCategory().name());
                break;
            case "WEAR_COUNT":
                // Comparator for wear count sorting
                comparator = Comparator.comparingInt(Clothing::getWearCount);
                break;
            case "DATE_ADDED":
                // Comparator for date sorting
                comparator = Comparator.comparing(Clothing::getDateAdded);
                break;
            case "COLOR":
                // Comparator for color sorting
                comparator = Comparator.comparing(Clothing::getColor, String.CASE_INSENSITIVE_ORDER);
                break;
            default:
                comparator = Comparator.comparing(Clothing::getName, String.CASE_INSENSITIVE_ORDER);
        }
        
        // Reverse if descending
        if (!ascending) {
            comparator = comparator.reversed();
        }
        
        // Sort using ArrayList's sort method with Comparator
        clothing.sort(comparator);
        return clothing;
    }
    
    /**
     * Get all unique colors in user's wardrobe
     * Uses TreeSet for sorted unique values
     */
    public Set<String> getUniqueColors(String userId) throws DatabaseException {
        List<Clothing> allClothing = clothingRepository.findByUserId(userId);
        
        // TreeSet maintains sorted order and ensures uniqueness
        Set<String> uniqueColors = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        
        for (Clothing clothing : allClothing) {
            uniqueColors.add(clothing.getColor());
        }
        
        logger.info("Found {} unique colors for user", uniqueColors.size());
        return uniqueColors;
    }
    
    /**
     * Get all categories present in user's wardrobe
     * Uses HashSet for unique categories
     */
    public Set<Clothing.Category> getAvailableCategories(String userId) throws DatabaseException {
        List<Clothing> allClothing = clothingRepository.findByUserId(userId);
        
        // HashSet for unique categories - no duplicates
        Set<Clothing.Category> categories = new HashSet<>();
        
        for (Clothing clothing : allClothing) {
            categories.add(clothing.getCategory());
        }
        
        return categories;
    }
    
    /**
     * Group clothing by category
     * Uses HashMap for efficient grouping and lookups
     */
    public Map<Clothing.Category, List<Clothing>> groupByCategory(String userId) 
            throws DatabaseException {
        
        List<Clothing> allClothing = clothingRepository.findByUserId(userId);
        
        // HashMap for O(1) lookups and grouping
        Map<Clothing.Category, List<Clothing>> grouped = new HashMap<>();
        
        for (Clothing clothing : allClothing) {
            // Get or create ArrayList for category
            grouped.computeIfAbsent(clothing.getCategory(), k -> new ArrayList<>())
                   .add(clothing);
        }
        
        logger.info("Grouped {} items into {} categories", allClothing.size(), grouped.size());
        return grouped;
    }
    
    /**
     * Group clothing by color
     * Uses HashMap for color-based grouping
     */
    public Map<String, List<Clothing>> groupByColor(String userId) throws DatabaseException {
        List<Clothing> allClothing = clothingRepository.findByUserId(userId);
        
        // HashMap for color grouping
        Map<String, List<Clothing>> grouped = new HashMap<>();
        
        for (Clothing clothing : allClothing) {
            String color = clothing.getColor().toLowerCase();
            grouped.computeIfAbsent(color, k -> new ArrayList<>())
                   .add(clothing);
        }
        
        return grouped;
    }
    
    /**
     * Get clothing IDs that match filters
     * Uses HashSet to ensure uniqueness
     */
    public Set<String> getMatchingClothingIds(String userId, SearchCriteria criteria) 
            throws DatabaseException {
        
        List<Clothing> matches = searchClothing(userId, criteria);
        
        // HashSet prevents duplicate IDs
        Set<String> ids = new HashSet<>();
        
        for (Clothing clothing : matches) {
            ids.add(clothing.getClothingId());
        }
        
        return ids;
    }
    
    /**
     * Search criteria class
     */
    public static class SearchCriteria {
        private String searchQuery;
        private Clothing.Category category;
        private String color;
        private Clothing.Season season;
        private Clothing.Occasion occasion;
        private Clothing.Style style;
        private boolean onlyFavorites;
        private boolean excludeLaundry;
        private String sortBy = "NAME"; // NAME, CATEGORY, WEAR_COUNT, DATE_ADDED, COLOR
        private boolean ascending = true;
        
        // Getters and Setters
        public String getSearchQuery() { return searchQuery; }
        public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }
        
        public Clothing.Category getCategory() { return category; }
        public void setCategory(Clothing.Category category) { this.category = category; }
        
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        
        public Clothing.Season getSeason() { return season; }
        public void setSeason(Clothing.Season season) { this.season = season; }
        
        public Clothing.Occasion getOccasion() { return occasion; }
        public void setOccasion(Clothing.Occasion occasion) { this.occasion = occasion; }
        
        public Clothing.Style getStyle() { return style; }
        public void setStyle(Clothing.Style style) { this.style = style; }
        
        public boolean isOnlyFavorites() { return onlyFavorites; }
        public void setOnlyFavorites(boolean onlyFavorites) { this.onlyFavorites = onlyFavorites; }
        
        public boolean isExcludeLaundry() { return excludeLaundry; }
        public void setExcludeLaundry(boolean excludeLaundry) { this.excludeLaundry = excludeLaundry; }
        
        public String getSortBy() { return sortBy; }
        public void setSortBy(String sortBy) { this.sortBy = sortBy; }
        
        public boolean isAscending() { return ascending; }
        public void setAscending(boolean ascending) { this.ascending = ascending; }
        
        @Override
        public String toString() {
            return "SearchCriteria{" +
                    "query='" + searchQuery + '\'' +
                    ", category=" + category +
                    ", color='" + color + '\'' +
                    ", season=" + season +
                    ", occasion=" + occasion +
                    ", style=" + style +
                    '}';
        }
    }
}
