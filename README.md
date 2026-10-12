# TraceBack --- File Integrity & Forensics Workbench

TraceBack is a web-based file integrity and basic digital forensics
application. It uses SHA-256 cryptographic hashing to help users analyze
files, compare file contents, identify duplicate files, and verify an
uploaded file against a previously stored hash. The application also
maintains analysis, comparison, and verification history.

## Features

-   **Single File Analysis:** Upload a file and calculate its SHA-256
    hash.
-   **File Comparison:** Compare two files and report whether their
    contents are identical or different.
-   **Analysis History:** View previously analyzed files and their
    recorded details.
-   **Comparison History:** Review past file comparison results.
-   **Duplicate Detection:** Identify records with matching SHA-256
    hashes.
-   **CSV Export:** Export duplicate-file records to CSV.
-   **File Verification:** Compare an uploaded file's hash with a
    previously stored hash.
-   **Verification History:** Review previous verification attempts.
-   **PDF Reports:** Generate PDF reports for analysis and file
    comparisons.

## Technology Stack

-   Backend: Java Servlets
-   Frontend: HTML, CSS, JavaScript, JSP
-   Database access: JDBC
-   Database: MySQL
-   Web server: Apache Tomcat 10.1
-   IDE: Eclipse
-   Hashing algorithm: SHA-256
-   PDF generation: iText 5

## How It Works

1.  A user uploads a file for analysis.
2.  TraceBack calculates its SHA-256 hash and stores the analysis record
    in MySQL.
3.  For comparison, TraceBack checks whether two files have matching
    hashes.
4.  For verification, the hash of an uploaded file is compared with the
    stored hash of a selected original record.
5.  Analysis, comparison, and verification activities can be reviewed
    through history pages.

### Understanding Results

-   **MATCH / IDENTICAL:** The compared SHA-256 hashes match.
-   **MISMATCH / DIFFERENT:** The compared SHA-256 hashes do not match.

A hash mismatch indicates that file bytes differ from the recorded
version. By itself, it does not prove malicious tampering or explain why
the file changed.

## Prerequisites

-   Java Development Kit (JDK)
-   Eclipse IDE with web application support
-   Apache Tomcat 10.1
-   MySQL Server
-   MySQL Connector/J
-   iText 5 library for PDF reports

## Setup

The exact database credentials and project configuration depend on your
local environment. Never commit real passwords or secrets to a public
repository.

1.  Import the `TraceBack` project into Eclipse as a web application
    project.

2.  Create a MySQL database named `traceback_db`.

3.  Create the tables required by the application, including file
    analysis, comparison history, and verification history tables. Use
    the SQL schema from your project.

4.  Configure the database connection in `DBConnection.java` (or the
    database configuration used by your project) with your local MySQL
    URL, username, and password.

5.  Add MySQL Connector/J and iText 5 to the project's libraries if they
    are not already included.

6.  Configure Apache Tomcat 10.1 in Eclipse.

7.  Clean and publish the project to Tomcat.

8.  Start Tomcat and open the application. A typical local URL is:

    `http://localhost:8080/TraceBack/`

    If your port or context path differs, use the URL configured in
    Eclipse.

## Basic Usage

### Analyze a File

1.  Open the TraceBack home page.
2.  Select a file and run the analysis.
3.  Review the generated SHA-256 hash and saved record.

### Compare Files

1.  Open the comparison feature.
2.  Select two files and run the comparison.
3.  Review the result and comparison history.

### Find Duplicate Files

1.  Analyze files so their hashes are stored.
2.  Open the Duplicates page.
3.  Review records with matching hashes and export CSV if needed.

### Verify a File

1.  Select an existing analysis record.
2.  Upload the file you want to verify against it.
3.  Review the MATCH or MISMATCH result and verification history.

## Project Structure

Depending on the Eclipse configuration, Java source files may appear
under `Java Resources/src` or `src/main/java`, and web resources under
`WebContent` or `src/main/webapp`.

Typical packages include:

-   `com.traceback.model` --- data models
-   `com.traceback.dao` --- JDBC data access objects
-   `com.traceback.servlet` --- request-handling servlets
-   `com.traceback.util` --- shared utilities, such as database
    connection handling

## Current Scope and Limitations

-   Verification compares an uploaded file with a previously stored
    hash; it does not automatically monitor files continuously in the
    background.
-   Hash mismatches show that contents differ, but do not establish the
    cause or intent behind the change.
-   Metadata extraction, scheduled integrity monitoring, tampering
    alerts, and login/access control should be described as future
    enhancements unless they are implemented and tested.
-   SHA-256 checks integrity; it does not encrypt file contents.

## Future Enhancements

-   Scheduled file integrity monitoring
-   Alerts when a monitored file's hash changes
-   Detailed file metadata analysis
-   A consolidated audit timeline
-   Secure user authentication and access control

## Security Notes

-   Validate uploaded files and enforce file-size limits.
-   Keep database credentials out of public repositories.
-   Use prepared statements for database operations.
-   Never execute uploaded files.
-   Restrict access to sensitive analysis records when deploying beyond
    a local demonstration environment.

## Testing

The main workflows have been manually tested, including single-file
analysis, identical and different file comparison, history pages,
duplicate detection, CSV export, file verification, and browser
navigation. Re-test in the target environment before release or
demonstration.

## Author

**Shubham**

## Disclaimer

TraceBack is an educational file integrity and basic digital forensics
project. It helps compare file hashes and maintain analysis records; it
is not a complete forensic chain-of-custody system and cannot
independently prove malicious activity.
