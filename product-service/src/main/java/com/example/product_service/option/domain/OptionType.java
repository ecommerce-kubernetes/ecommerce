package com.example.product_service.option.domain;

import com.example.product_service.common.exception.BusinessException;
import com.example.product_service.option.exception.OptionErrorCode;
import com.example.product_service.option.domain.context.CreateOptionTypeContext;
import com.example.product_service.option.domain.context.CreateOptionValueContext;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
        Assert.hasText(name, "옵션 타입 이름은 필수이다");

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

    private static void validateDuplicateValueName(List<CreateOptionValueContext> valueContexts) {
        HashSet<String> nameSet = new HashSet<>();

        boolean isDuplicateName = valueContexts.stream().anyMatch(context -> !nameSet.add(context.name()));

        if (isDuplicateName) {
            throw new BusinessException(OptionErrorCode.OPTION_VALUE_DUPLICATE_NAME);
        }
    }

    public void update(String name) {
        this.name = name;
    }

    public OptionValue addOptionValue(CreateOptionValueContext context) {
        validateDuplicateNameInState(context.id(), context.name());

        OptionValue optionValue = OptionValue.create(context, this);
        addOptionValue(optionValue);
        return optionValue;
    }

    public void updateOptionValue(Long optionValueId, String newName) {
        OptionValue findValue = getOptionValueById(optionValueId);

        validateDuplicateNameInState(optionValueId, newName);

        findValue.update(newName);
    }

    public void deleteOptionValue(Long optionValueId) {
        OptionValue findValue = getOptionValueById(optionValueId);

        findValue.detachOptionType();
        this.optionValues.remove(findValue);
    }

    private void addOptionValue(OptionValue optionValue) {
        this.optionValues.add(optionValue);
    }

    private void addOptionValues(List<OptionValue> optionValues) {
        this.optionValues.addAll(optionValues);
    }

    private OptionValue getOptionValueById(Long optionValueId) {
        return this.optionValues.stream().filter(optionValue ->
                optionValue.getId().equals(optionValueId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(OptionErrorCode.OPTION_VALUE_NOT_FOUND));
    }

    private void validateDuplicateNameInState(Long optionValueId, String name) {
        boolean isDuplicate = this.optionValues.stream()
                .anyMatch(value -> !value.getId().equals(optionValueId) && value.getName().equals(name));

        if (isDuplicate) {
            throw new BusinessException(OptionErrorCode.OPTION_VALUE_DUPLICATE_NAME);
        }
    }
}
