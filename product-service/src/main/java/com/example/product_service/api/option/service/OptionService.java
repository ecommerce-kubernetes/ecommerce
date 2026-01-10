package com.example.product_service.api.option.service;

import com.example.product_service.api.option.service.dto.OptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OptionService {

    public OptionResponse saveOption(String name, List<String> values) {
        return null;
    }

    public OptionResponse updateOption(Long optionTypeId, String name, List<String> values) {
        return null;
    }

    public void deleteOption(Long optionTypeId) {

    }

    public OptionResponse getOption(Long optionTypeId) {
        return null;
    }

    public List<OptionResponse> getOptions() {
        return null;
    }
}
