<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Trang nhân viên quản lý | LibMan</title><link rel="stylesheet" href="../../assets/app.css"></head>
<body><main><h1>Trang nhân viên quản lý</h1>
<p>Nhân viên: <c:out value="${sessionScope.currentManagementStaff.fullName}" /></p>
<p>Chức năng báo cáo thống kê chưa được triển khai.</p>
<a href="../member/LogoutView4.jsp">Đăng xuất</a>
</main></body></html>
