package org.example.videoviewer.services;

import org.example.videoviewer.enums.Roles;
import org.example.videoviewer.exceptions.FileExistsException;
import org.example.videoviewer.exceptions.FileNotFoundException;
import org.example.videoviewer.exceptions.UserNotFoundException;
import org.example.videoviewer.models.FileType;
import org.example.videoviewer.models.FilesRequest;
import org.example.videoviewer.models.PageFilesResponse;
import org.example.videoviewer.repositories.model.Users;
import org.example.videoviewer.utils.NaturalOrderComparator;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;

@Service
public class FilesService {
    //todo: get all the files if user has admin authority
    @Value("${home.dir}")
    private String homeDir;
    @Value("${resources.dir}")
    private String resourcesDir;
    @Autowired
    private FFMpegImageExtractor imageExtractor;
    @Autowired
    private UsersService usersService;

    public PageFilesResponse getFiles(final String path,
                                      final long page,
                                      final long size) {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = usersService.getByUsername(username).orElseThrow(UserNotFoundException::new);

        PageFilesResponse response = new PageFilesResponse();

        File dir = new File(getPath(path, user));

        if (dir.exists() && dir.isDirectory()) {
            var filesList = Arrays.stream(dir.listFiles()).map(f -> {
                try {
                    String mimeType = f.isDirectory()? "directory" : Files.probeContentType(f.toPath());
                    mimeType = mimeType == null ? "other" : mimeType;

                    return new org.example.videoviewer.models.File(
                            f.getName(),
                            f.getPath().replaceAll("\\\\", "/").replace(getRoot(user), ""),
                            FileType.getTypeFor(mimeType),
                            mimeType,
                            f.getTotalSpace());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
//            }).sorted((f1, f2) -> f1.getName().compareTo(f2.getName())).toList();
            }).sorted(new NaturalOrderComparator()).toList();

            var totalElementsCount = filesList.size();
            var totalPagesCount = (int) Math.ceil((double) totalElementsCount / size);
            var elementsOnPage = filesList.stream().skip((page-1) * size).limit(size).count();
            var isPageFull = elementsOnPage == size;
            var files = filesList.stream().skip((page-1) * size).limit(size).toList();

            response.setTotalElements(totalElementsCount);
            response.setTotalPages(totalPagesCount);
            response.setRequestedPage(page);
            response.setRequestedSize(size);
            response.setElementsOnPage(elementsOnPage);
            response.setPageFull(isPageFull);
            response.setFiles(files);
        }

        return response;
    }

    public ResponseEntity<FileSystemResource> getFile(String path) throws IOException {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = usersService.getByUsername(username).orElseThrow(UserNotFoundException::new);

        Path filePath = Paths.get(getPath(path, user));

        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            throw new IllegalArgumentException("File '" + filePath.getFileName() + "' can not be downloaded!");
        }

        String mimeType = Files.probeContentType(filePath);
        mimeType = mimeType == null? "application/octet-stream" : mimeType;

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(mimeType)).body(new FileSystemResource(filePath));
    }

    public ResponseEntity<org.example.videoviewer.models.File> createDirectoryAtForUser(final String path,
                                                                                        final String dirName,
                                                                                        final Users user) throws IOException {
        var normalizedPath = Paths.get(getPath(path, user), dirName);
        if (Files.exists(normalizedPath) && Files.isDirectory(normalizedPath)) {
            throw new FileExistsException(dirName);
        }

        var dirPath = Files.createDirectory(normalizedPath);
        var dir = new File(dirPath.toUri());

        var dirResponse = new org.example.videoviewer.models.File(dir.getName(),
                dir.getPath().replaceAll("\\\\", "/").replace(getRoot(user), ""),
                FileType.DIRECTORY,
                Files.probeContentType(dirPath),
                dir.getTotalSpace());


        return ResponseEntity.status(HttpStatus.CREATED).body(dirResponse);
    }

    public ResponseEntity<List<org.example.videoviewer.models.File>> importFiles(final FilesRequest request, final List<MultipartFile> files) throws IOException {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = usersService.getByUsername(username).orElseThrow(UserNotFoundException::new);

        var filesList = new ArrayList<org.example.videoviewer.models.File>();
        for (MultipartFile file : files) {
            filesList.add(importFile(request, file, user).getBody());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(filesList);
    }

    private ResponseEntity<org.example.videoviewer.models.File> importFile(final FilesRequest metadata, final MultipartFile file, final Users user) throws IOException {
        var normalizedPath = Paths.get(getPath(metadata.getPath(), user), file.getOriginalFilename());

        if (Files.exists(normalizedPath)) {
            throw new FileExistsException(file.getOriginalFilename());
        }

        var newFilePath = Files.write(normalizedPath, file.getBytes());
        var newFile = new File(newFilePath.toUri());

        var mimetype = Files.probeContentType(newFilePath);

        var fileResponse = new org.example.videoviewer.models.File(newFile.getName(),
                newFile.getPath().replaceAll("\\\\", "/").replace(getRoot(user), ""),
                FileType.getTypeFor(mimetype),
                mimetype,
                newFile.getTotalSpace());

        return ResponseEntity.status(HttpStatus.CREATED).body(fileResponse);
    }

    public ResponseEntity<Void> deleteFile(final String path) throws IOException {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = usersService.getByUsername(username).orElseThrow(UserNotFoundException::new);

        var normalizedPath = Paths.get(getPath(path, user));

        if (!Files.exists(normalizedPath)) {
            throw new FileNotFoundException(normalizedPath.getFileName().toString());
        }

        deleteRecursively(new File(normalizedPath.toUri()));

        return ResponseEntity.noContent().build();
    }

    private void deleteRecursively(final File file) throws IOException {
        if (file.isDirectory()) {
            var innerFiles = file.listFiles();

            for (var innerFile : innerFiles) {
                deleteRecursively(innerFile);
            }

            file.delete();
        } else {
            file.delete();
        }
    }

    private String getFileName(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    private String getPath(final String path, final Users user) {
        var root = getRoot(user);

        if (!root.substring(root.length() - 1).equals("/")) {
            return root + "/" + path.replaceFirst("/", "");
        }

        return root + path.replaceFirst("/", "");
    }

    private String getRoot(final Users user) {
        var isAdmin = user.getRoles().contains(Roles.ADMIN);

        return isAdmin ? homeDir : homeDir + "/" + user.getUsername();
    }

    public Resource getImage(String filePath, FileType type) {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = usersService.getByUsername(username).orElseThrow(UserNotFoundException::new);

        Path path = getCorrectFileImagePath(filePath, type, user);
        return new FileSystemResource(path);
    }

    public Resource getScaledImage(String filePath, FileType type) throws IOException {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = usersService.getByUsername(username).orElseThrow(UserNotFoundException::new);

        Path path = getCorrectFileImagePath(filePath, type, user);
        var image = Scalr.resize(ImageIO.read(path.toFile()), Scalr.Method.SPEED, Scalr.Mode.AUTOMATIC, 320, 180, Scalr.OP_ANTIALIAS);
        var baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return new ByteArrayResource(baos.toByteArray());
    }

    public ResponseEntity<Resource> getImageResponse(String filePath, FileType type) throws IOException, InterruptedException {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = usersService.getByUsername(username).orElseThrow(UserNotFoundException::new);

        ResponseEntity<Resource> response;

        if (!FileType.VIDEO.equals(type)) {
            Path path = getCorrectFileImagePath(filePath, type, user);
            response = ResponseEntity.ok().contentType(MediaType.parseMediaType(Files.probeContentType(path))).body(getImage(filePath, type));
        } else {
            response =  ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(new ByteArrayResource(getFirstVideoFrameBytes(filePath, user)));
        }

        return response;
    }

    public ResponseEntity<Resource> getScaledImageResponse(String filePath, FileType type) throws IOException, InterruptedException {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = usersService.getByUsername(username).orElseThrow(UserNotFoundException::new);

        ResponseEntity<Resource> response;

        if (!FileType.VIDEO.equals(type)) {
            Path path = getCorrectFileImagePath(filePath, type, user);
            response = ResponseEntity.ok().contentType(MediaType.parseMediaType(Files.probeContentType(path))).body(getScaledImage(filePath, type));
        } else {
            response =  ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(new ByteArrayResource(getScaledFirstVideoFrameBytes(filePath, user)));
        }

        return response;
    }

    private Path getCorrectFileImagePath(final String filePath,
                                         final FileType type,
                                         final Users user) {
        switch (type) {
            case DIRECTORY:
                return Paths.get(getImagesDir() + "/default/folder.png");
            case TEXT_FILE:
                return Paths.get(getImagesDir() + "/default/txt.png");
            case PDF_FILE:
                return Paths.get(getImagesDir() + "/default/pdf.png");
            case IMAGE:
                return Paths.get(getPath(filePath, user));
            default:
                return Paths.get(getImagesDir() + "/default/file.png");
        }
    }

    private byte[] getFirstVideoFrameBytes(final String filePath,
                                           final Users user) throws IOException, InterruptedException {
        return imageExtractor.getFirstFrameFromVideo(getPath(filePath, user), getImagesDir());
    }

    private byte[] getScaledFirstVideoFrameBytes(final String filePath,
                                                 final Users user) throws IOException, InterruptedException {
        return imageExtractor.getScaledFirstFrameFromVideo(getPath(filePath, user), getImagesDir());
    }

    private String getImagesDir() {
        return resourcesDir + "/images";
    }
}
