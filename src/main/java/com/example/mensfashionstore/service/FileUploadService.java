package com.example.mensfashionstore.service;

import com.example.mensfashionstore.exception.InvalidRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class FileUploadService {
    
    @Value("${file.upload.dir:uploads/products}")
    private String uploadDir;

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp"
    ));

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public String saveFile(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("File is empty");
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidRequestException("File size exceeds maximum allowed size of 10MB");
        }

        // Validate file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isValidFileExtension(originalFilename)) {
            throw new InvalidRequestException("Invalid file type. Only image files are allowed (jpg, jpeg, png, gif, webp)");
        }

        // Create upload directory if it doesn't exist
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Generate unique filename
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString() + extension;

        // Save file
        Path filepath = Paths.get(uploadDir, filename);
        Files.write(filepath, file.getBytes());

        // Return URL path
        return "/uploads/products/" + filename;
    }

    public void deleteFile(String imagePath) throws Exception {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return;
        }

        String actualFilename = imagePath;
        if (imagePath.startsWith("/uploads/products/")) {
            actualFilename = imagePath.substring("/uploads/products/".length());
        }

        Path filepath = Paths.get(uploadDir, actualFilename);
        Files.deleteIfExists(filepath);
    }

    public boolean isValidFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return false;
        }
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }

    public boolean isValidImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return false;
        }
        try {
            String normalized = imageUrl.trim();
            if (normalized.startsWith("www.")) {
                normalized = "https://" + normalized;
            } else if (normalized.startsWith("//")) {
                normalized = "https:" + normalized;
            }
            new java.net.URL(normalized);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
