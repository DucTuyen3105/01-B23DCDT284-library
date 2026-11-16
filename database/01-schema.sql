-- MySQL 8.4, UTF-8. Section 2.2: one table per entity, shared PK inheritance.
CREATE TABLE tblMember4 (
 id INT AUTO_INCREMENT PRIMARY KEY,
 username VARCHAR(50) NOT NULL UNIQUE,
 password VARCHAR(255) NOT NULL,
 fullname VARCHAR(100) NOT NULL,
 address VARCHAR(255), phoneNumber VARCHAR(20), email VARCHAR(100)
);
CREATE TABLE tblEmployee4 (
 id INT PRIMARY KEY, position VARCHAR(100),
 FOREIGN KEY (id) REFERENCES tblMember4(id)
);
CREATE TABLE tblReader4 (
 id INT PRIMARY KEY, registrationDate DATE NOT NULL, status VARCHAR(30) NOT NULL,
 FOREIGN KEY (id) REFERENCES tblMember4(id)
);
CREATE TABLE tblLibraryStaff4 (
 id INT PRIMARY KEY, FOREIGN KEY (id) REFERENCES tblEmployee4(id)
);
CREATE TABLE tblManagementStaff4 (
 id INT PRIMARY KEY, FOREIGN KEY (id) REFERENCES tblEmployee4(id)
);
CREATE TABLE tblReaderCard4 (
 id INT AUTO_INCREMENT PRIMARY KEY, issueDate DATE NOT NULL, expiryDate DATE NOT NULL,
 status VARCHAR(30) NOT NULL, readerId INT NOT NULL,
 FOREIGN KEY (readerId) REFERENCES tblReader4(id),
 CHECK (expiryDate >= issueDate)
);
CREATE TABLE tblDocument4 (
 id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255) NOT NULL, category VARCHAR(100),
 author VARCHAR(255), publisher VARCHAR(255), publicationYear INT,
 description TEXT, quantity INT NOT NULL DEFAULT 0, status VARCHAR(30) NOT NULL,
 CHECK (quantity >= 0)
);
CREATE TABLE tblSupplier4 (
 id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255) NOT NULL,
 address VARCHAR(255), phoneNumber VARCHAR(20), email VARCHAR(100), description TEXT
);
CREATE TABLE tblLoanSlip4 (
 id INT AUTO_INCREMENT PRIMARY KEY, borrowingDate DATE NOT NULL, dueDate DATE NOT NULL,
 status VARCHAR(30) NOT NULL, readerId INT NOT NULL, libraryStaffId INT NOT NULL,
 FOREIGN KEY (readerId) REFERENCES tblReader4(id),
 FOREIGN KEY (libraryStaffId) REFERENCES tblLibraryStaff4(id),
 CHECK (dueDate >= borrowingDate)
);
CREATE TABLE tblDetailLoanSlip4 (
 id INT AUTO_INCREMENT PRIMARY KEY, actualReturnDate DATE, conditionOnReturn VARCHAR(255),
 loanSlipId INT NOT NULL, documentId INT NOT NULL,
 FOREIGN KEY (loanSlipId) REFERENCES tblLoanSlip4(id),
 FOREIGN KEY (documentId) REFERENCES tblDocument4(id),
 UNIQUE (loanSlipId, documentId)
);
CREATE TABLE tblImportedReceipt4 (
 id INT AUTO_INCREMENT PRIMARY KEY, importDate DATE NOT NULL,
 supplierId INT NOT NULL, libraryStaffId INT NOT NULL,
 FOREIGN KEY (supplierId) REFERENCES tblSupplier4(id),
 FOREIGN KEY (libraryStaffId) REFERENCES tblLibraryStaff4(id)
);
CREATE TABLE tblDetailImportedReceipt4 (
 id INT AUTO_INCREMENT PRIMARY KEY, quantity INT NOT NULL, unitPrice DECIMAL(18,2) NOT NULL,
 importedReceiptId INT NOT NULL, documentId INT NOT NULL,
 FOREIGN KEY (importedReceiptId) REFERENCES tblImportedReceipt4(id),
 FOREIGN KEY (documentId) REFERENCES tblDocument4(id),
 CHECK (quantity > 0), CHECK (unitPrice >= 0),
 UNIQUE (importedReceiptId, documentId)
);
