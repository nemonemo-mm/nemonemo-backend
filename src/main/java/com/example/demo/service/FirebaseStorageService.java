package com.example.demo.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.google.firebase.cloud.StorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
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

        // Firebase Storage 접근 권한 확인
        if (bucketName == null || bucketName.isEmpty()) {
            log.error("Firebase Storage Bucket이 설정되지 않았습니다.");
            throw new IllegalStateException("STORAGE_ERROR: Firebase Storage가 설정되지 않았습니다.");
        }

        // 고유한 파일명 생성
        String fileExtension = getFileExtension(originalFilename);
        String fileName = folderPath + "/" + UUID.randomUUID() + "." + fileExtension;

        try {
            // Firebase Storage에 업로드
            Storage storage = StorageClient.getInstance().bucket(bucketName).getStorage();
            if (storage == null) {
                log.error("Firebase Storage 인스턴스를 가져올 수 없습니다.");
                throw new IllegalStateException("STORAGE_ERROR: Firebase Storage에 접근할 수 없습니다.");
            }

            BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, fileName))
                    .setContentType(file.getContentType())
                    .build();

            // 파일 업로드 실행 (업로드 성공 여부는 예외로 확인)
            storage.create(blobInfo, file.getBytes());

            // Firebase Storage 공개 URL 생성 (프론트엔드에서 바로 사용 가능)
            // 형식: https://firebasestorage.googleapis.com/v0/b/{bucket}/o/{encodedPath}?alt=media
            String encodedPath = java.net.URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
            return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media", bucketName, encodedPath);
        } catch (StorageException e) {
            log.error("Firebase Storage 업로드 실패: {}", e.getMessage(), e);
            int code = e.getCode();
            if (code == 403) {
                throw new IllegalStateException("STORAGE_PERMISSION_DENIED: Firebase Storage에 대한 접근 권한이 없습니다. 관리자에게 문의하세요.");
            } else if (code == 404) {
                throw new IllegalStateException("STORAGE_NOT_FOUND: Firebase Storage Bucket을 찾을 수 없습니다.");
            } else if (code >= 500) {
                throw new IllegalStateException("STORAGE_ERROR: Firebase Storage 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            } else {
                throw new IllegalStateException("STORAGE_ERROR: 이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
            }
        } catch (UnknownHostException e) {
            log.error("네트워크 연결 실패: Firebase Storage 서버를 찾을 수 없습니다. {}", e.getMessage(), e);
            throw new IllegalStateException("STORAGE_NETWORK_ERROR: 네트워크 연결에 실패했습니다. 인터넷 연결을 확인해주세요.");
        } catch (SocketTimeoutException e) {
            log.error("네트워크 타임아웃: Firebase Storage 서버 응답 시간이 초과되었습니다. {}", e.getMessage(), e);
            throw new IllegalStateException("STORAGE_TIMEOUT: 서버 응답 시간이 초과되었습니다. 네트워크 연결을 확인하고 잠시 후 다시 시도해주세요.");
        } catch (ConnectException e) {
            log.error("연결 거부: Firebase Storage 서버에 연결할 수 없습니다. {}", e.getMessage(), e);
            throw new IllegalStateException("STORAGE_CONNECTION_ERROR: Firebase Storage 서버에 연결할 수 없습니다. 네트워크 연결을 확인해주세요.");
        } catch (IOException e) {
            log.error("네트워크 I/O 오류: {}", e.getMessage(), e);
            // IOException의 원인을 확인하여 더 구체적인 메시지 제공
            Throwable cause = e.getCause();
            if (cause instanceof UnknownHostException) {
                throw new IllegalStateException("STORAGE_NETWORK_ERROR: 네트워크 연결에 실패했습니다. 인터넷 연결을 확인해주세요.");
            } else if (cause instanceof SocketTimeoutException) {
                throw new IllegalStateException("STORAGE_TIMEOUT: 서버 응답 시간이 초과되었습니다. 네트워크 연결을 확인하고 잠시 후 다시 시도해주세요.");
            } else if (cause instanceof ConnectException) {
                throw new IllegalStateException("STORAGE_CONNECTION_ERROR: Firebase Storage 서버에 연결할 수 없습니다. 네트워크 연결을 확인해주세요.");
            } else {
                throw new IOException("이미지 업로드 중 네트워크 오류가 발생했습니다: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("이미지 업로드 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            if (e instanceof IllegalStateException) {
                throw e;
            }
            throw new IOException("이미지 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
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
            if (storage == null) {
                log.warn("Firebase Storage 인스턴스를 가져올 수 없습니다.");
                return;
            }

            BlobId blobId = BlobId.of(bucketName, filePath);
            boolean deleted = storage.delete(blobId);

            if (deleted) {
                log.info("이미지 삭제 성공: {}", filePath);
            } else {
                log.warn("이미지 삭제 실패 (파일이 존재하지 않을 수 있음): {}", filePath);
            }
        } catch (StorageException e) {
            log.error("Firebase Storage 삭제 실패: {}", e.getMessage(), e);
            // 삭제 실패해도 예외를 던지지 않음 (이미 삭제된 파일일 수 있음)
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
