/**
 * StyleSync - Main JavaScript
 * Common utilities and functions
 */

// Toggle mobile sidebar
function toggleSidebar() {
    const sidebar = document.querySelector('.sidebar');
    if (sidebar) {
        sidebar.classList.toggle('active');
    }
}

// Format date
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { 
        year: 'numeric', 
        month: 'short', 
        day: 'numeric' 
    });
}

// Show notification
function showNotification(message, type = 'info') {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `alert alert-${type} position-fixed top-0 end-0 m-3`;
    notification.style.zIndex = '9999';
    notification.innerHTML = message;
    
    document.body.appendChild(notification);
    
    // Auto remove after 3 seconds
    setTimeout(() => {
        notification.remove();
    }, 3000);
}

// Confirm delete
function confirmDelete(message = 'Are you sure you want to delete this item?') {
    return confirm(message);
}

// Image error handler
function handleImageError(img) {
    img.src = 'https://via.placeholder.com/300x400/667eea/FFFFFF?text=No+Image';
}

// Fetch with error handling
async function fetchAPI(url, options = {}) {
    try {
        const response = await fetch(url, options);
        const data = await response.json();
        
        if (!data.success && data.message) {
            showNotification(data.message, 'danger');
        }
        
        return data;
    } catch (error) {
        console.error('API Error:', error);
        showNotification('An error occurred. Please try again.', 'danger');
        return { success: false, message: error.message };
    }
}

// Initialize tooltips (Bootstrap)
document.addEventListener('DOMContentLoaded', function() {
    // Add mobile menu toggle if not present
    const sidebar = document.querySelector('.sidebar');
    if (sidebar && window.innerWidth < 768) {
        const toggleBtn = document.createElement('button');
        toggleBtn.className = 'btn btn-primary position-fixed';
        toggleBtn.style.top = '10px';
        toggleBtn.style.left = '10px';
        toggleBtn.style.zIndex = '1001';
        toggleBtn.innerHTML = '<i class="fas fa-bars"></i>';
        toggleBtn.onclick = toggleSidebar;
        document.body.appendChild(toggleBtn);
    }
    
    // Add image error handlers
    document.querySelectorAll('img').forEach(img => {
        if (!img.onerror) {
            img.onerror = function() { handleImageError(this); };
        }
    });
});

// Handle form submissions with loading state
function handleFormSubmit(formElement) {
    const submitBtn = formElement.querySelector('button[type="submit"]');
    if (submitBtn) {
        const originalText = submitBtn.innerHTML;
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Loading...';
        
        // Reset after form processes
        setTimeout(() => {
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalText;
        }, 3000);
    }
}

// Add event listeners to all forms
document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', function(e) {
            handleFormSubmit(this);
        });
    });
});

console.log('StyleSync loaded successfully!');
