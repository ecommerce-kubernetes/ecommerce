package com.example.product_service.option.domain;

import com.example.product_service.option.domain.context.CreateOptionValueContext;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OptionValue {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_type_id")
    private OptionType optionType;

    private String name;

    @Builder(access = AccessLevel.PRIVATE)
    private OptionValue(Long id, OptionType optionType, String name) {
        Assert.notNull(id, "옵션 값 아이디는 필수이다");
        Assert.notNull(optionType, "옵션 값 타입은 필수이다");
        Assert.hasText(name, "옵션 값 이름은 필수이다");

        this.id = id;
        this.optionType = optionType;
        this.name = name;
    }

    public static OptionValue create(CreateOptionValueContext context, OptionType optionType) {
        return OptionValue.builder()
                .id(context.id())
                .name(context.name())
                .optionType(optionType)
                .build();
    }

    void update(String newName) {
        this.name = newName;
    }

    void detachOptionType() {
        this.optionType = null;
    }
}
