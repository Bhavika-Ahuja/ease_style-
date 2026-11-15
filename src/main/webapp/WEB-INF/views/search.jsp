<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.stylesync.model.Clothing" %>
<%@ page import="java.util.*" %>
<%
    HttpSession userSession = request.getSession(false);
    if (userSession == null || userSession.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/");
        return;
    }
    String userName = (String) userSession.getAttribute("userName");
    List<Clothing> results = (List<Clothing>) request.getAttribute("results");
    Integer resultCount = (Integer) request.getAttribute("resultCount");
    Set<String> colors = (Set<String>) request.getAttribute("colors");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Search - StyleSync</title>
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
                <li><a href="<%= request.getContextPath() %>/search" class="active">
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
                    <h1><i class="fas fa-search"></i> Advanced Search</h1>
                    <p>Filter your wardrobe with multiple criteria</p>
                </div>
            </div>
            
            <!-- Search Form -->
            <div class="card mb-4">
                <form action="<%= request.getContextPath() %>/search" method="post">
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label">Search by Name</label>
                            <input type="text" name="searchQuery" class="form-control" 
                                   placeholder="Search clothing name or color">
                        </div>
                        
                        <div class="col-md-3 mb-3">
                            <label class="form-label">Category</label>
                            <select name="category" class="form-select">
                                <option value="">All Categories</option>
                                <option value="TOPS">Tops</option>
                                <option value="BOTTOMS">Bottoms</option>
                                <option value="DRESSES">Dresses</option>
                                <option value="SHOES">Shoes</option>
                                <option value="ACCESSORIES">Accessories</option>
                                <option value="OUTERWEAR">Outerwear</option>
                            </select>
                        </div>
                        
                        <div class="col-md-3 mb-3">
                            <label class="form-label">Color</label>
                            <select name="color" class="form-select">
                                <option value="">All Colors</option>
                                <% if (colors != null) {
                                    for (String color : colors) { %>
                                        <option value="<%= color %>"><%= color %></option>
                                <%  }
                                   } %>
                            </select>
                        </div>
                    </div>
                    
                    <div class="row">
                        <div class="col-md-3 mb-3">
                            <label class="form-label">Season</label>
                            <select name="season" class="form-select">
                                <option value="">All Seasons</option>
                                <option value="SUMMER">Summer</option>
                                <option value="WINTER">Winter</option>
                                <option value="MONSOON">Monsoon</option>
                                <option value="ALL_SEASON">All Season</option>
                            </select>
                        </div>
                        
                        <div class="col-md-3 mb-3">
                            <label class="form-label">Occasion</label>
                            <select name="occasion" class="form-select">
                                <option value="">All Occasions</option>
                                <option value="COLLEGE">College</option>
                                <option value="OFFICE">Office</option>
                                <option value="CASUAL">Casual</option>
                                <option value="PARTY">Party</option>
                                <option value="WEDDING">Wedding</option>
                                <option value="FORMAL_EVENTS">Formal Events</option>
                            </select>
                        </div>
                        
                        <div class="col-md-3 mb-3">
                            <label class="form-label">Style</label>
                            <select name="style" class="form-select">
                                <option value="">All Styles</option>
                                <option value="CASUAL">Casual</option>
                                <option value="FORMAL">Formal</option>
                                <option value="ETHNIC">Ethnic</option>
                                <option value="WESTERN">Western</option>
                                <option value="SPORTY">Sporty</option>
                                <option value="MINIMALIST">Minimalist</option>
                            </select>
                        </div>
                        
                        <div class="col-md-3 mb-3">
                            <label class="form-label">Sort By</label>
                            <select name="sortBy" class="form-select">
                                <option value="NAME">Name</option>
                                <option value="CATEGORY">Category</option>
                                <option value="WEAR_COUNT">Wear Count</option>
                                <option value="DATE_ADDED">Date Added</option>
                                <option value="COLOR">Color</option>
                            </select>
                        </div>
                    </div>
                    
                    <div class="row">
                        <div class="col-md-3 mb-3">
                            <label class="form-label">Order</label>
                            <select name="order" class="form-select">
                                <option value="asc">Ascending</option>
                                <option value="desc">Descending</option>
                            </select>
                        </div>
                        
                        <div class="col-md-3 mb-3">
                            <label class="form-label">Filters</label>
                            <div class="form-check">
                                <input type="checkbox" name="onlyFavorites" value="true" class="form-check-input" id="favCheck">
                                <label class="form-check-label" for="favCheck">Only Favorites</label>
                            </div>
                            <div class="form-check">
                                <input type="checkbox" name="excludeLaundry" value="true" class="form-check-input" id="laundryCheck">
                                <label class="form-check-label" for="laundryCheck">Exclude Laundry</label>
                            </div>
                        </div>
                    </div>
                    
                    <div class="d-flex gap-3">
                        <button type="submit" class="btn btn-primary">
                            <i class="fas fa-search"></i> Search
                        </button>
                        <button type="reset" class="btn btn-secondary" onclick="window.location.href='<%= request.getContextPath() %>/search'">
                            <i class="fas fa-redo"></i> Clear Filters
                        </button>
                    </div>
                </form>
            </div>
            
            <!-- Results -->
            <% if (resultCount != null) { %>
                <div class="card mb-3">
                    <h5><i class="fas fa-list"></i> Search Results: <%= resultCount %> items found</h5>
                </div>
            <% } %>
            
            <% if (results != null && !results.isEmpty()) { %>
                <div class="clothing-grid">
                    <% for (Clothing clothing : results) { %>
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
                                </div>
                                <div class="mt-2">
                                    <small class="text-muted">
                                        <i class="fas fa-walking"></i> Worn <%= clothing.getWearCount() %> times
                                    </small>
                                </div>
                                <div class="clothing-actions mt-3">
                                    <a href="<%= request.getContextPath() %>/wardrobe?action=view&id=<%= clothing.getClothingId() %>" 
                                       class="btn-icon" title="View">
                                        <i class="fas fa-eye"></i>
                                    </a>
                                    <button class="btn-icon favorite <%= clothing.isFavorite() ? "active" : "" %>" title="Favorite">
                                        <i class="fas fa-heart"></i>
                                    </button>
                                </div>
                            </div>
                        </div>
                    <% } %>
                </div>
            <% } else if (resultCount != null && resultCount == 0) { %>
                <div class="card text-center py-5">
                    <i class="fas fa-search fa-4x text-muted mb-3"></i>
                    <h4>No Results Found</h4>
                    <p class="text-muted">Try adjusting your search criteria</p>
                </div>
            <% } %>
        </main>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
