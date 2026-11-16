package model;

import java.time.LocalDate;

/** Entity from report section 2.1. */
public class ReaderCard4 {
    private int id;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String status;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
