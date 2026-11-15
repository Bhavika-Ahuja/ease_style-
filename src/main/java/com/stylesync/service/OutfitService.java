package com.stylesync.service;

import com.stylesync.exception.DatabaseException;
import com.stylesync.exception.ResourceNotFoundException;
import com.stylesync.exception.ValidationException;
import com.stylesync.model.Clothing;
import com.stylesync.model.Outfit;
import com.stylesync.model.WearHistory;
import com.stylesync.repository.ClothingRepository;
import com.stylesync.repository.OutfitRepository;
import com.stylesync.repository.WearHistoryRepository;
import com.stylesync.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * OutfitService - manages outfit operations
 * Demonstrates Java Collection usage in business logic
 */
public class OutfitService {
    private static final Logger logger = LoggerFactory.getLogger(OutfitService.class);
    private final OutfitRepository outfitRepository;
    private final ClothingRepository clothingRepository;
    private final WearHistoryRepository wearHistoryRepository;
    
    public OutfitService() {
        this.outfitRepository = new OutfitRepository();
        this.clothingRepository = new ClothingRepository();
        this.wearHistoryRepository = new WearHistoryRepository();
    }
    
    /**
     * Create new outfit
     */
    public Outfit createOutfit(Outfit outfit) throws ValidationException, DatabaseException {
        logger.info("Creating outfit: {} for user: {}", outfit.getName(), outfit.getUserId());
        
        // Validate
        ValidationUtil.validateRequired(outfit.getName(), "Outfit name");
        ValidationUtil.validateRequired(outfit.getUserId(), "User ID");
        
        if (outfit.getClothingIds() == null || outfit.getClothingIds().isEmpty()) {
            throw new ValidationException("Outfit must contain at least one clothing item");
        }
        
        // Set thumbnail to first clothing item's image
        if (outfit.getThumbnailUrl() == null && !outfit.getClothingIds().isEmpty()) {
            String firstClothingId = outfit.getClothingIds().get(0);
            Optional<Clothing> firstClothing = clothingRepository.findById(firstClothingId);
            firstClothing.ifPresent(clothing -> outfit.setThumbnailUrl(clothing.getImageUrl()));
        }
        
        return outfitRepository.save(outfit);
    }
    
    /**
     * Update outfit
     */
    public Outfit updateOutfit(Outfit outfit) 
            throws ValidationException, ResourceNotFoundException, DatabaseException {
        
        logger.info("Updating outfit: {}", outfit.getOutfitId());
        
        if (outfit.getOutfitId() == null) {
            throw new ValidationException("Outfit ID is required");
        }
        
        Optional<Outfit> existing = outfitRepository.findById(outfit.getOutfitId());
        if (existing.isEmpty()) {
            throw new ResourceNotFoundException("Outfit not found");
        }
        
        return outfitRepository.save(outfit);
    }
    
    /**
     * Delete outfit
     */
    public boolean deleteOutfit(String outfitId) 
            throws ResourceNotFoundException, DatabaseException {
        
        logger.info("Deleting outfit: {}", outfitId);
        
        boolean deleted = outfitRepository.deleteById(outfitId);
        if (!deleted) {
            throw new ResourceNotFoundException("Outfit not found");
        }
        
        return true;
    }
    
    /**
     * Get outfit by ID with clothing details
     */
    public OutfitWithClothing getOutfitWithClothing(String outfitId) 
            throws ResourceNotFoundException, DatabaseException {
        
        Optional<Outfit> outfitOpt = outfitRepository.findById(outfitId);
        if (outfitOpt.isEmpty()) {
            throw new ResourceNotFoundException("Outfit not found");
        }
        
        Outfit outfit = outfitOpt.get();
        
        // Get clothing details - uses ArrayList
        List<Clothing> clothingItems = new ArrayList<>();
        for (String clothingId : outfit.getClothingIds()) {
            Optional<Clothing> clothing = clothingRepository.findById(clothingId);
            clothing.ifPresent(clothingItems::add);
        }
        
        return new OutfitWithClothing(outfit, clothingItems);
    }
    
    /**
     * Get all outfits for user
     */
    public List<Outfit> getUserOutfits(String userId) throws DatabaseException {
        return outfitRepository.findByUserId(userId);
    }
    
    /**
     * Get favorite outfits
     */
    public List<Outfit> getFavoriteOutfits(String userId) throws DatabaseException {
        return outfitRepository.findFavoritesByUserId(userId);
    }
    
    /**
     * Toggle favorite status
     */
    public void toggleFavorite(String outfitId) 
            throws DatabaseException, ResourceNotFoundException {
        
        Optional<Outfit> outfitOpt = outfitRepository.findById(outfitId);
        if (outfitOpt.isEmpty()) {
            throw new ResourceNotFoundException("Outfit not found");
        }
        
        Outfit outfit = outfitOpt.get();
        outfitRepository.toggleFavorite(outfitId, !outfit.isFavorite());
    }
    
    /**
     * Mark outfit as worn
     */
    public void markAsWorn(String outfitId) 
            throws DatabaseException, ResourceNotFoundException {
        
        Optional<Outfit> outfitOpt = outfitRepository.findById(outfitId);
        if (outfitOpt.isEmpty()) {
            throw new ResourceNotFoundException("Outfit not found");
        }
        
        Outfit outfit = outfitOpt.get();
        
        // Increment outfit wear count
        outfitRepository.incrementWearCount(outfitId);
        
        // Increment wear count for all clothing items
        for (String clothingId : outfit.getClothingIds()) {
            clothingRepository.incrementWearCount(clothingId);
        }
        
        // Record in wear history
        WearHistory history = new WearHistory(
            outfit.getUserId(),
            outfitId,
            WearHistory.ItemType.OUTFIT
        );
        wearHistoryRepository.save(history);
        
        logger.info("Outfit {} marked as worn", outfitId);
    }
    
    /**
     * OutfitWithClothing data class
     */
    public static class OutfitWithClothing {
        private final Outfit outfit;
        private final List<Clothing> clothing;
        
        public OutfitWithClothing(Outfit outfit, List<Clothing> clothing) {
            this.outfit = outfit;
            this.clothing = clothing;
        }
        
        public Outfit getOutfit() {
            return outfit;
        }
        
        public List<Clothing> getClothing() {
            return clothing;
        }
    }
}
