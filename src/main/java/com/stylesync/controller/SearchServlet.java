package com.stylesync.controller;

import com.google.gson.Gson;
import com.stylesync.exception.DatabaseException;
import com.stylesync.model.Clothing;
import com.stylesync.service.SearchService;
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
import java.util.Map;
import java.util.Set;

/**
 * SearchServlet - handles advanced clothing search and filtering
 * Demonstrates Java Collection Framework usage in web tier
 */
@WebServlet(name = "SearchServlet", urlPatterns = {"/search"})
public class SearchServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(SearchServlet.class);
    private SearchService searchService;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.searchService = new SearchService();
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
            if ("colors".equals(action)) {
                // Get unique colors - uses TreeSet
                Set<String> colors = searchService.getUniqueColors(userId);
                sendJsonResponse(response, colors);
                
            } else if ("categories".equals(action)) {
                // Get available categories - uses HashSet
                Set<Clothing.Category> categories = searchService.getAvailableCategories(userId);
                sendJsonResponse(response, categories);
                
            } else if ("groupByCategory".equals(action)) {
                // Group by category - uses HashMap
                Map<Clothing.Category, List<Clothing>> grouped = 
                    searchService.groupByCategory(userId);
                sendJsonResponse(response, grouped);
                
            } else {
                // Show search page with filters
                Set<String> colors = searchService.getUniqueColors(userId);
                request.setAttribute("colors", colors);
                request.getRequestDispatcher("/WEB-INF/views/search.jsp").forward(request, response);
            }
            
        } catch (DatabaseException e) {
            logger.error("Database error during search", e);
            request.setAttribute("error", "System error. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/search.jsp").forward(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            sendJsonResponse(response, Map.of("error", "Unauthorized"));
            return;
        }
        
        String userId = (String) session.getAttribute("userId");
        
        try {
            // Build search criteria
            SearchService.SearchCriteria criteria = buildSearchCriteria(request);
            
            // Execute search - demonstrates Java Collection Framework
            List<Clothing> results = searchService.searchClothing(userId, criteria);
            
            logger.info("Search completed: {} results found", results.size());
            
            // Return results
            request.setAttribute("results", results);
            request.setAttribute("resultCount", results.size());
            request.setAttribute("criteria", criteria);
            
            // Get unique colors for filters
            Set<String> colors = searchService.getUniqueColors(userId);
            request.setAttribute("colors", colors);
            
            request.getRequestDispatcher("/WEB-INF/views/search.jsp").forward(request, response);
            
        } catch (DatabaseException e) {
            logger.error("Database error during search", e);
            request.setAttribute("error", "System error. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/search.jsp").forward(request, response);
        }
    }
    
    private SearchService.SearchCriteria buildSearchCriteria(HttpServletRequest request) {
        SearchService.SearchCriteria criteria = new SearchService.SearchCriteria();
        
        String searchQuery = request.getParameter("searchQuery");
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            criteria.setSearchQuery(searchQuery.trim());
        }
        
        String category = request.getParameter("category");
        if (category != null && !category.isEmpty() && !"ALL".equals(category)) {
            criteria.setCategory(Clothing.Category.valueOf(category));
        }
        
        String color = request.getParameter("color");
        if (color != null && !color.trim().isEmpty()) {
            criteria.setColor(color.trim());
        }
        
        String season = request.getParameter("season");
        if (season != null && !season.isEmpty() && !"ALL".equals(season)) {
            criteria.setSeason(Clothing.Season.valueOf(season));
        }
        
        String occasion = request.getParameter("occasion");
        if (occasion != null && !occasion.isEmpty() && !"ALL".equals(occasion)) {
            criteria.setOccasion(Clothing.Occasion.valueOf(occasion));
        }
        
        String style = request.getParameter("style");
        if (style != null && !style.isEmpty() && !"ALL".equals(style)) {
            criteria.setStyle(Clothing.Style.valueOf(style));
        }
        
        String onlyFavorites = request.getParameter("onlyFavorites");
        criteria.setOnlyFavorites("true".equals(onlyFavorites));
        
        String excludeLaundry = request.getParameter("excludeLaundry");
        criteria.setExcludeLaundry("true".equals(excludeLaundry));
        
        String sortBy = request.getParameter("sortBy");
        if (sortBy != null && !sortBy.isEmpty()) {
            criteria.setSortBy(sortBy);
        }
        
        String order = request.getParameter("order");
        criteria.setAscending(!"desc".equalsIgnoreCase(order));
        
        return criteria;
    }
    
    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(data));
    }
}
