
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" import="java.util.List"%>
<%@ page
	import="com.traceback.dao.ComparisonHistoryDAO.ComparisonRecord"%>
<%@ page import="java.time.format.DateTimeFormatter"%>

<%!public String escapeHtml(String value) {
		if (value == null)
			return "";

		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
				.replace("'", "&#39;");
	}%>

<%
List<ComparisonRecord> comparisons = (List<ComparisonRecord>) request.getAttribute("comparisons");

int total = comparisons == null ? 0 : comparisons.size();
int matches = 0;

if (comparisons != null) {
	for (ComparisonRecord record : comparisons) {
		if (record.isContentsMatch()) {
	matches++;
		}
	}
}

String context = request.getContextPath();
%>

<!DOCTYPE html>
<html lang="en" data-theme="dark">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Comparison History | TraceBack</title>

<link rel="stylesheet" href="<%=context%>/style.css?v=7">
<script src="<%=context%>/app.js" defer></script>
<link rel="icon" type="image/svg+xml" href="favicon.svg">
</head>

<body>
	<header class="topbar">
		<a class="brand" href="<%=context%>/index.html"> <span
			class="brand-icon">T</span> <span>TraceBack</span>
		</a>

		<nav class="nav-links">
			<a href="<%=context%>/index.html">Home</a> <a
				href="<%=context%>/history">Analysis History</a> <a
				href="<%=request.getContextPath()%>/comparison-history"
				class="active">Comparison History</a> <a href="duplicates">Duplicate
				Files</a> <a href="<%=request.getContextPath()%>/verify-file">
				Verify File </a> <a
				href="<%=request.getContextPath()%>/verification-history">
				Verification History </a>
		</nav>


	</header>

	<main class="dashboard-page">
		<section class="hero">
			<p class="eyebrow">FILE FORENSICS / HISTORY</p>

			<h1>
				Comparison <span class="gradient-text">History</span>
			</h1>

			<p class="result-description">Review previous file comparisons
				and their integrity results.</p>
		</section>

		<section class="stats-grid">
			<article class="stat-card">
				<p class="stat-label">TOTAL COMPARISONS</p>
				<h2 class="stat-value"><%=total%></h2>
				<p class="stat-note">Recorded comparison operations</p>
			</article>

			<article class="stat-card">
				<p class="stat-label">IDENTICAL FILE PAIRS</p>
				<h2 class="stat-value"><%=matches%></h2>
				<p class="stat-note">Matching SHA-256 hashes</p>
			</article>

			<article class="stat-card">
				<p class="stat-label">DIFFERENT FILE PAIRS</p>
				<h2 class="stat-value"><%=total - matches%></h2>
				<p class="stat-note">Non-matching SHA-256 hashes</p>
			</article>
		</section>

		<section class="history-panel">
			<div class="panel-header">
				<div>
					<p class="section-kicker">AUDIT LOG</p>
					<h2>Previous Comparisons</h2>
				</div>

				<a class="secondary-btn" href="<%=context%>/index.html"> Compare
					Files </a>
			</div>

			<div class="search-box">
				<input type="search" id="searchInput"
					placeholder="Search file names or result..."
					aria-label="Search comparison history">
			</div>

			<div class="table-wrap">
				<table class="history-table" id="historyTable">
					<thead>
						<tr>
							<th>ID</th>
							<th>FILE 1</th>
							<th>FILE 2</th>
							<th>RESULT</th>
							<th>COMPARED AT</th>
							<th>ACTIONS</th>
						</tr>
					</thead>

					<tbody>
						<%
						if (comparisons != null && !comparisons.isEmpty()) {
						%>

						<%
						for (ComparisonRecord record : comparisons) {
							String file1 = record.getFile1Name() == null ? "Unknown file" : record.getFile1Name();

							String file2 = record.getFile2Name() == null ? "Unknown file" : record.getFile2Name();

							String result = record.isContentsMatch() ? "IDENTICAL" : "DIFFERENT";

							String comparedAt = record.getComparedAt() == null ? "N/A" : record.getComparedAt().toString();
						%>

						<tr>
							<td>#<%=record.getId()%></td>

							<td><span class="file-name"> <%=escapeHtml(file1)%>
							</span></td>

							<td><span class="file-name"> <%=escapeHtml(file2)%>
							</span></td>

							<td><span class="status-badge"
								style="<%="IDENTICAL".equalsIgnoreCase(result.trim())
		? "color:#34d399 !important; background-color:rgba(52,211,153,0.12) !important; border:1px solid #34d399;"
		: "color:#ff4d4f !important; background-color:rgba(255,77,79,0.12) !important; border:1px solid #ff4d4f;"%>">
									<%=result%>
							</span></td>

							<td><%=escapeHtml(comparedAt)%></td>

							<td><a class="secondary-btn"
								href="<%=context%>/download-report?comparisonId=<%=record.getId()%>">
									Download PDF </a></td>

						</tr>

						<%
						}
						%>

						<%
						} else {
						%>

						<tr>
							<td colspan="6">
								<div class="empty-state">
									<h3>No comparisons found</h3>
									<p>Compare two files to create your first comparison
										history record.</p>
									<a class="primary-btn" href="<%=context%>/index.html">
										Start Comparing </a>
								</div>
							</td>
						</tr>

						<%
						}
						%>
					</tbody>
				</table>

				<p id="noResults" class="no-results" style="display: none;">No
					matching comparisons found.</p>
			</div>
		</section>
	</main>

	<footer class="page-footer">
		<p>TraceBack — File Integrity &amp; Forensics Workbench</p>
	</footer>

	<script>
		document.addEventListener("DOMContentLoaded", function() {
			const searchInput = document.getElementById("searchInput");
			const table = document.getElementById("historyTable");
			const noResults = document.getElementById("noResults");

			if (!searchInput || !table || !noResults)
				return;

			searchInput.addEventListener("input", function() {
				const query = this.value.toLowerCase().trim();
				const rows = table.querySelectorAll("tbody tr");
				let visibleCount = 0;

				rows
						.forEach(function(row) {
							const text = row.textContent.toLowerCase();
							const isEmptyMessage = row
									.querySelector(".empty-state") !== null;

							if (isEmptyMessage) {
								row.style.display = query ? "none" : "";
								return;
							}

							const show = text.includes(query);
							row.style.display = show ? "" : "none";

							if (show)
								visibleCount++;
						});

				noResults.style.display = query && visibleCount === 0 ? "block"
						: "none";
			});
		});
	</script>
	
</body>
</html>
