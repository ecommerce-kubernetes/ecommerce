package com.example.product_service.service;

import com.example.product_service.dto.request.options.OptionTypeRequest;
import com.example.product_service.dto.response.options.OptionTypesResponseDto;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.options.OptionValuesResponseDto;
import com.example.product_service.entity.OptionTypes;
import com.example.product_service.entity.OptionValues;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.OptionTypesRepository;
import com.example.product_service.repository.OptionValuesRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Slf4j
class OptionTypeServiceImplTest {
}