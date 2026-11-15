package com.stylesync.controller;

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
 * DashboardServlet - displays user dashboard
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(DashboardServlet.class);
    private AnalyticsService analyticsService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.analyticsService = new AnalyticsService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Check authentication
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }
        
        String userId = (String) session.getAttribute("userId");
        
        try {
            // Get dashboard statistics
            AnalyticsService.DashboardStats stats = analyticsService.getDashboardStats(userId);
            request.setAttribute("stats", stats);
            
            // Forward to dashboard JSP
            request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
            
        } catch (DatabaseException e) {
            logger.error("Error loading dashboard", e);
            request.setAttribute("error", "Failed to load dashboard");
            request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
        }
    }
}
