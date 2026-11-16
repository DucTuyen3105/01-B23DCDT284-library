package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Entity from report section 2.1. */
public class Reader4 extends Member4 {
    private LocalDate registrationDate;
    private String status;
    private List<ReaderCard4> readerCards = new ArrayList<>();

    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { this.registrationDate = registrationDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<ReaderCard4> getReaderCards() { return readerCards; }
    public void setReaderCards(List<ReaderCard4> readerCards) { this.readerCards = readerCards; }
}
