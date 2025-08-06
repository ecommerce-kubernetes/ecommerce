package com.example.product_service.service;

import com.example.product_service.dto.request.options.OptionTypeRequest;
import com.example.product_service.dto.response.options.OptionTypeResponse;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.options.OptionValuesResponseDto;
import com.example.product_service.entity.OptionTypes;
import com.example.product_service.entity.OptionValues;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.OptionTypesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class OptionTypeServiceImpl implements OptionTypeService {

    private final OptionTypesRepository optionTypesRepository;
    @Override
    @Transactional
    public OptionTypeResponse saveOptionTypes(OptionTypeRequest requestDto) {
        String name = requestDto.getName();
        OptionTypes optionType = new OptionTypes(name);
        try{
            OptionTypes save = optionTypesRepository.save(optionType);
            return new OptionTypeResponse(save);
        } catch (DataIntegrityViolationException ex){
            throw new DuplicateResourceException("OptionTypes name Conflict");
        }
    }

    @Override
    public PageDto<OptionTypeResponse> getOptionTypes(String query, Pageable pageable) {
        Page<OptionTypes> optionTypesPage = optionTypesRepository.findByNameOrAll(query, pageable);

        List<OptionTypes> content = optionTypesPage.getContent();

        List<OptionTypeResponse> list = content.stream().map(OptionTypeResponse::new).toList();
        return new PageDto<>(
                list,
                pageable.getPageNumber(),
                optionTypesPage.getTotalPages(),
                pageable.getPageSize(),
                optionTypesPage.getTotalElements()
        );
    }

    @Override
    @Transactional
    public OptionTypeResponse modifyOptionTypes(Long optionTypeId, OptionTypeRequest requestDto) {
        OptionTypes target = optionTypesRepository.findById(optionTypeId)
                .orElseThrow(() -> new NotFoundException("Not Found OptionType"));

        target.setName(requestDto.getName());
        try{
            optionTypesRepository.flush();
        } catch (DataIntegrityViolationException ex){
            throw new DuplicateResourceException("OptionTypes name Conflict");
        }
        return new OptionTypeResponse(target);
    }

    @Override
    @Transactional
    public void deleteOptionTypes(Long optionTypeId) {
        OptionTypes target = optionTypesRepository.findById(optionTypeId)
                .orElseThrow(() -> new NotFoundException("Not Found OptionType"));

        optionTypesRepository.delete(target);
    }

    @Override
    public List<OptionValuesResponseDto> getOptionValuesByTypeId(Long optionTypeId) {
        OptionTypes optionTypes = optionTypesRepository.findByIdWithOptionValues(optionTypeId)
                .orElseThrow(() -> new NotFoundException("Not Found OptionTypes"));

        List<OptionValues> optionValues = optionTypes.getOptionValues();

        return optionValues.stream().map(OptionValuesResponseDto::new)
                .toList();
    }
}
