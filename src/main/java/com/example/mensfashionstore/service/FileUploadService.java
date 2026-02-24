package com.example.mensfashionstore.service;

import com.example.mensfashionstore.exception.InvalidRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.nio.file.StandardCopyOption;

@Service
public class FileUploadService {
    
    @Value("${file.upload.dir:uploads/products}")
    private String uploadDir;

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "avif", "jfif", "tif", "tiff", "ico"
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

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new InvalidRequestException("Invalid file type. Only image files are allowed.");
        }

        // Validate file extension and derive one if missing
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            originalFilename = "image";
        }
        if (!isValidFileExtension(originalFilename)) {
            String extensionFromType = extensionFromContentType(contentType);
            if (extensionFromType == null) {
                throw new InvalidRequestException("Invalid file type. Only image files are allowed.");
            }
            originalFilename = originalFilename + "." + extensionFromType;
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
        Files.copy(file.getInputStream(), filepath, StandardCopyOption.REPLACE_EXISTING);

        // Return URL path
        return "/uploads/products/" + filename;
    }

    public String saveImageFromUrl(String imageUrl) throws Exception {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new InvalidRequestException("Image URL is empty");
        }

        String normalizedUrl = imageUrl.trim();
        URL url = new URL(normalizedUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setRequestProperty("Accept", "image/*,*/*;q=0.8");

        int responseCode = connection.getResponseCode();
        if (responseCode < 200 || responseCode >= 400) {
            throw new InvalidRequestException("Unable to download image from URL");
        }

        String contentType = connection.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new InvalidRequestException("Provided URL is not a direct image link");
        }

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String extension = extensionFromContentType(contentType);
        if (extension == null) {
            extension = extensionFromPath(url.getPath());
        }
        if (extension == null) {
            extension = "jpg";
        }

        String filename = UUID.randomUUID() + "." + extension;
        Path filepath = Paths.get(uploadDir, filename);

        try (InputStream inputStream = connection.getInputStream()) {
            Files.copy(inputStream, filepath, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            connection.disconnect();
        }

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

    private String extensionFromContentType(String contentType) {
        String ct = contentType.toLowerCase(Locale.ROOT);
        if (ct.contains("jpeg") || ct.contains("jpg")) return "jpg";
        if (ct.contains("png")) return "png";
        if (ct.contains("gif")) return "gif";
        if (ct.contains("webp")) return "webp";
        if (ct.contains("bmp")) return "bmp";
        if (ct.contains("svg")) return "svg";
        if (ct.contains("avif")) return "avif";
        if (ct.contains("tiff") || ct.contains("tif")) return "tiff";
        if (ct.contains("icon") || ct.contains("ico")) return "ico";
        return null;
    }

    private String extensionFromPath(String path) {
        if (path == null || !path.contains(".")) {
            return null;
        }
        String ext = path.substring(path.lastIndexOf(".") + 1).toLowerCase(Locale.ROOT);
        return ALLOWED_EXTENSIONS.contains(ext) ? ext : null;
    }
}
