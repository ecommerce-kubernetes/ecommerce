package com.example.product_service.option.domain;

import com.example.product_service.common.exception.BusinessException;
import com.example.product_service.common.exception.OptionErrorCode;
import com.example.product_service.option.domain.context.CreateOptionTypeContext;
import com.example.product_service.option.domain.context.CreateOptionValueContext;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OptionType {

    @Id
    private Long id;

    private String name;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "optionType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OptionValue> optionValues = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private OptionType(Long id, String name){
        Assert.notNull(id, "옵션 타입 아이디는 필수이다");
        Assert.notNull(name, "옵션 타입 이름은 필수이다");

        this.id = id;
        this.name = name;
    }

    public static OptionType create(CreateOptionTypeContext context) {
        validateDuplicateValueName(context.valueContexts());

        OptionType optionType = OptionType.builder()
                .id(context.id())
                .name(context.name())
                .build();

        List<OptionValue> optionValues = context.valueContexts().stream()
                .map(valueContext -> OptionValue.create(valueContext, optionType)).toList();

        optionType.addOptionValues(optionValues);
        return optionType;
    }

    private void addOptionValues(List<OptionValue> optionValues) {
        this.optionValues.addAll(optionValues);
    }

    private static void validateDuplicateValueName(List<CreateOptionValueContext> valueContexts) {
        HashSet<String> nameSet = new HashSet<>();

        boolean isDuplicateName = valueContexts.stream().anyMatch(context -> !nameSet.add(context.name()));

        if (isDuplicateName) {
            throw new BusinessException(OptionErrorCode.OPTION_VALUE_DUPLICATE_NAME);
        }
    }
}
