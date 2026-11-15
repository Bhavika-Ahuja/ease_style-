<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.stylesync.model.Clothing" %>
<%@ page import="com.stylesync.model.Outfit" %>
<%@ page import="java.util.List" %>
<%
    HttpSession userSession = request.getSession(false);
    if (userSession == null || userSession.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/");
        return;
    }
    String userName = (String) userSession.getAttribute("userName");
    // This page can show both favorite clothing and outfits
    // For simplicity, redirect to wardrobe favorites
    response.sendRedirect(request.getContextPath() + "/wardrobe?action=favorites");
%>
