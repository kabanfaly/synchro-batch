package com.michelin.gst.synchro.file;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class FileArchiver implements Tasklet {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileArchiver.class);

    private final String fileToMove;
    private final String archiveDirectory;

    public FileArchiver(String fileToMove, String archivePath) {
        this.fileToMove = fileToMove;
        this.archiveDirectory = archivePath;
    }

    @Override
    public RepeatStatus execute(StepContribution arg0, ChunkContext arg1) throws Exception {
        File file = new File(fileToMove); // Le fichier a deplacer
        if (file.exists()) {
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
            String currentTimeStr = LocalDateTime.now().format(formatter);
            String fileNameWithoutExtension = file.getName().replace(".json", "");

            String archiveFileName = archiveDirectory + File.separator + fileNameWithoutExtension + "-" + currentTimeStr + ".json";

            File archiveFile = new File(archiveFileName); // Le fichier dans l'archive (le meme fichier)

            if (file.renameTo(archiveFile)) {
                // if file copied successfully then delete the original file
                file.delete();
                LOGGER.info("File '{}' is moved successfully move to '{}'", fileToMove, archiveFile.getCanonicalPath());
                return null;
            }
            throw new RuntimeException(String.format("File is failed to move file '%s' to archive '%s': ", fileToMove, archiveFile.getCanonicalPath()));
        } else {
            LOGGER.info("File '{}' is missing", fileToMove);
            return null;
        }
    }

}
