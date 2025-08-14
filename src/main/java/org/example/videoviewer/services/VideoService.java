package org.example.videoviewer.services;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.example.videoviewer.enums.Roles;
import org.example.videoviewer.exceptions.UserNotFoundException;
import org.example.videoviewer.models.File;
import org.example.videoviewer.models.FileType;
import org.example.videoviewer.repositories.model.Users;
import org.example.videoviewer.services.models.VideoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class VideoService {
    private final UsersService usersService;

//    private static final String VIDEO_FILE = "C:/Users/Nicholas/Documents/%s.mp4";
//
////    @Autowired
////    private ResourceLoader resourceLoader;
//
//    public Mono<Resource> getVideo(String filename) throws IOException {
//        var video = new FileSystemResource(String.format(VIDEO_FILE, filename));
////        System.out.println("+\n\t\t" + video.contentLength() + "\n");
//
//        return Mono.fromSupplier(() -> video);
//
//
//
//
//
//
//
//        //        return Mono.fromSupplier(() -> resourceLoader.getResource(VIDEO_FILE));
//    }

@Value("${home.dir}")
private String homeDir;

    private static final long CHUNK_SIZE = 2 * 1024 * 1024; // 2 MB

    public VideoResponse getVideo(final String videoPath,
                                          final String range) throws IOException {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = usersService.getByUsername(username).orElseThrow(UserNotFoundException::new);

        var filePath = Paths.get(getPath(videoPath, user));
        var totalBytes = Files.size(filePath);
        var start = 0L;
        var end = totalBytes - 1;

        if (StringUtils.isNotBlank(range) && range.startsWith("bytes=")) {
            var rangeValues = range.substring(6).split("-");
            start = Long.parseLong(rangeValues[0]);

            if (rangeValues.length > 1 && StringUtils.isNotBlank(rangeValues[1])) {
                end = Math.min(Long.parseLong(rangeValues[1]), totalBytes - 1);
            } else {
                end = Math.min(start + CHUNK_SIZE - 1, totalBytes - 1);
            }
        }

        var inputStream = Files.newInputStream(filePath);
        inputStream.skip(start);

        var bytesToWrite = (int)(end - start + 1);

        StreamingResponseBody response = os -> {
            os.write(inputStream.readNBytes(bytesToWrite));
            inputStream.close();
        };

        return new VideoResponse(response, start, end, totalBytes);
    }

    private String getRoot(final Users user) {
        var isAdmin = user.getRoles().contains(Roles.ADMIN);

        return isAdmin ? homeDir : homeDir + "/" + user.getUsername();
    }

    private String getPath(final String path, final Users user) {
        var root = getRoot(user);

        if (!root.substring(root.length() - 1).equals("/")) {
            return root + "/" + path.replaceFirst("/", "");
        }

        return root + path.replaceFirst("/", "");
    }
}
