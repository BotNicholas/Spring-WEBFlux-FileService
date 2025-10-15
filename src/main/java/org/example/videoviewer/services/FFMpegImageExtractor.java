package org.example.videoviewer.services;

import org.imgscalr.Scalr;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.imageio.ImageIO;

@Service
public class FFMpegImageExtractor {
    public byte[] getFirstFrameFromVideo(String videoPath, String tmpImagePath) throws IOException, InterruptedException {
//        String frameFileName = videoPath.substring(videoPath.lastIndexOf("/") + 1, videoPath.lastIndexOf(".")) + ".png";
        String frameFileName = Paths.get(videoPath).getFileName().toString() + ".png";
        Path outputPath = Paths.get(tmpImagePath, frameFileName);

        String command = String.format("ffmpeg -y -i \"%s\" -frames:v 1 \"%s\"", videoPath, outputPath.toAbsolutePath());

        Runtime rt = Runtime.getRuntime();
        var pr = rt.exec(command);
        pr.waitFor();

        var imageBytes = Files.readAllBytes(outputPath);
        Files.delete(outputPath);
        return imageBytes;
    }


    public byte[] getScaledFirstFrameFromVideo(String videoPath, String tmpImagePath) throws IOException, InterruptedException {
//        String frameFileName = videoPath.substring(videoPath.lastIndexOf("/") + 1, videoPath.lastIndexOf(".")) + ".png";
        String frameFileName = Paths.get(videoPath).getFileName().toString() + ".png";
        Path outputPath = Paths.get(tmpImagePath, frameFileName);

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-y",
                "-i", videoPath,
                "-frames:v", "1",
                outputPath.toAbsolutePath().toString()
        );
        pb.redirectErrorStream(true);
        Process p = pb.start();
        String log = new String(p.getInputStream().readAllBytes());
        int exit = p.waitFor();
        if (exit != 0 || !Files.exists(outputPath)) {
            throw new IOException("ffmpeg failed (exit=" + exit + ")\n" + log);
        }

        try (var imageInputStream = Files.newInputStream(outputPath)) {
            var image = Scalr.resize(ImageIO.read(imageInputStream), Scalr.Method.SPEED, Scalr.Mode.AUTOMATIC, 320, 180, Scalr.OP_ANTIALIAS);
            var baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);

            Files.delete(outputPath);

            return baos.toByteArray();
        }
    }
}
