package swyp.hobbi.swyphobbiback.post_image.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.multipart.MultipartFile;
import swyp.hobbi.swyphobbiback.common.exception.FileUploadFailedException;
import swyp.hobbi.swyphobbiback.post_image.event.PostImageUploadEvent;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostImageUploadListener {
    private static final String BUCKET_NAME = "hobbi-dev";

    private final AmazonS3 amazonS3;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostImageUploadEvent(PostImageUploadEvent event) {
        try {
            MultipartFile file = event.getFile();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            String fileName = event.getFileName();

            PutObjectRequest request = new PutObjectRequest(BUCKET_NAME, fileName, file.getInputStream(), metadata);
            amazonS3.putObject(request);
        } catch (IOException e) {
            log.error("이미지 업로드 실패 : {}", event.getFileName());
            throw new FileUploadFailedException();
        }
    }
}
