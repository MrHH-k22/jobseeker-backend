package vn.hoidanit.jobhunter.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
public class FileService {

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.base-folder}")
    private String baseFolder;

    private S3Client s3Client;

    private S3Client getS3Client() {
        if (s3Client == null) {
            s3Client = S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
        }
        return s3Client;
    }

    public String store(MultipartFile file, String folder) throws IOException {
        // unique file name
        String fileName = System.currentTimeMillis() + "-" +
                file.getOriginalFilename();
        String key = (baseFolder != null ? baseFolder : "") + (folder != null ? folder + "/" : "") + fileName;

        PutObjectRequest putOb = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        getS3Client().putObject(putOb,
                software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes()));

        // Trả về tên/key file (hoặc URL nếu bạn muốn)
        return key;
    }

    // @Value("${hoidanit.upload-file.base-uri}")
    // private String baseURI;

    // public void createDirectory(String folder) throws URISyntaxException {
    // URI uri = new URI(folder);
    // Path path = Paths.get(uri);
    // File tmpDir = new File(path.toString());
    // if (!tmpDir.isDirectory()) {
    // try {
    // Files.createDirectory(tmpDir.toPath());
    // System.out.println(">>> CREATE NEW DIRECTORY SUCCESSFUL, PATH = " +
    // tmpDir.toPath());
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // } else {
    // System.out.println(">>> SKIP MAKING DIRECTORY, ALREADY EXISTS");
    // }
    // }

    // public String store(MultipartFile file, String folder) throws
    // URISyntaxException,
    // IOException {
    // // create unique filename
    // String finalName = System.currentTimeMillis() + "-" +
    // file.getOriginalFilename();
    // URI uri = new URI(baseURI + folder + "/" + finalName);
    // Path path = Paths.get(uri);
    // try (InputStream inputStream = file.getInputStream()) {
    // Files.copy(inputStream, path,
    // StandardCopyOption.REPLACE_EXISTING);
    // }
    // return finalName;
    // }

}
