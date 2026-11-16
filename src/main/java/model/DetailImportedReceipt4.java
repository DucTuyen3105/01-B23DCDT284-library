package model;

import java.math.BigDecimal;

/** Entity from report section 2.1. */
public class DetailImportedReceipt4 {
    private int id;
    private int quantity;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private Document4 document;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public Document4 getDocument() { return document; }
    public void setDocument(Document4 document) { this.document = document; }
}
