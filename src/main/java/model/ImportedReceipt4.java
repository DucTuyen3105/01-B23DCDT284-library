package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

/** Entity from report section 2.1. */
public class ImportedReceipt4 {
    private int id;
    private LocalDate importDate;
    private Supplier4 supplier;
    private LibraryStaff4 libraryStaff;
    private List<DetailImportedReceipt4> details = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getImportDate() { return importDate; }
    public void setImportDate(LocalDate importDate) { this.importDate = importDate; }

    public Supplier4 getSupplier() { return supplier; }
    public void setSupplier(Supplier4 supplier) { this.supplier = supplier; }

    public LibraryStaff4 getLibraryStaff() { return libraryStaff; }
    public void setLibraryStaff(LibraryStaff4 libraryStaff) { this.libraryStaff = libraryStaff; }

    public List<DetailImportedReceipt4> getDetails() { return details; }
    public void setDetails(List<DetailImportedReceipt4> details) { this.details = details; }

    /** Derived value: not stored in tblImportedReceipt4. */
    public BigDecimal getTotalAmount() {
        return details.stream().map(d -> d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
