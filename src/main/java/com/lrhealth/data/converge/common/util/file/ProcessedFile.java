package com.lrhealth.data.converge.common.util.file;

import java.io.InputStream;

public class ProcessedFile {
    private InputStream inputStream;

    private String fileName;

    private String fileType;

    public InputStream getInputStream() {
        return this.inputStream;
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getFileType() {
        return this.fileType;
    }

    public ProcessedFile(InputStream inputStream, String fileName, String fileType) {
        this.inputStream = inputStream;
        this.fileName = fileName;
        this.fileType = fileType;
    }

    @Override
    public String toString() {
        return this.fileName+" will be processed.";
    }
}
