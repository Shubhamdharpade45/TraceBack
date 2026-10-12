
package com.traceback.model;

import java.sql.Timestamp;

public class VerificationHistory {

    private long id;
    private long originalAnalysisId;
    private String uploadedFileName;
    private String originalSha256;
    private String uploadedSha256;
    private boolean verificationResult;
    private Timestamp verifiedAt;

    public VerificationHistory() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getOriginalAnalysisId() {
        return originalAnalysisId;
    }

    public void setOriginalAnalysisId(long originalAnalysisId) {
        this.originalAnalysisId = originalAnalysisId;
    }

    public String getUploadedFileName() {
        return uploadedFileName;
    }

    public void setUploadedFileName(String uploadedFileName) {
        this.uploadedFileName = uploadedFileName;
    }

    public String getOriginalSha256() {
        return originalSha256;
    }

    public void setOriginalSha256(String originalSha256) {
        this.originalSha256 = originalSha256;
    }

    public String getUploadedSha256() {
        return uploadedSha256;
    }

    public void setUploadedSha256(String uploadedSha256) {
        this.uploadedSha256 = uploadedSha256;
    }

    public boolean isVerificationResult() {
        return verificationResult;
    }

    public void setVerificationResult(boolean verificationResult) {
        this.verificationResult = verificationResult;
    }

    public Timestamp getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Timestamp verifiedAt) {
        this.verifiedAt = verifiedAt;
    }
}
