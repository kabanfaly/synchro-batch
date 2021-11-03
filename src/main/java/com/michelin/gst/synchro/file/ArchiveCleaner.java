package com.michelin.gst.synchro.file;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArchiveCleaner {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveCleaner.class);

    private final int maxDays;
    private final String archiverDirectory;

    public ArchiveCleaner(int maxDays, String archiverDirectory) {
        this.maxDays = maxDays;
        this.archiverDirectory = archiverDirectory;
    }

    public void cleanArchives() {
        LOGGER.info("Archives cleaning started");
        var archivesDirFile = new File(archiverDirectory);

        // Date limite autorisee
        LocalDateTime limitDateTime = LocalDateTime.now().minusDays(maxDays);

        if (archivesDirFile.isDirectory()) {
            
            // Supprimer tous les fichiers json dont la date de creaction est inferieurs a la data courante - maxDays
            Arrays.stream(archivesDirFile.listFiles())
                .filter(file -> file.getName().contains(".json") && isOldEnough(file, limitDateTime))
                .forEach(file -> {
                    if(file.delete()){
                        LOGGER.info("File {} --> deleted", file.getName());
                    } else {
                        LOGGER.warn("File {} --> not deleted", file.getName());
                    }
                });
        }
        LOGGER.info("Archives cleaning ended");
    }

    private LocalDateTime tsToLocalDateTime(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp),
            TimeZone.getDefault().toZoneId());
    }

    private boolean isOldEnough(File file, LocalDateTime limitDateTime) {
        LocalDateTime fileLastModifDateTime = tsToLocalDateTime(file.lastModified());
        return fileLastModifDateTime.isBefore(limitDateTime);
    }

}
