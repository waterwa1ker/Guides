package com.example.guides.util;

import com.example.guides.constant.FilesFormat;
import com.example.guides.model.Person;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class MediaSaver {

    @Value("${upload.profile.path}")
    private String profilePath;

    public boolean saveProfilePhoto(MultipartFile file, Person person) {
        String fileName = String.format("%s/%s.%s", profilePath, person.getUsername(), FilesFormat.IMAGE.getFormat());
        System.out.println(fileName);
        Path filePath = Path.of(fileName);
        return saveMedia(file, filePath);
    }

    private boolean saveMedia(MultipartFile file, Path filePath) {
        try(InputStream inputStream = file.getInputStream()) {
            try (OutputStream outputStream = Files.newOutputStream(filePath)) {
                IOUtils.copy(inputStream, outputStream);
            } catch (IOException e) {
                throw new IOException("Failed to save image");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read image");
        }
        return true;
    }

}
