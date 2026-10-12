
<%@ page import="com.traceback.model.FileAnalysis"%>

<%!private String escapeHtml(String value) {
		if (value == null)
			return "";

		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
				.replace("'", "&#39;");
	}%>

<%
String ctx = request.getContextPath();

String errorMessage = (String) request.getAttribute("errorMessage");

FileAnalysis original = (FileAnalysis) request.getAttribute("originalRecord");

String uploadedFileName = (String) request.getAttribute("uploadedFileName");

String uploadedHash = (String) request.getAttribute("uploadedHash");

Boolean hashMatches = (Boolean) request.getAttribute("hashMatches");

boolean completed = Boolean.TRUE.equals(request.getAttribute("verificationCompleted"));

String originalHash = original == null ? "" : original.getHashSha256();

String resultClass = "";
String resultTitle = "";
String resultDescription = "";

if (completed && Boolean.TRUE.equals(hashMatches)) {
	resultClass = "success";
	resultTitle = "Integrity Check Passed";
	resultDescription = "Both SHA-256 hashes match. The uploaded file has the same content as the saved reference.";
} else if (completed) {
	resultClass = "danger";
	resultTitle = "Hash Mismatch Detected";
	resultDescription = "The SHA-256 hashes differ. The uploaded file's content is not identical to the saved reference.";
}
%>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<title>File Integrity Verification | TraceBack</title>

<link rel="stylesheet" href="<%=ctx%>/style.css?v=8">
<link rel="icon" type="image/svg+xml" href="favicon.svg">

<style>
.verify-page {
	width: min(1000px, 92%);
	margin: 38px auto 60px;
}

.verify-hero {
	margin-bottom: 28px;
}

.verify-eyebrow {
	color: #b99aff;
	font-size: 12px;
	font-weight: 700;
	letter-spacing: 1.8px;
	text-transform: uppercase;
	margin-bottom: 12px;
}

.verify-hero h1 {
	font-size: clamp(28px, 4vw, 40px);
	margin: 0 0 12px;
}

.verify-hero p {
	opacity: .72;
	line-height: 1.7;
	max-width: 700px;
	margin: 0;
}

.verify-panel {
	padding: 28px;
	border: 1px solid rgba(150, 130, 255, .23);
	border-radius: 18px;
	background: rgba(255, 255, 255, .035);
	margin-bottom: 22px;
}

.verify-panel h2 {
	font-size: 20px;
	margin: 0 0 9px;
}

.panel-description {
	font-size: 13px;
	opacity: .7;
	line-height: 1.6;
	margin-bottom: 24px;
}

.verify-form {
	display: grid;
	gap: 20px;
}

.field-group {
	display: grid;
	gap: 9px;
}

.field-group label {
	font-size: 13px;
	font-weight: 650;
}

.field-help {
	font-size: 12px;
	opacity: .65;
	line-height: 1.5;
}

.verify-input {
	display: block;
	width: 100%;
	min-height: 46px;
	padding: 12px 14px;
	box-sizing: border-box;
	border-radius: 10px;
	color: inherit;
	background: rgba(255, 255, 255, .04);
	border: 1px solid rgba(150, 130, 255, .3);
	outline: none;
}

.verify-input:focus {
	border-color: #a78bfa;
	box-shadow: 0 0 0 3px rgba(118, 87, 246, .12);
}

input[type="file"].verify-input {
	padding: 8px;
}

input[type="file"]::file-selector-button {
	padding: 9px 13px;
	margin-right: 12px;
	border: 0;
	border-radius: 7px;
	background: rgba(118, 87, 246, .2);
	color: inherit;
	cursor: pointer;
}

.verify-submit {
	display: inline-flex;
	justify-content: center;
	align-items: center;
	gap: 9px;
	width: fit-content;
	min-height: 46px;
	padding: 12px 22px;
	border: 0;
	border-radius: 10px;
	color: #fff;
	background: #7657f6;
	font-weight: 700;
	font-size: 14px;
	cursor: pointer;
}

.verify-submit:hover {
	background: #6545e8;
}

.message {
	padding: 15px 17px;
	border-radius: 11px;
	margin-bottom: 22px;
	line-height: 1.6;
	font-size: 13px;
}

.error-message {
	background: rgba(239, 68, 68, .1);
	border: 1px solid rgba(239, 68, 68, .35);
	color: #fca5a5;
}

.result-card {
	padding: 24px;
	border-radius: 16px;
	margin-bottom: 22px;
	border: 1px solid;
}

.result-card.success {
	background: rgba(34, 197, 94, .07);
	border-color: rgba(34, 197, 94, .35);
}

.result-card.danger {
	background: rgba(239, 68, 68, .07);
	border-color: rgba(239, 68, 68, .35);
}

.result-title {
	font-size: 22px;
	font-weight: 750;
	margin: 0 0 10px;
}

.success .result-title {
	color: #4ade80;
}

.danger .result-title {
	color: #f87171;
}

.result-description {
	font-size: 13px;
	line-height: 1.7;
	opacity: .85;
	margin: 0;
}

.hash-box {
	padding: 17px;
	border-radius: 12px;
	background: rgba(255, 255, 255, .025);
	border: 1px solid rgba(150, 130, 255, .18);
	margin-top: 17px;
}

.hash-label {
	font-size: 11px;
	text-transform: uppercase;
	letter-spacing: .7px;
	opacity: .7;
	margin-bottom: 10px;
}

.hash-value {
	font-family: monospace;
	font-size: 12px;
	line-height: 1.8;
	overflow-wrap: anywhere;
	user-select: text;
}

.record-details {
	display: grid;
	grid-template-columns: repeat(2, minmax(0, 1fr));
	gap: 15px;
	margin-top: 18px;
}

.detail-item {
	padding: 15px;
	border-radius: 10px;
	background: rgba(255, 255, 255, .025);
	border: 1px solid rgba(150, 130, 255, .15);
	min-width: 0;
}

.detail-label {
	font-size: 11px;
	opacity: .65;
	margin-bottom: 8px;
}

.detail-value {
	font-size: 13px;
	font-weight: 650;
	overflow-wrap: anywhere;
}

.verify-navigation {
	display: flex;
	gap: 12px;
	flex-wrap: wrap;
	margin-top: 22px;
}

.verify-navigation a {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	padding: 11px 16px;
	min-height: 42px;
	box-sizing: border-box;
	border-radius: 9px;
	text-decoration: none;
	color: inherit;
	border: 1px solid rgba(150, 130, 255, .3);
	background: rgba(118, 87, 246, .1);
}

@media ( max-width : 600px) {
	.verify-page {
		margin-top: 25px;
	}
	.verify-panel {
		padding: 19px;
	}
	.record-details {
		grid-template-columns: 1fr;
	}
	.verify-submit {
		width: 100%;
	}
}

/* TraceBack logo - Verify File page */
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
	font-weight: 700;
}

.topbar .logo .logo-text {
	display: flex;
	flex-direction: column;
	gap: 5px;
}

.topbar .logo .logo-name {
	font-size: 20px;
	font-weight: 700;
	color: #f5f3ff;
}

.topbar .logo .logo-tagline {
	font-size: 12px;
	font-weight: 700;
	letter-spacing: 2px;
	color: #a99bbf;
}
</style>
</head>

<body>

	<header class="topbar">


		<div class="logo">
			<a href="<%=ctx%>/index.html" class="logo-link"> <span
				class="logo-mark">T</span> <span class="logo-text"> <span
					class="logo-name">TraceBack</span>
			</span>
			</a>
		</div>



		<nav>
			<a href="<%=ctx%>/index.html">Home</a> <a href="<%=ctx%>/history">Analysis
				History</a> <a href="<%=ctx%>/comparison-history">Comparison History</a>
			<a href="<%=ctx%>/duplicates">Duplicates</a> <a
				href="<%=ctx%>/verify-file" class="active">Verify File</a> <a
				href="<%=request.getContextPath()%>/verification-history">
				Verification History </a>
		</nav>
	</header>

	<main class="verify-page">

		<section class="verify-hero">
			<div class="verify-eyebrow">Digital Forensics / Integrity Tools</div>
			<h1>File Integrity Verification</h1>
			<p>Verify a file against a previously saved analysis record using
				its SHA-256 fingerprint.</p>
		</section>

		<%
		if (errorMessage != null) {
		%>
		<div class="message error-message">
			<strong>Verification could not be completed.</strong><br>
			<%=escapeHtml(errorMessage)%>
		</div>
		<%
		}
		%>

		<section class="verify-panel">
			<h2>Verify a File</h2>

			<p class="panel-description">Enter the ID of a saved analysis
				record and select the file you want to verify. Maximum upload size:
				10 MB.</p>

			<form class="verify-form" action="<%=ctx%>/verify-file" method="post"
				enctype="multipart/form-data">

				<div class="field-group">
					<label for="recordId">Original Analysis Record ID</label> <input
						class="verify-input" type="number" id="recordId" name="recordId"
						min="1" step="1" required placeholder="Example: 12"
						value="<%=request.getParameter("recordId") == null ? "" : escapeHtml(request.getParameter("recordId"))%>">

					<span class="field-help"> Find the record ID in Analysis
						History or File Details. </span>
				</div>

				<div class="field-group">
					<label for="verificationFile">File to Verify</label> <input
						class="verify-input" type="file" id="verificationFile"
						name="verificationFile" required> <span class="field-help">
						The file is read to calculate its hash; it is not saved as a new
						analysis record. </span>
				</div>

				<button class="verify-submit" type="submit">Verify File
					Integrity</button>
			</form>
		</section>

		<%
		if (completed && original != null) {
		%>

		<section class="result-card <%=resultClass%>">
			<h2 class="result-title">
				<%=resultTitle%>
			</h2>

			<p class="result-description">
				<%=resultDescription%>
			</p>

			<div class="record-details">
				<div class="detail-item">
					<div class="detail-label">Reference Record ID</div>
					<div class="detail-value">
						#<%=original.getId()%>
					</div>
				</div>

				<div class="detail-item">
					<div class="detail-label">Saved File Name</div>
					<div class="detail-value">
						<%=escapeHtml(original.getOriginalFileName())%>
					</div>
				</div>

				<div class="detail-item">
					<div class="detail-label">Uploaded File Name</div>
					<div class="detail-value">
						<%=escapeHtml(uploadedFileName)%>
					</div>
				</div>

				<div class="detail-item">
					<div class="detail-label">Result</div>
					<div class="detail-value">
						<%=Boolean.TRUE.equals(hashMatches) ? "SHA-256 MATCH" : "SHA-256 MISMATCH"%>
					</div>
				</div>
			</div>

			<div class="hash-box">
				<div class="hash-label">Saved SHA-256 Hash</div>
				<div class="hash-value">
					<%=escapeHtml(originalHash)%>
				</div>
			</div>

			<div class="hash-box">
				<div class="hash-label">Uploaded File SHA-256 Hash</div>
				<div class="hash-value">
					<%=escapeHtml(uploadedHash)%>
				</div>
			</div>
		</section>

		<%
		}
		%>

		<div class="verify-navigation">
			<a href="<%=ctx%>/history"> Analysis History</a> <a
				href="<%=ctx%>/index.html">Home</a>

		</div>

	</main>



</body>
</html>
