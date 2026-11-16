package model;

import java.time.LocalDate;

/** Entity from report section 2.1. */
public class DetailLoanSlip4 {
    private int id;
    private LocalDate actualReturnDate;
    private String conditionOnReturn;
    private Document4 document;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getActualReturnDate() { return actualReturnDate; }
    public void setActualReturnDate(LocalDate actualReturnDate) { this.actualReturnDate = actualReturnDate; }

    public String getConditionOnReturn() { return conditionOnReturn; }
    public void setConditionOnReturn(String conditionOnReturn) { this.conditionOnReturn = conditionOnReturn; }

    public Document4 getDocument() { return document; }
    public void setDocument(Document4 document) { this.document = document; }
}
