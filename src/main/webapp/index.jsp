<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.stylesync.model.User" %>
<%
    // Redirect to dashboard if already logged in
    HttpSession userSession = request.getSession(false);
    if (userSession != null && userSession.getAttribute("user") != null) {
        response.sendRedirect(request.getContextPath() + "/dashboard");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>StyleSync - Smart Wardrobe Manager</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
    <style>
        body {
            background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
            min-height: 100vh;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }
        .landing-container {
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }
        .auth-card {
            background: white;
            border-radius: 20px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.1);
            overflow: hidden;
            max-width: 1000px;
            width: 100%;
        }
        .auth-left {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 60px 40px;
            display: flex;
            flex-direction: column;
            justify-content: center;
        }
        .auth-left h1 {
            font-size: 2.5rem;
            font-weight: 700;
            margin-bottom: 20px;
        }
        .auth-left p {
            font-size: 1.1rem;
            opacity: 0.9;
            margin-bottom: 30px;
        }
        .feature-item {
            display: flex;
            align-items: center;
            margin-bottom: 20px;
        }
        .feature-item i {
            font-size: 1.5rem;
            margin-right: 15px;
        }
        .auth-right {
            padding: 60px 40px;
        }
        .form-tabs {
            display: flex;
            justify-content: center;
            margin-bottom: 30px;
            border-bottom: 2px solid #f0f0f0;
        }
        .form-tab {
            padding: 15px 40px;
            cursor: pointer;
            color: #666;
            font-weight: 600;
            transition: all 0.3s;
            border-bottom: 3px solid transparent;
            margin-bottom: -2px;
        }
        .form-tab.active {
            color: #667eea;
            border-bottom-color: #667eea;
        }
        .form-container {
            display: none;
        }
        .form-container.active {
            display: block;
        }
        .form-control {
            padding: 12px 15px;
            border: 2px solid #e0e0e0;
            border-radius: 10px;
            transition: all 0.3s;
        }
        .form-control:focus {
            border-color: #667eea;
            box-shadow: 0 0 0 0.2rem rgba(102, 126, 234, 0.15);
        }
        .btn-primary {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border: none;
            padding: 12px;
            border-radius: 10px;
            font-weight: 600;
            transition: transform 0.2s;
        }
        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
        }
        .alert {
            border-radius: 10px;
            border: none;
        }
    </style>
</head>
<body>
    <div class="landing-container">
        <div class="auth-card row g-0">
            <!-- Left side - Branding -->
            <div class="col-lg-5 auth-left d-none d-lg-block">
                <h1><i class="fas fa-tshirt"></i> StyleSync</h1>
                <p>Your Smart Wardrobe Management Solution</p>
                
                <div class="feature-item">
                    <i class="fas fa-magic"></i>
                    <span>Smart Outfit Recommendations</span>
                </div>
                <div class="feature-item">
                    <i class="fas fa-search"></i>
                    <span>Advanced Search & Filtering</span>
                </div>
                <div class="feature-item">
                    <i class="fas fa-chart-line"></i>
                    <span>Wardrobe Analytics</span>
                </div>
                <div class="feature-item">
                    <i class="fas fa-heart"></i>
                    <span>Favorites & Outfit Planner</span>
                </div>
            </div>
            
            <!-- Right side - Auth Forms -->
            <div class="col-lg-7 auth-right">
                <!-- Mobile header -->
                <div class="d-lg-none text-center mb-4">
                    <h2><i class="fas fa-tshirt text-primary"></i> StyleSync</h2>
                    <p class="text-muted">Smart Wardrobe Manager</p>
                </div>
                
                <!-- Tabs -->
                <div class="form-tabs">
                    <div class="form-tab active" onclick="showForm('login')">Login</div>
                    <div class="form-tab" onclick="showForm('register')">Register</div>
                </div>
                
                <!-- Error/Success Messages -->
                <% if (request.getAttribute("error") != null) { %>
                    <div class="alert alert-danger" role="alert">
                        <i class="fas fa-exclamation-circle"></i> <%= request.getAttribute("error") %>
                    </div>
                <% } %>
                
                <% if (request.getAttribute("success") != null) { %>
                    <div class="alert alert-success" role="alert">
                        <i class="fas fa-check-circle"></i> <%= request.getAttribute("success") %>
                    </div>
                <% } %>
                
                <!-- Login Form -->
                <div id="login-form" class="form-container active">
                    <form action="<%= request.getContextPath() %>/login" method="post">
                        <div class="mb-3">
                            <label for="login-email" class="form-label">Email Address</label>
                            <input type="email" class="form-control" id="login-email" name="email" 
                                   placeholder="Enter your email" required>
                        </div>
                        <div class="mb-4">
                            <label for="login-password" class="form-label">Password</label>
                            <input type="password" class="form-control" id="login-password" name="password" 
                                   placeholder="Enter your password" required>
                        </div>
                        <button type="submit" class="btn btn-primary w-100">
                            <i class="fas fa-sign-in-alt"></i> Login
                        </button>
                    </form>
                </div>
                
                <!-- Register Form -->
                <div id="register-form" class="form-container">
                    <form action="<%= request.getContextPath() %>/register" method="post">
                        <div class="mb-3">
                            <label for="register-name" class="form-label">Full Name</label>
                            <input type="text" class="form-control" id="register-name" name="name" 
                                   placeholder="Enter your name" 
                                   value="<%= request.getAttribute("name") != null ? request.getAttribute("name") : "" %>" 
                                   required>
                        </div>
                        <div class="mb-3">
                            <label for="register-email" class="form-label">Email Address</label>
                            <input type="email" class="form-control" id="register-email" name="email" 
                                   placeholder="Enter your email" 
                                   value="<%= request.getAttribute("email") != null ? request.getAttribute("email") : "" %>" 
                                   required>
                        </div>
                        <div class="mb-3">
                            <label for="register-password" class="form-label">Password</label>
                            <input type="password" class="form-control" id="register-password" name="password" 
                                   placeholder="At least 6 characters" required minlength="6">
                            <small class="text-muted">Must contain at least one letter and one number</small>
                        </div>
                        <div class="mb-4">
                            <label for="register-confirm" class="form-label">Confirm Password</label>
                            <input type="password" class="form-control" id="register-confirm" name="confirmPassword" 
                                   placeholder="Re-enter your password" required>
                        </div>
                        <button type="submit" class="btn btn-primary w-100">
                            <i class="fas fa-user-plus"></i> Create Account
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </div>
    
    <script>
        function showForm(formType) {
            // Update tabs
            document.querySelectorAll('.form-tab').forEach(tab => {
                tab.classList.remove('active');
            });
            event.target.classList.add('active');
            
            // Show/hide forms
            document.querySelectorAll('.form-container').forEach(container => {
                container.classList.remove('active');
            });
            document.getElementById(formType + '-form').classList.add('active');
        }
    </script>
</body>
</html>
