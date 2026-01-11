package com.yizhcr.my_blog.service;

import com.yizhcr.my_blog.dto.FileDTO;
import com.yizhcr.my_blog.entity.File;
import com.yizhcr.my_blog.repository.FileRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {
    
    @Value("${file.upload.path:${user.home}/uploads/}")
    private String uploadPath;
    
    @Value("${file.upload.base-url:http://localhost:8080/uploads/}")
    private String baseUrl;
    
    @Value("${file.upload.max-size:10485760}") // 默认10MB
    private long maxFileSize;
    
    @Value("${file.upload.allowed-types:image/jpeg,image/png,image/gif,application/pdf,text/plain,video/mp4,video/mpeg,video/quicktime,audio/mpeg,audio/wav}")
    private String allowedTypes;
    
    @Autowired
    private FileRepository fileRepository;
    
    /**
     * 保存上传的文件
     */
    public List<FileDTO> saveFiles(List<MultipartFile> multipartFiles) throws IOException {
        List<FileDTO> fileDTOs = new ArrayList<>();
        
        // 解析允许的文件类型
        List<String> allowedTypeList = Arrays.asList(allowedTypes.split(","));
        
        // 确保上传目录存在
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        for (MultipartFile multipartFile : multipartFiles) {
            if (multipartFile.isEmpty()) {
                continue;
            }
            
            // 检查文件大小
            if (multipartFile.getSize() > maxFileSize) {
                throw new IOException("文件大小超过限制 (" + maxFileSize + " bytes)");
            }
            
            // 检查文件类型
            String contentType = multipartFile.getContentType();
            if (contentType == null || !allowedTypeList.contains(contentType.trim())) {
                throw new IOException("不允许的文件类型: " + contentType + ". 允许的类型: " + allowedTypes);
            }
            
            // 检查文件扩展名作为额外保护
            String originalFileName = multipartFile.getOriginalFilename();
            if (originalFileName == null) {
                throw new IOException("文件名不能为空");
            }
            
            String extension = "";
            if (originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();
            } else {
                throw new IOException("文件必须有扩展名");
            }
            
            // 根据扩展名推断MIME类型并验证
            String inferredMimeType = inferMimeType(extension);
            if (!allowedTypeList.contains(inferredMimeType)) {
                throw new IOException("不允许的文件扩展名: " + extension + ". 允许的类型: " + allowedTypes);
            }
            
            // 生成唯一文件名
            String fileName = UUID.randomUUID().toString() + extension;
            
            // 构建文件路径
            Path filePath = Paths.get(uploadPath, fileName);
            
            // 保存文件
            Files.write(filePath, multipartFile.getBytes());
            
            // 保存文件信息到数据库
            File file = new File();
            file.setOriginalName(originalFileName);
            file.setFileName(fileName);
            file.setFilePath(filePath.toString());
            file.setContentType(multipartFile.getContentType());
            file.setSize(multipartFile.getSize());
            file.setUrl(baseUrl + fileName);
            file.setCreatedAt(new Date());
            
            File savedFile = fileRepository.save(file);
            
            // 转换为DTO
            FileDTO fileDTO = new FileDTO();
            BeanUtils.copyProperties(savedFile, fileDTO);
            fileDTOs.add(fileDTO);
        }
        
        return fileDTOs;
    }
    
    /**
     * 删除文件
     */
    public boolean deleteFile(Long fileId) throws IOException {
        File file = fileRepository.findById(fileId).orElse(null);
        if (file == null) {
            return false;
        }
        
        // 删除物理文件
        Path filePath = Paths.get(file.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
        
        // 从数据库中删除记录
        fileRepository.deleteById(fileId);
        return true;
    }
    
    /**
     * 根据文件ID查找文件
     */
    public FileDTO getFileById(Long id) {
        File file = fileRepository.findById(id).orElse(null);
        if (file == null) {
            return null;
        }
        
        FileDTO fileDTO = new FileDTO();
        BeanUtils.copyProperties(file, fileDTO);
        return fileDTO;
    }
    
    /**
     * 根据文件扩展名推断MIME类型
     */
    private String inferMimeType(String extension) {
        switch (extension.toLowerCase()) {
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            case ".png":
                return "image/png";
            case ".gif":
                return "image/gif";
            case ".pdf":
                return "application/pdf";
            case ".txt":
                return "text/plain";
            case ".mp4":
                return "video/mp4";
            case ".mpeg":
                return "video/mpeg";
            case ".mov":
                return "video/quicktime";
            case ".mp3":
                return "audio/mpeg";
            case ".wav":
                return "audio/wav";
            default:
                return "application/octet-stream"; // 未知类型
        }
    }
    
    /**
     * 根据ID查找文件
     */
    public File findById(Long id) {
        return fileRepository.findById(id).orElse(null);
    }
    
    /**
     * 获取所有文件
     */
    public List<FileDTO> getAllFiles() {
        List<File> files = fileRepository.findAll();
        List<FileDTO> fileDTOs = new ArrayList<>();
        
        for (File file : files) {
            FileDTO fileDTO = new FileDTO();
            BeanUtils.copyProperties(file, fileDTO);
            fileDTOs.add(fileDTO);
        }
        
        return fileDTOs;
    }
}