package org.example.videoviewer.controllers;

import lombok.RequiredArgsConstructor;
import org.example.videoviewer.security.jwt.AuthService;
import org.example.videoviewer.security.jwt.dto.JwtAuthentication;
import org.example.videoviewer.security.jwt.dto.VideoSighRequest;
import org.example.videoviewer.services.VideoService;
import org.example.videoviewer.services.models.VideoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.util.Base64;

@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RestController
public class VideoController {
    private final VideoService videoService;
    private final AuthService authService;

    //todo: must be removed
    @Deprecated
    @GetMapping(value = "/default/video/{fileName}", produces = "video/mp4")
    public ResponseEntity<StreamingResponseBody> defaultVideo(@PathVariable("fileName") String fileName, @RequestHeader("Range") String range) throws IOException {
        System.out.println("Playing video in range " + range);
        var response = videoService.getVideo("AWS\\Associate\\AWS Certified Developer - Associate\\" + fileName + ".mp4", range);

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).headers(buildHeaders(response)).body(response.getResponseBody());
    }

    @GetMapping(value = "/video/play", produces = "video/mp4")
    public ResponseEntity<StreamingResponseBody> playVideo(final @CookieValue(name = "STREAM_TOKEN", required = false) String token,
                                                           final @RequestParam("video") String videoPath,
                                                           final @RequestHeader("Range") String range) throws IOException {
        var responseEntity = ResponseEntity.status(HttpStatus.PARTIAL_CONTENT);

        var cookie = authService.refreshStreamingCookieIfNeeded(token);

        if (cookie == null) {
            responseEntity.header(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        System.out.println("Playing video in range " + range);
        System.out.println("Requested video: " + new String(Base64.getDecoder().decode(videoPath)));
        try {
            var response = videoService.getVideo(new String(Base64.getDecoder().decode(videoPath)), range);

            return responseEntity.headers(buildHeaders(response)).body(response.getResponseBody());
        } catch (Exception e) {
            if (e.getMessage().contains("Illegal base64 character")) {
                throw new RuntimeException("Illegal base64 encoding");
            }
            throw new RuntimeException(e);
        }
    }

    private HttpHeaders buildHeaders(final VideoResponse videoResponse) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "video/mp4");
        headers.set("Accept-Ranges", "bytes");
        headers.set("Content-Range", String.format("bytes %d-%d/%d",
                videoResponse.getStart(),
                videoResponse.getEnd(),
                videoResponse.getTotalBytes()));
        headers.setContentLength(videoResponse.getEnd() - videoResponse.getStart() + 1);

        return headers;
    }
}
/**
 * To make the video play we have to return 206 Partial Content and content length as well, as content type video/mp4, accept ranges - bytes and content range in bytes FROM-TO/LAST_BYTE
 */