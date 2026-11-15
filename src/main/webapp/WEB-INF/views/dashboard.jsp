<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.stylesync.service.AnalyticsService" %>
<%@ page import="com.stylesync.model.Clothing" %>
<%@ page import="java.util.*" %>
<%
    // Authentication check
    HttpSession userSession = request.getSession(false);
    if (userSession == null || userSession.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/");
        return;
    }
    String userName = (String) userSession.getAttribute("userName");
    
    // Get dashboard stats from request
    AnalyticsService.DashboardStats stats = 
        (AnalyticsService.DashboardStats) request.getAttribute("stats");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - StyleSync</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
</head>
<body>
    <div class="main-wrapper">
        <!-- Sidebar -->
        <aside class="sidebar">
            <div class="sidebar-header">
                <h2><i class="fas fa-tshirt"></i> StyleSync</h2>
                <p>Welcome, <%= userName %>!</p>
            </div>
            <ul class="sidebar-menu">
                <li><a href="<%= request.getContextPath() %>/dashboard" class="active">
                    <i class="fas fa-home"></i> Dashboard
                </a></li>
                <li><a href="<%= request.getContextPath() %>/wardrobe">
                    <i class="fas fa-tshirt"></i> My Wardrobe
                </a></li>
                <li><a href="<%= request.getContextPath() %>/wardrobe?action=edit&mode=add">
                    <i class="fas fa-plus"></i> Add Clothing
                </a></li>
                <li><a href="<%= request.getContextPath() %>/suggestions">
                    <i class="fas fa-magic"></i> Smart Suggestions
                </a></li>
                <li><a href="<%= request.getContextPath() %>/outfits">
                    <i class="fas fa-layer-group"></i> Outfit Planner
                </a></li>
                <li><a href="<%= request.getContextPath() %>/search">
                    <i class="fas fa-search"></i> Search
                </a></li>
                <li><a href="<%= request.getContextPath() %>/analytics">
                    <i class="fas fa-chart-line"></i> Analytics
                </a></li>
                <li><a href="<%= request.getContextPath() %>/wardrobe?action=favorites">
                    <i class="fas fa-heart"></i> Favourites
                </a></li>
                <li><a href="<%= request.getContextPath() %>/logout">
                    <i class="fas fa-sign-out-alt"></i> Logout
                </a></li>
            </ul>
        </aside>
        
        <!-- Main Content -->
        <main class="main-content">
            <!-- Page Header -->
            <div class="page-header">
                <div class="page-title">
                    <h1>Dashboard</h1>
                    <p>Welcome back, <%= userName %>! Here's your wardrobe summary.</p>
                </div>
                <div class="header-actions">
                    <a href="<%= request.getContextPath() %>/suggestions" class="btn btn-primary">
                        <i class="fas fa-magic"></i> Get Outfit Suggestions
                    </a>
                </div>
            </div>
            
            <!-- Statistics Cards -->
            <div class="row g-4 mb-4">
                <div class="col-md-3">
                    <div class="card stat-card">
                        <div class="stat-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                            <i class="fas fa-tshirt text-white"></i>
                        </div>
                        <h3><%= stats != null ? stats.getTotalClothingItems() : 0 %></h3>
                        <p>Total Clothing Items</p>
                    </div>
                </div>
                
                <div class="col-md-3">
                    <div class="card stat-card">
                        <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb, #f5576c);">
                            <i class="fas fa-layer-group text-white"></i>
                        </div>
                        <h3><%= stats != null ? stats.getTotalOutfits() : 0 %></h3>
                        <p>Saved Outfits</p>
                    </div>
                </div>
                
                <div class="col-md-3">
                    <div class="card stat-card">
                        <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe, #00f2fe);">
                            <i class="fas fa-heart text-white"></i>
                        </div>
                        <h3><%= stats != null ? stats.getFavoriteClothingCount() : 0 %></h3>
                        <p>Favourite Items</p>
                    </div>
                </div>
                
                <div class="col-md-3">
                    <div class="card stat-card">
                        <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b, #38f9d7);">
                            <i class="fas fa-star text-white"></i>
                        </div>
                        <h3><%= stats != null ? stats.getFavoriteOutfitsCount() : 0 %></h3>
                        <p>Favourite Outfits</p>
                    </div>
                </div>
            </div>
            
            <!-- Quick Actions -->
            <div class="card mb-4">
                <h5 class="mb-3"><i class="fas fa-bolt"></i> Quick Actions</h5>
                <div class="d-flex gap-3 flex-wrap">
                    <a href="<%= request.getContextPath() %>/wardrobe?action=edit&mode=add" class="btn btn-primary">
                        <i class="fas fa-plus"></i> Add New Clothing
                    </a>
                    <a href="<%= request.getContextPath() %>/suggestions" class="btn btn-secondary">
                        <i class="fas fa-magic"></i> Get Suggestions
                    </a>
                    <a href="<%= request.getContextPath() %>/outfits?action=create" class="btn btn-secondary">
                        <i class="fas fa-layer-group"></i> Create Outfit
                    </a>
                    <a href="<%= request.getContextPath() %>/analytics" class="btn btn-secondary">
                        <i class="fas fa-chart-line"></i> View Analytics
                    </a>
                </div>
            </div>
            
            <% if (stats != null && stats.getMostWornClothing() != null) { %>
            <!-- Most Worn Item -->
            <div class="card mb-4">
                <h5 class="mb-3"><i class="fas fa-fire"></i> Most Worn Item</h5>
                <div class="row align-items-center">
                    <div class="col-md-2">
                        <img src="<%= stats.getMostWornClothing().getImageUrl() %>" 
                             alt="<%= stats.getMostWornClothing().getName() %>"
                             class="img-fluid rounded" style="height: 120px; object-fit: cover;">
                    </div>
                    <div class="col-md-10">
                        <h4><%= stats.getMostWornClothing().getName() %></h4>
                        <p class="text-muted mb-2">
                            <span class="badge badge-category"><%= stats.getMostWornClothing().getCategory().getDisplayName() %></span>
                            <span class="badge badge-color"><%= stats.getMostWornClothing().getColor() %></span>
                            <span class="badge badge-season"><%= stats.getMostWornClothing().getSeason().getDisplayName() %></span>
                        </p>
                        <p><strong>Worn <%= stats.getMostWornClothing().getWearCount() %> times</strong></p>
                    </div>
                </div>
            </div>
            <% } %>
            
            <!-- Recently Added Clothing -->
            <% if (stats != null && stats.getRecentlyAdded() != null && !stats.getRecentlyAdded().isEmpty()) { %>
            <div class="card">
                <h5 class="mb-4"><i class="fas fa-clock"></i> Recently Added</h5>
                <div class="clothing-grid">
                    <% for (Clothing clothing : stats.getRecentlyAdded()) { %>
                    <div class="clothing-card">
                        <img src="<%= clothing.getImageUrl() %>" 
                             alt="<%= clothing.getName() %>"
                             class="clothing-image">
                        <div class="clothing-body">
                            <h6 class="clothing-name"><%= clothing.getName() %></h6>
                            <div class="clothing-meta">
                                <span class="badge badge-category"><%= clothing.getCategory().getDisplayName() %></span>
                                <span class="badge badge-color"><%= clothing.getColor() %></span>
                            </div>
                            <div class="clothing-actions">
                                <button class="btn-icon" title="View">
                                    <i class="fas fa-eye"></i>
                                </button>
                                <button class="btn-icon favorite <%= clothing.isFavorite() ? "active" : "" %>" title="Favorite">
                                    <i class="fas fa-heart"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                    <% } %>
                </div>
            </div>
            <% } %>
            
            <% if (stats == null || stats.getTotalClothingItems() == 0) { %>
            <!-- Empty State -->
            <div class="card text-center py-5">
                <i class="fas fa-tshirt fa-4x text-muted mb-3"></i>
                <h4>Your Wardrobe is Empty</h4>
                <p class="text-muted mb-4">Start by adding your first clothing item</p>
                <a href="<%= request.getContextPath() %>/wardrobe?action=edit&mode=add" 
                   class="btn btn-primary">
                    <i class="fas fa-plus"></i> Add Your First Item
                </a>
            </div>
            <% } %>
        </main>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
