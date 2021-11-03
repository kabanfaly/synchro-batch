package com.michelin.gst.synchro.cron;

import com.michelin.gst.synchro.file.ArchiveCleaner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ArchiverCleanerCron {

    private final ArchiveCleaner archiveCleaner;

    public ArchiverCleanerCron(ArchiveCleaner archiveCleaner) {
        this.archiveCleaner = archiveCleaner;
    }

    @Scheduled(cron = "${gst.clean-achives-cron}")
    public void cleanArchives() {
        archiveCleaner.cleanArchives();
    }

}
