package dao;

import model.Document4;
import model.DetailLoanSlip4;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** JDBC access for document search/detail (module 1) and lending (module 2). */
public class DocumentDAO4 extends DAO4 {
    public DocumentDAO4() { }
    public DocumentDAO4(Connection connection) { super(connection); }
    private static final String COLUMNS = "id, name, category, author, publisher, "
            + "publicationYear, description, quantity, status";

    /**
     * Finds names containing the trimmed keyword. Null/blank input returns an
     * empty list. %, _ and ! are literal characters, not LIKE wildcards.
     * Case/accent matching follows the database collation.
     */
    public List<Document4> getTaiLieuTheoTen(String keyword) throws SQLException {
        List<Document4> documents = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return documents;
        }
        String escaped = keyword.trim().replace("!", "!!")
                .replace("%", "!%").replace("_", "!_");
        String sql = "SELECT " + COLUMNS + " FROM tblDocument4 "
                + "WHERE name LIKE ? ESCAPE '!' ORDER BY name, id";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + escaped + "%");
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    documents.add(mapDocument(rs));
                }
            }
        }
        return documents;
    }

    /** Returns null when the id is invalid or the document does not exist. */
    public Document4 getTaiLieuTheoID(int documentId) throws SQLException {
        if (documentId <= 0) {
            return null;
        }
        String sql = "SELECT " + COLUMNS + " FROM tblDocument4 WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, documentId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? mapDocument(rs) : null;
            }
        }
    }

    private Document4 mapDocument(ResultSet rs) throws SQLException {
        Document4 document = new Document4();
        document.setId(rs.getInt("id"));
        document.setName(rs.getString("name"));
        document.setCategory(rs.getString("category"));
        document.setAuthor(rs.getString("author"));
        document.setPublisher(rs.getString("publisher"));
        document.setPublicationYear(rs.getInt("publicationYear"));
        document.setDescription(rs.getString("description"));
        document.setQuantity(rs.getInt("quantity"));
        document.setStatus(rs.getString("status"));
        return document;
    }

    public boolean kiemTraKhaDung(int documentId) throws SQLException {
        if (documentId <= 0) return false;
        try (Connection c = getConnection(); PreparedStatement s = c.prepareStatement(
                "SELECT id FROM tblDocument4 WHERE id=? AND quantity>0 AND status='AVAILABLE'")) {
            s.setInt(1, documentId);
            try (ResultSet rs = s.executeQuery()) { return rs.next(); }
        }
    }

    /** Called on the transaction connection shared with the slip/detail DAOs. */
    public boolean capNhatSoLuong(List<DetailLoanSlip4> details) throws SQLException {
        if (connection == null || connection.getAutoCommit()) {
            throw new SQLException("A transaction connection is required");
        }
        if (details == null || details.isEmpty()) throw new IllegalArgumentException("Empty documents");
        java.util.TreeSet<Integer> ids = new java.util.TreeSet<>();
        for (DetailLoanSlip4 detail : details) {
            if (detail == null || detail.getDocument() == null || detail.getDocument().getId() <= 0
                    || !ids.add(detail.getDocument().getId())) {
                throw new IllegalArgumentException("Invalid or duplicate document");
            }
        }
        try (PreparedStatement s = connection.prepareStatement("UPDATE tblDocument4 SET quantity=quantity-1 "
                + "WHERE id=? AND quantity>0 AND status='AVAILABLE'")) {
            for (int id : ids) {
                s.setInt(1, id);
                if (s.executeUpdate() != 1) throw new SQLException("Document is no longer available: " + id, "45000");
            }
        }
        return true;
    }

    public boolean capNhatSoLuong(Connection connection, List<DetailLoanSlip4> details) throws SQLException {
        return new DocumentDAO4(connection).capNhatSoLuong(details);
    }

}
