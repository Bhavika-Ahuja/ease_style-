package com.stylesync.service;

import com.stylesync.exception.DatabaseException;
import com.stylesync.model.Clothing;
import com.stylesync.repository.ClothingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * RecommendationService - Smart outfit recommendation engine
 * DEMONSTRATES KEY JAVA COLLECTION FRAMEWORK CONCEPTS:
 * - PriorityQueue for ranking recommendations
 * - Comparator for custom scoring and sorting
 * - ArrayList for storing combinations
 * - HashMap for quick lookups
 * - HashSet for deduplication
 */
public class RecommendationService {
    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);
    private final ClothingRepository clothingRepository;
    
    public RecommendationService() {
        this.clothingRepository = new ClothingRepository();
    }
    
    /**
     * Generate outfit recommendations based on criteria
     * Uses PriorityQueue to rank by compatibility score
     */
    public List<OutfitRecommendation> generateRecommendations(
            String userId,
            Clothing.Occasion occasion,
            Clothing.Season season,
            Clothing.Style style,
            String preferredColor) throws DatabaseException {
        
        logger.info("Generating recommendations for user {} - occasion: {}, season: {}, style: {}", 
                   userId, occasion, season, style);
        
        // Get all available clothing
        List<Clothing> allClothing = clothingRepository.findByUserId(userId);
        
        // Group by category using HashMap for O(1) lookups
        Map<Clothing.Category, List<Clothing>> clothingByCategory = 
            groupByCategory(allClothing);
        
        // Get eligible clothing for each category
        List<Clothing> tops = filterEligible(
            clothingByCategory.get(Clothing.Category.TOPS), 
            occasion, season, style
        );
        List<Clothing> bottoms = filterEligible(
            clothingByCategory.get(Clothing.Category.BOTTOMS), 
            occasion, season, style
        );
        List<Clothing> dresses = filterEligible(
            clothingByCategory.get(Clothing.Category.DRESSES), 
            occasion, season, style
        );
        List<Clothing> shoes = filterEligible(
            clothingByCategory.get(Clothing.Category.SHOES), 
            occasion, season, style
        );
        List<Clothing> accessories = filterEligible(
            clothingByCategory.get(Clothing.Category.ACCESSORIES), 
            occasion, season, style
        );
        List<Clothing> outerwear = filterEligible(
            clothingByCategory.get(Clothing.Category.OUTERWEAR), 
            occasion, season, style
        );
        
        // Generate outfit combinations
        List<OutfitRecommendation> allCombinations = new ArrayList<>(); // ArrayList for dynamic list
        
        // Strategy 1: Dress-based outfits
        if (dresses != null && !dresses.isEmpty()) {
            for (Clothing dress : dresses) {
                OutfitRecommendation recommendation = new OutfitRecommendation();
                recommendation.addClothing(dress);
                
                // Add shoes
                if (shoes != null && !shoes.isEmpty()) {
                    recommendation.addClothing(selectBestMatch(shoes, dress.getColor(), preferredColor));
                }
                
                // Add accessories
                if (accessories != null && !accessories.isEmpty()) {
                    recommendation.addClothing(selectBestMatch(accessories, dress.getColor(), preferredColor));
                }
                
                // Add outerwear if season requires
                if (requiresOuterwear(season) && outerwear != null && !outerwear.isEmpty()) {
                    recommendation.addClothing(selectBestMatch(outerwear, dress.getColor(), preferredColor));
                }
                
                allCombinations.add(recommendation);
            }
        }
        
        // Strategy 2: Top + Bottom combinations
        if (tops != null && !tops.isEmpty() && bottoms != null && !bottoms.isEmpty()) {
            for (Clothing top : tops) {
                for (Clothing bottom : bottoms) {
                    // Check color compatibility
                    if (!areColorsCompatible(top.getColor(), bottom.getColor())) {
                        continue;
                    }
                    
                    OutfitRecommendation recommendation = new OutfitRecommendation();
                    recommendation.addClothing(top);
                    recommendation.addClothing(bottom);
                    
                    // Add shoes
                    if (shoes != null && !shoes.isEmpty()) {
                        recommendation.addClothing(selectBestMatch(shoes, bottom.getColor(), preferredColor));
                    }
                    
                    // Add accessories
                    if (accessories != null && !accessories.isEmpty()) {
                        recommendation.addClothing(selectBestMatch(accessories, top.getColor(), preferredColor));
                    }
                    
                    // Add outerwear
                    if (requiresOuterwear(season) && outerwear != null && !outerwear.isEmpty()) {
                        recommendation.addClothing(selectBestMatch(outerwear, top.getColor(), preferredColor));
                    }
                    
                    allCombinations.add(recommendation);
                }
            }
        }
        
        // Calculate compatibility scores for all combinations
        for (OutfitRecommendation rec : allCombinations) {
            rec.setScore(calculateCompatibilityScore(rec, occasion, season, style, preferredColor));
        }
        
        // Use PriorityQueue to rank recommendations by score (highest first)
        // DEMONSTRATES PRIORITYQUEUE AND COMPARATOR
        PriorityQueue<OutfitRecommendation> rankedQueue = new PriorityQueue<>(
            Comparator.comparingDouble(OutfitRecommendation::getScore).reversed()
        );
        
        rankedQueue.addAll(allCombinations);
        
        // Extract top recommendations (limit to 10)
        List<OutfitRecommendation> topRecommendations = new ArrayList<>();
        int limit = Math.min(10, rankedQueue.size());
        
        for (int i = 0; i < limit && !rankedQueue.isEmpty(); i++) {
            topRecommendations.add(rankedQueue.poll());
        }
        
        logger.info("Generated {} outfit recommendations", topRecommendations.size());
        return topRecommendations;
    }
    
    /**
     * Group clothing by category - uses HashMap
     */
    private Map<Clothing.Category, List<Clothing>> groupByCategory(List<Clothing> clothing) {
        Map<Clothing.Category, List<Clothing>> grouped = new HashMap<>(); // HashMap for grouping
        
        for (Clothing item : clothing) {
            grouped.computeIfAbsent(item.getCategory(), k -> new ArrayList<>())
                   .add(item);
        }
        
        return grouped;
    }
    
    /**
     * Filter clothing that matches criteria
     */
    private List<Clothing> filterEligible(List<Clothing> clothing, 
                                         Clothing.Occasion occasion,
                                         Clothing.Season season,
                                         Clothing.Style style) {
        if (clothing == null || clothing.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Clothing> eligible = new ArrayList<>();
        
        for (Clothing item : clothing) {
            // Skip if in laundry
            if (item.isInLaundry()) {
                continue;
            }
            
            // Check occasion match
            if (occasion != null && item.getOccasion() != occasion) {
                continue;
            }
            
            // Check season match (ALL_SEASON matches everything)
            if (season != null && item.getSeason() != season && 
                item.getSeason() != Clothing.Season.ALL_SEASON) {
                continue;
            }
            
            // Check style match
            if (style != null && item.getStyle() != style) {
                continue;
            }
            
            eligible.add(item);
        }
        
        // Sort by wear count (ascending) - prefer less worn items
        eligible.sort(Comparator.comparingInt(Clothing::getWearCount));
        
        return eligible;
    }
    
    /**
     * Select best matching item from list based on color
     */
    private Clothing selectBestMatch(List<Clothing> items, String primaryColor, String preferredColor) {
        if (items.isEmpty()) {
            return null;
        }
        
        // Prefer items with preferred color
        if (preferredColor != null && !preferredColor.isEmpty()) {
            for (Clothing item : items) {
                if (item.getColor().equalsIgnoreCase(preferredColor)) {
                    return item;
                }
            }
        }
        
        // Prefer items that complement primary color
        for (Clothing item : items) {
            if (areColorsCompatible(item.getColor(), primaryColor)) {
                return item;
            }
        }
        
        // Return least worn item
        return items.get(0);
    }
    
    /**
     * Check if season requires outerwear
     */
    private boolean requiresOuterwear(Clothing.Season season) {
        return season == Clothing.Season.WINTER || season == Clothing.Season.MONSOON;
    }
    
    /**
     * Calculate outfit compatibility score (0-100)
     * Higher score = better recommendation
     */
    private double calculateCompatibilityScore(OutfitRecommendation recommendation,
                                              Clothing.Occasion occasion,
                                              Clothing.Season season,
                                              Clothing.Style style,
                                              String preferredColor) {
        double score = 50.0; // Base score
        
        List<Clothing> items = recommendation.getClothing();
        if (items.isEmpty()) {
            return 0.0;
        }
        
        // Bonus for having complete outfit
        boolean hasTop = items.stream().anyMatch(c -> c.getCategory() == Clothing.Category.TOPS);
        boolean hasBottom = items.stream().anyMatch(c -> c.getCategory() == Clothing.Category.BOTTOMS);
        boolean hasDress = items.stream().anyMatch(c -> c.getCategory() == Clothing.Category.DRESSES);
        boolean hasShoes = items.stream().anyMatch(c -> c.getCategory() == Clothing.Category.SHOES);
        
        if ((hasTop && hasBottom) || hasDress) {
            score += 10;
        }
        if (hasShoes) {
            score += 5;
        }
        
        // Bonus for matching occasion
        long occasionMatches = items.stream()
            .filter(c -> c.getOccasion() == occasion)
            .count();
        score += (occasionMatches * 5);
        
        // Bonus for matching season
        long seasonMatches = items.stream()
            .filter(c -> c.getSeason() == season || c.getSeason() == Clothing.Season.ALL_SEASON)
            .count();
        score += (seasonMatches * 5);
        
        // Bonus for matching style
        long styleMatches = items.stream()
            .filter(c -> c.getStyle() == style)
            .count();
        score += (styleMatches * 5);
        
        // Bonus for preferred color
        if (preferredColor != null && !preferredColor.isEmpty()) {
            long colorMatches = items.stream()
                .filter(c -> c.getColor().equalsIgnoreCase(preferredColor))
                .count();
            score += (colorMatches * 8);
        }
        
        // Bonus for less worn items
        double avgWearCount = items.stream()
            .mapToInt(Clothing::getWearCount)
            .average()
            .orElse(0);
        score += Math.max(0, 10 - avgWearCount);
        
        // Color compatibility bonus
        if (items.size() >= 2) {
            boolean allCompatible = true;
            for (int i = 0; i < items.size() - 1; i++) {
                for (int j = i + 1; j < items.size(); j++) {
                    if (!areColorsCompatible(items.get(i).getColor(), items.get(j).getColor())) {
                        allCompatible = false;
                        break;
                    }
                }
            }
            if (allCompatible) {
                score += 10;
            }
        }
        
        return Math.min(100, score); // Cap at 100
    }
    
    /**
     * Check if two colors are compatible
     * Basic color theory rules
     */
    private boolean areColorsCompatible(String color1, String color2) {
        if (color1 == null || color2 == null) {
            return true;
        }
        
        color1 = color1.toLowerCase();
        color2 = color2.toLowerCase();
        
        // Same color is always compatible
        if (color1.equals(color2)) {
            return true;
        }
        
        // Neutral colors go with everything
        Set<String> neutrals = new HashSet<>(Arrays.asList(
            "white", "black", "gray", "grey", "beige", "cream", "brown", "navy"
        ));
        
        if (neutrals.contains(color1) || neutrals.contains(color2)) {
            return true;
        }
        
        // Compatible color combinations
        Map<String, Set<String>> compatibleColors = new HashMap<>();
        compatibleColors.put("blue", new HashSet<>(Arrays.asList("white", "beige", "brown", "yellow")));
        compatibleColors.put("red", new HashSet<>(Arrays.asList("white", "black", "navy", "beige")));
        compatibleColors.put("green", new HashSet<>(Arrays.asList("white", "brown", "beige", "yellow")));
        compatibleColors.put("yellow", new HashSet<>(Arrays.asList("blue", "white", "green", "brown")));
        compatibleColors.put("pink", new HashSet<>(Arrays.asList("white", "gray", "navy", "beige")));
        compatibleColors.put("purple", new HashSet<>(Arrays.asList("white", "gray", "yellow")));
        
        Set<String> compatible1 = compatibleColors.get(color1);
        Set<String> compatible2 = compatibleColors.get(color2);
        
        if (compatible1 != null && compatible1.contains(color2)) {
            return true;
        }
        if (compatible2 != null && compatible2.contains(color1)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * OutfitRecommendation class to hold recommendation data
     */
    public static class OutfitRecommendation {
        private List<Clothing> clothing; // ArrayList for dynamic clothing list
        private double score;
        
        public OutfitRecommendation() {
            this.clothing = new ArrayList<>();
            this.score = 0.0;
        }
        
        public void addClothing(Clothing item) {
            if (item != null) {
                this.clothing.add(item);
            }
        }
        
        public List<Clothing> getClothing() {
            return clothing;
        }
        
        public void setClothing(List<Clothing> clothing) {
            this.clothing = clothing;
        }
        
        public double getScore() {
            return score;
        }
        
        public void setScore(double score) {
            this.score = score;
        }
        
        public int getItemCount() {
            return clothing.size();
        }
        
        @Override
        public String toString() {
            return "OutfitRecommendation{" +
                    "items=" + clothing.size() +
                    ", score=" + String.format("%.2f", score) +
                    '}';
        }
    }
}
