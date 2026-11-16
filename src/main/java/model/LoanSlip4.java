package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Entity from report section 2.1. */
public class LoanSlip4 {
    private int id;
    private LocalDate borrowingDate;
    private LocalDate dueDate;
    private String status;
    private Reader4 reader;
    private LibraryStaff4 libraryStaff;
    private List<DetailLoanSlip4> details = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getBorrowingDate() { return borrowingDate; }
    public void setBorrowingDate(LocalDate borrowingDate) { this.borrowingDate = borrowingDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Reader4 getReader() { return reader; }
    public void setReader(Reader4 reader) { this.reader = reader; }

    public LibraryStaff4 getLibraryStaff() { return libraryStaff; }
    public void setLibraryStaff(LibraryStaff4 libraryStaff) { this.libraryStaff = libraryStaff; }

    public List<DetailLoanSlip4> getDetails() { return details; }
    public void setDetails(List<DetailLoanSlip4> details) { this.details = details; }
}
