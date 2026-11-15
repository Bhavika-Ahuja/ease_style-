package com.stylesync.controller;

import com.google.gson.Gson;
import com.stylesync.exception.DatabaseException;
import com.stylesync.exception.ResourceNotFoundException;
import com.stylesync.exception.ValidationException;
import com.stylesync.model.Clothing;
import com.stylesync.service.WardrobeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WardrobeServlet - manages clothing CRUD operations
 * Demonstrates RESTful-style servlet with JSON responses
 */
@WebServlet(name = "WardrobeServlet", urlPatterns = {"/wardrobe", "/wardrobe/*"})
public class WardrobeServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(WardrobeServlet.class);
    private WardrobeService wardrobeService;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.wardrobeService = new WardrobeService();
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
                // View specific clothing
                String clothingId = request.getParameter("id");
                Clothing clothing = wardrobeService.getClothingById(clothingId);
                request.setAttribute("clothing", clothing);
                request.getRequestDispatcher("/WEB-INF/views/view-clothing.jsp").forward(request, response);
                
            } else if ("edit".equals(action)) {
                // Edit form
                String clothingId = request.getParameter("id");
                Clothing clothing = wardrobeService.getClothingById(clothingId);
                request.setAttribute("clothing", clothing);
                request.setAttribute("mode", "edit");
                request.getRequestDispatcher("/WEB-INF/views/add-clothing.jsp").forward(request, response);
                
            } else if ("favorites".equals(action)) {
                // Get favorites
                List<Clothing> favorites = wardrobeService.getFavoriteClothing(userId);
                request.setAttribute("clothingList", favorites);
                request.setAttribute("pageTitle", "Favorite Clothing");
                request.getRequestDispatcher("/WEB-INF/views/wardrobe.jsp").forward(request, response);
                
            } else {
                // List all clothing
                List<Clothing> clothingList = wardrobeService.getUserClothing(userId);
                request.setAttribute("clothingList", clothingList);
                request.getRequestDispatcher("/WEB-INF/views/wardrobe.jsp").forward(request, response);
            }
            
        } catch (ResourceNotFoundException e) {
            logger.warn("Clothing not found", e);
            request.setAttribute("error", "Clothing not found");
            response.sendRedirect(request.getContextPath() + "/wardrobe");
            
        } catch (DatabaseException e) {
            logger.error("Database error", e);
            request.setAttribute("error", "System error. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/wardrobe.jsp").forward(request, response);
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
            if ("add".equals(action)) {
                // Add new clothing
                Clothing clothing = buildClothingFromRequest(request, userId);
                clothing = wardrobeService.addClothing(clothing);
                
                sendJsonResponse(response, 200, true, "Clothing added successfully", clothing);
                
            } else if ("update".equals(action)) {
                // Update clothing
                Clothing clothing = buildClothingFromRequest(request, userId);
                clothing.setClothingId(request.getParameter("clothingId"));
                clothing = wardrobeService.updateClothing(clothing);
                
                sendJsonResponse(response, 200, true, "Clothing updated successfully", clothing);
                
            } else if ("toggleFavorite".equals(action)) {
                // Toggle favorite
                String clothingId = request.getParameter("clothingId");
                wardrobeService.toggleFavorite(clothingId);
                
                sendJsonResponse(response, 200, true, "Favorite status updated");
                
            } else if ("toggleLaundry".equals(action)) {
                // Toggle laundry status
                String clothingId = request.getParameter("clothingId");
                boolean inLaundry = Boolean.parseBoolean(request.getParameter("inLaundry"));
                wardrobeService.updateLaundryStatus(clothingId, inLaundry);
                
                sendJsonResponse(response, 200, true, "Laundry status updated");
                
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
        
        String clothingId = request.getParameter("id");
        
        try {
            wardrobeService.deleteClothing(clothingId);
            sendJsonResponse(response, 200, true, "Clothing deleted successfully");
            
        } catch (ResourceNotFoundException e) {
            sendJsonResponse(response, 404, false, "Clothing not found");
            
        } catch (DatabaseException e) {
            logger.error("Database error", e);
            sendJsonResponse(response, 500, false, "System error. Please try again.");
        }
    }
    
    private Clothing buildClothingFromRequest(HttpServletRequest request, String userId) {
        Clothing clothing = new Clothing();
        clothing.setUserId(userId);
        clothing.setName(request.getParameter("name"));
        clothing.setCategory(Clothing.Category.valueOf(request.getParameter("category")));
        clothing.setColor(request.getParameter("color"));
        clothing.setSeason(Clothing.Season.valueOf(request.getParameter("season")));
        clothing.setOccasion(Clothing.Occasion.valueOf(request.getParameter("occasion")));
        clothing.setStyle(Clothing.Style.valueOf(request.getParameter("style")));
        clothing.setImageUrl(request.getParameter("imageUrl"));
        
        return clothing;
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
