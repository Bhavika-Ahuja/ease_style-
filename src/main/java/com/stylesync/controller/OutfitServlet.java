package com.stylesync.controller;

import com.google.gson.Gson;
import com.stylesync.exception.DatabaseException;
import com.stylesync.exception.ResourceNotFoundException;
import com.stylesync.exception.ValidationException;
import com.stylesync.model.Clothing;
import com.stylesync.model.Outfit;
import com.stylesync.service.OutfitService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OutfitServlet - manages outfit CRUD operations
 */
@WebServlet(name = "OutfitServlet", urlPatterns = {"/outfits", "/outfits/*"})
public class OutfitServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(OutfitServlet.class);
    private OutfitService outfitService;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.outfitService = new OutfitService();
        this.gson = new Gson();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }
        
        String userId = (String) session.getAttribute("userId");
        String action = request.getParameter("action");
        
        try {
            if ("view".equals(action)) {
                // View specific outfit with clothing details
                String outfitId = request.getParameter("id");
                OutfitService.OutfitWithClothing outfitData = 
                    outfitService.getOutfitWithClothing(outfitId);
                
                request.setAttribute("outfit", outfitData.getOutfit());
                request.setAttribute("clothingItems", outfitData.getClothing());
                request.getRequestDispatcher("/WEB-INF/views/view-outfit.jsp").forward(request, response);
                
            } else if ("favorites".equals(action)) {
                // Get favorite outfits
                List<Outfit> favorites = outfitService.getFavoriteOutfits(userId);
                request.setAttribute("outfits", favorites);
                request.setAttribute("pageTitle", "Favorite Outfits");
                request.getRequestDispatcher("/WEB-INF/views/outfits.jsp").forward(request, response);
                
            } else {
                // List all outfits
                List<Outfit> outfits = outfitService.getUserOutfits(userId);
                request.setAttribute("outfits", outfits);
                request.getRequestDispatcher("/WEB-INF/views/outfits.jsp").forward(request, response);
            }
            
        } catch (ResourceNotFoundException e) {
            logger.warn("Outfit not found", e);
            response.sendRedirect(request.getContextPath() + "/outfits");
            
        } catch (DatabaseException e) {
            logger.error("Database error", e);
            request.setAttribute("error", "System error. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/outfits.jsp").forward(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            sendJsonResponse(response, 401, false, "Unauthorized");
            return;
        }
        
        String userId = (String) session.getAttribute("userId");
        String action = request.getParameter("action");
        
        try {
            if ("create".equals(action)) {
                // Create new outfit
                Outfit outfit = buildOutfitFromRequest(request, userId);
                outfit = outfitService.createOutfit(outfit);
                
                sendJsonResponse(response, 200, true, "Outfit created successfully", outfit);
                
            } else if ("update".equals(action)) {
                // Update outfit
                Outfit outfit = buildOutfitFromRequest(request, userId);
                outfit.setOutfitId(request.getParameter("outfitId"));
                outfit = outfitService.updateOutfit(outfit);
                
                sendJsonResponse(response, 200, true, "Outfit updated successfully", outfit);
                
            } else if ("toggleFavorite".equals(action)) {
                // Toggle favorite
                String outfitId = request.getParameter("outfitId");
                outfitService.toggleFavorite(outfitId);
                
                sendJsonResponse(response, 200, true, "Favorite status updated");
                
            } else if ("markWorn".equals(action)) {
                // Mark as worn
                String outfitId = request.getParameter("outfitId");
                outfitService.markAsWorn(outfitId);
                
                sendJsonResponse(response, 200, true, "Outfit marked as worn");
                
            } else {
                sendJsonResponse(response, 400, false, "Invalid action");
            }
            
        } catch (ValidationException e) {
            logger.warn("Validation error: {}", e.getMessage());
            sendJsonResponse(response, 400, false, e.getMessage());
            
        } catch (ResourceNotFoundException e) {
            logger.warn("Resource not found: {}", e.getMessage());
            sendJsonResponse(response, 404, false, e.getMessage());
            
        } catch (DatabaseException e) {
            logger.error("Database error", e);
            sendJsonResponse(response, 500, false, "System error. Please try again.");
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            sendJsonResponse(response, 401, false, "Unauthorized");
            return;
        }
        
        String outfitId = request.getParameter("id");
        
        try {
            outfitService.deleteOutfit(outfitId);
            sendJsonResponse(response, 200, true, "Outfit deleted successfully");
            
        } catch (ResourceNotFoundException e) {
            sendJsonResponse(response, 404, false, "Outfit not found");
            
        } catch (DatabaseException e) {
            logger.error("Database error", e);
            sendJsonResponse(response, 500, false, "System error. Please try again.");
        }
    }
    
    private Outfit buildOutfitFromRequest(HttpServletRequest request, String userId) {
        Outfit outfit = new Outfit();
        outfit.setUserId(userId);
        outfit.setName(request.getParameter("name"));
        
        // Parse clothing IDs (comma-separated)
        String clothingIdsStr = request.getParameter("clothingIds");
        if (clothingIdsStr != null && !clothingIdsStr.trim().isEmpty()) {
            List<String> clothingIds = Arrays.asList(clothingIdsStr.split(","));
            outfit.setClothingIds(clothingIds);
        }
        
        String season = request.getParameter("season");
        if (season != null && !season.isEmpty()) {
            outfit.setSeason(Clothing.Season.valueOf(season));
        }
        
        String occasion = request.getParameter("occasion");
        if (occasion != null && !occasion.isEmpty()) {
            outfit.setOccasion(Clothing.Occasion.valueOf(occasion));
        }
        
        String style = request.getParameter("style");
        if (style != null && !style.isEmpty()) {
            outfit.setStyle(Clothing.Style.valueOf(style));
        }
        
        outfit.setThumbnailUrl(request.getParameter("thumbnailUrl"));
        
        return outfit;
    }
    
    private void sendJsonResponse(HttpServletResponse response, int status, boolean success, 
                                 String message) throws IOException {
        sendJsonResponse(response, status, success, message, null);
    }
    
    private void sendJsonResponse(HttpServletResponse response, int status, boolean success, 
                                 String message, Object data) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> jsonResponse = new HashMap<>();
        jsonResponse.put("success", success);
        jsonResponse.put("message", message);
        if (data != null) {
            jsonResponse.put("data", data);
        }
        
        response.getWriter().write(gson.toJson(jsonResponse));
    }
}
