<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="dao.DocumentDAO4,model.Document4,java.sql.SQLException" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    request.setCharacterEncoding("UTF-8");
    Integer documentId;
    try {
        documentId = Integer.valueOf(request.getParameter("documentId"));
        if (documentId <= 0) throw new NumberFormatException();
    } catch (NumberFormatException e) {
        response.sendError(400);
        return;
    }
    Document4 document;
    // Scenario 3.0, steps 13-20: detail JSP queries the selected id again.
    try {
        DocumentDAO4 documentDAO = new DocumentDAO4();
        document = documentDAO.getTaiLieuTheoID(documentId);
    } catch (SQLException e) {
        application.log("DocumentDetailView4: document lookup failed", e);
        response.sendError(500);
        return;
    }
    if (document == null) {
        response.sendError(404);
        return;
    }
    request.setAttribute("documentId", documentId);
    request.setAttribute("document", document);
%>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Chi tiết tài liệu</title>
    <link rel="stylesheet" href="../../assets/reader-module1.css">
</head>
<body>
<main class="panel detail-panel">
    <h1>Chi tiết tài liệu</h1>
    <dl class="document-details">
        <dt>Mã tài liệu:</dt><dd id="outDocumentCode"><c:out value="${document.id}" /></dd>
        <dt>Tên tài liệu:</dt><dd id="outName"><c:out value="${document.name}" /></dd>
        <dt>Thể loại:</dt><dd id="outCategory"><c:out value="${document.category}" /></dd>
        <dt>Tác giả:</dt><dd id="outAuthor"><c:out value="${document.author}" /></dd>
        <dt>Nhà xuất bản:</dt><dd id="outPublisher"><c:out value="${document.publisher}" /></dd>
        <dt>Số lượng:</dt><dd id="outQuantity"><c:out value="${document.quantity}" /></dd>
    </dl>
    <c:url var="backUrl" value="DocumentSearchView4.jsp">
        <c:if test="${param.keyword != null}"><c:param name="keyword" value="${param.keyword}" /></c:if>
    </c:url>
    <a href="<c:out value='${backUrl}' />" id="subBack" class="button detail-back">Quay lại</a>
</main>
</body>
</html>
