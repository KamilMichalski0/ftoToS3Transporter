package com.example.ftoToS3Transporter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "ftp")
public class FtpConfig {
    private List<FtpServer> servers;

    public List<FtpServer> getServers() {
        return servers;
    }

    public void setServers(List<FtpServer> servers) {
        this.servers = servers;
    }

    public static class FtpServer {
        private String host;
        private int port;
        private String username;
        private String password;
        private String remoteDirectory;
        private int downloadIntervalSeconds;
        private String processedDirectory;  // Nowe pole dla folderu processed

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getRemoteDirectory() {
            return remoteDirectory;
        }

        public void setRemoteDirectory(String remoteDirectory) {
            this.remoteDirectory = remoteDirectory;
        }

        public int getDownloadIntervalSeconds() {
            return downloadIntervalSeconds;
        }

        public void setDownloadIntervalSeconds(int downloadIntervalSeconds) {
            this.downloadIntervalSeconds = downloadIntervalSeconds;
        }

        public String getProcessedDirectory() {
            return processedDirectory;
        }

        public void setProcessedDirectory(String processedDirectory) {
            this.processedDirectory = processedDirectory;
        }
    }
}
