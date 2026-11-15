package com.stylesync.controller;

import com.google.gson.Gson;
import com.stylesync.exception.DatabaseException;
import com.stylesync.model.Clothing;
import com.stylesync.service.RecommendationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * RecommendationServlet - generates smart outfit recommendations
 * Demonstrates PriorityQueue and Comparator usage through the service layer
 */
@WebServlet(name = "RecommendationServlet", urlPatterns = {"/recommendations", "/suggestions"})
public class RecommendationServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(RecommendationServlet.class);
    private RecommendationService recommendationService;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.recommendationService = new RecommendationService();
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
        
        // Show recommendation form
        request.getRequestDispatcher("/WEB-INF/views/suggestions.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }
        
        String userId = (String) session.getAttribute("userId");
        
        try {
            // Get recommendation parameters
            String occasionStr = request.getParameter("occasion");
            String seasonStr = request.getParameter("season");
            String styleStr = request.getParameter("style");
            String preferredColor = request.getParameter("preferredColor");
            
            Clothing.Occasion occasion = occasionStr != null && !occasionStr.isEmpty() ? 
                Clothing.Occasion.valueOf(occasionStr) : null;
            
            Clothing.Season season = seasonStr != null && !seasonStr.isEmpty() ? 
                Clothing.Season.valueOf(seasonStr) : null;
            
            Clothing.Style style = styleStr != null && !styleStr.isEmpty() ? 
                Clothing.Style.valueOf(styleStr) : null;
            
            // Generate recommendations using PriorityQueue ranking
            List<RecommendationService.OutfitRecommendation> recommendations = 
                recommendationService.generateRecommendations(
                    userId, occasion, season, style, preferredColor
                );
            
            logger.info("Generated {} recommendations for user {}", recommendations.size(), userId);
            
            // Set attributes for JSP
            request.setAttribute("recommendations", recommendations);
            request.setAttribute("recommendationCount", recommendations.size());
            request.setAttribute("selectedOccasion", occasion);
            request.setAttribute("selectedSeason", season);
            request.setAttribute("selectedStyle", style);
            request.setAttribute("preferredColor", preferredColor);
            
            // Forward to suggestions page
            request.getRequestDispatcher("/WEB-INF/views/suggestions.jsp").forward(request, response);
            
        } catch (DatabaseException e) {
            logger.error("Database error generating recommendations", e);
            request.setAttribute("error", "Failed to generate recommendations. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/suggestions.jsp").forward(request, response);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid parameters for recommendations", e);
            request.setAttribute("error", "Invalid selection. Please check your choices.");
            request.getRequestDispatcher("/WEB-INF/views/suggestions.jsp").forward(request, response);
        }
    }
}
