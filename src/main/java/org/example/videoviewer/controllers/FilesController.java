package org.example.videoviewer.controllers;

import lombok.RequiredArgsConstructor;
import org.example.videoviewer.exceptions.FileNotFoundException;
import org.example.videoviewer.exceptions.UserNotFoundException;
import org.example.videoviewer.models.CreateDirectoryRequest;
import org.example.videoviewer.models.File;
import org.example.videoviewer.models.FileType;
import org.example.videoviewer.models.FilesRequest;
import org.example.videoviewer.models.FilesResponse;
import org.example.videoviewer.models.PageFilesResponse;
import org.example.videoviewer.security.jwt.dto.JwtAuthentication;
import org.example.videoviewer.services.FilesService;
import org.example.videoviewer.services.UsersService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@CrossOrigin(origins = "*", exposedHeaders = "*")
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FilesController {
    private final FilesService filesService;
    private final UsersService usersService;

    @PostMapping
    public ResponseEntity<FilesResponse> getFiles(/*final @CookieValue(name = "ACCESS_TOKEN", required = false) String token,*/
                                                  final @RequestBody FilesRequest request,
                                                  final @RequestParam long page,
                                                  final @RequestParam long size,
                                                  final JwtAuthentication authentication) {
//        System.out.println(token);
        request.setPath(normalizePath(request.getPath()));
        var normalizedPage = page<1 ? 1 : page;
        var normalizedSize = size<1 ? 1 : size;
        var pageResponse = filesService.getFiles(request.getPath(), normalizedPage, normalizedSize);
        return ResponseEntity.ok().headers(getPageHeaders(pageResponse)).body(new FilesResponse(pageResponse.getFiles()));
    }

    @PostMapping("/download")
    public ResponseEntity<FileSystemResource> downloadFile(@RequestBody FilesRequest request) throws IOException {
        return filesService.getFile(request.getPath());
    }

    private HttpHeaders getPageHeaders(final PageFilesResponse response) {
        HttpHeaders headers = new HttpHeaders();

        headers.put("P-Total-Elements", List.of(String.valueOf(response.getTotalElements())));
        headers.put("P-Total-Pages", List.of(String.valueOf(response.getTotalPages())));
        headers.put("P-Requested-Page", List.of(String.valueOf(response.getRequestedPage())));
        headers.put("P-Requested-Size", List.of(String.valueOf(response.getRequestedSize())));
        headers.put("P-Elements-On-Page", List.of(String.valueOf(response.getElementsOnPage())));
        headers.put("P-Is-Page-Full", List.of(String.valueOf(response.isPageFull())));

        return headers;
    }

    @GetMapping("/image")
    public ResponseEntity<Resource> loadImage(@RequestParam("path") String filePath, @RequestParam("type")FileType type) throws IOException, InterruptedException {
        try {
            var response = filesService.getImageResponse(new String(Base64.getDecoder().decode(filePath)), type);
            if (!response.getBody().exists()) {
                throw new FileNotFoundException(String.format("File '%S' not found", filePath));
            }
            return response;
        } catch (Exception e) {
            if (e.getMessage().contains("Illegal base64 character")) {
                throw new RuntimeException("Illegal base64 encoding");
            }
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/image/preview")
    public ResponseEntity<Resource> loadImagePreview(@RequestParam("path") String filePath, @RequestParam("type")FileType type) throws IOException, InterruptedException {
        try {
            return filesService.getScaledImageResponse(new String(Base64.getDecoder().decode(filePath)), type);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Illegal base64 character")) {
                throw new RuntimeException("Illegal base64 encoding");
            }
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/create/directory")
    public ResponseEntity<File> createDirectory(final @RequestBody CreateDirectoryRequest request,
                                                final JwtAuthentication authentication) throws IOException {
        var user = usersService.getByUsername(authentication.getUsername()).orElseThrow(() -> new UserNotFoundException(authentication.getUsername()));
        return filesService.createDirectoryAtForUser(request.getPath(), request.getName(), user);
    }

    @PostMapping("/import")
    public ResponseEntity<List<File>> importFile(@RequestPart(value = "metadata") FilesRequest request, @RequestPart(value = "files") List<MultipartFile> files) throws IOException {
        return filesService.importFiles(request, files);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteFile(@RequestParam String path) throws IOException {
        try {
            return filesService.deleteFile(new String(Base64.getDecoder().decode(path)));
        } catch (Exception e) {
            if (e.getMessage().contains("Illegal base64 character")) {
                throw new RuntimeException("Illegal base64 encoding");
            }
            throw new RuntimeException(e);
        }
    }

    private String normalizePath(String path) {
        return path.replaceAll("\\.+/", "/");
    }
}
