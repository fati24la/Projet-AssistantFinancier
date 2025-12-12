package com.assistantfinancer.util;

import java.io.File;
import java.io.IOException;

public class AudioConverter {

    /**
     * Convertit un fichier AAC en MP3 en utilisant FFmpeg.
     * @param aacFile le fichier source AAC
     * @return un fichier MP3 temporaire
     */
    public static File convertAacToMp3(File aacFile) throws IOException, InterruptedException {
        File mp3File = File.createTempFile("audio", ".mp3");

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y", // écrase le fichier de sortie si existe
                "-i", aacFile.getAbsolutePath(),
                "-acodec", "libmp3lame",
                mp3File.getAbsolutePath()
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IOException("Erreur lors de la conversion AAC -> MP3");
        }

        return mp3File;
    }
}
