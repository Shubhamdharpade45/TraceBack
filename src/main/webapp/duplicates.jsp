
<%@ page import="java.util.List"%>
<%@ page import="com.traceback.model.FileAnalysis"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.LinkedHashMap"%>
<%@ page import="java.text.DecimalFormat"%>

<%!public String escapeHtml(String value) {
		if (value == null) {
			return "";
		}

		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
				.replace("'", "&#39;");
	}%>

<%
String ctx = request.getContextPath();

List<FileAnalysis> duplicateFiles = (List<FileAnalysis>) request.getAttribute("duplicateFiles");

if (duplicateFiles == null) {
	duplicateFiles = new java.util.ArrayList<FileAnalysis>();
}

Map<String, List<FileAnalysis>> duplicateGroups = new LinkedHashMap<String, List<FileAnalysis>>();

for (FileAnalysis file : duplicateFiles) {
	String hash = file.getHashSha256();

	if (hash == null || hash.trim().isEmpty()) {
		continue;
	}

	if (!duplicateGroups.containsKey(hash)) {
		duplicateGroups.put(hash, new java.util.ArrayList<FileAnalysis>());
	}

	duplicateGroups.get(hash).add(file);
}

long duplicateExtraEntries = 0;

for (List<FileAnalysis> group : duplicateGroups.values()) {
	duplicateExtraEntries += group.size() - 1;
}

DecimalFormat df = new DecimalFormat("#,##0.##");
%>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<title>Duplicate Files | TraceBack</title>

<link rel="stylesheet" href="<%=ctx%>/style.css?v=7">
<link rel="icon" type="image/svg+xml" href="favicon.svg">

<style>
.duplicates-page {
	width: min(1400px, 94%);
	margin: 38px auto;
}

.duplicates-heading {
	display: flex;
	align-items: center;
	justify-content: space-between;
	flex-wrap: wrap;
	gap: 18px;
	margin-bottom: 30px;
}

.eyebrow {
	color: #b99aff;
	font-size: 12px;
	font-weight: 700;
	letter-spacing: 2px;
	text-transform: uppercase;
	margin-bottom: 12px;
}

.duplicates-heading h1 {
	font-size: clamp(28px, 4vw, 43px);
	margin: 0 0 10px;
}

.duplicates-heading p {
	margin: 0;
	opacity: .72;
	line-height: 1.7;
}

.back-button {
	display: inline-block;
	padding: 12px 17px;
	color: #fff;
	background: #7657f6;
	border-radius: 10px;
	text-decoration: none;
	font-weight: 600;
	white-space: nowrap;
}

.duplicate-summary {
	display: grid;
	grid-template-columns: repeat(3, minmax(0, 1fr));
	gap: 18px;
	margin-bottom: 30px;
}

.summary-card {
	padding: 24px;
	border-radius: 17px;
	background: rgba(255, 255, 255, .035);
	border: 1px solid rgba(150, 130, 255, .2);
}

.summary-label {
	font-size: 12px;
	opacity: .75;
	margin-bottom: 14px;
	text-transform: uppercase;
	letter-spacing: .6px;
}

.summary-value {
	font-size: 32px;
	font-weight: 750;
	margin-bottom: 8px;
}

.summary-note {
	font-size: 12px;
	opacity: .65;
}

.duplicate-toolbar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 15px;
	flex-wrap: wrap;
	margin-bottom: 22px;
}

.duplicate-toolbar h2 {
	margin: 0 0 7px;
	font-size: 22px;
}

.duplicate-toolbar p {
	margin: 0;
	opacity: .7;
	font-size: 13px;
}

.duplicate-search {
	width: min(100%, 360px);
	box-sizing: border-box;
	padding: 12px 14px;
	color: inherit;
	background: rgba(255, 255, 255, .04);
	border: 1px solid rgba(150, 130, 255, .3);
	border-radius: 10px;
	outline: none;
}

.duplicate-search:focus {
	border-color: #a78bfa;
	box-shadow: 0 0 0 3px rgba(118, 87, 246, .13);
}

.duplicate-group {
	border: 1px solid rgba(150, 130, 255, .22);
	border-radius: 16px;
	overflow: hidden;
	margin-bottom: 22px;
	background: rgba(255, 255, 255, .025);
}

.group-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	gap: 15px;
	flex-wrap: wrap;
	padding: 20px;
	background: rgba(118, 87, 246, .09);
	border-bottom: 1px solid rgba(150, 130, 255, .17);
}

.group-header h3 {
	margin: 0 0 9px;
	font-size: 15px;
}

.group-hash {
	font-family: monospace;
	font-size: 11px;
	opacity: .75;
	overflow-wrap: anywhere;
}

.group-badge {
	padding: 8px 11px;
	border-radius: 8px;
	font-size: 11px;
	font-weight: 700;
	background: rgba(245, 158, 11, .12);
	color: #fbbf24;
	border: 1px solid rgba(245, 158, 11, .25);
	white-space: nowrap;
}

.duplicate-table-wrapper {
	overflow-x: auto;
}

.duplicate-table {
	width: 100%;
	border-collapse: collapse;
	text-align: left;
}

.duplicate-table th {
	padding: 14px 18px;
	font-size: 11px;
	text-transform: uppercase;
	letter-spacing: .5px;
	opacity: .7;
	white-space: nowrap;
}

.duplicate-table td {
	padding: 15px 18px;
	font-size: 13px;
	border-top: 1px solid rgba(150, 130, 255, .12);
	vertical-align: middle;
}

.duplicate-table tbody tr:hover {
	background: rgba(118, 87, 246, .05);
}

.duplicate-file-name {
	font-weight: 600;
	overflow-wrap: anywhere;
	min-width: 150px;
}

.duplicate-id {
	font-size: 11px;
	opacity: .6;
	margin-top: 5px;
}

.details-link {
	display: inline-block;
	padding: 8px 11px;
	border-radius: 8px;
	text-decoration: none;
	color: inherit;
	font-size: 11px;
	border: 1px solid rgba(150, 130, 255, .3);
	background: rgba(118, 87, 246, .13);
	white-space: nowrap;
}

.empty-state {
	padding: 55px 20px;
	text-align: center;
	border-radius: 17px;
	border: 1px dashed rgba(150, 130, 255, .3);
	background: rgba(255, 255, 255, .02);
}

.empty-state .empty-icon {
	font-size: 38px;
	margin-bottom: 15px;
}

.empty-state h2 {
	margin: 0 0 10px;
}

.empty-state p {
	opacity: .7;
	line-height: 1.7;
}

.no-results {
	display: none;
	padding: 30px 15px;
	text-align: center;
	opacity: .7;
}

@media ( max-width : 800px) {
	.duplicate-summary {
		grid-template-columns: 1fr;
	}
}

@media ( max-width : 560px) {
	.duplicates-page {
		width: 92%;
		margin: 24px auto;
	}
	.duplicate-toolbar {
		align-items: stretch;
	}
	.duplicate-search {
		width: 100%;
	}
	.group-header {
		padding: 15px;
	}

	/* Navigation spacing */
	.topbar {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 24px;
		padding: 18px 5%;
		box-sizing: border-box;
	}
	.main-nav {
		display: flex;
		align-items: center;
		justify-content: flex-end;
		flex-wrap: wrap;
		gap: 10px;
	}
	.main-nav a {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		padding: 10px 15px;
		border-radius: 9px;
		text-decoration: none;
		white-space: nowrap;
		line-height: 1.4;
		box-sizing: border-box;
	}
	.main-nav a:hover, .main-nav a.active {
		background: rgba(118, 87, 246, 0.16);
	}

	/* Page buttons */
	.back-button {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		padding: 13px 22px;
		min-height: 46px;
		border-radius: 11px;
		box-sizing: border-box;
		line-height: 1.4;
	}

	/* View Details buttons */
	.details-link {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		padding: 10px 15px;
		min-height: 38px;
		border-radius: 9px;
		line-height: 1.3;
		box-sizing: border-box;
	}

	/* Small-screen layout */
	@media ( max-width : 760px) {
		.topbar {
			align-items: flex-start;
			flex-direction: column;
			gap: 14px;
			padding: 16px 4%;
		}
		.main-nav {
			justify-content: flex-start;
			gap: 7px;
		}
		.main-nav a {
			padding: 9px 11px;
			font-size: 13px;
		}
	}
}

/* Navigation buttons spacing */
.topbar nav {
	display: flex;
	align-items: center;
	justify-content: flex-end;
	flex-wrap: wrap;
	gap: 12px;
}

.topbar nav a {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	padding: 12px 18px;
	margin: 0;
	border-radius: 9px;
	line-height: 1.5;
	box-sizing: border-box;
	text-decoration: none;
	white-space: nowrap;
}

/* Analysis History button */
.back-button {
	padding: 14px 24px;
	min-height: 48px;
	border-radius: 11px;
}

/* View Details buttons */
.details-link {
	padding: 11px 17px;
	min-height: 40px;
	border-radius: 9px;
	line-height: 1.4;
}

/* Mobile spacing */
@media ( max-width : 760px) {
	.topbar nav {
		justify-content: flex-start;
		gap: 8px;
	}
	.topbar nav a {
		padding: 10px 13px;
		font-size: 13px;
	}
}


.topbar .logo .logo-mark {
    width: 46px;
    height: 46px;
    border-radius: 14px;
    font-size: 23px;
}

.topbar .logo .logo-link {
    gap: 10px;
}

.topbar .logo .logo-name {
    font-size: 22px;
}

.topbar .logo .logo-tagline {
    font-size: 9px;
    letter-spacing: 1.5px;
}

/* TraceBack logo fix for Duplicates page */
.topbar .logo {
    display: flex;
    align-items: center;
}

.topbar .logo .logo-link {
    display: inline-flex;
    align-items: center;
    gap: 14px;
    text-decoration: none;
    color: inherit;
}

.topbar .logo .logo-mark {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 44px;
    height: 44px;
    flex-shrink: 0;
    border-radius: 13px;
    background: linear-gradient(135deg, #c084fc, #7c3aed);
    color: #fff;
    font-size: 20px;
    font-weight: 800;
}

.topbar .logo .logo-text {
    display: flex;
    flex-direction: column;
    gap: 5px;
}

.topbar .logo .logo-name {
    font-size: 28px;
    font-weight: 600;
    color: #f5f3ff;
}

.topbar .logo .logo-tagline {
    font-size: 12px;
    font-weight: 500;
    letter-spacing: 2px;
    color: #a99bbf;
}

</style>
</head>

<body>

	<header class="topbar">
		
<div class="logo">
    <a href="<%=ctx%>/index.html" class="logo-link">
        <span class="logo-mark">T</span>
        <span class="logo-text">
            <span class="logo-name">TraceBack</span>
       
        </span>
    </a>
</div>



		<nav class="main-nav">
			<a href="<%=ctx%>/index.html">Home</a> <a href="<%=ctx%>/history">Analysis
				History</a> <a href="<%=ctx%>/comparison-history">Comparison History</a>
			<a href="<%=ctx%>/duplicates" class="active">Duplicates</a> <a
				href="<%=request.getContextPath()%>/verify-file"> Verify File </a> <a
				href="<%=request.getContextPath()%>/verification-history">
				Verification History </a>
		</nav>

	</header>

	<main class="duplicates-page">

		<section class="duplicates-heading">
			<div>
				<div class="eyebrow">Forensics Workspace / Integrity Scan</div>
				<h1>
					Duplicate <span style="color: #b99aff;">Files</span>
				</h1>
				<p>Identify files sharing the same SHA-256 fingerprint. Inspect
					matching records and their analysis details.</p>
			</div>

			<a class="back-button" href="<%=ctx%>/history"> Analysis History
			</a>
		</section>

		<section class="duplicate-summary">

			<article class="summary-card">
				<div class="summary-label">Duplicate Hash Groups</div>
				<div class="summary-value"><%=duplicateGroups.size()%></div>
				<div class="summary-note">Hashes appearing in multiple records
				</div>
			</article>

			<article class="summary-card">
				<div class="summary-label">Matching File Records</div>
				<div class="summary-value"><%=duplicateFiles.size()%></div>
				<div class="summary-note">All records belonging to duplicate
					groups</div>
			</article>

			<article class="summary-card">
				<div class="summary-label">Extra Repeated Entries</div>
				<div class="summary-value"><%=duplicateExtraEntries%></div>
				<div class="summary-note">Records beyond the first entry per
					hash</div>
			</article>

		</section>

		<section>
			<div class="duplicate-toolbar">
				<div>
					<h2>Detected Duplicate Groups</h2>
					<p>Files are grouped by their exact SHA-256 hash.</p>
				</div>

				<input type="search" id="duplicateSearch" class="duplicate-search"
					placeholder="Search filename, ID or hash..." autocomplete="off">




			</div>

			<%
			if (duplicateGroups.isEmpty()) {
			%>

			<div class="empty-state">
				<div class="empty-icon">🔐</div>
				<h2>No Duplicate Files Found</h2>
				<p>No repeated SHA-256 fingerprints were found among the
					recorded file analyses.</p>
				<a class="back-button" href="<%=ctx%>/index.html"> Analyze a
					File </a>
			</div>

			<%
			} else {
			%>

			<div id="duplicateGroups">

				<%
				for (Map.Entry<String, List<FileAnalysis>> entry : duplicateGroups.entrySet()) {

					String hash = entry.getKey();
					List<FileAnalysis> group = entry.getValue();
				%>

				<article class="duplicate-group">

					<div class="group-header">
						<div>
							<h3>SHA-256 Fingerprint</h3>
							<div class="group-hash">
								<%=hash%>
							</div>
						</div>

						<span class="group-badge"> <%=group.size()%> matching
							records
						</span>
					</div>

					<div class="duplicate-table-wrapper">
						<table class="duplicate-table">
							<thead>
								<tr>
									<th>Record ID</th>
									<th>File Name</th>
									<th>File Size</th>
									<th>MIME Type</th>
									<th>Analysis Date</th>
									<th>Action</th>
								</tr>
							</thead>

							<tbody>
								<%
								for (FileAnalysis file : group) {

									long bytes = file.getFileSizeBytes();

									String size;
									if (bytes >= 1024L * 1024L) {
										size = df.format(bytes / (1024.0 * 1024.0)) + " MB";
									} else if (bytes >= 1024L) {
										size = df.format(bytes / 1024.0) + " KB";
									} else {
										size = bytes + " B";
									}

									String name = file.getOriginalFileName() == null ? "Unknown file" : file.getOriginalFileName();

									String mime = file.getMimeType() == null ? "Unknown" : file.getMimeType();
								%>

								<tr class="duplicate-row">
									<td>#<%=file.getId()%></td>

									<td>
										<div class="duplicate-file-name">
											<%=escapeHtml(name)%>
										</div>
										<div class="duplicate-id">Same fingerprint detected</div>
									</td>

									<td><%=size%></td>

									<td><%=escapeHtml(mime)%></td>

									<td><%=file.getCreatedAt() == null ? "N/A" : file.getCreatedAt()%>
									</td>

									<td><a class="details-link"
										href="<%=ctx%>/file-details?id=<%=file.getId()%>"> View
											Details </a></td>
								</tr>

								<%
								}
								%>
							</tbody>
						</table>
					</div>

				</article>

				<%
				}
				%>
			</div>

			<div class="no-results" id="noDuplicateResults">No duplicate
				records match your search.</div>

			<%
			}
			%>
		</section>

	</main>

	<script>
		(function() {
			const search = document.getElementById("duplicateSearch");
			const groupsContainer = document.getElementById("duplicateGroups");
			const noResults = document.getElementById("noDuplicateResults");

			if (!search || !groupsContainer) {
				return;
			}

			const groups = Array.from(groupsContainer
					.querySelectorAll(".duplicate-group"));

			search.addEventListener("input", function() {
				const query = search.value.trim().toLowerCase();
				let visibleGroups = 0;

				groups.forEach(function(group) {
					const matches = group.textContent.toLowerCase().includes(
							query);
					group.style.display = matches ? "" : "none";

					if (matches) {
						visibleGroups++;
					}
				});

				if (noResults) {
					noResults.style.display = visibleGroups === 0 ? "block"
							: "none";
				}
			});
		})();
	</script>

</body>
</html>
