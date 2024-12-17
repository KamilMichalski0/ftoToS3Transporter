package com.example.ftoToS3Transporter.config;

import com.amazonaws.services.s3.AmazonS3;
import com.example.ftoToS3Transporter.service.FtpFileReader;
import com.example.ftoToS3Transporter.service.S3FileWriter;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.integration.file.remote.RemoteFileTemplate;


@Configuration
public class BatchConfig {

    @Autowired
    private RemoteFileTemplate<FTPFile> ftpRemoteFileTemplate;

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private S3Config s3Config;

    @Autowired
    private FtpConfig ftpConfig;

    @Bean
    public Job uploadFilesToS3Job(JobRepository jobRepository, Step uploadFilesToS3Step) {
        return new JobBuilder("uploadFilesToS3Job", jobRepository)
                .start(uploadFilesToS3Step)
                .build();
    }

    @Bean
    public Step uploadFilesToS3Step(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("uploadFilesToS3Step", jobRepository)
                .<FTPFile, FTPFile>chunk(1, transactionManager)
                .reader(new FtpFileReader(ftpConfig.getServers(), ftpRemoteFileTemplate))
                .writer(s3FileWriter(amazonS3))
                .build();
    }

    @Bean
    public S3FileWriter s3FileWriter(AmazonS3 amazonS3) {
        return new S3FileWriter(amazonS3, s3Config.getBucketName());
    }
}
