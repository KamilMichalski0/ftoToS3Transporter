package com.example.ftoToS3Transporter.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class S3FileWriter implements ItemWriter<FTPFile> {

    private final AmazonS3 amazonS3;
    private final String s3BucketName;

    public S3FileWriter(AmazonS3 amazonS3, String s3BucketName) {
        this.amazonS3 = amazonS3;
        this.s3BucketName = s3BucketName;
    }

    @Override
    public void write(Chunk<? extends FTPFile> chunk) throws Exception {
        FTPFile file = chunk.getItems().get(0);
        String tempDir = System.getProperty("java.io.tmpdir");
        File localFile = new File(tempDir, file.getName());

        if (!localFile.exists()) {
            throw new IOException("File not found in temp directory: " + localFile.getPath());
        }

        try (FileInputStream fileInputStream = new FileInputStream(localFile)) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(s3BucketName, file.getName(), fileInputStream, null);
            amazonS3.putObject(putObjectRequest);
            System.out.println("Uploaded file to S3: " + file.getName());
            
            if (!localFile.delete()) {
                System.err.println("Warning: Could not delete temporary file: " + localFile.getPath());
            }
        } catch (IOException e) {
            System.err.println("Failed to upload file to S3: " + e.getMessage());
            throw new IOException("Failed to upload file to S3", e);
        }
    }
}
