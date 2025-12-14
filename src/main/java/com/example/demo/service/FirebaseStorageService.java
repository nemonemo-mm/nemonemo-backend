package com.example.demo.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
public class FirebaseStorageService {

    @Value("${firebase.storage-bucket:}")
    private String bucketName;

    /**
     * 이미지 업로드
     * @param file 업로드할 파일
     * @param folderPath 저장할 폴더 경로 (예: "users", "teams")
     * @return 업로드된 파일의 URL
     */
    public String uploadImage(MultipartFile file, String folderPath) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isImageFile(originalFilename)) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다. (jpg, jpeg, png, gif, webp)");
        }

        // 파일 크기 검증 (최대 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("파일 크기는 5MB를 초과할 수 없습니다.");
        }

        // 고유한 파일명 생성
        String fileExtension = getFileExtension(originalFilename);
        String fileName = folderPath + "/" + UUID.randomUUID() + "." + fileExtension;

        // Firebase Storage에 업로드
        Storage storage = StorageClient.getInstance().bucket(bucketName).getStorage();
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, fileName))
                .setContentType(file.getContentType())
                .build();

        Blob blob = storage.create(blobInfo, file.getBytes());

        // Firebase Storage 공개 URL 생성 (프론트엔드에서 바로 사용 가능)
        // 형식: https://firebasestorage.googleapis.com/v0/b/{bucket}/o/{encodedPath}?alt=media
        String encodedPath = java.net.URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
        return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media", bucketName, encodedPath);
    }

    /**
     * 이미지 삭제
     * @param imageUrl 삭제할 이미지의 URL
     */
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        try {
            // URL에서 파일 경로 추출
            String filePath = extractFilePathFromUrl(imageUrl);
            if (filePath == null) {
                log.warn("이미지 URL에서 파일 경로를 추출할 수 없습니다: {}", imageUrl);
                return;
            }

            // Firebase Storage에서 삭제
            Storage storage = StorageClient.getInstance().bucket(bucketName).getStorage();
            BlobId blobId = BlobId.of(bucketName, filePath);
            boolean deleted = storage.delete(blobId);

            if (deleted) {
                log.info("이미지 삭제 성공: {}", filePath);
            } else {
                log.warn("이미지 삭제 실패 (파일이 존재하지 않을 수 있음): {}", filePath);
            }
        } catch (Exception e) {
            log.error("이미지 삭제 중 오류 발생: {}", imageUrl, e);
            // 삭제 실패해도 예외를 던지지 않음 (이미 삭제된 파일일 수 있음)
        }
    }

    /**
     * 이미지 수정 (기존 이미지 삭제 후 새 이미지 업로드)
     * @param file 새 이미지 파일
     * @param oldImageUrl 기존 이미지 URL (삭제할 이미지)
     * @param folderPath 저장할 폴더 경로
     * @return 새 이미지 URL
     */
    public String updateImage(MultipartFile file, String oldImageUrl, String folderPath) throws IOException {
        // 기존 이미지 삭제
        if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
            deleteImage(oldImageUrl);
        }

        // 새 이미지 업로드
        return uploadImage(file, folderPath);
    }

    /**
     * 이미지 파일인지 확인
     */
    private boolean isImageFile(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return extension.equals("jpg") || extension.equals("jpeg") || 
               extension.equals("png") || extension.equals("gif") || 
               extension.equals("webp");
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

    /**
     * URL에서 파일 경로 추출
     * Firebase Storage URL 형식: https://firebasestorage.googleapis.com/v0/b/{bucket}/o/{encodedPath}?alt=media&token={token}
     */
    private String extractFilePathFromUrl(String url) {
        try {
            // URL에서 파일 경로 부분 추출
            if (url.contains("/o/")) {
                String[] parts = url.split("/o/");
                if (parts.length > 1) {
                    String pathPart = parts[1].split("\\?")[0];
                    // URL 디코딩
                    return java.net.URLDecoder.decode(pathPart, "UTF-8");
                }
            }
            return null;
        } catch (Exception e) {
            log.error("URL에서 파일 경로 추출 실패: {}", url, e);
            return null;
        }
    }
}
