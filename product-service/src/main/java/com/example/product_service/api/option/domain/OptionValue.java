package com.example.product_service.api.option.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OptionValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_type_id")
    private OptionType optionType;

    @Setter
    private String name;

    void setOptionType(OptionType optionType){
        this.optionType = optionType;
    }

    @Builder(access = AccessLevel.PRIVATE)
    private OptionValue(String name){
        this.name = name;
    }

    public static OptionValue create(String name) {
        return OptionValue.builder().name(name).build();
    }
}
