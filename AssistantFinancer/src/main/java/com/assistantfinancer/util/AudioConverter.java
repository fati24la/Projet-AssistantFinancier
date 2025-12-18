package com.assistantfinancer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;


public class AudioConverter {

    private static final Logger logger = LoggerFactory.getLogger(AudioConverter.class);

    /**
     * Convertit un fichier AAC en MP3 en utilisant FFmpeg.
     * Note: FFmpeg doit être installé et disponible dans le PATH système.
     * 
     * @param aacFile le fichier source AAC
     * @return un fichier MP3 temporaire
     * @throws IOException si FFmpeg n'est pas trouvé ou si la conversion échoue
     */
    public static File convertAacToMp3(File aacFile) throws IOException, InterruptedException {
        File mp3File = File.createTempFile("audio", ".mp3");

        // Vérifier si FFmpeg est disponible
        String ffmpegCommand = findFfmpeg();
        
        if (ffmpegCommand == null) {
            // FFmpeg n'est pas disponible, on doit lancer une exception car Whisper API ne supporte pas AAC
            String errorMessage = "FFmpeg n'est pas installé ou n'est pas dans le PATH système. " +
                    "FFmpeg est requis pour convertir les fichiers AAC en MP3. " +
                    "Veuillez installer FFmpeg et l'ajouter au PATH système.";
            logger.error(errorMessage);
            throw new IOException(errorMessage);
        }

        logger.info("Exécution de la commande FFmpeg: {} -y -i {} -acodec libmp3lame {}", 
                ffmpegCommand, aacFile.getAbsolutePath(), mp3File.getAbsolutePath());

        ProcessBuilder pb = new ProcessBuilder(
                ffmpegCommand,
                "-y", // écrase le fichier de sortie si existe
                "-i", aacFile.getAbsolutePath(),
                "-acodec", "libmp3lame",
                mp3File.getAbsolutePath()
        );

        pb.redirectErrorStream(true);
        
        Process process;
        try {
            process = pb.start();
        } catch (IOException e) {
            logger.error("Impossible de démarrer le processus FFmpeg: {}", e.getMessage(), e);
            throw new IOException("Impossible de démarrer FFmpeg. Assurez-vous qu'il est installé et dans le PATH. " + e.getMessage(), e);
        }

        // Lire la sortie du processus pour le débogage
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.debug("FFmpeg output: {}", line);
            }
        }

        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            logger.error("Le processus FFmpeg a été interrompu: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new InterruptedException("La conversion FFmpeg a été interrompue.");
        }

        logger.info("FFmpeg s'est terminé avec le code de sortie: {}", exitCode);

        if (exitCode != 0) {
            String errorMessage = "Erreur lors de la conversion AAC -> MP3. Code de sortie FFmpeg: " + exitCode;
            logger.error(errorMessage);
            throw new IOException(errorMessage);
        }

        if (!mp3File.exists() || mp3File.length() == 0) {
            String errorMessage = "Le fichier MP3 n'a pas été créé ou est vide après conversion.";
            logger.error(errorMessage);
            throw new IOException(errorMessage);
        }

        logger.info("Conversion réussie: {} -> {} ({} bytes)", 
                aacFile.getAbsolutePath(), mp3File.getAbsolutePath(), mp3File.length());
        
        return mp3File;
    }
    
    /**
     * Trouve le chemin vers FFmpeg dans le système
     * @return le nom de la commande FFmpeg ("ffmpeg" ou "ffmpeg.exe") ou null si non trouvé
     */
    private static String findFfmpeg() {
        String os = System.getProperty("os.name").toLowerCase();
        logger.debug("Recherche de FFmpeg sur le système: {}", os);
        
        if (os.contains("windows")) {
            try {
                // Essayer "ffmpeg" directement
                ProcessBuilder testPb = new ProcessBuilder("ffmpeg", "-version");
                Process testProcess = testPb.start();
                if (testProcess.waitFor() == 0) {
                    logger.info("FFmpeg trouvé: commande 'ffmpeg'");
                    return "ffmpeg";
                }
            } catch (Exception e) {
                logger.debug("Commande 'ffmpeg' non disponible: {}", e.getMessage());
            }
            
            // Essayer avec .exe
            try {
                ProcessBuilder testPb = new ProcessBuilder("ffmpeg.exe", "-version");
                Process testProcess = testPb.start();
                if (testProcess.waitFor() == 0) {
                    logger.info("FFmpeg trouvé: commande 'ffmpeg.exe'");
                    return "ffmpeg.exe";
                }
            } catch (Exception e) {
                logger.debug("Commande 'ffmpeg.exe' non disponible: {}", e.getMessage());
            }
        } else {
            // Linux/Mac
            try {
                ProcessBuilder testPb = new ProcessBuilder("ffmpeg", "-version");
                Process testProcess = testPb.start();
                if (testProcess.waitFor() == 0) {
                    logger.info("FFmpeg trouvé: commande 'ffmpeg'");
                    return "ffmpeg";
                }
            } catch (Exception e) {
                logger.debug("Commande 'ffmpeg' non disponible: {}", e.getMessage());
            }
        }
        
        logger.warn("FFmpeg non trouvé dans le PATH système");
        return null; // FFmpeg non trouvé
    }
}
