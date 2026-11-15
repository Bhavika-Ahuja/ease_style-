<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.stylesync.model.Clothing" %>
<%@ page import="java.util.List" %>
<%
    HttpSession userSession = request.getSession(false);
    if (userSession == null || userSession.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/");
        return;
    }
    String userName = (String) userSession.getAttribute("userName");
    List<Clothing> clothingList = (List<Clothing>) request.getAttribute("clothingList");
    String pageTitle = (String) request.getAttribute("pageTitle");
    if (pageTitle == null) pageTitle = "My Wardrobe";
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
                <li><a href="<%= request.getContextPath() %>/wardrobe" class="active">
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
                    <h1><%= pageTitle %></h1>
                    <p>Manage your clothing collection</p>
                </div>
                <div class="header-actions">
                    <a href="<%= request.getContextPath() %>/wardrobe?action=edit&mode=add" class="btn btn-primary">
                        <i class="fas fa-plus"></i> Add New Item
                    </a>
                </div>
            </div>
            
            <!-- Filter Buttons -->
            <div class="card mb-4">
                <div class="d-flex gap-2 flex-wrap">
                    <a href="<%= request.getContextPath() %>/wardrobe" class="btn btn-secondary">
                        <i class="fas fa-th"></i> All Items
                    </a>
                    <a href="<%= request.getContextPath() %>/wardrobe?action=favorites" class="btn btn-secondary">
                        <i class="fas fa-heart"></i> Favorites
                    </a>
                    <a href="<%= request.getContextPath() %>/search" class="btn btn-secondary">
                        <i class="fas fa-filter"></i> Advanced Filters
                    </a>
                </div>
            </div>
            
            <% if (clothingList != null && !clothingList.isEmpty()) { %>
                <!-- Clothing Grid -->
                <div class="clothing-grid">
                    <% for (Clothing clothing : clothingList) { %>
                        <div class="clothing-card">
                            <img src="<%= clothing.getImageUrl() %>" 
                                 alt="<%= clothing.getName() %>"
                                 class="clothing-image"
                                 onerror="this.src='https://via.placeholder.com/300x400/667eea/FFFFFF?text=No+Image'">
                            <div class="clothing-body">
                                <h6 class="clothing-name"><%= clothing.getName() %></h6>
                                <div class="clothing-meta">
                                    <span class="badge badge-category">
                                        <%= clothing.getCategory().getDisplayName() %>
                                    </span>
                                    <span class="badge badge-color">
                                        <%= clothing.getColor() %>
                                    </span>
                                    <span class="badge badge-season">
                                        <%= clothing.getSeason().getDisplayName() %>
                                    </span>
                                </div>
                                <div class="d-flex justify-content-between align-items-center mt-2">
                                    <small class="text-muted">
                                        <i class="fas fa-walking"></i> Worn <%= clothing.getWearCount() %> times
                                    </small>
                                    <% if (clothing.isInLaundry()) { %>
                                        <span class="badge" style="background: #ffa500;">
                                            <i class="fas fa-tint"></i> Laundry
                                        </span>
                                    <% } %>
                                </div>
                                <div class="clothing-actions mt-3">
                                    <button class="btn-icon" onclick="viewClothing('<%= clothing.getClothingId() %>')" title="View">
                                        <i class="fas fa-eye"></i>
                                    </button>
                                    <button class="btn-icon" onclick="editClothing('<%= clothing.getClothingId() %>')" title="Edit">
                                        <i class="fas fa-edit"></i>
                                    </button>
                                    <button class="btn-icon favorite <%= clothing.isFavorite() ? "active" : "" %>" 
                                            onclick="toggleFavorite('<%= clothing.getClothingId() %>')" title="Favorite">
                                        <i class="fas fa-heart"></i>
                                    </button>
                                    <button class="btn-icon" 
                                            onclick="toggleLaundry('<%= clothing.getClothingId() %>', <%= !clothing.isInLaundry() %>)" 
                                            title="Laundry">
                                        <i class="fas fa-tint"></i>
                                    </button>
                                    <button class="btn-icon" onclick="deleteClothing('<%= clothing.getClothingId() %>')" 
                                            title="Delete" style="background: #fee;">
                                        <i class="fas fa-trash"></i>
                                    </button>
                                </div>
                            </div>
                        </div>
                    <% } %>
                </div>
            <% } else { %>
                <!-- Empty State -->
                <div class="card text-center py-5">
                    <i class="fas fa-tshirt fa-4x text-muted mb-3"></i>
                    <h4>No Clothing Items Found</h4>
                    <p class="text-muted mb-4">Start building your digital wardrobe</p>
                    <a href="<%= request.getContextPath() %>/wardrobe?action=edit&mode=add" 
                       class="btn btn-primary">
                        <i class="fas fa-plus"></i> Add Your First Item
                    </a>
                </div>
            <% } %>
        </main>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        function viewClothing(id) {
            window.location.href = '<%= request.getContextPath() %>/wardrobe?action=view&id=' + id;
        }
        
        function editClothing(id) {
            window.location.href = '<%= request.getContextPath() %>/wardrobe?action=edit&id=' + id;
        }
        
        function toggleFavorite(id) {
            fetch('<%= request.getContextPath() %>/wardrobe', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'action=toggleFavorite&clothingId=' + id
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
        
        function toggleLaundry(id, inLaundry) {
            fetch('<%= request.getContextPath() %>/wardrobe', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'action=toggleLaundry&clothingId=' + id + '&inLaundry=' + inLaundry
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
        
        function deleteClothing(id) {
            if (confirm('Are you sure you want to delete this item?')) {
                fetch('<%= request.getContextPath() %>/wardrobe?id=' + id, {
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
