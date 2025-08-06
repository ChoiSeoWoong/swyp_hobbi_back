package swyp.hobbi.swyphobbiback.mypage.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import swyp.hobbi.swyphobbiback.common.error.ErrorCode;
import swyp.hobbi.swyphobbiback.common.exception.CustomException;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileImageService {

    private static final String BUCKET_NAME = "hobbi-dev";
    private static final String PREFIX_IMAGE_URL = "https://hobbi-dev.s3.ap-northeast-2.amazonaws.com/";

    private final AmazonS3 amazonS3;

    public String uploadProfileImage(MultipartFile file, String userEmail) {
        String fileName = "profile/" + userEmail + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        try {
            metadata.setContentLength(file.getSize());
            //이미지 업로드
            amazonS3.putObject(
                    new PutObjectRequest(BUCKET_NAME, fileName, file.getInputStream(), metadata)
                            .withCannedAcl(CannedAccessControlList.PublicRead)
            );
        } catch (IOException e) {
            log.error("프로필 이미지 업로드 실패 (userEmail: {}, fileName: {}): {}", userEmail, fileName, e.getMessage());
            throw new CustomException(ErrorCode.FAILED_TO_UPLOAD_FILE);
        }

        return PREFIX_IMAGE_URL + BUCKET_NAME + "/" + fileName;
    }

    public void deleteProfileImage(String imageUrl) {
        String key = imageUrl.replace(PREFIX_IMAGE_URL + BUCKET_NAME + "/", "");
        amazonS3.deleteObject(new DeleteObjectRequest(BUCKET_NAME, key));
    }
}

