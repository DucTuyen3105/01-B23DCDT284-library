<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="dao.DocumentDAO4,model.Document4,java.util.List,java.util.ArrayList,java.sql.SQLException" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    request.setCharacterEncoding("UTF-8");
    String keyword = request.getParameter("keyword");
    List<Document4> documents = new ArrayList<>();
    // Scenario 3.0, steps 4-11: JSP -> DocumentDAO4 -> Document4 -> list.
    if (keyword != null) {
        try {
            DocumentDAO4 documentDAO = new DocumentDAO4();
            documents = documentDAO.getTaiLieuTheoTen(keyword);
        } catch (SQLException e) {
            application.log("DocumentSearchView4: document search failed", e);
            response.sendError(500);
            return;
        }
    }
    request.setAttribute("keyword", keyword);
    request.setAttribute("documents", documents);
%>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Tìm kiếm tài liệu</title>
    <link rel="stylesheet" href="../../assets/reader-module1.css">
</head>
<body>
<main class="panel search-panel">
    <h1>Tìm kiếm tài liệu</h1>
    <form action="DocumentSearchView4.jsp" method="get" class="search-form">
        <label for="inDocumentName">Tên tài liệu:</label>
        <input type="text" id="inDocumentName" name="keyword" value="<c:out value='${keyword}' />">
        <button type="submit" id="subSearch">Tìm kiếm</button>
        <a href="ReaderHomeView4.jsp" id="subBack" class="button">Về trang chủ</a>
    </form>
    <p class="results-heading">Kết quả tìm kiếm</p>
    <table id="outDocumentList">
        <colgroup><col class="code-col"><col class="name-col"><col class="category-col"><col class="author-col"><col class="publisher-col"><col class="quantity-col"></colgroup>
        <thead><tr><th scope="col">Mã</th><th scope="col">Tên tài liệu</th><th scope="col">Thể loại</th><th scope="col">Tác giả</th><th scope="col">Nhà xuất bản</th><th scope="col">Số lượng</th></tr></thead>
        <tbody>
        <c:forEach var="item" items="${documents}">
            <%-- Figure 2.4: selectedDocumentId is passed to the detail view as documentId. --%>
            <c:set var="selectedDocumentId" value="${item.id}" />
            <c:url var="detailUrl" value="DocumentDetailView4.jsp">
                <c:param name="documentId" value="${selectedDocumentId}" />
                <c:param name="keyword" value="${keyword}" />
            </c:url>
            <tr>
                <td><a class="subViewDocumentDetail" href="<c:out value='${detailUrl}' />"><c:out value="${item.id}" /></a></td>
                <td><c:out value="${item.name}" /></td>
                <td><c:out value="${item.category}" /></td>
                <td><c:out value="${item.author}" /></td>
                <td><c:out value="${item.publisher}" /></td>
                <td><c:out value="${item.quantity}" /></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</main>
</body>
</html>
