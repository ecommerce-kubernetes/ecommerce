package com.example.product_service.api.option.service;

import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.OptionErrorCode;
import com.example.product_service.api.option.domain.OptionType;
import com.example.product_service.api.option.service.dto.OptionResponse;
import com.example.product_service.api.option.service.dto.OptionValueResponse;
import com.example.product_service.api.support.ExcludeInfraTest;
import com.example.product_service.repository.OptionTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
public class OptionServiceTest extends ExcludeInfraTest {

    @Autowired
    private OptionService optionService;
    @Autowired
    private OptionTypeRepository optionTypeRepository;

    @Nested
    @DisplayName("옵션 저장")
    class Create {

        @Test
        @DisplayName("옵션을 저장한다")
        void saveOption(){
            //given
            List<String> optionValues = List.of("XL", "L", "M", "S");
            //when
            OptionResponse result = optionService.saveOption("사이즈", optionValues);
            //then
            assertThat(result.getId()).isNotNull();
            assertThat(result.getName()).isEqualTo("사이즈");
            assertThat(result.getValues()).hasSize(4)
                    .extracting(OptionValueResponse::getName)
                    .containsExactly("XL", "L", "M", "S");
        }
    }

    @Nested
    @DisplayName("옵션 조회")
    class Read {
        @Test
        @DisplayName("옵션을 조회한다")
        void getOption(){
            //given
            OptionType optionType = OptionType.create("사이즈", List.of("XL", "L", "M", "S"));
            OptionType savedOptionType = optionTypeRepository.save(optionType);
            //when
            OptionResponse result = optionService.getOption(savedOptionType.getId());
            //then
            assertThat(result)
                    .extracting(OptionResponse::getId, OptionResponse::getName)
                    .containsExactly(savedOptionType.getId(), "사이즈");

            assertThat(result.getValues())
                    .extracting(OptionValueResponse::getName)
                    .containsExactlyInAnyOrder(
                            "XL", "L", "M", "S"
                    );
        }

        @Test
        @DisplayName("조회할 옵션을 찾을 수 없는 경우 예외를 던진다")
        void getOption_notFound(){
            //given
            //when
            //then
            assertThatThrownBy(() -> optionService.getOption(999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OptionErrorCode.OPTION_NOT_FOUND);
        }

        @Test
        @DisplayName("옵션 목록을 조회한다")
        void getOptions(){
            //given
            OptionType size = OptionType.create("사이즈", List.of("XL", "L", "M", "S"));
            OptionType storage = OptionType.create("용량", List.of("256GB", "128GB", "64GB"));
            optionTypeRepository.saveAll(List.of(size, storage));
            //when
            List<OptionResponse> result = optionService.getOptions();
            //then
            assertThat(result).hasSize(2);

            // 1. 사이즈 옵션 검증
            OptionResponse sizeResponse = result.stream()
                    .filter(r -> r.getName().equals("사이즈"))
                    .findFirst()
                    .orElseThrow();

            assertThat(sizeResponse.getId()).isEqualTo(size.getId());
            assertThat(sizeResponse.getValues())
                    .extracting(OptionValueResponse::getName)
                    .containsExactlyInAnyOrder("XL", "L", "M", "S");

            // 2. 용량 옵션 검증
            OptionResponse storageResponse = result.stream()
                    .filter(r -> r.getName().equals("용량"))
                    .findFirst()
                    .orElseThrow();

            assertThat(storageResponse.getId()).isEqualTo(storage.getId());
            assertThat(storageResponse.getValues())
                    .extracting(OptionValueResponse::getName)
                    .containsExactlyInAnyOrder("256GB", "128GB", "64GB");
        }
    }

    @Nested
    @DisplayName("옵션 수정")
    class Update {

        @Test
        @DisplayName("옵션을 수정한다")
        void updateOption(){
            //given
            OptionType optionType = OptionType.create("사이즈", List.of("XL", "L", "M", "S"));
            OptionType savedOptionType = optionTypeRepository.save(optionType);
            //when
            OptionResponse result = optionService.updateOption(savedOptionType.getId(), "용량", List.of("256GB", "128GB", "64GB"));
            //then
            assertThat(result.getId()).isEqualTo(savedOptionType.getId());
            assertThat(result.getName()).isEqualTo("용량");
            assertThat(result.getValues()).hasSize(3)
                    .extracting(OptionValueResponse::getName)
                    .containsExactly("256GB", "128GB", "64GB");
        }

        @Test
        @DisplayName("수정할 옵션을 찾을 수 없는 경우 예외를 던진다")
        void updateOption_notFound(){
            //given
            //when
            //then
            assertThatThrownBy(() -> optionService.updateOption(999L, "새 이름", List.of("value")))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OptionErrorCode.OPTION_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("옵션 삭제")
    class Delete {
        @Test
        @DisplayName("옵션을 삭제한다")
        void deleteOption(){
            //given
            OptionType optionType = OptionType.create("사이즈", List.of("XL", "L", "M", "S"));
            OptionType savedOptionType = optionTypeRepository.save(optionType);
            //when
            optionService.deleteOption(savedOptionType.getId());
            //then
            Optional<OptionType> find = optionTypeRepository.findById(savedOptionType.getId());
            assertThat(find).isEmpty();
        }

        @Test
        @DisplayName("삭제할 옵션을 찾을 수 없으면 예외를 던진다")
        void deleteOption_notFound(){
            //given
            //when
            //then
            assertThatThrownBy(() -> optionService.deleteOption(999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OptionErrorCode.OPTION_NOT_FOUND);
        }
    }
}
