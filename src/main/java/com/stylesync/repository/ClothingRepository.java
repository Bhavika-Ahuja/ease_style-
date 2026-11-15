package com.stylesync.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.stylesync.database.MongoDBConnection;
import com.stylesync.exception.DatabaseException;
import com.stylesync.model.Clothing;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * ClothingRepository - implements Repository interface
 * Demonstrates extensive use of Java Collection Framework
 */
public class ClothingRepository implements Repository<Clothing> {
    private static final Logger logger = LoggerFactory.getLogger(ClothingRepository.class);
    private final MongoCollection<Document> collection;
    
    public ClothingRepository() {
        this.collection = MongoDBConnection.getInstance()
                                          .getDatabase()
                                          .getCollection("clothing");
    }
    
    @Override
    public Clothing save(Clothing clothing) throws DatabaseException {
        try {
            Document doc = clothingToDocument(clothing);
            
            if (clothing.getId() == null) {
                collection.insertOne(doc);
                clothing.setId(doc.getObjectId("_id"));
            } else {
                collection.replaceOne(Filters.eq("_id", clothing.getId()), doc);
            }
            
            logger.info("Clothing saved: {}", clothing.getName());
            return clothing;
        } catch (Exception e) {
            logger.error("Error saving clothing", e);
            throw new DatabaseException("Failed to save clothing", e);
        }
    }
    
    @Override
    public Optional<Clothing> findById(ObjectId id) throws DatabaseException {
        try {
            Document doc = collection.find(Filters.eq("_id", id)).first();
            return doc != null ? Optional.of(documentToClothing(doc)) : Optional.empty();
        } catch (Exception e) {
            logger.error("Error finding clothing by ID", e);
            throw new DatabaseException("Failed to find clothing", e);
        }
    }
    
    @Override
    public Optional<Clothing> findById(String id) throws DatabaseException {
        return findById(new ObjectId(id));
    }
    
    /**
     * Find all clothing items for a specific user
     * Returns ArrayList for dynamic operations
     */
    public List<Clothing> findByUserId(String userId) throws DatabaseException {
        try {
            List<Clothing> clothingList = new ArrayList<>(); // ArrayList for dynamic list
            collection.find(Filters.eq("userId", userId))
                     .forEach(doc -> clothingList.add(documentToClothing(doc)));
            return clothingList;
        } catch (Exception e) {
            logger.error("Error finding clothing by user ID", e);
            throw new DatabaseException("Failed to find clothing", e);
        }
    }
    
    /**
     * Find clothing by multiple IDs
     * Uses ArrayList to store results
     */
    public List<Clothing> findByIds(List<String> ids) throws DatabaseException {
        try {
            List<ObjectId> objectIds = new ArrayList<>(); // ArrayList for ID conversion
            for (String id : ids) {
                objectIds.add(new ObjectId(id));
            }
            
            List<Clothing> clothingList = new ArrayList<>();
            collection.find(Filters.in("_id", objectIds))
                     .forEach(doc -> clothingList.add(documentToClothing(doc)));
            return clothingList;
        } catch (Exception e) {
            logger.error("Error finding clothing by IDs", e);
            throw new DatabaseException("Failed to find clothing", e);
        }
    }
    
    /**
     * Find favorite clothing items
     */
    public List<Clothing> findFavoritesByUserId(String userId) throws DatabaseException {
        try {
            List<Clothing> favorites = new ArrayList<>();
            Bson filter = Filters.and(
                Filters.eq("userId", userId),
                Filters.eq("isFavorite", true)
            );
            collection.find(filter).forEach(doc -> favorites.add(documentToClothing(doc)));
            return favorites;
        } catch (Exception e) {
            logger.error("Error finding favorites", e);
            throw new DatabaseException("Failed to find favorites", e);
        }
    }
    
    /**
     * Find clothing by category
     * Demonstrates filtering with Java Collections
     */
    public List<Clothing> findByUserIdAndCategory(String userId, Clothing.Category category) 
            throws DatabaseException {
        try {
            List<Clothing> clothingList = new ArrayList<>();
            Bson filter = Filters.and(
                Filters.eq("userId", userId),
                Filters.eq("category", category.name())
            );
            collection.find(filter).forEach(doc -> clothingList.add(documentToClothing(doc)));
            return clothingList;
        } catch (Exception e) {
            logger.error("Error finding clothing by category", e);
            throw new DatabaseException("Failed to find clothing by category", e);
        }
    }
    
    /**
     * Find recently added clothing
     */
    public List<Clothing> findRecentByUserId(String userId, int limit) throws DatabaseException {
        try {
            List<Clothing> recentClothing = new ArrayList<>();
            collection.find(Filters.eq("userId", userId))
                     .sort(Sorts.descending("dateAdded"))
                     .limit(limit)
                     .forEach(doc -> recentClothing.add(documentToClothing(doc)));
            return recentClothing;
        } catch (Exception e) {
            logger.error("Error finding recent clothing", e);
            throw new DatabaseException("Failed to find recent clothing", e);
        }
    }
    
    /**
     * Find most worn clothing
     */
    public List<Clothing> findMostWornByUserId(String userId, int limit) throws DatabaseException {
        try {
            List<Clothing> mostWorn = new ArrayList<>();
            collection.find(Filters.eq("userId", userId))
                     .sort(Sorts.descending("wearCount"))
                     .limit(limit)
                     .forEach(doc -> mostWorn.add(documentToClothing(doc)));
            return mostWorn;
        } catch (Exception e) {
            logger.error("Error finding most worn clothing", e);
            throw new DatabaseException("Failed to find most worn clothing", e);
        }
    }
    
    /**
     * Find least worn clothing
     */
    public List<Clothing> findLeastWornByUserId(String userId, int limit) throws DatabaseException {
        try {
            List<Clothing> leastWorn = new ArrayList<>();
            collection.find(Filters.eq("userId", userId))
                     .sort(Sorts.ascending("wearCount"))
                     .limit(limit)
                     .forEach(doc -> leastWorn.add(documentToClothing(doc)));
            return leastWorn;
        } catch (Exception e) {
            logger.error("Error finding least worn clothing", e);
            throw new DatabaseException("Failed to find least worn clothing", e);
        }
    }
    
    /**
     * Update wear count
     */
    public void incrementWearCount(String clothingId) throws DatabaseException {
        try {
            collection.updateOne(
                Filters.eq("_id", new ObjectId(clothingId)),
                Updates.combine(
                    Updates.inc("wearCount", 1),
                    Updates.set("lastWorn", new Date())
                )
            );
        } catch (Exception e) {
            logger.error("Error incrementing wear count", e);
            throw new DatabaseException("Failed to increment wear count", e);
        }
    }
    
    /**
     * Toggle favorite status
     */
    public void toggleFavorite(String clothingId, boolean isFavorite) throws DatabaseException {
        try {
            collection.updateOne(
                Filters.eq("_id", new ObjectId(clothingId)),
                Updates.set("isFavorite", isFavorite)
            );
        } catch (Exception e) {
            logger.error("Error toggling favorite", e);
            throw new DatabaseException("Failed to toggle favorite", e);
        }
    }
    
    /**
     * Update laundry status
     */
    public void updateLaundryStatus(String clothingId, boolean inLaundry) throws DatabaseException {
        try {
            collection.updateOne(
                Filters.eq("_id", new ObjectId(clothingId)),
                Updates.set("inLaundry", inLaundry)
            );
        } catch (Exception e) {
            logger.error("Error updating laundry status", e);
            throw new DatabaseException("Failed to update laundry status", e);
        }
    }
    
    /**
     * Count clothing by category for a user
     * Returns HashMap for category statistics
     */
    public Map<Clothing.Category, Long> countByCategory(String userId) throws DatabaseException {
        try {
            Map<Clothing.Category, Long> categoryCount = new HashMap<>(); // HashMap for quick lookups
            
            List<Clothing> allClothing = findByUserId(userId);
            for (Clothing clothing : allClothing) {
                categoryCount.merge(clothing.getCategory(), 1L, Long::sum);
            }
            
            return categoryCount;
        } catch (Exception e) {
            logger.error("Error counting by category", e);
            throw new DatabaseException("Failed to count by category", e);
        }
    }
    
    @Override
    public List<Clothing> findAll() throws DatabaseException {
        try {
            List<Clothing> clothingList = new ArrayList<>();
            collection.find().forEach(doc -> clothingList.add(documentToClothing(doc)));
            return clothingList;
        } catch (Exception e) {
            logger.error("Error finding all clothing", e);
            throw new DatabaseException("Failed to find clothing", e);
        }
    }
    
    @Override
    public boolean deleteById(ObjectId id) throws DatabaseException {
        try {
            return collection.deleteOne(Filters.eq("_id", id)).getDeletedCount() > 0;
        } catch (Exception e) {
            logger.error("Error deleting clothing", e);
            throw new DatabaseException("Failed to delete clothing", e);
        }
    }
    
    @Override
    public boolean deleteById(String id) throws DatabaseException {
        return deleteById(new ObjectId(id));
    }
    
    @Override
    public long count() throws DatabaseException {
        try {
            return collection.countDocuments();
        } catch (Exception e) {
            logger.error("Error counting clothing", e);
            throw new DatabaseException("Failed to count clothing", e);
        }
    }
    
    public long countByUserId(String userId) throws DatabaseException {
        try {
            return collection.countDocuments(Filters.eq("userId", userId));
        } catch (Exception e) {
            logger.error("Error counting user clothing", e);
            throw new DatabaseException("Failed to count clothing", e);
        }
    }
    
    @Override
    public boolean existsById(ObjectId id) throws DatabaseException {
        try {
            return collection.countDocuments(Filters.eq("_id", id)) > 0;
        } catch (Exception e) {
            logger.error("Error checking clothing existence", e);
            throw new DatabaseException("Failed to check clothing", e);
        }
    }
    
    // Helper methods
    private Document clothingToDocument(Clothing clothing) {
        Document doc = new Document();
        if (clothing.getId() != null) {
            doc.append("_id", clothing.getId());
        }
        doc.append("userId", clothing.getUserId())
           .append("name", clothing.getName())
           .append("category", clothing.getCategory().name())
           .append("color", clothing.getColor())
           .append("season", clothing.getSeason().name())
           .append("occasion", clothing.getOccasion().name())
           .append("style", clothing.getStyle().name())
           .append("imageUrl", clothing.getImageUrl())
           .append("wearCount", clothing.getWearCount())
           .append("inLaundry", clothing.isInLaundry())
           .append("isFavorite", clothing.isFavorite())
           .append("dateAdded", Date.from(clothing.getDateAdded().atZone(ZoneId.systemDefault()).toInstant()));
        
        if (clothing.getLastWorn() != null) {
            doc.append("lastWorn", Date.from(clothing.getLastWorn().atZone(ZoneId.systemDefault()).toInstant()));
        }
        
        return doc;
    }
    
    private Clothing documentToClothing(Document doc) {
        Clothing clothing = new Clothing();
        clothing.setId(doc.getObjectId("_id"));
        clothing.setUserId(doc.getString("userId"));
        clothing.setName(doc.getString("name"));
        clothing.setCategory(Clothing.Category.valueOf(doc.getString("category")));
        clothing.setColor(doc.getString("color"));
        clothing.setSeason(Clothing.Season.valueOf(doc.getString("season")));
        clothing.setOccasion(Clothing.Occasion.valueOf(doc.getString("occasion")));
        clothing.setStyle(Clothing.Style.valueOf(doc.getString("style")));
        clothing.setImageUrl(doc.getString("imageUrl"));
        clothing.setWearCount(doc.getInteger("wearCount", 0));
        clothing.setInLaundry(doc.getBoolean("inLaundry", false));
        clothing.setFavorite(doc.getBoolean("isFavorite", false));
        
        Date dateAdded = doc.getDate("dateAdded");
        if (dateAdded != null) {
            clothing.setDateAdded(LocalDateTime.ofInstant(dateAdded.toInstant(), ZoneId.systemDefault()));
        }
        
        Date lastWorn = doc.getDate("lastWorn");
        if (lastWorn != null) {
            clothing.setLastWorn(LocalDateTime.ofInstant(lastWorn.toInstant(), ZoneId.systemDefault()));
        }
        
        return clothing;
    }
}
