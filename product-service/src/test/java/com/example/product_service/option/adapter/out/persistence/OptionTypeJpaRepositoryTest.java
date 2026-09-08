package com.example.product_service.option.adapter.out.persistence;

import com.example.product_service.option.domain.OptionType;
import com.example.product_service.option.domain.context.CreateOptionTypeContext;
import com.example.product_service.option.domain.context.CreateOptionValueContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OptionTypeJpaRepositoryTest {

    @Autowired
    private OptionTypeJpaRepository optionTypeJpaRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("동일한 이름이 존재하면 true를 반환한다")
    void existsByName_whenExists_thenReturnsTrue() {
        //given
        OptionType optionType = anOptionType(1L, "색상");
        entityManager.persist(optionType);
        entityManager.flush();
        //when
        boolean exists = optionTypeJpaRepository.existsByName("색상");
        //then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("동일한 이름이 없으면 false를 반환한다")
    void existsByName_whenNotExists_thenReturnsFalse() {
        //given
        OptionType optionType = anOptionType(1L, "색상");
        entityManager.persist(optionType);
        entityManager.flush();
        //when
        boolean exists = optionTypeJpaRepository.existsByName("사이즈");
        //then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("자기 자신을 제외하고 동일한 이름이 존재하면 true를 반환한다")
    void existsByNameAndIdNot_whenDuplicateExcludingSelf_thenReturnsTrue() {
        //given
        OptionType color1 = anOptionType(1L, "색상");
        OptionType color2 = anOptionType(2L, "색상");
        entityManager.persist(color1);
        entityManager.persist(color2);
        entityManager.flush();
        //when
        boolean exists = optionTypeJpaRepository.existsByNameAndIdNot("색상", color2.getId());
        //then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("자기 자신만 동일한 이름이면 false를 반환한다")
    void existsByNameAndIdNot_whenOnlySelfMatches_thenReturnsFalse() {
        //given
        OptionType optionType = anOptionType(1L, "색상");
        entityManager.persist(optionType);
        entityManager.flush();
        //when
        boolean exists = optionTypeJpaRepository.existsByNameAndIdNot("색상", optionType.getId());
        //then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("저장된 옵션 값이 함께 조회된다")
    void save_persistsOptionValuesByCascade() {
        //given
        CreateOptionTypeContext context = CreateOptionTypeContext.of(
                1L,
                "색상",
                List.of(CreateOptionValueContext.of(2L, "BLUE"))
        );
        OptionType optionType = OptionType.create(context);
        //when
        entityManager.persist(optionType);
        entityManager.flush();
        entityManager.clear();

        OptionType found = optionTypeJpaRepository.findById(optionType.getId()).orElseThrow();
        //then
        assertThat(found.getOptionValues()).extracting("name").containsExactly("BLUE");
    }

    private OptionType anOptionType(Long id, String name) {
        CreateOptionTypeContext context = CreateOptionTypeContext.of(id, name, List.of());
        return OptionType.create(context);
    }
}
