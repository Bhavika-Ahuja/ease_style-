<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.stylesync.service.RecommendationService" %>
<%@ page import="com.stylesync.model.Clothing" %>
<%@ page import="java.util.List" %>
<%
    HttpSession userSession = request.getSession(false);
    if (userSession == null || userSession.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/");
        return;
    }
    String userName = (String) userSession.getAttribute("userName");
    List<RecommendationService.OutfitRecommendation> recommendations = 
        (List<RecommendationService.OutfitRecommendation>) request.getAttribute("recommendations");
    Integer recommendationCount = (Integer) request.getAttribute("recommendationCount");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Smart Suggestions - StyleSync</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
    <style>
        .recommendation-card {
            background: white;
            border-radius: 15px;
            padding: 25px;
            margin-bottom: 25px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.08);
            border: 2px solid #e2e8f0;
            transition: all 0.3s;
        }
        .recommendation-card:hover {
            box-shadow: 0 10px 30px rgba(0,0,0,0.12);
            transform: translateY(-3px);
        }
        .recommendation-score {
            display: inline-block;
            background: linear-gradient(135deg, #48bb78, #38a169);
            color: white;
            padding: 10px 20px;
            border-radius: 25px;
            font-weight: 700;
            font-size: 1.1rem;
            margin-bottom: 20px;
        }
        .recommendation-items {
            display: flex;
            gap: 15px;
            flex-wrap: wrap;
        }
        .recommendation-item {
            flex: 1;
            min-width: 150px;
            max-width: 200px;
            text-align: center;
        }
        .recommendation-item img {
            width: 100%;
            height: 180px;
            object-fit: cover;
            border-radius: 12px;
            margin-bottom: 10px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }
        .recommendation-item h6 {
            font-size: 0.9rem;
            margin-bottom: 5px;
        }
    </style>
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
                <li><a href="<%= request.getContextPath() %>/suggestions" class="active">
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
            <div class="page-header">
                <div class="page-title">
                    <h1><i class="fas fa-magic"></i> Smart Outfit Suggestions</h1>
                    <p>Get AI-powered outfit recommendations based on your preferences</p>
                </div>
            </div>
            
            <!-- Recommendation Form -->
            <div class="card mb-4">
                <h5 class="mb-4"><i class="fas fa-sliders-h"></i> Set Your Preferences</h5>
                <form action="<%= request.getContextPath() %>/suggestions" method="post">
                    <div class="row">
                        <div class="col-md-3 mb-3">
                            <label class="form-label">Occasion *</label>
                            <select name="occasion" class="form-select" required>
                                <option value="">Select Occasion</option>
                                <option value="COLLEGE">College</option>
                                <option value="OFFICE">Office</option>
                                <option value="CASUAL">Casual</option>
                                <option value="PARTY">Party</option>
                                <option value="WEDDING">Wedding</option>
                                <option value="FORMAL_EVENTS">Formal Events</option>
                            </select>
                        </div>
                        
                        <div class="col-md-3 mb-3">
                            <label class="form-label">Season *</label>
                            <select name="season" class="form-select" required>
                                <option value="">Select Season</option>
                                <option value="SUMMER">Summer</option>
                                <option value="WINTER">Winter</option>
                                <option value="MONSOON">Monsoon</option>
                                <option value="ALL_SEASON">All Season</option>
                            </select>
                        </div>
                        
                        <div class="col-md-3 mb-3">
                            <label class="form-label">Style *</label>
                            <select name="style" class="form-select" required>
                                <option value="">Select Style</option>
                                <option value="CASUAL">Casual</option>
                                <option value="FORMAL">Formal</option>
                                <option value="ETHNIC">Ethnic</option>
                                <option value="WESTERN">Western</option>
                                <option value="SPORTY">Sporty</option>
                                <option value="MINIMALIST">Minimalist</option>
                            </select>
                        </div>
                        
                        <div class="col-md-3 mb-3">
                            <label class="form-label">Preferred Color (Optional)</label>
                            <input type="text" name="preferredColor" class="form-control" 
                                   placeholder="e.g., Blue, Black">
                        </div>
                    </div>
                    
                    <button type="submit" class="btn btn-primary">
                        <i class="fas fa-magic"></i> Generate Outfit Suggestions
                    </button>
                </form>
            </div>
            
            <!-- How It Works -->
            <div class="card mb-4" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white;">
                <h5 class="mb-3"><i class="fas fa-info-circle"></i> How Smart Recommendations Work</h5>
                <div class="row">
                    <div class="col-md-3">
                        <h6><i class="fas fa-filter"></i> Step 1: Filter</h6>
                        <p style="opacity: 0.9; font-size: 0.9rem;">We filter your wardrobe based on your selected criteria</p>
                    </div>
                    <div class="col-md-3">
                        <h6><i class="fas fa-puzzle-piece"></i> Step 2: Combine</h6>
                        <p style="opacity: 0.9; font-size: 0.9rem;">Generate outfit combinations that work together</p>
                    </div>
                    <div class="col-md-3">
                        <h6><i class="fas fa-calculator"></i> Step 3: Score</h6>
                        <p style="opacity: 0.9; font-size: 0.9rem;">Calculate compatibility score (0-100) for each outfit</p>
                    </div>
                    <div class="col-md-3">
                        <h6><i class="fas fa-sort-amount-down"></i> Step 4: Rank</h6>
                        <p style="opacity: 0.9; font-size: 0.9rem;">PriorityQueue ranks outfits by score</p>
                    </div>
                </div>
            </div>
            
            <!-- Recommendations Results -->
            <% if (recommendationCount != null) { %>
                <div class="card mb-4">
                    <h4><i class="fas fa-star"></i> Your Top Recommendations: <%= recommendationCount %> outfits</h4>
                </div>
            <% } %>
            
            <% if (recommendations != null && !recommendations.isEmpty()) { %>
                <% int rank = 1;
                   for (RecommendationService.OutfitRecommendation rec : recommendations) { %>
                    <div class="recommendation-card">
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <h5><i class="fas fa-medal"></i> Recommendation #<%= rank++ %></h5>
                            <div class="recommendation-score">
                                <i class="fas fa-star"></i> <%= String.format("%.0f", rec.getScore()) %>% Match
                            </div>
                        </div>
                        
                        <div class="recommendation-items">
                            <% for (Clothing item : rec.getClothing()) { %>
                                <div class="recommendation-item">
                                    <img src="<%= item.getImageUrl() %>" 
                                         alt="<%= item.getName() %>"
                                         onerror="this.src='https://via.placeholder.com/200x180/667eea/FFFFFF?text=No+Image'">
                                    <h6><%= item.getName() %></h6>
                                    <small class="text-muted"><%= item.getCategory().getDisplayName() %></small>
                                </div>
                            <% } %>
                        </div>
                        
                        <div class="mt-4 d-flex gap-3">
                            <button class="btn btn-success" onclick="saveOutfit(<%= rank-1 %>)">
                                <i class="fas fa-save"></i> Save This Outfit
                            </button>
                            <button class="btn btn-secondary" onclick="viewDetails(<%= rank-1 %>)">
                                <i class="fas fa-info-circle"></i> View Details
                            </button>
                        </div>
                    </div>
                <% } %>
            <% } else if (recommendationCount != null && recommendationCount == 0) { %>
                <div class="card text-center py-5">
                    <i class="fas fa-frown fa-4x text-muted mb-3"></i>
                    <h4>No Recommendations Available</h4>
                    <p class="text-muted mb-4">We couldn't find matching outfits for your criteria.<br>
                    Try different preferences or add more clothing items to your wardrobe.</p>
                    <a href="<%= request.getContextPath() %>/wardrobe?action=edit&mode=add" class="btn btn-primary">
                        <i class="fas fa-plus"></i> Add More Clothing
                    </a>
                </div>
            <% } %>
        </main>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        function saveOutfit(index) {
            alert('Save outfit functionality - integrate with OutfitServlet');
            // TODO: Implement save outfit to database
        }
        
        function viewDetails(index) {
            alert('View outfit details - show full breakdown');
            // TODO: Show modal with detailed outfit information
        }
    </script>
</body>
</html>
