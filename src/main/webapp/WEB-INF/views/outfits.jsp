<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.stylesync.model.Outfit" %>
<%@ page import="java.util.List" %>
<%
    HttpSession userSession = request.getSession(false);
    if (userSession == null || userSession.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/");
        return;
    }
    String userName = (String) userSession.getAttribute("userName");
    List<Outfit> outfits = (List<Outfit>) request.getAttribute("outfits");
    String pageTitle = (String) request.getAttribute("pageTitle");
    if (pageTitle == null) pageTitle = "My Outfits";
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= pageTitle %> - StyleSync</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
    <style>
        .outfit-card {
            background: white;
            border-radius: 15px;
            padding: 20px;
            margin-bottom: 20px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.08);
            transition: all 0.3s;
        }
        .outfit-card:hover {
            box-shadow: 0 10px 30px rgba(0,0,0,0.12);
            transform: translateY(-2px);
        }
        .outfit-thumbnail {
            width: 100%;
            height: 200px;
            object-fit: cover;
            border-radius: 12px;
            margin-bottom: 15px;
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
                <li><a href="<%= request.getContextPath() %>/suggestions">
                    <i class="fas fa-magic"></i> Smart Suggestions
                </a></li>
                <li><a href="<%= request.getContextPath() %>/outfits" class="active">
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
                    <h1><i class="fas fa-layer-group"></i> <%= pageTitle %></h1>
                    <p>Create and manage your outfit combinations</p>
                </div>
                <div class="header-actions">
                    <a href="<%= request.getContextPath() %>/suggestions" class="btn btn-primary">
                        <i class="fas fa-magic"></i> Get Suggestions
                    </a>
                </div>
            </div>
            
            <!-- Filter Buttons -->
            <div class="card mb-4">
                <div class="d-flex gap-2 flex-wrap">
                    <a href="<%= request.getContextPath() %>/outfits" class="btn btn-secondary">
                        <i class="fas fa-th"></i> All Outfits
                    </a>
                    <a href="<%= request.getContextPath() %>/outfits?action=favorites" class="btn btn-secondary">
                        <i class="fas fa-heart"></i> Favorite Outfits
                    </a>
                </div>
            </div>
            
            <% if (outfits != null && !outfits.isEmpty()) { %>
                <div class="row">
                    <% for (Outfit outfit : outfits) { %>
                        <div class="col-md-6 col-lg-4">
                            <div class="outfit-card">
                                <% if (outfit.getThumbnailUrl() != null && !outfit.getThumbnailUrl().isEmpty()) { %>
                                    <img src="<%= outfit.getThumbnailUrl() %>" 
                                         alt="<%= outfit.getName() %>"
                                         class="outfit-thumbnail"
                                         onerror="this.src='https://via.placeholder.com/400x200/667eea/FFFFFF?text=Outfit'">
                                <% } else { %>
                                    <div class="outfit-thumbnail" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); display: flex; align-items: center; justify-content: center; color: white;">
                                        <i class="fas fa-layer-group fa-3x"></i>
                                    </div>
                                <% } %>
                                
                                <h5><%= outfit.getName() %></h5>
                                
                                <div class="d-flex flex-wrap gap-2 mb-3">
                                    <% if (outfit.getOccasion() != null) { %>
                                        <span class="badge" style="background: #4299e1;">
                                            <%= outfit.getOccasion().getDisplayName() %>
                                        </span>
                                    <% } %>
                                    <% if (outfit.getSeason() != null) { %>
                                        <span class="badge" style="background: #48bb78;">
                                            <%= outfit.getSeason().getDisplayName() %>
                                        </span>
                                    <% } %>
                                    <% if (outfit.getStyle() != null) { %>
                                        <span class="badge" style="background: #9f7aea;">
                                            <%= outfit.getStyle().getDisplayName() %>
                                        </span>
                                    <% } %>
                                </div>
                                
                                <div class="d-flex justify-content-between align-items-center mb-3">
                                    <small class="text-muted">
                                        <i class="fas fa-tshirt"></i> <%= outfit.getClothingCount() %> items
                                    </small>
                                    <small class="text-muted">
                                        <i class="fas fa-walking"></i> Worn <%= outfit.getWearCount() %> times
                                    </small>
                                    <% if (outfit.isFavorite()) { %>
                                        <span class="badge" style="background: #f56565;">
                                            <i class="fas fa-heart"></i> Favorite
                                        </span>
                                    <% } %>
                                </div>
                                
                                <div class="d-flex gap-2">
                                    <button class="btn btn-sm btn-primary flex-fill" 
                                            onclick="viewOutfit('<%= outfit.getOutfitId() %>')">
                                        <i class="fas fa-eye"></i> View
                                    </button>
                                    <button class="btn btn-sm btn-success" 
                                            onclick="markWorn('<%= outfit.getOutfitId() %>')" title="Mark as Worn">
                                        <i class="fas fa-check"></i>
                                    </button>
                                    <button class="btn btn-sm <%= outfit.isFavorite() ? "btn-danger" : "btn-outline-secondary" %>" 
                                            onclick="toggleFavorite('<%= outfit.getOutfitId() %>')" title="Favorite">
                                        <i class="fas fa-heart"></i>
                                    </button>
                                    <button class="btn btn-sm btn-danger" 
                                            onclick="deleteOutfit('<%= outfit.getOutfitId() %>')" title="Delete">
                                        <i class="fas fa-trash"></i>
                                    </button>
                                </div>
                            </div>
                        </div>
                    <% } %>
                </div>
            <% } else { %>
                <div class="card text-center py-5">
                    <i class="fas fa-layer-group fa-4x text-muted mb-3"></i>
                    <h4>No Outfits Yet</h4>
                    <p class="text-muted mb-4">Create outfit combinations from your wardrobe</p>
                    <a href="<%= request.getContextPath() %>/suggestions" class="btn btn-primary">
                        <i class="fas fa-magic"></i> Get Outfit Suggestions
                    </a>
                </div>
            <% } %>
        </main>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        function viewOutfit(id) {
            window.location.href = '<%= request.getContextPath() %>/outfits?action=view&id=' + id;
        }
        
        function markWorn(id) {
            if (confirm('Mark this outfit as worn today?')) {
                fetch('<%= request.getContextPath() %>/outfits', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: 'action=markWorn&outfitId=' + id
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        alert('Outfit marked as worn!');
                        location.reload();
                    } else {
                        alert('Error: ' + data.message);
                    }
                });
            }
        }
        
        function toggleFavorite(id) {
            fetch('<%= request.getContextPath() %>/outfits', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'action=toggleFavorite&outfitId=' + id
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    location.reload();
                } else {
                    alert('Error: ' + data.message);
                }
            });
        }
        
        function deleteOutfit(id) {
            if (confirm('Are you sure you want to delete this outfit?')) {
                fetch('<%= request.getContextPath() %>/outfits?id=' + id, {
                    method: 'DELETE'
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        location.reload();
                    } else {
                        alert('Error: ' + data.message);
                    }
                });
            }
        }
    </script>
</body>
</html>
