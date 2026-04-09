package com.example.product_service.api.option.service;

import com.example.product_service.api.category.domain.model.Category;
import com.example.product_service.api.category.domain.repository.CategoryRepository;
import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.OptionErrorCode;
import com.example.product_service.api.option.domain.model.OptionType;
import com.example.product_service.api.option.domain.model.OptionValue;
import com.example.product_service.api.option.domain.repository.OptionTypeRepository;
import com.example.product_service.api.option.service.dto.command.OptionCommand;
import com.example.product_service.api.option.service.dto.result.OptionResult;
import com.example.product_service.api.option.service.dto.result.OptionValueResult;
import com.example.product_service.api.product.domain.model.Product;
import com.example.product_service.api.product.domain.model.ProductVariant;
import com.example.product_service.api.product.domain.repository.ProductRepository;
import com.example.product_service.support.ExcludeInfraTest;
import jakarta.persistence.EntityManager;
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
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private EntityManager em;

    private OptionType saveTestOption(String name, List<String> values) {
        OptionType optionType = OptionType.create(name, values);
        return optionTypeRepository.save(optionType);
    }

    private OptionValue findOptionValue(OptionType optionType, String name) {
        return optionType.getOptionValues().stream().filter(optionValue -> optionValue.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }

    private void settingProductOption(OptionType optionType, OptionValue optionValue) {
        Category category = categoryRepository.save(Category.create("카테고리", null, "/test/image.jpg"));
        ProductVariant variant = ProductVariant.create("TEST", 1000L, 100, 10);
        variant.addProductVariantOptions(List.of(optionValue));
        Product product = Product.create("상품", "상품 설명", category);
        product.updateOptions(List.of(optionType));
        product.addVariant(variant);
        productRepository.save(product);
    }

    @Nested
    @DisplayName("옵션 저장")
    class Create {

        @Test
        @DisplayName("옵션을 저장한다")
        void saveOption(){
            //given
            OptionCommand.Create command = OptionCommand.Create.builder()
                    .name("사이즈")
                    .valueNames(List.of("XL", "L", "M", "S"))
                    .build();
            //when
            OptionResult result = optionService.saveOption(command);
            //then
            assertThat(result.getId()).isNotNull();
            assertThat(result.getName()).isEqualTo("사이즈");
            assertThat(result.getValues()).hasSize(4)
                    .extracting(OptionValueResult::getName)
                    .containsExactly("XL", "L", "M", "S");
        }

        @Test
        @DisplayName("동일한 이름을 가진 옵션 타입은 생성할 수 없다")
        void saveOption_duplicate_name(){
            //given
            OptionCommand.Create command = OptionCommand.Create.builder()
                    .name("사이즈")
                    .valueNames(List.of("XL", "L", "M", "S"))
                    .build();
            saveTestOption("사이즈", List.of("XL", "L"));
            //when
            //then
            assertThatThrownBy(() -> optionService.saveOption(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OptionErrorCode.DUPLICATE_NAME);
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
            OptionResult result = optionService.getOption(savedOptionType.getId());
            //then
            assertThat(result)
                    .extracting(OptionResult::getId, OptionResult::getName)
                    .containsExactly(savedOptionType.getId(), "사이즈");

            assertThat(result.getValues())
                    .extracting(OptionValueResult::getName)
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
            List<OptionResult> result = optionService.getOptions();
            //then
            assertThat(result).hasSize(2);

            // 1. 사이즈 옵션 검증
            OptionResult sizeResponse = result.stream()
                    .filter(r -> r.getName().equals("사이즈"))
                    .findFirst()
                    .orElseThrow();

            assertThat(sizeResponse.getId()).isEqualTo(size.getId());
            assertThat(sizeResponse.getValues())
                    .extracting(OptionValueResult::getName)
                    .containsExactlyInAnyOrder("XL", "L", "M", "S");

            // 2. 용량 옵션 검증
            OptionResult storageResponse = result.stream()
                    .filter(r -> r.getName().equals("용량"))
                    .findFirst()
                    .orElseThrow();

            assertThat(storageResponse.getId()).isEqualTo(storage.getId());
            assertThat(storageResponse.getValues())
                    .extracting(OptionValueResult::getName)
                    .containsExactlyInAnyOrder("256GB", "128GB", "64GB");
        }
    }

    @Nested
    @DisplayName("옵션 수정")
    class Update {

        @Test
        @DisplayName("옵션 이름을 수정한다")
        void updateOptionTypeName(){
            //given
            OptionType optionType = saveTestOption("사이즈", List.of("XL", "L", "M", "S"));
            OptionCommand.UpdateOptionType command = OptionCommand.UpdateOptionType.builder()
                    .id(optionType.getId())
                    .name("용량")
                    .build();
            //when
            OptionResult result = optionService.updateOptionTypeName(command);
            //then
            assertThat(result.getId()).isEqualTo(optionType.getId());
            assertThat(result.getName()).isEqualTo("용량");
            assertThat(result.getValues()).hasSize(4)
                    .extracting(OptionValueResult::getName)
                    .containsExactly("XL", "L", "M", "S");
        }

        @Test
        @DisplayName("수정할 옵션을 찾을 수 없으면 옵션 이름을 수정할 수 없다")
        void updateOptionTypeName_notFound(){
            //given
            OptionCommand.UpdateOptionType command = OptionCommand.UpdateOptionType.builder()
                    .id(999L)
                    .name("새 이름")
                    .build();
            //when
            //then
            assertThatThrownBy(() -> optionService.updateOptionTypeName(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OptionErrorCode.OPTION_NOT_FOUND);
        }

        @Test
        @DisplayName("동일한 이름의 옵션이 있는 경우 옵션 이름을 수정할 수 없다")
        void updateOptionTypeName_duplicate_name(){
            //given
            OptionType optionType = saveTestOption("사이즈", List.of("XL", "L", "M"));
            saveTestOption("용량", List.of("256GB"));
            OptionCommand.UpdateOptionType command = OptionCommand.UpdateOptionType.builder()
                    .id(optionType.getId())
                    .name("용량")
                    .build();
            //when
            //then
            assertThatThrownBy(() -> optionService.updateOptionTypeName(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OptionErrorCode.DUPLICATE_NAME);
        }

        @Test
        @DisplayName("옵션 값을 찾을 수 없는 경우 옵션 값 이름을 수정할 수 없다")
        void updateOptionValueName_not_found_option_value(){
            //given
            OptionCommand.UpdateOptionValue command = OptionCommand.UpdateOptionValue.builder()
                    .id(999L)
                    .name("새 이름")
                    .build();
            //when
            //then
            assertThatThrownBy(() -> optionService.updateOptionValueName(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OptionErrorCode.OPTION_VALUE_NOT_FOUND);
        }

        @Test
        @DisplayName("옵션에 동일한 옵션 값이 있는 경우 옵션 값 이름을 수정할 수 없다")
        void updateOptionValueName_duplicate_name(){
            //given
            OptionType size = saveTestOption("사이즈", List.of("XL", "L"));
            OptionValue xl = findOptionValue(size, "XL");
            OptionCommand.UpdateOptionValue command = OptionCommand.UpdateOptionValue.builder()
                    .id(xl.getId())
                    .name("L")
                    .build();
            //when
            //then
            assertThatThrownBy(() -> optionService.updateOptionValueName(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OptionErrorCode.OPTION_VALUE_DUPLICATE_NAME);
        }
    }

    @Nested
    @DisplayName("옵션 삭제")
    class Delete {

        @Test
        @DisplayName("옵션을 삭제한다")
        void deleteOption(){
            //given
            OptionType optionType = saveTestOption("사이즈", List.of("XL", "L"));
            //when
            optionService.deleteOption(optionType.getId());
            //then
            Optional<OptionType> find = optionTypeRepository.findById(optionType.getId());
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

        @Test
        @DisplayName("삭제할 옵션이 설정된 상품이 있는 경우 삭제할 수 없다")
        void deleteOption_option_used_by_product(){
            //given
            OptionType size = saveTestOption("사이즈", List.of("XL", "L"));
            OptionValue xl = findOptionValue(size, "XL");
            settingProductOption(size, xl);
            Long targetId = size.getId();
            //when
            //then
            assertThatThrownBy(() -> optionService.deleteOption(targetId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OptionErrorCode.OPTION_IN_PRODUCT_OPTION);
        }

        @Test
        @DisplayName("옵션 값을 삭제한다")
        void deleteOptionValue() {
            //given
            OptionType optionType = saveTestOption("사이즈", List.of("XL", "L"));
            OptionValue xl = findOptionValue(optionType, "XL");
            OptionValue l = findOptionValue(optionType, "L");
            em.flush();
            em.clear();
            //when
            optionService.deleteOptionValue(xl.getId());
            em.flush();
            em.clear();
            //then
            OptionType findOption = optionTypeRepository.findById(optionType.getId()).orElseThrow();
            assertThat(findOption.getOptionValues()).hasSize(1)
                    .extracting(OptionValue::getName)
                    .containsExactly(l.getName());
        }

        @Test
        @DisplayName("옵션 값이 상품 변형에 사용중이면 삭제할 수 없다")
        void deleteOptionValue_used_product_variant_option() {
            //given
            OptionType optionType = saveTestOption("사이즈", List.of("XL", "L"));
            OptionValue xl = findOptionValue(optionType, "XL");
            settingProductOption(optionType, xl);
            Long targetId = xl.getId();
            //when
            //then
            assertThatThrownBy(() -> optionService.deleteOptionValue(targetId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OptionErrorCode.OPTION_VALUE_IN_VARIANT);
        }
    }
}
