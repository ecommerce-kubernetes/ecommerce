package com.example.image_service.service;

import com.example.image_service.entity.Image;
import com.example.image_service.exception.NotFoundException;
import com.example.image_service.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements ImageService{
    private final ImageRepository imageRepository;
    @Value("${upload-dir}")
    private String UPLOAD_DIR;

    @Value("${gateway.domain}")
    private String DOMAIN;

    @Value("${gateway.port}")
    private String PORT;

    @Value("${spring.application.name}")
    private String APPLICATION_NAME;
    @Override
    @Transactional
    public String saveImage(MultipartFile file) {
        String storeFileName = createStoreFileName(file.getOriginalFilename());
        String imageUrl = createImageUrl(storeFileName);
        try{
            File uploadPath = new File(UPLOAD_DIR+storeFileName);
            file.transferTo(uploadPath);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
        Image image = new Image(UPLOAD_DIR + storeFileName, imageUrl);
        imageRepository.save(image);
        return imageUrl;
    }

    @Override
    @Transactional
    public void deleteImage(String imgUrl) {
        String fileName = getFileName(imgUrl);
        Image image = imageRepository.findByImageUrl(imgUrl).orElseThrow(()-> new NotFoundException("Not Found"));
        try{
            Path filePath = Paths.get(UPLOAD_DIR, fileName).normalize();
            File file = filePath.toFile();
            if(!file.exists()){
                throw new NotFoundException("Not Found");
            }
            if(!file.delete()){
                throw new RuntimeException();
            }
        }catch (Exception e){
            throw new RuntimeException();
        }
        imageRepository.delete(image);
    }

    private String createStoreFileName(String originFileName){
        String uuid = UUID.randomUUID().toString();
        String ext = extractExt(originFileName);
        return uuid + "." + ext;
    }

    private String extractExt(String originFileName){
        int index = originFileName.lastIndexOf(".");
        return originFileName.substring(index+1);
    }

    private String createImageUrl(String savedUrl){
        return  DOMAIN + ":" + PORT + "/" + APPLICATION_NAME +"/images/" + savedUrl;
    }

    private String getFileName(String imageUrl){
        return imageUrl.replace(DOMAIN + ":" + PORT + "/" + APPLICATION_NAME +"/images/", "");
    }
}
