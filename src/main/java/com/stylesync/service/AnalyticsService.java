package com.stylesync.service;

import com.stylesync.exception.DatabaseException;
import com.stylesync.model.Clothing;
import com.stylesync.model.Outfit;
import com.stylesync.repository.ClothingRepository;
import com.stylesync.repository.OutfitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AnalyticsService - provides wardrobe statistics and insights
 * DEMONSTRATES JAVA COLLECTION FRAMEWORK:
 * - HashMap for frequency counting and grouping
 * - TreeMap for sorted statistics
 * - ArrayList for top N lists
 * - Comparator for custom sorting
 */
public class AnalyticsService {
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);
    private final ClothingRepository clothingRepository;
    private final OutfitRepository outfitRepository;
    
    public AnalyticsService() {
        this.clothingRepository = new ClothingRepository();
        this.outfitRepository = new OutfitRepository();
    }
    
    /**
     * Get comprehensive wardrobe analytics
     */
    public WardrobeAnalytics getWardrobeAnalytics(String userId) throws DatabaseException {
        logger.info("Generating analytics for user: {}", userId);
        
        WardrobeAnalytics analytics = new WardrobeAnalytics();
        
        // Get all data
        List<Clothing> allClothing = clothingRepository.findByUserId(userId);
        List<Outfit> allOutfits = outfitRepository.findByUserId(userId);
        
        // Basic counts
        analytics.setTotalClothingItems(allClothing.size());
        analytics.setTotalOutfits(allOutfits.size());
        analytics.setFavoriteClothingCount(
            (int) allClothing.stream().filter(Clothing::isFavorite).count()
        );
        analytics.setFavoriteOutfitsCount(
            (int) allOutfits.stream().filter(Outfit::isFavorite).count()
        );
        
        // Most worn clothing
        analytics.setMostWornClothing(getMostWornClothing(allClothing, 5));
        
        // Least worn clothing
        analytics.setLeastWornClothing(getLeastWornClothing(allClothing, 5));
        
        // Most worn outfits
        analytics.setMostWornOutfits(getMostWornOutfits(allOutfits, 5));
        
        // Category distribution - uses HashMap
        analytics.setCategoryDistribution(getCategoryDistribution(allClothing));
        
        // Color frequency - uses HashMap
        analytics.setColorFrequency(getColorFrequency(allClothing));
        
        // Season distribution
        analytics.setSeasonDistribution(getSeasonDistribution(allClothing));
        
        // Style distribution
        analytics.setStyleDistribution(getStyleDistribution(allClothing));
        
        // Occasion distribution
        analytics.setOccasionDistribution(getOccasionDistribution(allClothing));
        
        logger.info("Analytics generated successfully");
        return analytics;
    }
    
    /**
     * Get most worn clothing items
     * Uses Comparator for sorting
     */
    private List<Clothing> getMostWornClothing(List<Clothing> clothing, int limit) {
        return clothing.stream()
                .sorted(Comparator.comparingInt(Clothing::getWearCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Get least worn clothing items
     */
    private List<Clothing> getLeastWornClothing(List<Clothing> clothing, int limit) {
        return clothing.stream()
                .sorted(Comparator.comparingInt(Clothing::getWearCount))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Get most worn outfits
     */
    private List<Outfit> getMostWornOutfits(List<Outfit> outfits, int limit) {
        return outfits.stream()
                .sorted(Comparator.comparingInt(Outfit::getWearCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Get category distribution
     * Uses HashMap for counting
     */
    private Map<String, Integer> getCategoryDistribution(List<Clothing> clothing) {
        Map<String, Integer> distribution = new HashMap<>(); // HashMap for O(1) operations
        
        for (Clothing item : clothing) {
            String category = item.getCategory().getDisplayName();
            distribution.put(category, distribution.getOrDefault(category, 0) + 1);
        }
        
        return distribution;
    }
    
    /**
     * Get color frequency
     * Uses HashMap and sorts by frequency
     */
    private Map<String, Integer> getColorFrequency(List<Clothing> clothing) {
        Map<String, Integer> frequency = new HashMap<>();
        
        for (Clothing item : clothing) {
            String color = item.getColor();
            frequency.put(color, frequency.getOrDefault(color, 0) + 1);
        }
        
        // Sort by frequency (descending) using LinkedHashMap to maintain order
        return frequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    LinkedHashMap::new
                ));
    }
    
    /**
     * Get season distribution
     */
    private Map<String, Integer> getSeasonDistribution(List<Clothing> clothing) {
        Map<String, Integer> distribution = new HashMap<>();
        
        for (Clothing item : clothing) {
            String season = item.getSeason().getDisplayName();
            distribution.put(season, distribution.getOrDefault(season, 0) + 1);
        }
        
        return distribution;
    }
    
    /**
     * Get style distribution
     */
    private Map<String, Integer> getStyleDistribution(List<Clothing> clothing) {
        Map<String, Integer> distribution = new HashMap<>();
        
        for (Clothing item : clothing) {
            String style = item.getStyle().getDisplayName();
            distribution.put(style, distribution.getOrDefault(style, 0) + 1);
        }
        
        return distribution;
    }
    
    /**
     * Get occasion distribution
     */
    private Map<String, Integer> getOccasionDistribution(List<Clothing> clothing) {
        Map<String, Integer> distribution = new HashMap<>();
        
        for (Clothing item : clothing) {
            String occasion = item.getOccasion().getDisplayName();
            distribution.put(occasion, distribution.getOrDefault(occasion, 0) + 1);
        }
        
        return distribution;
    }
    
    /**
     * Get dashboard statistics
     */
    public DashboardStats getDashboardStats(String userId) throws DatabaseException {
        logger.info("Getting dashboard stats for user: {}", userId);
        
        DashboardStats stats = new DashboardStats();
        
        // Total counts
        stats.setTotalClothingItems((int) clothingRepository.countByUserId(userId));
        stats.setTotalOutfits((int) outfitRepository.countByUserId(userId));
        
        // Favorites
        List<Clothing> favoriteClothing = clothingRepository.findFavoritesByUserId(userId);
        stats.setFavoriteClothingCount(favoriteClothing.size());
        
        List<Outfit> favoriteOutfits = outfitRepository.findFavoritesByUserId(userId);
        stats.setFavoriteOutfitsCount(favoriteOutfits.size());
        
        // Most worn
        List<Clothing> mostWorn = clothingRepository.findMostWornByUserId(userId, 1);
        if (!mostWorn.isEmpty()) {
            stats.setMostWornClothing(mostWorn.get(0));
        }
        
        // Recently added
        List<Clothing> recent = clothingRepository.findRecentByUserId(userId, 5);
        stats.setRecentlyAdded(recent);
        
        // Category stats
        stats.setCategoryCounts(clothingRepository.countByCategory(userId));
        
        return stats;
    }
    
    /**
     * WardrobeAnalytics data class
     */
    public static class WardrobeAnalytics {
        private int totalClothingItems;
        private int totalOutfits;
        private int favoriteClothingCount;
        private int favoriteOutfitsCount;
        private List<Clothing> mostWornClothing;
        private List<Clothing> leastWornClothing;
        private List<Outfit> mostWornOutfits;
        private Map<String, Integer> categoryDistribution;
        private Map<String, Integer> colorFrequency;
        private Map<String, Integer> seasonDistribution;
        private Map<String, Integer> styleDistribution;
        private Map<String, Integer> occasionDistribution;
        
        // Getters and Setters
        public int getTotalClothingItems() { return totalClothingItems; }
        public void setTotalClothingItems(int totalClothingItems) { 
            this.totalClothingItems = totalClothingItems; 
        }
        
        public int getTotalOutfits() { return totalOutfits; }
        public void setTotalOutfits(int totalOutfits) { 
            this.totalOutfits = totalOutfits; 
        }
        
        public int getFavoriteClothingCount() { return favoriteClothingCount; }
        public void setFavoriteClothingCount(int favoriteClothingCount) { 
            this.favoriteClothingCount = favoriteClothingCount; 
        }
        
        public int getFavoriteOutfitsCount() { return favoriteOutfitsCount; }
        public void setFavoriteOutfitsCount(int favoriteOutfitsCount) { 
            this.favoriteOutfitsCount = favoriteOutfitsCount; 
        }
        
        public List<Clothing> getMostWornClothing() { return mostWornClothing; }
        public void setMostWornClothing(List<Clothing> mostWornClothing) { 
            this.mostWornClothing = mostWornClothing; 
        }
        
        public List<Clothing> getLeastWornClothing() { return leastWornClothing; }
        public void setLeastWornClothing(List<Clothing> leastWornClothing) { 
            this.leastWornClothing = leastWornClothing; 
        }
        
        public List<Outfit> getMostWornOutfits() { return mostWornOutfits; }
        public void setMostWornOutfits(List<Outfit> mostWornOutfits) { 
            this.mostWornOutfits = mostWornOutfits; 
        }
        
        public Map<String, Integer> getCategoryDistribution() { return categoryDistribution; }
        public void setCategoryDistribution(Map<String, Integer> categoryDistribution) { 
            this.categoryDistribution = categoryDistribution; 
        }
        
        public Map<String, Integer> getColorFrequency() { return colorFrequency; }
        public void setColorFrequency(Map<String, Integer> colorFrequency) { 
            this.colorFrequency = colorFrequency; 
        }
        
        public Map<String, Integer> getSeasonDistribution() { return seasonDistribution; }
        public void setSeasonDistribution(Map<String, Integer> seasonDistribution) { 
            this.seasonDistribution = seasonDistribution; 
        }
        
        public Map<String, Integer> getStyleDistribution() { return styleDistribution; }
        public void setStyleDistribution(Map<String, Integer> styleDistribution) { 
            this.styleDistribution = styleDistribution; 
        }
        
        public Map<String, Integer> getOccasionDistribution() { return occasionDistribution; }
        public void setOccasionDistribution(Map<String, Integer> occasionDistribution) { 
            this.occasionDistribution = occasionDistribution; 
        }
    }
    
    /**
     * DashboardStats data class
     */
    public static class DashboardStats {
        private int totalClothingItems;
        private int totalOutfits;
        private int favoriteClothingCount;
        private int favoriteOutfitsCount;
        private Clothing mostWornClothing;
        private List<Clothing> recentlyAdded;
        private Map<Clothing.Category, Long> categoryCounts;
        
        // Getters and Setters
        public int getTotalClothingItems() { return totalClothingItems; }
        public void setTotalClothingItems(int totalClothingItems) { 
            this.totalClothingItems = totalClothingItems; 
        }
        
        public int getTotalOutfits() { return totalOutfits; }
        public void setTotalOutfits(int totalOutfits) { 
            this.totalOutfits = totalOutfits; 
        }
        
        public int getFavoriteClothingCount() { return favoriteClothingCount; }
        public void setFavoriteClothingCount(int favoriteClothingCount) { 
            this.favoriteClothingCount = favoriteClothingCount; 
        }
        
        public int getFavoriteOutfitsCount() { return favoriteOutfitsCount; }
        public void setFavoriteOutfitsCount(int favoriteOutfitsCount) { 
            this.favoriteOutfitsCount = favoriteOutfitsCount; 
        }
        
        public Clothing getMostWornClothing() { return mostWornClothing; }
        public void setMostWornClothing(Clothing mostWornClothing) { 
            this.mostWornClothing = mostWornClothing; 
        }
        
        public List<Clothing> getRecentlyAdded() { return recentlyAdded; }
        public void setRecentlyAdded(List<Clothing> recentlyAdded) { 
            this.recentlyAdded = recentlyAdded; 
        }
        
        public Map<Clothing.Category, Long> getCategoryCounts() { return categoryCounts; }
        public void setCategoryCounts(Map<Clothing.Category, Long> categoryCounts) { 
            this.categoryCounts = categoryCounts; 
        }
    }
}
