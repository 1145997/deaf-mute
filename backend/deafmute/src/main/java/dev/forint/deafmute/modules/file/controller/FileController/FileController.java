package dev.forint.deafmute.modules.file.controller;

import dev.forint.deafmute.common.api.Result;
import dev.forint.deafmute.common.config.MinioProperties;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @PostMapping("/upload")
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            return Result.fail(400, "上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String objectName = "deafmute/" + UUID.randomUUID() + suffix;

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(minioProperties.getBucketName())
                        .object(objectName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );

        String fileUrl = minioProperties.getReadPath() + "/" + objectName;

        Map<String, Object> data = new HashMap<>();
        data.put("fileName", originalFilename);
        data.put("objectName", objectName);
        data.put("url", fileUrl);

        return Result.success(data);
    }
}
