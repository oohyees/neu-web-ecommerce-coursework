package com.example.ecommerce.controller;
import com.example.ecommerce.common.ApiResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
@RestController @RequestMapping("/api/files") @CrossOrigin
public class FileController {
    private static final long MAX_UPLOAD_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

    @PostMapping("/upload")
    public ApiResponse<?> upload(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) return ApiResponse.fail("文件不能为空");
        if (file.getSize() > MAX_UPLOAD_SIZE) return ApiResponse.fail("文件不能超过 5MB");
        String original = file.getOriginalFilename() == null ? "" : Paths.get(file.getOriginalFilename()).getFileName().toString();
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0 && dot < original.length() - 1) ext = original.substring(dot + 1).toLowerCase(Locale.ROOT);
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(ext) || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            return ApiResponse.fail("仅支持 jpg、jpeg、png、gif、webp 图片");
        }
        Path dir = Paths.get("uploads");
        Files.createDirectories(dir);
        String name = UUID.randomUUID() + "." + ext;
        Files.copy(file.getInputStream(), dir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
        return ApiResponse.ok("/uploads/" + name);
    }
}
