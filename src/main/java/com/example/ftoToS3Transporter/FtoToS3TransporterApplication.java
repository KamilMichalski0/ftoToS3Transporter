package com.example.ftoToS3Transporter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;

@SpringBootApplication
@EnableScheduling
public class FtoToS3TransporterApplication {

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private Job uploadFilesToS3Job;

	public static void main(String[] args) {
		SpringApplication.run(FtoToS3TransporterApplication.class, args);
	}

	@Scheduled(cron = "${batch.job.cron}")
	public void runJob() throws Exception {
		JobParameters jobParameters = new JobParametersBuilder()
				.addLong("time", System.currentTimeMillis())
				.toJobParameters();
		
		jobLauncher.run(uploadFilesToS3Job, jobParameters);
	}

}
