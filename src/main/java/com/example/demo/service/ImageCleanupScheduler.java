package com.example.demo.service;

import com.example.demo.repository.TeamRepository;
import com.example.demo.repository.UserRepository;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * 주기적으로 Firebase Storage에서 사용되지 않는 이미지를 정리하는 스케줄러
 * 매일 새벽 3시에 실행됩니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageCleanupScheduler {

    @Value("${firebase.storage-bucket:}")
    private String bucketName;

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    /**
     * 주기적으로 사용되지 않는 이미지를 정리
     * 매일 새벽 3시에 실행 (cron: 초 분 시 일 월 요일)
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional(readOnly = true)
    public void cleanupOrphanedImages() {
        log.info("사용되지 않는 이미지 정리 작업 시작");
        
        try {
            // 1. DB에서 사용 중인 모든 image_url 수집
            Set<String> usedImageUrls = new HashSet<>();
            
            // User의 image_url 수집
            userRepository.findAll().forEach(user -> {
                if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                    usedImageUrls.add(user.getImageUrl());
                }
            });
            
            // Team의 image_url 수집
            teamRepository.findAll().forEach(team -> {
                if (team.getImageUrl() != null && !team.getImageUrl().isEmpty()) {
                    usedImageUrls.add(team.getImageUrl());
                }
            });
            
            log.info("DB에서 사용 중인 이미지 URL 개수: {}", usedImageUrls.size());
            
            // 2. Firebase Storage의 모든 파일 목록 가져오기
            Storage storage = StorageClient.getInstance().bucket(bucketName).getStorage();
            int deletedCount = 0;
            int errorCount = 0;
            
            // users 폴더의 파일들 확인
            for (Blob blob : storage.list(bucketName, Storage.BlobListOption.prefix("users/")).iterateAll()) {
                String blobUrl = generateBlobUrl(blob.getName());
                if (!usedImageUrls.contains(blobUrl)) {
                    try {
                        boolean deleted = storage.delete(BlobId.of(bucketName, blob.getName()));
                        if (deleted) {
                            deletedCount++;
                            log.debug("사용되지 않는 이미지 삭제: {}", blob.getName());
                        }
                    } catch (Exception e) {
                        errorCount++;
                        log.warn("이미지 삭제 실패: {}", blob.getName(), e);
                    }
                }
            }
            
            // teams 폴더의 파일들 확인
            for (Blob blob : storage.list(bucketName, Storage.BlobListOption.prefix("teams/")).iterateAll()) {
                String blobUrl = generateBlobUrl(blob.getName());
                if (!usedImageUrls.contains(blobUrl)) {
                    try {
                        boolean deleted = storage.delete(BlobId.of(bucketName, blob.getName()));
                        if (deleted) {
                            deletedCount++;
                            log.debug("사용되지 않는 이미지 삭제: {}", blob.getName());
                        }
                    } catch (Exception e) {
                        errorCount++;
                        log.warn("이미지 삭제 실패: {}", blob.getName(), e);
                    }
                }
            }
            
            log.info("이미지 정리 작업 완료 - 삭제된 파일: {}개, 오류: {}개", deletedCount, errorCount);
            
        } catch (Exception e) {
            log.error("이미지 정리 작업 중 오류 발생", e);
        }
    }
    
    /**
     * Blob 이름으로부터 Firebase Storage URL 생성
     * FirebaseStorageService의 uploadImage 메서드와 동일한 형식
     */
    private String generateBlobUrl(String blobName) {
        try {
            String encodedPath = java.net.URLEncoder.encode(blobName, "UTF-8").replace("+", "%20");
            return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media", bucketName, encodedPath);
        } catch (Exception e) {
            log.warn("Blob URL 생성 실패: {}", blobName, e);
            return null;
        }
    }
}

