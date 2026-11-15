package com.stylesync.controller;

import com.google.gson.Gson;
import com.stylesync.exception.DatabaseException;
import com.stylesync.service.AnalyticsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * AnalyticsServlet - displays wardrobe analytics and statistics
 * Demonstrates HashMap usage for analytics data
 */
@WebServlet(name = "AnalyticsServlet", urlPatterns = {"/analytics"})
public class AnalyticsServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsServlet.class);
    private AnalyticsService analyticsService;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.analyticsService = new AnalyticsService();
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
        String format = request.getParameter("format");
        
        try {
            // Get comprehensive analytics
            AnalyticsService.WardrobeAnalytics analytics = 
                analyticsService.getWardrobeAnalytics(userId);
            
            if ("json".equals(format)) {
                // Return JSON for Chart.js
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(gson.toJson(analytics));
            } else {
                // Forward to analytics JSP
                request.setAttribute("analytics", analytics);
                request.getRequestDispatcher("/WEB-INF/views/analytics.jsp").forward(request, response);
            }
            
        } catch (DatabaseException e) {
            logger.error("Error loading analytics", e);
            request.setAttribute("error", "Failed to load analytics");
            request.getRequestDispatcher("/WEB-INF/views/analytics.jsp").forward(request, response);
        }
    }
}
