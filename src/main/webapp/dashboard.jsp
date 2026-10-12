
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.traceback.model.FileAnalysis" %>

<%!
    public String esc(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }

    public String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }

        if (bytes < 1024L * 1024L) {
            return String.format("%.2f KB", bytes / 1024.0);
        }

        if (bytes < 1024L * 1024L * 1024L) {
            return String.format("%.2f MB",
                    bytes / (1024.0 * 1024.0));
        }

        return String.format("%.2f GB",
                bytes / (1024.0 * 1024.0 * 1024.0));
    }
%>

<%
    String ctx = request.getContextPath();

    List<FileAnalysis> analyses =
        (List<FileAnalysis>) request.getAttribute("analyses");

    if (analyses == null) {
        analyses = new ArrayList<FileAnalysis>();
    }

    Set<String> uniqueHashes = new HashSet<String>();

    for (FileAnalysis item : analyses) {
        if (item.getHashSha256() != null) {
            uniqueHashes.add(item.getHashSha256());
        }
    }

    int totalFiles = request.getAttribute("totalAnalyses") != null
        ? (Integer) request.getAttribute("totalAnalyses")
        : analyses.size();

    int uniqueFiles = request.getAttribute("uniqueHashes") != null
        ? (Integer) request.getAttribute("uniqueHashes")
        : uniqueHashes.size();

    int duplicateCount = request.getAttribute("duplicateCount") != null
        ? (Integer) request.getAttribute("duplicateCount")
        : Math.max(0, totalFiles - uniqueFiles);

    Object totalComparisons =
        request.getAttribute("totalComparisons");

    Object matchingComparisons =
        request.getAttribute("matchingComparisons");

    Object mismatchingComparisons =
        request.getAttribute("mismatchingComparisons");
%>

<!DOCTYPE html>
<html lang="en" data-theme="dark">

<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Analysis History | TraceBack</title>

    <link rel="stylesheet"
          href="<%=ctx%>/style.css?v=8">
          <link rel="icon" type="image/svg+xml" href="favicon.svg">

    <script src="<%=ctx%>/app.js" defer></script>

    <style>
        .history-section {
            margin: 35px 30px;
            padding: 26px;
            border: 1px solid rgba(255,255,255,0.10);
            border-radius: 20px;
            background: rgba(255,255,255,0.025);
        }

        .history-heading {
            display: flex;
            align-items: center;
            justify-content: space-between;
            flex-wrap: wrap;
            gap: 16px;
            margin-bottom: 22px;
        }

        .history-heading h2 {
            margin: 0 0 8px;
        }

        .history-heading p {
            margin: 0;
            opacity: 0.75;
        }

        .history-count {
            padding: 10px 15px;
            border: 1px solid rgba(255,255,255,0.12);
            border-radius: 10px;
        }

        .history-search {
            width: 100%;
            max-width: 430px;
            padding: 13px 15px;
            margin-bottom: 20px;
            border: 1px solid rgba(180,140,255,0.25);
            border-radius: 10px;
            background: rgba(255,255,255,0.045);
            color: inherit;
            outline: none;
            box-sizing: border-box;
        }

        .history-search:focus {
            border-color: #a879ff;
        }

        .history-table-wrap {
            width: 100%;
            overflow-x: auto;
        }

        .history-table {
            width: 100%;
            min-width: 900px;
            border-collapse: collapse;
            text-align: left;
        }

        .history-table th {
            font-size: 12px;
            text-transform: uppercase;
            letter-spacing: 0.6px;
            opacity: 0.75;
        }

        .history-table th,
        .history-table td {
            padding: 16px 13px;
            border-bottom: 1px solid rgba(255,255,255,0.09);
            vertical-align: top;
        }

        .history-table tbody tr:hover {
            background: rgba(167,120,255,0.055);
        }

        .file-hash {
            display: inline-block;
            max-width: 300px;
            overflow-wrap: anywhere;
            font-family: monospace;
            font-size: 12px;
            line-height: 1.7;
        }

        .action-cell {
            white-space: nowrap;
        }

        .history-action {
            display: inline-block;
            margin: 3px 0;
            padding: 9px 12px;
            border-radius: 8px;
            background: #7657f6;
            color: #fff;
            text-decoration: none;
            font-size: 12px;
            font-weight: 600;
        }

        .history-action:hover {
            background: #6242e8;
        }

        .empty-history {
            padding: 35px 15px;
            text-align: center;
            opacity: 0.8;
        }

        .no-results {
            display: none;
            padding: 25px;
            text-align: center;
            opacity: 0.8;
        }

        @media (max-width: 700px) {
            .history-section {
                margin: 25px 14px;
                padding: 18px;
            }
        }
    </style>
</head>

<body>

<header class="topbar">

    <a href="<%=ctx%>/index.html" class="brand">
        <span class="brand-icon">⌁</span>
        <span>TraceBack</span>
    </a>

    <nav class="nav-links">
        <a href="<%=ctx%>/index.html">Home</a>

        <a href="<%=ctx%>/history" class="active">
            Analysis History
        </a>

        <a href="<%=ctx%>/comparison-history">
            Comparison History
        </a>

        <a href="<%=ctx%>/duplicates">
            Duplicate Files
        </a>

        <a href="<%=ctx%>/verify-file">
            Verify File
        </a>

        <a href="<%=ctx%>/verification-history">
            Verification History
        </a>
    </nav>

</header>

<main class="dashboard-page">

    <section class="hero dashboard-hero">

        <div class="hero-content">

            <div class="eyebrow">
                <span class="status-dot"></span>
                FORENSICS WORKSPACE
            </div>

            <h1>
                Analysis
                <span class="gradient-text">History</span>
            </h1>

            <p>
                Review analyzed files, inspect SHA-256 fingerprints,
                and identify files sharing the same hash.
            </p>

        </div>

        <a href="<%=ctx%>/index.html" class="primary-btn">
            + Analyze New File
        </a>

    </section>

    <!-- Statistics cards -->

    <section class="stats-grid">

        <article class="stat-card">
            <span class="stat-label">TOTAL ANALYSES</span>
            <strong class="stat-value"><%=totalFiles%></strong>
            <span class="stat-note">Files processed</span>
        </article>

        <article class="stat-card">
            <span class="stat-label">UNIQUE HASHES</span>
            <strong class="stat-value"><%=uniqueFiles%></strong>
            <span class="stat-note">Distinct fingerprints</span>
        </article>

        <article class="stat-card">
            <span class="stat-label">POSSIBLE DUPLICATES</span>
            <strong class="stat-value"><%=duplicateCount%></strong>
            <span class="stat-note">Repeated hash entries</span>
        </article>

        <article class="stat-card">
            <span class="stat-label">TOTAL COMPARISONS</span>
            <strong class="stat-value">
                <%=totalComparisons != null ? totalComparisons : 0%>
            </strong>
            <span class="stat-note">Files compared</span>
        </article>

        <article class="stat-card">
            <span class="stat-label">MATCHING</span>
            <strong class="stat-value">
                <%=matchingComparisons != null ? matchingComparisons : 0%>
            </strong>
            <span class="stat-note">Identical SHA-256 hashes</span>
        </article>

        <article class="stat-card">
            <span class="stat-label">MISMATCHING</span>
            <strong class="stat-value">
                <%=mismatchingComparisons != null ? mismatchingComparisons : 0%>
            </strong>
            <span class="stat-note">Different SHA-256 hashes</span>
        </article>

    </section>

    <!-- Single-file analysis history -->

    <section class="history-section">

        <div class="history-heading">

            <div>
                <h2>Single File Analysis History</h2>
                <p>
                    All files analyzed individually in TraceBack.
                </p>
            </div>

            <div class="history-count">
                Total Records:
                <strong><%=analyses.size()%></strong>
            </div>

        </div>

        <% if (analyses.isEmpty()) { %>

            <div class="empty-history">
                <h3>No analysis records found</h3>
                <p>
                    Analyze a file first. Its record will appear here.
                </p>

                <a href="<%=ctx%>/index.html" class="history-action">
                    Analyze New File
                </a>
            </div>

        <% } else { %>

            <input
                type="search"
                id="historySearch"
                class="history-search"
                placeholder="Search by filename, ID or SHA-256 hash..."
                aria-label="Search analysis history">

            <div class="history-table-wrap">

                <table class="history-table" id="historyTable">

                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>File Name</th>
                            <th>SHA-256 Hash</th>
                            <th>File Size</th>
                            <th>Action</th>
                        </tr>
                    </thead>

                    <tbody>

                        <% for (FileAnalysis file : analyses) { %>

                            <tr class="history-row">

                                <td>
                                    #<%=file.getId()%>
                                </td>

                                <td>
                                    <%=esc(file.getOriginalFileName())%>
                                </td>

                                <td>
                                    <span class="file-hash"><%=esc(file.getHashSha256())%></span>
                                </td>

                                <td>
                                    <%=formatSize(file.getFileSizeBytes())%>
                                </td>

                                <td class="action-cell">

                                    <a
                                        class="history-action"
                                        href="<%=ctx%>/file-details?id=<%=file.getId()%>">
                                        View Details
                                    </a>

                                    <a
                                        class="history-action"
                                        href="<%=ctx%>/download-analysis-report?id=<%=file.getId()%>">
                                        Download PDF
                                    </a>

                                </td>

                            </tr>

                        <% } %>

                    </tbody>

                </table>

                <div id="noResults" class="no-results">
                    No matching analysis records found.
                </div>

            </div>

        <% } %>

    </section>

</main>

<footer class="page-footer">
    <span>
        TraceBack — File Integrity &amp; Forensics Workbench
    </span>

    <span>Built for file integrity analysis</span>
</footer>

<script>
    const searchInput = document.getElementById("historySearch");
    const historyRows = document.querySelectorAll(".history-row");
    const noResults = document.getElementById("noResults");

    if (searchInput) {
        searchInput.addEventListener("input", function () {
            const query = this.value.toLowerCase().trim();
            let visibleRows = 0;

            historyRows.forEach(function (row) {
                const matches = row.textContent
                    .toLowerCase()
                    .includes(query);

                row.style.display = matches ? "" : "none";

                if (matches) {
                    visibleRows++;
                }
            });

            noResults.style.display =
                visibleRows === 0 ? "block" : "none";
        });
    }
</script>

</body>
</html>
