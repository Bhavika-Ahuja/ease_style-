package com.stylesync.service;

import com.stylesync.exception.DatabaseException;
import com.stylesync.exception.ResourceNotFoundException;
import com.stylesync.exception.ValidationException;
import com.stylesync.model.Clothing;
import com.stylesync.repository.ClothingRepository;
import com.stylesync.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * WardrobeService - manages clothing operations
 */
public class WardrobeService {
    private static final Logger logger = LoggerFactory.getLogger(WardrobeService.class);
    private final ClothingRepository clothingRepository;
    
    public WardrobeService() {
        this.clothingRepository = new ClothingRepository();
    }
    
    /**
     * Add new clothing item
     */
    public Clothing addClothing(Clothing clothing) throws ValidationException, DatabaseException {
        logger.info("Adding clothing: {} for user: {}", clothing.getName(), clothing.getUserId());
        
        // Validate
        ValidationUtil.validateRequired(clothing.getName(), "Clothing name");
        ValidationUtil.validateRequired(clothing.getUserId(), "User ID");
        
        if (clothing.getCategory() == null) {
            throw new ValidationException("Category is required");
        }
        if (clothing.getSeason() == null) {
            throw new ValidationException("Season is required");
        }
        if (clothing.getOccasion() == null) {
            throw new ValidationException("Occasion is required");
        }
        if (clothing.getStyle() == null) {
            throw new ValidationException("Style is required");
        }
        
        return clothingRepository.save(clothing);
    }
    
    /**
     * Update clothing item
     */
    public Clothing updateClothing(Clothing clothing) 
            throws ValidationException, ResourceNotFoundException, DatabaseException {
        
        logger.info("Updating clothing: {}", clothing.getClothingId());
        
        // Validate
        if (clothing.getClothingId() == null) {
            throw new ValidationException("Clothing ID is required");
        }
        
        // Check if exists
        Optional<Clothing> existing = clothingRepository.findById(clothing.getClothingId());
        if (existing.isEmpty()) {
            throw new ResourceNotFoundException("Clothing not found");
        }
        
        return clothingRepository.save(clothing);
    }
    
    /**
     * Delete clothing item
     */
    public boolean deleteClothing(String clothingId) 
            throws ResourceNotFoundException, DatabaseException {
        
        logger.info("Deleting clothing: {}", clothingId);
        
        boolean deleted = clothingRepository.deleteById(clothingId);
        if (!deleted) {
            throw new ResourceNotFoundException("Clothing not found");
        }
        
        return true;
    }
    
    /**
     * Get clothing by ID
     */
    public Clothing getClothingById(String clothingId) 
            throws ResourceNotFoundException, DatabaseException {
        
        Optional<Clothing> clothing = clothingRepository.findById(clothingId);
        return clothing.orElseThrow(() -> new ResourceNotFoundException("Clothing not found"));
    }
    
    /**
     * Get all clothing for user
     */
    public List<Clothing> getUserClothing(String userId) throws DatabaseException {
        return clothingRepository.findByUserId(userId);
    }
    
    /**
     * Get favorite clothing
     */
    public List<Clothing> getFavoriteClothing(String userId) throws DatabaseException {
        return clothingRepository.findFavoritesByUserId(userId);
    }
    
    /**
     * Toggle favorite status
     */
    public void toggleFavorite(String clothingId) throws DatabaseException, ResourceNotFoundException {
        Optional<Clothing> clothingOpt = clothingRepository.findById(clothingId);
        if (clothingOpt.isEmpty()) {
            throw new ResourceNotFoundException("Clothing not found");
        }
        
        Clothing clothing = clothingOpt.get();
        clothingRepository.toggleFavorite(clothingId, !clothing.isFavorite());
    }
    
    /**
     * Update laundry status
     */
    public void updateLaundryStatus(String clothingId, boolean inLaundry) 
            throws DatabaseException, ResourceNotFoundException {
        
        Optional<Clothing> clothingOpt = clothingRepository.findById(clothingId);
        if (clothingOpt.isEmpty()) {
            throw new ResourceNotFoundException("Clothing not found");
        }
        
        clothingRepository.updateLaundryStatus(clothingId, inLaundry);
    }
    
    /**
     * Mark clothing as worn
     */
    public void markAsWorn(String clothingId) throws DatabaseException, ResourceNotFoundException {
        Optional<Clothing> clothingOpt = clothingRepository.findById(clothingId);
        if (clothingOpt.isEmpty()) {
            throw new ResourceNotFoundException("Clothing not found");
        }
        
        clothingRepository.incrementWearCount(clothingId);
    }
}
