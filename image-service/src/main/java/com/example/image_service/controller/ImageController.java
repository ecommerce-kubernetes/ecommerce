package com.example.image_service.controller;

import com.example.image_service.dto.ImageURLDto;
import com.example.image_service.dto.ImageUrlListDto;
import com.example.image_service.exception.BadRequestException;
import com.example.image_service.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
@Slf4j
public class ImageController {
    private final ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<ImageURLDto> imageUpload(@RequestPart("image") MultipartFile file,
                                                   @RequestParam("directory") String directory){
        if(file.isEmpty()){
            throw new BadRequestException("BAD REQUEST");
        }
        String[] validateImage = {"jpg","jpeg","png"};
        String[] validateDirectory = {"products", "reviews"};
        String fileName = file.getOriginalFilename();

        if (fileName == null || !isValidImageExtension(fileName, validateImage)) {
            throw new BadRequestException("Invalid extension");
        }
        if (directory == null || !isValidateDirectory(directory, validateDirectory)){
            throw new BadRequestException("Invalid directory");
        }
        String imageUrl = imageService.saveImage(file, directory);
        ImageURLDto imageURLDto = new ImageURLDto(imageUrl);
        return ResponseEntity.ok(imageURLDto);
    }
    @DeleteMapping("/delete")
    public ResponseEntity<?> imageDelete(@RequestParam("imageUrl") String imageUrl){
        imageService.deleteImage(imageUrl);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/batch-delete")
    public ResponseEntity<Void> imageBatchDelete(@RequestBody ImageUrlListDto imageUrlListDto){
        imageService.deleteImageBatch(imageUrlListDto.getImageUrls());
        return ResponseEntity.noContent().build();
    }

    private boolean isValidImageExtension(String fileName, String[] allowedExtensions) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return Arrays.asList(allowedExtensions).contains(extension);
    }

    private boolean isValidateDirectory(String directory, String[] allowedDirectories){
        return Arrays.asList(allowedDirectories).contains(directory);
    }
}



