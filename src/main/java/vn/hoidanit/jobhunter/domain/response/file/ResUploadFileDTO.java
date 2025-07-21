package vn.hoidanit.jobhunter.domain.response.file;

import java.time.Instant;

public class ResUploadFileDTO {
    private String fileName;
    private Instant uploadedAt;

    public ResUploadFileDTO() {
    }

    public ResUploadFileDTO(String fileName, Instant uploadedAt) {
        this.fileName = fileName;
        this.uploadedAt = uploadedAt;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

}
