package com.example.ftoToS3Transporter.service;

import com.example.ftoToS3Transporter.config.FtpConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.file.remote.RemoteFileTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Arrays;

public class FtpFileReader implements ItemReader<FTPFile> {

    @Autowired
    private RemoteFileTemplate<FTPFile> ftpRemoteFileTemplate;

    private int currentServerIndex = 0;
    private int currentFileIndex = 0;
    private List<FtpConfig.FtpServer> servers;

    public FtpFileReader(List<FtpConfig.FtpServer> servers, RemoteFileTemplate<FTPFile> ftpRemoteFileTemplate) {
        this.servers = servers;
        this.ftpRemoteFileTemplate = ftpRemoteFileTemplate;
    }

    @Override
    public FTPFile read() throws Exception {
        if (currentServerIndex >= servers.size()) {
            return null;
        }

        FtpConfig.FtpServer currentServer = servers.get(currentServerIndex);
        String remoteDirectory = currentServer.getRemoteDirectory();
        List<FTPFile> files = Arrays.asList(ftpRemoteFileTemplate.list(remoteDirectory));

        while (currentFileIndex < files.size()) {
            FTPFile file = files.get(currentFileIndex);
            currentFileIndex++;

            // Sprawdź czy plik mieści się w interwale czasowym
            long currentTime = System.currentTimeMillis();
            long fileTime = file.getTimestamp().getTimeInMillis();
            long intervalMillis = currentServer.getDownloadIntervalSeconds() * 1000;

            if (currentTime - fileTime <= intervalMillis) {
                // Pobierz plik do katalogu tymczasowego
                String tempDir = System.getProperty("java.io.tmpdir");
                File localFile = new File(tempDir, file.getName());
                ftpRemoteFileTemplate.get(remoteDirectory + "/" + file.getName(), 
                    inputStream -> {
                        try (FileOutputStream fos = new FileOutputStream(localFile)) {
                            org.springframework.util.FileCopyUtils.copy(inputStream, fos);
                        }
                    });

                // Przenieś plik do folderu processed na tym samym serwerze FTP
                moveFileToProcessed(file, currentServer);

                return file;
            }
        }

        // Jeśli nie znaleziono plików w interwale, przejdź do następnego serwera
        currentServerIndex++;
        currentFileIndex = 0;
        return read();
    }

    private void moveFileToProcessed(FTPFile file, FtpConfig.FtpServer server) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String newFileName = file.getName().replaceAll("(\\.[^.]+)$", "_" + timestamp + "$1");
        
        ftpRemoteFileTemplate.rename(
            server.getRemoteDirectory() + "/" + file.getName(),
            server.getProcessedDirectory() + "/" + newFileName
        );
    }
}
