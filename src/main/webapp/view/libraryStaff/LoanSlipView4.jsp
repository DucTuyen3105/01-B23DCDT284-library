<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="dao.*,model.*,java.sql.Connection,java.sql.SQLException,java.time.LocalDate,java.util.UUID" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    request.setCharacterEncoding("UTF-8");
    synchronized (session) {
        if ("GET".equals(request.getMethod()) && "1".equals(request.getParameter("new"))) {
            session.removeAttribute("module2.loanSlip"); session.removeAttribute("module2.readerCard");
            session.removeAttribute("module2.document"); session.removeAttribute("module2.csrf");
            session.removeAttribute("module2.readerLookupMode");
        }
        LoanSlip4 loanSlip = (LoanSlip4) session.getAttribute("module2.loanSlip");
        if (loanSlip == null) {
            loanSlip = new LoanSlip4(); loanSlip.setBorrowingDate(LocalDate.now());
            loanSlip.setDueDate(LocalDate.now().plusDays(Math.max(1, Integer.getInteger("loan.days", 14))));
            session.setAttribute("module2.loanSlip", loanSlip);
        }
        String csrf = (String) session.getAttribute("module2.csrf");
        if (csrf == null) { csrf = UUID.randomUUID().toString(); session.setAttribute("module2.csrf", csrf); }
        Reader4 reader = loanSlip.getReader();
        ReaderCard4 readerCard = (ReaderCard4) session.getAttribute("module2.readerCard");
        Document4 document = (Document4) session.getAttribute("module2.document");
        String readerLookupMode = (String) session.getAttribute("module2.readerLookupMode");
        if (readerLookupMode == null) readerLookupMode = "reader";
        String message = "";
        if ("POST".equals(request.getMethod())) {
            if (!csrf.equals(request.getParameter("csrf"))) { response.sendError(403); return; }
            String action = request.getParameter("action");
            try {
                if (loanSlip.getId() > 0) {
                    message = "Phiếu mượn đã được lưu. Chọn In phiếu mượn.";
                } else if ("identify".equals(action)) {
                    // Scenario 3.0: ReaderDAO4 first, then ReaderCardDAO4.
                    loanSlip.setReader(null); loanSlip.getDetails().clear();
                    reader = null; readerCard = null; document = null;
                    session.removeAttribute("module2.readerCard"); session.removeAttribute("module2.document");
                    String readerCode = request.getParameter("readerCode");
                    int code = Integer.parseInt(readerCode == null ? "" : readerCode.trim());
                    if (code <= 0) throw new NumberFormatException();
                    ReaderDAO4 readerDAO = new ReaderDAO4();
                    // Figure 2.5 has a single code field and a static label, no mode selector.
                    Reader4 byReaderCode = readerDAO.getReaderTheoID(code);
                    Reader4 byCardCode = readerDAO.getReaderTheoMaThe(code);
                    boolean ambiguousCode = byReaderCode != null && byCardCode != null
                            && byReaderCode.getId() != byCardCode.getId();
                    readerLookupMode = byReaderCode != null ? "reader" : "card";
                    session.setAttribute("module2.readerLookupMode", readerLookupMode);
                    reader = ambiguousCode ? null : (byReaderCode != null ? byReaderCode : byCardCode);
                    readerCard = reader == null ? null : new ReaderCardDAO4().getTheCuaReader(reader.getId());
                    loanSlip.setReader(null); loanSlip.getDetails().clear(); document = null;
                    LocalDate today = LocalDate.now();
                    if (ambiguousCode) message = "Mã trùng giữa độc giả và thẻ của người khác. Vui lòng kiểm tra lại mã.";
                    else if (reader == null) message = "Không tìm thấy độc giả.";
                    else if (!"ACTIVE".equals(reader.getStatus())) message = "Độc giả không hoạt động.";
                    else if (readerCard == null || !"ACTIVE".equals(readerCard.getStatus())
                            || readerCard.getIssueDate().isAfter(today) || readerCard.getExpiryDate().isBefore(today)
                            || ("card".equals(readerLookupMode) && readerCard.getId() != code)) {
                        message = "Thẻ độc giả không hợp lệ hoặc đã hết hạn.";
                    } else { loanSlip.setReader(reader); }
                    session.setAttribute("module2.readerCard", readerCard);
                    session.removeAttribute("module2.document");
                } else if ("lookup".equals(action)) {
                    document = null; session.removeAttribute("module2.document");
                    int code = Integer.parseInt(request.getParameter("documentCode"));
                    if (code <= 0) throw new NumberFormatException();
                    DocumentDAO4 documentDAO = new DocumentDAO4();
                    document = documentDAO.getTaiLieuTheoID(code);
                    boolean available = document != null && documentDAO.kiemTraKhaDung(code);
                    session.setAttribute("module2.document", document);
                    if (document == null) message = "Không tìm thấy tài liệu.";
                    else if (!available) message = "Tài liệu không khả dụng.";
                } else if ("add".equals(action)) {
                    if (loanSlip.getReader() == null) message = "Cần xác định độc giả và thẻ hợp lệ trước.";
                    else if (document == null || !String.valueOf(document.getId()).equals(request.getParameter("documentCode"))) {
                        message = "Cần tra cứu tài liệu trước khi thêm.";
                    } else if (document.getQuantity() <= 0 || !"AVAILABLE".equals(document.getStatus())) {
                        message = "Tài liệu không khả dụng.";
                    } else {
                        boolean duplicate = false;
                        for (DetailLoanSlip4 d : loanSlip.getDetails()) if (d.getDocument().getId() == document.getId()) duplicate = true;
                        if (duplicate) message = "Tài liệu đã có trong danh sách mượn.";
                        else { DetailLoanSlip4 detail = new DetailLoanSlip4(); detail.setDocument(document); loanSlip.getDetails().add(detail); }
                    }
                } else if ("submit".equals(action)) {
                    LibraryStaff4 currentStaff = (LibraryStaff4) session.getAttribute("currentStaff");
                    if (currentStaff == null) message = "Cần đăng nhập bằng tài khoản nhân viên thư viện để xác nhận.";
                    else if (loanSlip.getReader() == null || readerCard == null || loanSlip.getDetails().isEmpty()) {
                        message = "Cần độc giả, thẻ hợp lệ và ít nhất một tài liệu.";
                    } else {
                        // Keep the designed write order; all three DAOs share one transaction.
                        try (Connection connection = DAO4.getConnection()) {
                            connection.setAutoCommit(false);
                            try {
                                Reader4 freshReader = new ReaderDAO4(connection).getReaderTheoID(loanSlip.getReader().getId());
                                ReaderCard4 freshCard = new ReaderCardDAO4(connection).getTheCuaReader(loanSlip.getReader().getId());
                                LocalDate today = LocalDate.now();
                                if (freshReader == null || !"ACTIVE".equals(freshReader.getStatus()) || freshCard == null
                                        || freshCard.getId() != readerCard.getId() || !"ACTIVE".equals(freshCard.getStatus())
                                        || freshCard.getIssueDate().isAfter(today) || freshCard.getExpiryDate().isBefore(today)) {
                                    throw new IllegalArgumentException("Độc giả hoặc thẻ không còn hợp lệ.");
                                }
                                LoanSlip4 saved = new LoanSlip4(); saved.setReader(freshReader); saved.setLibraryStaff(currentStaff);
                                saved.setBorrowingDate(today); saved.setDueDate(today.plusDays(Math.max(1, Integer.getInteger("loan.days", 14))));
                                saved.setStatus("BORROWING");
                                for (DetailLoanSlip4 d : loanSlip.getDetails()) {
                                    DetailLoanSlip4 copy = new DetailLoanSlip4(); copy.setDocument(d.getDocument()); saved.getDetails().add(copy);
                                }
                                new LoanSlipDAO4(connection).taoPhieuMuon(saved);
                                new DetailLoanSlipDAO4(connection).taoChiTietPhieuMuon(saved.getId(), saved.getDetails());
                                new DocumentDAO4(connection).capNhatSoLuong(saved.getDetails());
                                connection.commit();
                                loanSlip = saved; reader = freshReader; readerCard = freshCard;
                                session.setAttribute("module2.loanSlip", saved);
                                csrf = UUID.randomUUID().toString(); session.setAttribute("module2.csrf", csrf);
                                message = "Lưu phiếu mượn thành công.";
                            } catch (SQLException | IllegalArgumentException e) {
                                connection.rollback(); throw e;
                            }
                        }
                    }
                } else { response.sendError(400); return; }
            } catch (NumberFormatException e) {
                message = "Mã phải là số nguyên dương.";
            } catch (IllegalArgumentException e) {
                message = e.getMessage();
            } catch (SQLException e) {
                application.log("LoanSlipView4: lending failed", e);
                message = "Không thể lưu hoặc tra cứu. Kiểm tra độc giả, thẻ và số lượng tài liệu rồi thử lại.";
            }
        }
        request.setAttribute("loanSlip", loanSlip); request.setAttribute("reader", reader);
        request.setAttribute("readerCard", readerCard); request.setAttribute("document", document);
        request.setAttribute("readerLookupMode", readerLookupMode); request.setAttribute("csrf", csrf);
        request.setAttribute("message", message);
    }
%>
<!doctype html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Lập phiếu mượn</title><link rel="stylesheet" href="../../assets/library-module2.css"></head>
<body><main class="panel loan-panel">
    <h1>Lập phiếu mượn</h1>
    <form action="LoanSlipView4.jsp" method="post" class="reader-lookup">
        <input type="hidden" name="csrf" value="<c:out value='${csrf}' />">
        <label for="inReaderCode">Tra theo: Mã độc giả / Mã thẻ</label>
        <input id="inReaderCode" name="readerCode" aria-label="Mã độc giả hoặc mã thẻ" placeholder="Nhập hoặc quét mã" value="<c:out value='${param.readerCode}' />" ${loanSlip.id > 0 ? 'disabled' : ''}>
        <button id="subIdentifyReader" name="action" value="identify" ${loanSlip.id > 0 ? 'disabled' : ''}>Xác định</button>
    </form>
    <p id="outReaderInformation">Độc giả: <c:out value="${reader.fullName}" />
        <span class="separator">|</span> Thẻ: <c:if test="${loanSlip.reader != null}">hợp lệ</c:if>
        <span class="separator">|</span> Hết hạn: <c:out value="${readerCard.expiryDate}" />
    </p>
    <form action="LoanSlipView4.jsp" method="post" class="document-lookup">
        <input type="hidden" name="csrf" value="<c:out value='${csrf}' />">
        <label for="inDocumentCode">Mã tài liệu:</label>
        <input id="inDocumentCode" name="documentCode" placeholder="Nhập hoặc quét mã" value="<c:out value='${document.id}' />" ${loanSlip.id > 0 ? 'disabled' : ''}>
        <button id="subSelectDocument" name="action" value="lookup" ${loanSlip.id > 0 ? 'disabled' : ''}>Tra cứu</button>
        <button id="subAddDocument" name="action" value="add" ${loanSlip.id > 0 ? 'disabled' : ''}>Thêm tài liệu</button>
    </form>
    <p id="outDocumentInformation">Tài liệu đang xem: <c:out value="${document.name}" /> <span class="separator">|</span> Còn: <c:out value="${document.quantity}" /></p>
    <table id="outDocumentList">
        <colgroup><col style="width:9%"><col style="width:32%"><col style="width:21%"><col style="width:21%"><col style="width:17%"></colgroup>
        <thead><tr><th scope="col">Mã</th><th scope="col">Tên tài liệu</th><th scope="col">Thể loại</th><th scope="col">Tác giả</th><th scope="col">Số lượng khả dụng</th></tr></thead>
        <tbody><c:forEach var="detail" items="${loanSlip.details}"><tr>
            <td><c:out value="${detail.document.id}" /></td><td><c:out value="${detail.document.name}" /></td>
            <td><c:out value="${detail.document.category}" /></td><td><c:out value="${detail.document.author}" /></td>
            <td><c:out value="${detail.document.quantity}" /></td>
        </tr></c:forEach></tbody>
    </table>
    <p class="loan-dates"><span>Ngày mượn: <c:out value="${loanSlip.borrowingDate}" /></span><span>Hạn trả: <c:out value="${loanSlip.dueDate}" /></span></p>
    <div class="loan-actions">
        <form action="LoanSlipView4.jsp" method="post"><input type="hidden" name="csrf" value="<c:out value='${csrf}' />">
            <button id="subSubmit" name="action" value="submit" ${loanSlip.id > 0 ? 'disabled' : ''}>Xác nhận</button>
        </form>
        <form action="LoanSlipPrintView4.jsp" method="get"><input type="hidden" name="loanSlipId" value="<c:out value='${loanSlip.id}' />">
            <button id="subPrint" ${loanSlip.id <= 0 ? 'disabled' : ''}>In phiếu mượn</button>
        </form>
    </div>
    <c:if test="${not empty message}"><p class="operation-message" role="status"><c:out value="${message}" /></p></c:if>
</main></body></html>
