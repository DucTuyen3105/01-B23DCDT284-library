<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="dao.MemberDAO4,model.*,java.util.UUID,java.sql.SQLException" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    request.setCharacterEncoding("UTF-8");
    String message = "";
    String loginCsrf = (String) session.getAttribute("auth.loginCsrf");
    if (loginCsrf == null) { loginCsrf = UUID.randomUUID().toString(); session.setAttribute("auth.loginCsrf",loginCsrf); }
    // Opening this URL must always show the login form, even with an existing session.
    // Replace the old session only after the submitted credentials are verified.
    Member4 member = null;
    if ("POST".equals(request.getMethod())) {
        if (!loginCsrf.equals(request.getParameter("csrf"))) {
            response.setStatus(400); message = "Phiên đăng nhập không hợp lệ. Vui lòng thử lại.";
        } else {
            try {
                member = new MemberDAO4().dangNhap(request.getParameter("username"),request.getParameter("password"));
                if (member == null) message = "Tên đăng nhập hoặc mật khẩu không đúng, hoặc tài khoản không hoạt động.";
                else {
                    session.invalidate(); session = request.getSession(true);
                    session.setAttribute("currentMember",member);
                    session.setAttribute("auth.csrf",UUID.randomUUID().toString());
                    if (member instanceof Reader4) session.setAttribute("currentReader",member);
                    else if (member instanceof LibraryStaff4) session.setAttribute("currentStaff",member);
                    else if (member instanceof ManagementStaff4) session.setAttribute("currentManagementStaff",member);
                }
            } catch (SQLException e) {
                application.log("LoginView4: authentication failed",e);
                message = "Không thể đăng nhập lúc này. Vui lòng thử lại.";
                member = null;
            }
        }
    }
    if (member != null && message.isEmpty()) {
        String home = member instanceof Reader4 ? "reader/ReaderHomeView4.jsp"
                : member instanceof LibraryStaff4 ? "libraryStaff/LibraryStaffHomeView4.jsp"
                : "managementStaff/ManagementStaffHomeView4.jsp";
        response.sendRedirect(request.getContextPath()+"/view/"+home); return;
    }
    request.setAttribute("message",message); request.setAttribute("loginCsrf",loginCsrf);
%>
<!doctype html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Đăng nhập</title><link rel="stylesheet" href="../../assets/member.css"></head>
<body><main class="member-panel">
    <h1>Đăng nhập</h1>
    <form action="LoginView4.jsp" method="post">
        <input type="hidden" name="csrf" value="<c:out value='${loginCsrf}' />">
        <label for="username">Tên đăng nhập:</label>
        <input id="username" name="username" autocomplete="username" maxlength="50" required value="<c:out value='${param.username}' />">
        <label for="password">Mật khẩu:</label>
        <input type="password" id="password" name="password" autocomplete="current-password" maxlength="1024" required>
        <button type="submit">Đăng nhập</button>
        <c:if test="${not empty message}"><p role="alert"><c:out value="${message}" /></p></c:if>
    </form>
</main></body></html>
