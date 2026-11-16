<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="dao.LoanSlipDAO4,model.LoanSlip4,model.LibraryStaff4,java.sql.SQLException" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    LibraryStaff4 currentStaff = (LibraryStaff4) session.getAttribute("currentStaff");
    if (currentStaff == null) { response.sendError(403); return; }
    int loanSlipId;
    try { loanSlipId = Integer.parseInt(request.getParameter("loanSlipId")); if (loanSlipId <= 0) throw new NumberFormatException(); }
    catch (NumberFormatException e) { response.sendError(400); return; }
    LoanSlip4 loanSlip;
    try { loanSlip = new LoanSlipDAO4().getPhieuMuonTheoID(loanSlipId); }
    catch (SQLException e) { application.log("LoanSlipPrintView4: lookup failed", e); response.sendError(500); return; }
    if (loanSlip == null) { response.sendError(404); return; }
    if (loanSlip.getLibraryStaff().getId() != currentStaff.getId()) { response.sendError(403); return; }
    request.setAttribute("loanSlip", loanSlip);
    request.setAttribute("loanSlipCode", String.format("PM%03d", loanSlip.getId()));
%>
<!doctype html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Phiếu mượn tài liệu</title><link rel="stylesheet" href="../../assets/library-module2.css"></head>
<body><main class="panel print-panel" id="outLoanSlipInformation">
    <h1>Phiếu mượn tài liệu</h1>
    <p>Mã phiếu: <c:out value="${loanSlipCode}" /></p>
    <p class="print-pair"><span>Độc giả: <c:out value="${loanSlip.reader.fullName}" /></span><span>Nhân viên: <c:out value="${loanSlip.libraryStaff.fullName}" /></span></p>
    <p class="print-pair"><span>Ngày mượn: <c:out value="${loanSlip.borrowingDate}" /></span><span>Hạn trả: <c:out value="${loanSlip.dueDate}" /></span></p>
    <table><colgroup><col style="width:17%"><col style="width:83%"></colgroup>
        <thead><tr><th scope="col">Mã tài liệu</th><th scope="col">Tên tài liệu</th></tr></thead>
        <tbody><c:forEach var="detail" items="${loanSlip.details}"><tr><td><c:out value="${detail.document.id}" /></td><td><c:out value="${detail.document.name}" /></td></tr></c:forEach></tbody>
    </table>
    <button type="button" id="subPrint" onclick="window.print()">In</button>
</main></body></html>
