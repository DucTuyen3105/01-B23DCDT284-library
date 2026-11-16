<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="model.Reader4" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    // Figure 2.4: currentReader is supplied by the preceding login use case.
    Reader4 currentReader = (Reader4) session.getAttribute("currentReader");
    request.setAttribute("currentReader", currentReader);
%>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Trang chủ độc giả</title>
    <link rel="stylesheet" href="../../assets/reader-module1.css">
</head>
<body>
<main class="panel home-panel">
    <h1>Trang chủ độc giả</h1>
    <p class="reader-name">Độc giả: <c:out value="${currentReader.fullName}" /></p>
    <%-- Scenario 3.0, steps 1-3: open the search JSP, without querying the DAO. --%>
    <form action="DocumentSearchView4.jsp" method="get" class="home-action">
        <button type="submit" id="subFindDocument">Tìm kiếm tài liệu</button>
    </form>
</main>
</body>
</html>
