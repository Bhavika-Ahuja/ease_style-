<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.stylesync.model.Clothing" %>
<%
    HttpSession userSession = request.getSession(false);
    if (userSession == null || userSession.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/");
        return;
    }
    String userName = (String) userSession.getAttribute("userName");
    String mode = (String) request.getAttribute("mode");
    Clothing clothing = (Clothing) request.getAttribute("clothing");
    boolean isEdit = "edit".equals(mode) && clothing != null;
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= isEdit ? "Edit" : "Add" %> Clothing - StyleSync</title>
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
                <li><a href="<%= request.getContextPath() %>/wardrobe?action=edit&mode=add" class="active">
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
            <div class="page-header">
                <div class="page-title">
                    <h1><i class="fas fa-<%= isEdit ? "edit" : "plus" %>"></i> <%= isEdit ? "Edit" : "Add New" %> Clothing</h1>
                    <p><%= isEdit ? "Update clothing details" : "Add a new item to your wardrobe" %></p>
                </div>
            </div>
            
            <div class="row">
                <div class="col-lg-8">
                    <div class="card">
                        <form action="<%= request.getContextPath() %>/wardrobe" method="post" onsubmit="return validateForm()">
                            <input type="hidden" name="action" value="<%= isEdit ? "update" : "add" %>">
                            <% if (isEdit) { %>
                                <input type="hidden" name="clothingId" value="<%= clothing.getClothingId() %>">
                            <% } %>
                            
                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label class="form-label">Clothing Name *</label>
                                    <input type="text" name="name" class="form-control" 
                                           value="<%= isEdit ? clothing.getName() : "" %>"
                                           placeholder="e.g., Blue Cotton T-Shirt" required>
                                </div>
                                
                                <div class="col-md-6 mb-3">
                                    <label class="form-label">Category *</label>
                                    <select name="category" class="form-select" required>
                                        <option value="">Select Category</option>
                                        <option value="TOPS" <%= isEdit && clothing.getCategory() == Clothing.Category.TOPS ? "selected" : "" %>>Tops</option>
                                        <option value="BOTTOMS" <%= isEdit && clothing.getCategory() == Clothing.Category.BOTTOMS ? "selected" : "" %>>Bottoms</option>
                                        <option value="DRESSES" <%= isEdit && clothing.getCategory() == Clothing.Category.DRESSES ? "selected" : "" %>>Dresses</option>
                                        <option value="SHOES" <%= isEdit && clothing.getCategory() == Clothing.Category.SHOES ? "selected" : "" %>>Shoes</option>
                                        <option value="ACCESSORIES" <%= isEdit && clothing.getCategory() == Clothing.Category.ACCESSORIES ? "selected" : "" %>>Accessories</option>
                                        <option value="OUTERWEAR" <%= isEdit && clothing.getCategory() == Clothing.Category.OUTERWEAR ? "selected" : "" %>>Outerwear</option>
                                    </select>
                                </div>
                            </div>
                            
                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label class="form-label">Color *</label>
                                    <input type="text" name="color" class="form-control" 
                                           value="<%= isEdit ? clothing.getColor() : "" %>"
                                           placeholder="e.g., Blue, Red, Black" required>
                                </div>
                                
                                <div class="col-md-6 mb-3">
                                    <label class="form-label">Season *</label>
                                    <select name="season" class="form-select" required>
                                        <option value="">Select Season</option>
                                        <option value="SUMMER" <%= isEdit && clothing.getSeason() == Clothing.Season.SUMMER ? "selected" : "" %>>Summer</option>
                                        <option value="WINTER" <%= isEdit && clothing.getSeason() == Clothing.Season.WINTER ? "selected" : "" %>>Winter</option>
                                        <option value="MONSOON" <%= isEdit && clothing.getSeason() == Clothing.Season.MONSOON ? "selected" : "" %>>Monsoon</option>
                                        <option value="ALL_SEASON" <%= isEdit && clothing.getSeason() == Clothing.Season.ALL_SEASON ? "selected" : "" %>>All Season</option>
                                    </select>
                                </div>
                            </div>
                            
                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label class="form-label">Occasion *</label>
                                    <select name="occasion" class="form-select" required>
                                        <option value="">Select Occasion</option>
                                        <option value="COLLEGE" <%= isEdit && clothing.getOccasion() == Clothing.Occasion.COLLEGE ? "selected" : "" %>>College</option>
                                        <option value="OFFICE" <%= isEdit && clothing.getOccasion() == Clothing.Occasion.OFFICE ? "selected" : "" %>>Office</option>
                                        <option value="CASUAL" <%= isEdit && clothing.getOccasion() == Clothing.Occasion.CASUAL ? "selected" : "" %>>Casual</option>
                                        <option value="PARTY" <%= isEdit && clothing.getOccasion() == Clothing.Occasion.PARTY ? "selected" : "" %>>Party</option>
                                        <option value="WEDDING" <%= isEdit && clothing.getOccasion() == Clothing.Occasion.WEDDING ? "selected" : "" %>>Wedding</option>
                                        <option value="FORMAL_EVENTS" <%= isEdit && clothing.getOccasion() == Clothing.Occasion.FORMAL_EVENTS ? "selected" : "" %>>Formal Events</option>
                                    </select>
                                </div>
                                
                                <div class="col-md-6 mb-3">
                                    <label class="form-label">Style *</label>
                                    <select name="style" class="form-select" required>
                                        <option value="">Select Style</option>
                                        <option value="CASUAL" <%= isEdit && clothing.getStyle() == Clothing.Style.CASUAL ? "selected" : "" %>>Casual</option>
                                        <option value="FORMAL" <%= isEdit && clothing.getStyle() == Clothing.Style.FORMAL ? "selected" : "" %>>Formal</option>
                                        <option value="ETHNIC" <%= isEdit && clothing.getStyle() == Clothing.Style.ETHNIC ? "selected" : "" %>>Ethnic</option>
                                        <option value="WESTERN" <%= isEdit && clothing.getStyle() == Clothing.Style.WESTERN ? "selected" : "" %>>Western</option>
                                        <option value="SPORTY" <%= isEdit && clothing.getStyle() == Clothing.Style.SPORTY ? "selected" : "" %>>Sporty</option>
                                        <option value="MINIMALIST" <%= isEdit && clothing.getStyle() == Clothing.Style.MINIMALIST ? "selected" : "" %>>Minimalist</option>
                                    </select>
                                </div>
                            </div>
                            
                            <div class="mb-3">
                                <label class="form-label">Image URL *</label>
                                <input type="url" name="imageUrl" class="form-control" 
                                       value="<%= isEdit ? clothing.getImageUrl() : "" %>"
                                       placeholder="https://example.com/image.jpg" required>
                                <small class="text-muted">Paste a URL to an image of your clothing item</small>
                            </div>
                            
                            <div class="d-flex gap-3">
                                <button type="submit" class="btn btn-primary">
                                    <i class="fas fa-save"></i> <%= isEdit ? "Update" : "Save" %> Clothing
                                </button>
                                <a href="<%= request.getContextPath() %>/wardrobe" class="btn btn-secondary">
                                    <i class="fas fa-times"></i> Cancel
                                </a>
                            </div>
                        </form>
                    </div>
                </div>
                
                <div class="col-lg-4">
                    <div class="card">
                        <h5 class="mb-3"><i class="fas fa-lightbulb"></i> Quick Tips</h5>
                        <ul style="line-height: 2;">
                            <li>Use clear, descriptive names</li>
                            <li>Choose accurate categories</li>
                            <li>Select all applicable occasions</li>
                            <li>Use high-quality image URLs</li>
                            <li>Keep your wardrobe organized</li>
                        </ul>
                    </div>
                    
                    <div class="card mt-3">
                        <h5 class="mb-3"><i class="fas fa-palette"></i> Popular Colors</h5>
                        <div class="d-flex flex-wrap gap-2">
                            <button type="button" class="btn btn-sm btn-outline-secondary" onclick="setColor('Black')">Black</button>
                            <button type="button" class="btn btn-sm btn-outline-secondary" onclick="setColor('White')">White</button>
                            <button type="button" class="btn btn-sm btn-outline-secondary" onclick="setColor('Blue')">Blue</button>
                            <button type="button" class="btn btn-sm btn-outline-secondary" onclick="setColor('Red')">Red</button>
                            <button type="button" class="btn btn-sm btn-outline-secondary" onclick="setColor('Green')">Green</button>
                            <button type="button" class="btn btn-sm btn-outline-secondary" onclick="setColor('Pink')">Pink</button>
                            <button type="button" class="btn btn-sm btn-outline-secondary" onclick="setColor('Gray')">Gray</button>
                            <button type="button" class="btn btn-sm btn-outline-secondary" onclick="setColor('Brown')">Brown</button>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        function setColor(color) {
            document.querySelector('input[name="color"]').value = color;
        }
        
        function validateForm() {
            const imageUrl = document.querySelector('input[name="imageUrl"]').value;
            if (!imageUrl.startsWith('http://') && !imageUrl.startsWith('https://')) {
                alert('Please enter a valid image URL starting with http:// or https://');
                return false;
            }
            return true;
        }
    </script>
</body>
</html>
