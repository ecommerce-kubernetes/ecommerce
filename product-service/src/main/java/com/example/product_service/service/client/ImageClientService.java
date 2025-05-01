package com.example.product_service.service.client;

import com.example.product_service.client.ImageClient;
import com.example.product_service.dto.client.ImageUrlListDto;
import com.example.product_service.exception.NotFoundException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageClientService {
    private final ImageClient imageClient;

    @CircuitBreaker(name = "imageService", fallbackMethod = "deleteImageFallback")
    public void deleteImage(String imageUrl){
        imageClient.deleteImage(imageUrl);
    }

    @CircuitBreaker(name = "imageService", fallbackMethod = "deleteImageFallback")
    public void deleteImages(List<String> imageUrls){
        ImageUrlListDto imageUrlListDto = new ImageUrlListDto(imageUrls);
        imageClient.imageBatchDelete(imageUrlListDto);
    }

    //TODO 500 코드일때 커스텀 예외를 발생하게 하여 Controller Advice 에서 메시지가 추가된 응답을 반환하도록
    public void deleteImageFallback(String imageUrl, Throwable throwable){
        if(throwable instanceof CallNotPermittedException){
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Image Service unavailable"
            );
        }
        else if (throwable instanceof FeignException){
            if (((FeignException) throwable).status() == 404){
                throw new NotFoundException("Not Found Image");
            }
        }
        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Image Service Error"
        );
    }
}
