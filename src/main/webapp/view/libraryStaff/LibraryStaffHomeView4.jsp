<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="model.LibraryStaff4" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% LibraryStaff4 currentStaff = (LibraryStaff4) session.getAttribute("currentStaff");
   request.setAttribute("currentStaff", currentStaff); %>
<!doctype html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Trang chủ nhân viên thư viện</title><link rel="stylesheet" href="../../assets/library-module2.css"></head>
<body><main class="panel staff-home-panel">
    <h1>Trang chủ nhân viên thư viện</h1>
    <p class="staff-name">Nhân viên: <c:out value="${currentStaff.fullName}" /></p>
    <form action="LoanSlipView4.jsp" method="get" class="home-action">
        <input type="hidden" name="new" value="1">
        <button type="submit" id="subLendDocument">Cho mượn tài liệu</button>
    </form>
</main></body></html>
