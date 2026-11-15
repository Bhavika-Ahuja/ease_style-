package com.stylesync.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.stylesync.database.MongoDBConnection;
import com.stylesync.exception.DatabaseException;
import com.stylesync.model.Clothing;
import com.stylesync.model.Outfit;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * OutfitRepository - demonstrates Java Collection Framework usage
 */
public class OutfitRepository implements Repository<Outfit> {
    private static final Logger logger = LoggerFactory.getLogger(OutfitRepository.class);
    private final MongoCollection<Document> collection;
    
    public OutfitRepository() {
        this.collection = MongoDBConnection.getInstance()
                                          .getDatabase()
                                          .getCollection("outfits");
    }
    
    @Override
    public Outfit save(Outfit outfit) throws DatabaseException {
        try {
            Document doc = outfitToDocument(outfit);
            
            if (outfit.getId() == null) {
                collection.insertOne(doc);
                outfit.setId(doc.getObjectId("_id"));
            } else {
                collection.replaceOne(Filters.eq("_id", outfit.getId()), doc);
            }
            
            logger.info("Outfit saved: {}", outfit.getName());
            return outfit;
        } catch (Exception e) {
            logger.error("Error saving outfit", e);
            throw new DatabaseException("Failed to save outfit", e);
        }
    }
    
    @Override
    public Optional<Outfit> findById(ObjectId id) throws DatabaseException {
        try {
            Document doc = collection.find(Filters.eq("_id", id)).first();
            return doc != null ? Optional.of(documentToOutfit(doc)) : Optional.empty();
        } catch (Exception e) {
            logger.error("Error finding outfit by ID", e);
            throw new DatabaseException("Failed to find outfit", e);
        }
    }
    
    @Override
    public Optional<Outfit> findById(String id) throws DatabaseException {
        return findById(new ObjectId(id));
    }
    
    /**
     * Find all outfits for a user
     * Uses ArrayList for dynamic operations
     */
    public List<Outfit> findByUserId(String userId) throws DatabaseException {
        try {
            List<Outfit> outfits = new ArrayList<>(); // ArrayList for dynamic list
            collection.find(Filters.eq("userId", userId))
                     .forEach(doc -> outfits.add(documentToOutfit(doc)));
            return outfits;
        } catch (Exception e) {
            logger.error("Error finding outfits by user ID", e);
            throw new DatabaseException("Failed to find outfits", e);
        }
    }
    
    /**
     * Find favorite outfits
     */
    public List<Outfit> findFavoritesByUserId(String userId) throws DatabaseException {
        try {
            List<Outfit> favorites = new ArrayList<>();
            collection.find(Filters.and(
                Filters.eq("userId", userId),
                Filters.eq("isFavorite", true)
            )).forEach(doc -> favorites.add(documentToOutfit(doc)));
            return favorites;
        } catch (Exception e) {
            logger.error("Error finding favorite outfits", e);
            throw new DatabaseException("Failed to find favorite outfits", e);
        }
    }
    
    /**
     * Find most worn outfits
     */
    public List<Outfit> findMostWornByUserId(String userId, int limit) throws DatabaseException {
        try {
            List<Outfit> mostWorn = new ArrayList<>();
            collection.find(Filters.eq("userId", userId))
                     .sort(Sorts.descending("wearCount"))
                     .limit(limit)
                     .forEach(doc -> mostWorn.add(documentToOutfit(doc)));
            return mostWorn;
        } catch (Exception e) {
            logger.error("Error finding most worn outfits", e);
            throw new DatabaseException("Failed to find most worn outfits", e);
        }
    }
    
    /**
     * Find recently created outfits
     */
    public List<Outfit> findRecentByUserId(String userId, int limit) throws DatabaseException {
        try {
            List<Outfit> recent = new ArrayList<>();
            collection.find(Filters.eq("userId", userId))
                     .sort(Sorts.descending("dateCreated"))
                     .limit(limit)
                     .forEach(doc -> recent.add(documentToOutfit(doc)));
            return recent;
        } catch (Exception e) {
            logger.error("Error finding recent outfits", e);
            throw new DatabaseException("Failed to find recent outfits", e);
        }
    }
    
    /**
     * Increment wear count
     */
    public void incrementWearCount(String outfitId) throws DatabaseException {
        try {
            collection.updateOne(
                Filters.eq("_id", new ObjectId(outfitId)),
                Updates.combine(
                    Updates.inc("wearCount", 1),
                    Updates.set("lastWorn", new Date())
                )
            );
        } catch (Exception e) {
            logger.error("Error incrementing outfit wear count", e);
            throw new DatabaseException("Failed to increment wear count", e);
        }
    }
    
    /**
     * Toggle favorite status
     */
    public void toggleFavorite(String outfitId, boolean isFavorite) throws DatabaseException {
        try {
            collection.updateOne(
                Filters.eq("_id", new ObjectId(outfitId)),
                Updates.set("isFavorite", isFavorite)
            );
        } catch (Exception e) {
            logger.error("Error toggling outfit favorite", e);
            throw new DatabaseException("Failed to toggle favorite", e);
        }
    }
    
    @Override
    public List<Outfit> findAll() throws DatabaseException {
        try {
            List<Outfit> outfits = new ArrayList<>();
            collection.find().forEach(doc -> outfits.add(documentToOutfit(doc)));
            return outfits;
        } catch (Exception e) {
            logger.error("Error finding all outfits", e);
            throw new DatabaseException("Failed to find outfits", e);
        }
    }
    
    @Override
    public boolean deleteById(ObjectId id) throws DatabaseException {
        try {
            return collection.deleteOne(Filters.eq("_id", id)).getDeletedCount() > 0;
        } catch (Exception e) {
            logger.error("Error deleting outfit", e);
            throw new DatabaseException("Failed to delete outfit", e);
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
            logger.error("Error counting outfits", e);
            throw new DatabaseException("Failed to count outfits", e);
        }
    }
    
    public long countByUserId(String userId) throws DatabaseException {
        try {
            return collection.countDocuments(Filters.eq("userId", userId));
        } catch (Exception e) {
            logger.error("Error counting user outfits", e);
            throw new DatabaseException("Failed to count outfits", e);
        }
    }
    
    @Override
    public boolean existsById(ObjectId id) throws DatabaseException {
        try {
            return collection.countDocuments(Filters.eq("_id", id)) > 0;
        } catch (Exception e) {
            logger.error("Error checking outfit existence", e);
            throw new DatabaseException("Failed to check outfit", e);
        }
    }
    
    // Helper methods
    private Document outfitToDocument(Outfit outfit) {
        Document doc = new Document();
        if (outfit.getId() != null) {
            doc.append("_id", outfit.getId());
        }
        doc.append("userId", outfit.getUserId())
           .append("name", outfit.getName())
           .append("clothingIds", new ArrayList<>(outfit.getClothingIds())) // ArrayList conversion
           .append("wearCount", outfit.getWearCount())
           .append("isFavorite", outfit.isFavorite())
           .append("dateCreated", Date.from(outfit.getDateCreated().atZone(ZoneId.systemDefault()).toInstant()));
        
        if (outfit.getSeason() != null) {
            doc.append("season", outfit.getSeason().name());
        }
        if (outfit.getOccasion() != null) {
            doc.append("occasion", outfit.getOccasion().name());
        }
        if (outfit.getStyle() != null) {
            doc.append("style", outfit.getStyle().name());
        }
        if (outfit.getThumbnailUrl() != null) {
            doc.append("thumbnailUrl", outfit.getThumbnailUrl());
        }
        if (outfit.getLastWorn() != null) {
            doc.append("lastWorn", Date.from(outfit.getLastWorn().atZone(ZoneId.systemDefault()).toInstant()));
        }
        
        return doc;
    }
    
    private Outfit documentToOutfit(Document doc) {
        Outfit outfit = new Outfit();
        outfit.setId(doc.getObjectId("_id"));
        outfit.setUserId(doc.getString("userId"));
        outfit.setName(doc.getString("name"));
        
        @SuppressWarnings("unchecked")
        List<String> clothingIds = (List<String>) doc.get("clothingIds");
        outfit.setClothingIds(clothingIds != null ? new ArrayList<>(clothingIds) : new ArrayList<>());
        
        outfit.setWearCount(doc.getInteger("wearCount", 0));
        outfit.setFavorite(doc.getBoolean("isFavorite", false));
        
        String season = doc.getString("season");
        if (season != null) {
            outfit.setSeason(Clothing.Season.valueOf(season));
        }
        
        String occasion = doc.getString("occasion");
        if (occasion != null) {
            outfit.setOccasion(Clothing.Occasion.valueOf(occasion));
        }
        
        String style = doc.getString("style");
        if (style != null) {
            outfit.setStyle(Clothing.Style.valueOf(style));
        }
        
        outfit.setThumbnailUrl(doc.getString("thumbnailUrl"));
        
        Date dateCreated = doc.getDate("dateCreated");
        if (dateCreated != null) {
            outfit.setDateCreated(LocalDateTime.ofInstant(dateCreated.toInstant(), ZoneId.systemDefault()));
        }
        
        Date lastWorn = doc.getDate("lastWorn");
        if (lastWorn != null) {
            outfit.setLastWorn(LocalDateTime.ofInstant(lastWorn.toInstant(), ZoneId.systemDefault()));
        }
        
        return outfit;
    }
}
