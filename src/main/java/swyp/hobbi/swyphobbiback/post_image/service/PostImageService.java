package swyp.hobbi.swyphobbiback.post_image.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import swyp.hobbi.swyphobbiback.post.domain.Post;
import swyp.hobbi.swyphobbiback.post_image.domain.PostImage;
import swyp.hobbi.swyphobbiback.post_image.repository.PostImageRepository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostImageService {
    private static final String BUCKET_NAME = "hobbi-dev";
    private static final String PREFIX_FILE_NAME = "post";
    private static final String FILE_FORMAT = "%s%s%s%s";
    private static final String GENERATE_FILE_FORMAT = "%s%s";
    private static final String FILE_SEPARATOR = "-";
    private static final String DIRECTORY_SEPARATOR = "/";
    private static final String PREFIX_IMAGE_URL = "https://hobbi-dev.s3.ap-northeast-2.amazonaws.com/";

    private final AmazonS3 amazonS3;

    private final PostImageRepository postImageRepository;

    public PostImage savePostImage(MultipartFile file, Post post, String imageUrl) {
        PostImage image = PostImage.builder()
                .post(post)
                .imageUrl(imageUrl)
                .imageFileName(file.getOriginalFilename())
                .build();

        return postImageRepository.save(image);
    }

    public void deletePostImage(String imageUrl) {
        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(BUCKET_NAME, imageUrl);
        amazonS3.deleteObject(deleteObjectRequest);
    }

    public String fileNameFormatter(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String sanitizedFilename = sanitizeFilename(originalFilename);

        return FILE_FORMAT.formatted(
                PREFIX_FILE_NAME,
                FILE_SEPARATOR,
                UUID.randomUUID().toString(),
                sanitizedFilename
        );
    }

    public String generateS3Url(String fileName) {
        return GENERATE_FILE_FORMAT.formatted(
                PREFIX_IMAGE_URL,
                fileName
        );
    }

    public String getSuffixImageUrl(String imageUrl) {
        String prefixAndBucketName = PREFIX_IMAGE_URL + BUCKET_NAME + DIRECTORY_SEPARATOR;
        return imageUrl.replace(prefixAndBucketName, "");
    }

    private String sanitizeFilename(String originalFilename) {
        if(originalFilename == null) {
            return "unnamed-file";
        }

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if(dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
            originalFilename = originalFilename.substring(0, dotIndex);
        }

        String sanitizedFilename = Normalizer.normalize(originalFilename, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("[^a-zA-Z0-9-_]", "_");

        if(sanitizedFilename.isBlank()) {
            sanitizedFilename = UUID.randomUUID().toString();
        }

        return sanitizedFilename + extension;
    }
}
