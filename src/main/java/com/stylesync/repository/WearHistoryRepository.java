package com.stylesync.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.stylesync.database.MongoDBConnection;
import com.stylesync.exception.DatabaseException;
import com.stylesync.model.WearHistory;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * WearHistoryRepository - tracks clothing and outfit wear history
 */
public class WearHistoryRepository implements Repository<WearHistory> {
    private static final Logger logger = LoggerFactory.getLogger(WearHistoryRepository.class);
    private final MongoCollection<Document> collection;
    
    public WearHistoryRepository() {
        this.collection = MongoDBConnection.getInstance()
                                          .getDatabase()
                                          .getCollection("wear_history");
    }
    
    @Override
    public WearHistory save(WearHistory history) throws DatabaseException {
        try {
            Document doc = wearHistoryToDocument(history);
            
            if (history.getId() == null) {
                collection.insertOne(doc);
                history.setId(doc.getObjectId("_id"));
            } else {
                collection.replaceOne(Filters.eq("_id", history.getId()), doc);
            }
            
            logger.info("Wear history saved for item: {}", history.getItemId());
            return history;
        } catch (Exception e) {
            logger.error("Error saving wear history", e);
            throw new DatabaseException("Failed to save wear history", e);
        }
    }
    
    @Override
    public Optional<WearHistory> findById(ObjectId id) throws DatabaseException {
        try {
            Document doc = collection.find(Filters.eq("_id", id)).first();
            return doc != null ? Optional.of(documentToWearHistory(doc)) : Optional.empty();
        } catch (Exception e) {
            logger.error("Error finding wear history by ID", e);
            throw new DatabaseException("Failed to find wear history", e);
        }
    }
    
    @Override
    public Optional<WearHistory> findById(String id) throws DatabaseException {
        return findById(new ObjectId(id));
    }
    
    /**
     * Find wear history by user
     */
    public List<WearHistory> findByUserId(String userId) throws DatabaseException {
        try {
            List<WearHistory> history = new ArrayList<>();
            collection.find(Filters.eq("userId", userId))
                     .sort(Sorts.descending("wornOn"))
                     .forEach(doc -> history.add(documentToWearHistory(doc)));
            return history;
        } catch (Exception e) {
            logger.error("Error finding wear history by user", e);
            throw new DatabaseException("Failed to find wear history", e);
        }
    }
    
    /**
     * Find wear history for a specific item
     */
    public List<WearHistory> findByItemId(String userId, String itemId) throws DatabaseException {
        try {
            List<WearHistory> history = new ArrayList<>();
            collection.find(Filters.and(
                Filters.eq("userId", userId),
                Filters.eq("itemId", itemId)
            ))
            .sort(Sorts.descending("wornOn"))
            .forEach(doc -> history.add(documentToWearHistory(doc)));
            return history;
        } catch (Exception e) {
            logger.error("Error finding item wear history", e);
            throw new DatabaseException("Failed to find item wear history", e);
        }
    }
    
    /**
     * Find recent wear history
     */
    public List<WearHistory> findRecentByUserId(String userId, int limit) throws DatabaseException {
        try {
            List<WearHistory> recent = new ArrayList<>();
            collection.find(Filters.eq("userId", userId))
                     .sort(Sorts.descending("wornOn"))
                     .limit(limit)
                     .forEach(doc -> recent.add(documentToWearHistory(doc)));
            return recent;
        } catch (Exception e) {
            logger.error("Error finding recent wear history", e);
            throw new DatabaseException("Failed to find recent wear history", e);
        }
    }
    
    @Override
    public List<WearHistory> findAll() throws DatabaseException {
        try {
            List<WearHistory> history = new ArrayList<>();
            collection.find().forEach(doc -> history.add(documentToWearHistory(doc)));
            return history;
        } catch (Exception e) {
            logger.error("Error finding all wear history", e);
            throw new DatabaseException("Failed to find wear history", e);
        }
    }
    
    @Override
    public boolean deleteById(ObjectId id) throws DatabaseException {
        try {
            return collection.deleteOne(Filters.eq("_id", id)).getDeletedCount() > 0;
        } catch (Exception e) {
            logger.error("Error deleting wear history", e);
            throw new DatabaseException("Failed to delete wear history", e);
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
            logger.error("Error counting wear history", e);
            throw new DatabaseException("Failed to count wear history", e);
        }
    }
    
    @Override
    public boolean existsById(ObjectId id) throws DatabaseException {
        try {
            return collection.countDocuments(Filters.eq("_id", id)) > 0;
        } catch (Exception e) {
            logger.error("Error checking wear history existence", e);
            throw new DatabaseException("Failed to check wear history", e);
        }
    }
    
    // Helper methods
    private Document wearHistoryToDocument(WearHistory history) {
        Document doc = new Document();
        if (history.getId() != null) {
            doc.append("_id", history.getId());
        }
        doc.append("userId", history.getUserId())
           .append("itemId", history.getItemId())
           .append("itemType", history.getItemType().name())
           .append("wornOn", Date.from(history.getWornOn().atZone(ZoneId.systemDefault()).toInstant()));
        
        if (history.getNotes() != null) {
            doc.append("notes", history.getNotes());
        }
        
        return doc;
    }
    
    private WearHistory documentToWearHistory(Document doc) {
        WearHistory history = new WearHistory();
        history.setId(doc.getObjectId("_id"));
        history.setUserId(doc.getString("userId"));
        history.setItemId(doc.getString("itemId"));
        history.setItemType(WearHistory.ItemType.valueOf(doc.getString("itemType")));
        history.setNotes(doc.getString("notes"));
        
        Date wornOn = doc.getDate("wornOn");
        if (wornOn != null) {
            history.setWornOn(LocalDateTime.ofInstant(wornOn.toInstant(), ZoneId.systemDefault()));
        }
        
        return history;
    }
}
