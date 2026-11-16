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

    // Lending operations are implemented with module 2.
    public boolean kiemTraKhaDung(int documentId) throws SQLException {
        throw new UnsupportedOperationException("Module 2 is not implemented yet");
    }

    public boolean capNhatSoLuong(Connection connection, List<DetailLoanSlip4> details) throws SQLException {
        throw new UnsupportedOperationException("Module 2 is not implemented yet");
    }
}