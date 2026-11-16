<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    String csrf = (String) session.getAttribute("auth.csrf");
    if (session.getAttribute("currentMember") == null || csrf == null) {
        response.sendRedirect(request.getContextPath()+"/view/member/LoginView4.jsp"); return;
    }
    if ("POST".equals(request.getMethod())) {
        if (!csrf.equals(request.getParameter("csrf"))) { response.sendError(403); return; }
        session.invalidate(); response.sendRedirect(request.getContextPath()+"/view/member/LoginView4.jsp"); return;
    }
    request.setAttribute("csrf",csrf);
%>
<!doctype html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Đăng xuất</title><link rel="stylesheet" href="../../assets/member.css"></head>
<body><main class="member-panel"><h1>Đăng xuất</h1>
<form action="LogoutView4.jsp" method="post"><input type="hidden" name="csrf" value="<c:out value='${csrf}' />"><button type="submit">Đăng xuất</button></form>
</main></body></html>
