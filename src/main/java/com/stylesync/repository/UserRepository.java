package com.stylesync.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.stylesync.database.MongoDBConnection;
import com.stylesync.exception.DatabaseException;
import com.stylesync.model.User;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * UserRepository - implements Repository interface
 * Demonstrates INHERITANCE and POLYMORPHISM
 */
public class UserRepository implements Repository<User> {
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);
    private final MongoCollection<Document> collection;
    
    public UserRepository() {
        this.collection = MongoDBConnection.getInstance()
                                          .getDatabase()
                                          .getCollection("users");
    }
    
    @Override
    public User save(User user) throws DatabaseException {
        try {
            Document doc = userToDocument(user);
            
            if (user.getId() == null) {
                // Insert new user
                collection.insertOne(doc);
                user.setId(doc.getObjectId("_id"));
            } else {
                // Update existing user
                collection.replaceOne(
                    Filters.eq("_id", user.getId()),
                    doc
                );
            }
            
            logger.info("User saved: {}", user.getEmail());
            return user;
        } catch (Exception e) {
            logger.error("Error saving user", e);
            throw new DatabaseException("Failed to save user", e);
        }
    }
    
    @Override
    public Optional<User> findById(ObjectId id) throws DatabaseException {
        try {
            Document doc = collection.find(Filters.eq("_id", id)).first();
            return doc != null ? Optional.of(documentToUser(doc)) : Optional.empty();
        } catch (Exception e) {
            logger.error("Error finding user by ID", e);
            throw new DatabaseException("Failed to find user", e);
        }
    }
    
    @Override
    public Optional<User> findById(String id) throws DatabaseException {
        return findById(new ObjectId(id));
    }
    
    public Optional<User> findByEmail(String email) throws DatabaseException {
        try {
            Document doc = collection.find(Filters.eq("email", email.toLowerCase())).first();
            return doc != null ? Optional.of(documentToUser(doc)) : Optional.empty();
        } catch (Exception e) {
            logger.error("Error finding user by email", e);
            throw new DatabaseException("Failed to find user by email", e);
        }
    }
    
    public boolean existsByEmail(String email) throws DatabaseException {
        try {
            return collection.countDocuments(Filters.eq("email", email.toLowerCase())) > 0;
        } catch (Exception e) {
            logger.error("Error checking email existence", e);
            throw new DatabaseException("Failed to check email", e);
        }
    }
    
    public void updateLastLogin(String userId) throws DatabaseException {
        try {
            collection.updateOne(
                Filters.eq("_id", new ObjectId(userId)),
                Updates.set("lastLogin", new Date())
            );
        } catch (Exception e) {
            logger.error("Error updating last login", e);
            throw new DatabaseException("Failed to update last login", e);
        }
    }
    
    @Override
    public List<User> findAll() throws DatabaseException {
        try {
            List<User> users = new ArrayList<>(); // ArrayList for dynamic list
            collection.find().forEach(doc -> users.add(documentToUser(doc)));
            return users;
        } catch (Exception e) {
            logger.error("Error finding all users", e);
            throw new DatabaseException("Failed to find users", e);
        }
    }
    
    @Override
    public boolean deleteById(ObjectId id) throws DatabaseException {
        try {
            return collection.deleteOne(Filters.eq("_id", id)).getDeletedCount() > 0;
        } catch (Exception e) {
            logger.error("Error deleting user", e);
            throw new DatabaseException("Failed to delete user", e);
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
            logger.error("Error counting users", e);
            throw new DatabaseException("Failed to count users", e);
        }
    }
    
    @Override
    public boolean existsById(ObjectId id) throws DatabaseException {
        try {
            return collection.countDocuments(Filters.eq("_id", id)) > 0;
        } catch (Exception e) {
            logger.error("Error checking user existence", e);
            throw new DatabaseException("Failed to check user", e);
        }
    }
    
    // Helper methods to convert between User and Document
    private Document userToDocument(User user) {
        Document doc = new Document();
        if (user.getId() != null) {
            doc.append("_id", user.getId());
        }
        doc.append("name", user.getName())
           .append("email", user.getEmail().toLowerCase())
           .append("passwordHash", user.getPasswordHash())
           .append("createdAt", Date.from(user.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()));
        
        if (user.getLastLogin() != null) {
            doc.append("lastLogin", Date.from(user.getLastLogin().atZone(ZoneId.systemDefault()).toInstant()));
        }
        
        return doc;
    }
    
    private User documentToUser(Document doc) {
        User user = new User();
        user.setId(doc.getObjectId("_id"));
        user.setName(doc.getString("name"));
        user.setEmail(doc.getString("email"));
        user.setPasswordHash(doc.getString("passwordHash"));
        
        Date createdAt = doc.getDate("createdAt");
        if (createdAt != null) {
            user.setCreatedAt(LocalDateTime.ofInstant(createdAt.toInstant(), ZoneId.systemDefault()));
        }
        
        Date lastLogin = doc.getDate("lastLogin");
        if (lastLogin != null) {
            user.setLastLogin(LocalDateTime.ofInstant(lastLogin.toInstant(), ZoneId.systemDefault()));
        }
        
        return user;
    }
}
