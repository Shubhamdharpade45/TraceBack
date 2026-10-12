
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="com.traceback.model.VerificationHistory" %>

<%!
    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
%>

<%
    List<VerificationHistory> verifications =
        (List<VerificationHistory>) request.getAttribute("verifications");

    if (verifications == null) {
        verifications = java.util.Collections.emptyList();
    }

    SimpleDateFormat dateFormat =
        new SimpleDateFormat("dd MMM yyyy, hh:mm a");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Verification History | TraceBack</title>

    <link rel="stylesheet"
          href="<%= request.getContextPath() %>/style.css?v=5">
          <link rel="icon" type="image/svg+xml" href="favicon.svg">

    <style>
        .verification-page {
            max-width: 1450px;
            margin: 0 auto;
            padding: 35px 24px;
        }

        .verification-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 16px;
            margin-bottom: 28px;
        }

        .verification-header h1 {
            margin: 0 0 8px;
            font-size: 30px;
        }

        .verification-header p {
            margin: 0;
            opacity: 0.75;
        }

        .history-count {
            padding: 12px 18px;
            border: 1px solid rgba(255,255,255,0.12);
            border-radius: 12px;
            background: rgba(255,255,255,0.04);
        }

        .history-table-wrap {
            width: 100%;
            overflow-x: auto;
            border: 1px solid rgba(255,255,255,0.12);
            border-radius: 14px;
            background: rgba(255,255,255,0.025);
        }

        .history-table {
            width: 100%;
            min-width: 1150px;
            border-collapse: collapse;
        }

        .history-table th,
        .history-table td {
            padding: 16px;
            text-align: left;
            border-bottom: 1px solid rgba(255,255,255,0.09);
            vertical-align: top;
        }

        .history-table th {
            font-size: 12px;
            text-transform: uppercase;
            letter-spacing: 0.7px;
            opacity: 0.8;
        }

        .history-table td {
            font-size: 13px;
            overflow-wrap: anywhere;
        }

        .hash-value {
            display: inline-block;
            max-width: 220px;
            font-family: monospace;
            line-height: 1.7;
            overflow-wrap: anywhere;
        }

        .result-badge {
            display: inline-block;
            padding: 7px 11px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 700;
            white-space: nowrap;
        }

        .result-match {
            color: #86efac;
            background: rgba(34,197,94,0.14);
        }

        .result-mismatch {
            color: #fca5a5;
            background: rgba(239,68,68,0.14);
        }

        .empty-history {
            padding: 55px 20px;
            text-align: center;
        }

        .empty-history h2 {
            margin-bottom: 10px;
        }

        .empty-history p {
            opacity: 0.75;
            margin-bottom: 22px;
        }

        .history-button {
            display: inline-block;
            padding: 11px 17px;
            border-radius: 9px;
            text-decoration: none;
            background: #7c5cff;
            color: white;
            font-weight: 600;
        }

        @media (max-width: 700px) {
            .verification-page {
                padding: 24px 14px;
            }

            .verification-header h1 {
                font-size: 24px;
            }
        }
    </style>
</head>

<body>

    <div class="verification-page">

        <header class="verification-header">
            <div>
                <h1>Verification History</h1>
                <p>Review previous file integrity verification results.</p>
            </div>

            <div class="history-count">
                Total verifications:
                <strong><%= verifications.size() %></strong>
            </div>
        </header>

        <nav style="margin-bottom: 25px; display:flex; gap:12px; flex-wrap:wrap;">
            <a class="history-button"
               href="<%= request.getContextPath() %>/index.html">
                Home
            </a>

            <a class="history-button"
               href="<%= request.getContextPath() %>/history">
                Analysis History
            </a>

            <a class="history-button"
               href="<%= request.getContextPath() %>/verify-file">
                Verify File
            </a>
        </nav>

        <% if (verifications.isEmpty()) { %>

            <section class="empty-history">
                <h2>No verification records yet</h2>
                <p>Verify a file to see its result recorded here.</p>

                <a class="history-button"
                   href="<%= request.getContextPath() %>/verify-file">
                    Verify a File
                </a>
            </section>

        <% } else { %>

            <div class="history-table-wrap">
                <table class="history-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Original Record</th>
                            <th>Uploaded File</th>
                            <th>Original SHA-256</th>
                            <th>Uploaded SHA-256</th>
                            <th>Result</th>
                            <th>Verified At</th>
                        </tr>
                    </thead>

                    <tbody>
                        <% for (VerificationHistory item : verifications) { %>

                            <tr>
                                <td>
                                    #<%= item.getId() %>
                                </td>

                                <td>
                                    #<%= item.getOriginalAnalysisId() %>
                                </td>

                                <td>
                                    <%= escapeHtml(item.getUploadedFileName()) %>
                                </td>

                                <td>
                                    <span class="hash-value"><%= escapeHtml(item.getOriginalSha256()) %></span>
                                </td>

                                <td>
                                    <span class="hash-value"><%= escapeHtml(item.getUploadedSha256()) %></span>
                                </td>

                                <td>
                                    <% if (item.isVerificationResult()) { %>
                                        <span class="result-badge result-match">
                                            MATCH
                                        </span>
                                    <% } else { %>
                                        <span class="result-badge result-mismatch">
                                            MISMATCH
                                        </span>
                                    <% } %>
                                </td>

                                <td>
                                    <%
                                        if (item.getVerifiedAt() != null) {
                                            out.print(dateFormat.format(
                                                item.getVerifiedAt()
                                            ));
                                        } else {
                                            out.print("N/A");
                                        }
                                    %>
                                </td>
                            </tr>

                        <% } %>
                    </tbody>
                </table>
            </div>

        <% } %>

    </div>
    
    

</body>
</html>
