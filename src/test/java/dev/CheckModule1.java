package dev;

import dao.DocumentDAO4;
import java.util.List;
import model.Document4;

/** Read-only manual check of module 1 against the configured database. */
public final class CheckModule1 {
    public static void main(String[] args) throws Exception {
        String keyword = args.length > 0 ? args[0] : "Java";
        try {
            DocumentDAO4 dao = new DocumentDAO4();
            List<Document4> documents = dao.getTaiLieuTheoTen(keyword);
            System.out.println("Search [" + keyword + "]: " + documents.size() + " result(s)");
            for (Document4 d : documents) {
                System.out.println("id=" + d.getId() + ", name=" + d.getName()
                        + ", quantity=" + d.getQuantity() + ", status=" + d.getStatus());
                Document4 detail = dao.getTaiLieuTheoID(d.getId());
                if (detail == null || !d.getName().equals(detail.getName())) {
                    throw new IllegalStateException("Search/detail mismatch for id " + d.getId());
                }
                System.out.println("  Detail: author=" + detail.getAuthor()
                        + ", publisher=" + detail.getPublisher()
                        + ", year=" + detail.getPublicationYear()
                        + ", category=" + detail.getCategory()
                        + ", description=" + detail.getDescription());
            }
            System.out.println("Module 1 DAO check completed.");
        } finally {
            com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
        }
    }
}
