<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.stylesync.service.AnalyticsService" %>
<%@ page import="com.stylesync.model.Clothing" %>
<%@ page import="java.util.*" %>
<%
    HttpSession userSession = request.getSession(false);
    if (userSession == null || userSession.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/");
        return;
    }
    String userName = (String) userSession.getAttribute("userName");
    AnalyticsService.WardrobeAnalytics analytics = 
        (AnalyticsService.WardrobeAnalytics) request.getAttribute("analytics");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Analytics - StyleSync</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
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
                <li><a href="<%= request.getContextPath() %>/dashboard">
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
                <li><a href="<%= request.getContextPath() %>/analytics" class="active">
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
            <div class="page-header">
                <div class="page-title">
                    <h1><i class="fas fa-chart-line"></i> Wardrobe Analytics</h1>
                    <p>Insights into your clothing usage and preferences</p>
                </div>
            </div>
            
            <% if (analytics != null) { %>
                <!-- Summary Statistics -->
                <div class="row g-4 mb-4">
                    <div class="col-md-3">
                        <div class="card stat-card">
                            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                                <i class="fas fa-tshirt text-white"></i>
                            </div>
                            <h3><%= analytics.getTotalClothingItems() %></h3>
                            <p>Total Items</p>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="card stat-card">
                            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb, #f5576c);">
                                <i class="fas fa-layer-group text-white"></i>
                            </div>
                            <h3><%= analytics.getTotalOutfits() %></h3>
                            <p>Total Outfits</p>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="card stat-card">
                            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe, #00f2fe);">
                                <i class="fas fa-heart text-white"></i>
                            </div>
                            <h3><%= analytics.getFavoriteClothingCount() %></h3>
                            <p>Favorite Items</p>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="card stat-card">
                            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b, #38f9d7);">
                                <i class="fas fa-star text-white"></i>
                            </div>
                            <h3><%= analytics.getFavoriteOutfitsCount() %></h3>
                            <p>Favorite Outfits</p>
                        </div>
                    </div>
                </div>
                
                <!-- Charts Row -->
                <div class="row mb-4">
                    <div class="col-md-6">
                        <div class="card">
                            <h5 class="mb-4"><i class="fas fa-chart-pie"></i> Category Distribution</h5>
                            <canvas id="categoryChart"></canvas>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="card">
                            <h5 class="mb-4"><i class="fas fa-chart-bar"></i> Color Frequency</h5>
                            <canvas id="colorChart"></canvas>
                        </div>
                    </div>
                </div>
                
                <div class="row mb-4">
                    <div class="col-md-6">
                        <div class="card">
                            <h5 class="mb-4"><i class="fas fa-sun"></i> Season Distribution</h5>
                            <canvas id="seasonChart"></canvas>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="card">
                            <h5 class="mb-4"><i class="fas fa-palette"></i> Style Distribution</h5>
                            <canvas id="styleChart"></canvas>
                        </div>
                    </div>
                </div>
                
                <!-- Most Worn Items -->
                <% if (analytics.getMostWornClothing() != null && !analytics.getMostWornClothing().isEmpty()) { %>
                    <div class="card mb-4">
                        <h5 class="mb-4"><i class="fas fa-fire"></i> Most Worn Items</h5>
                        <div class="row">
                            <% for (Clothing item : analytics.getMostWornClothing()) { %>
                                <div class="col-md-4 mb-3">
                                    <div class="d-flex align-items-center">
                                        <img src="<%= item.getImageUrl() %>" 
                                             alt="<%= item.getName() %>"
                                             style="width: 80px; height: 80px; object-fit: cover; border-radius: 10px; margin-right: 15px;"
                                             onerror="this.src='https://via.placeholder.com/80x80/667eea/FFFFFF?text=Item'">
                                        <div>
                                            <h6 class="mb-1"><%= item.getName() %></h6>
                                            <small class="text-muted"><%= item.getCategory().getDisplayName() %></small><br>
                                            <strong class="text-primary">Worn <%= item.getWearCount() %> times</strong>
                                        </div>
                                    </div>
                                </div>
                            <% } %>
                        </div>
                    </div>
                <% } %>
                
                <!-- Least Worn Items -->
                <% if (analytics.getLeastWornClothing() != null && !analytics.getLeastWornClothing().isEmpty()) { %>
                    <div class="card">
                        <h5 class="mb-4"><i class="fas fa-clock"></i> Least Worn Items (Consider wearing!)</h5>
                        <div class="row">
                            <% for (Clothing item : analytics.getLeastWornClothing()) { %>
                                <div class="col-md-4 mb-3">
                                    <div class="d-flex align-items-center">
                                        <img src="<%= item.getImageUrl() %>" 
                                             alt="<%= item.getName() %>"
                                             style="width: 80px; height: 80px; object-fit: cover; border-radius: 10px; margin-right: 15px;"
                                             onerror="this.src='https://via.placeholder.com/80x80/667eea/FFFFFF?text=Item'">
                                        <div>
                                            <h6 class="mb-1"><%= item.getName() %></h6>
                                            <small class="text-muted"><%= item.getCategory().getDisplayName() %></small><br>
                                            <strong class="text-warning">Worn <%= item.getWearCount() %> times</strong>
                                        </div>
                                    </div>
                                </div>
                            <% } %>
                        </div>
                    </div>
                <% } %>
                
            <% } else { %>
                <div class="card text-center py-5">
                    <i class="fas fa-chart-line fa-4x text-muted mb-3"></i>
                    <h4>No Analytics Data Available</h4>
                    <p class="text-muted">Add clothing items to see your wardrobe analytics</p>
                </div>
            <% } %>
        </main>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <% if (analytics != null) { %>
    <script>
        // Category Distribution Chart
        const categoryCtx = document.getElementById('categoryChart').getContext('2d');
        new Chart(categoryCtx, {
            type: 'pie',
            data: {
                labels: <%= analytics.getCategoryDistribution() != null ? 
                    "[" + String.join(",", analytics.getCategoryDistribution().keySet().stream()
                        .map(k -> "'" + k + "'").toArray(String[]::new)) + "]" : "[]" %>,
                datasets: [{
                    data: <%= analytics.getCategoryDistribution() != null ? 
                        "[" + String.join(",", analytics.getCategoryDistribution().values().stream()
                            .map(String::valueOf).toArray(String[]::new)) + "]" : "[]" %>,
                    backgroundColor: ['#667eea', '#764ba2', '#f093fb', '#4facfe', '#43e97b', '#feca57']
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { position: 'bottom' }
                }
            }
        });
        
        // Color Frequency Chart
        const colorCtx = document.getElementById('colorChart').getContext('2d');
        new Chart(colorCtx, {
            type: 'bar',
            data: {
                labels: <%= analytics.getColorFrequency() != null ? 
                    "[" + String.join(",", analytics.getColorFrequency().keySet().stream()
                        .map(k -> "'" + k + "'").toArray(String[]::new)) + "]" : "[]" %>,
                datasets: [{
                    label: 'Number of Items',
                    data: <%= analytics.getColorFrequency() != null ? 
                        "[" + String.join(",", analytics.getColorFrequency().values().stream()
                            .map(String::valueOf).toArray(String[]::new)) + "]" : "[]" %>,
                    backgroundColor: '#667eea'
                }]
            },
            options: {
                responsive: true,
                scales: {
                    y: { beginAtZero: true }
                }
            }
        });
        
        // Season Distribution Chart
        const seasonCtx = document.getElementById('seasonChart').getContext('2d');
        new Chart(seasonCtx, {
            type: 'doughnut',
            data: {
                labels: <%= analytics.getSeasonDistribution() != null ? 
                    "[" + String.join(",", analytics.getSeasonDistribution().keySet().stream()
                        .map(k -> "'" + k + "'").toArray(String[]::new)) + "]" : "[]" %>,
                datasets: [{
                    data: <%= analytics.getSeasonDistribution() != null ? 
                        "[" + String.join(",", analytics.getSeasonDistribution().values().stream()
                            .map(String::valueOf).toArray(String[]::new)) + "]" : "[]" %>,
                    backgroundColor: ['#feca57', '#48dbfb', '#0abde3', '#00d2d3']
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { position: 'bottom' }
                }
            }
        });
        
        // Style Distribution Chart
        const styleCtx = document.getElementById('styleChart').getContext('2d');
        new Chart(styleCtx, {
            type: 'polarArea',
            data: {
                labels: <%= analytics.getStyleDistribution() != null ? 
                    "[" + String.join(",", analytics.getStyleDistribution().keySet().stream()
                        .map(k -> "'" + k + "'").toArray(String[]::new)) + "]" : "[]" %>,
                datasets: [{
                    data: <%= analytics.getStyleDistribution() != null ? 
                        "[" + String.join(",", analytics.getStyleDistribution().values().stream()
                            .map(String::valueOf).toArray(String[]::new)) + "]" : "[]" %>,
                    backgroundColor: ['#667eea', '#f093fb', '#4facfe', '#43e97b', '#feca57', '#ff6348']
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { position: 'bottom' }
                }
            }
        });
    </script>
    <% } %>
</body>
</html>
