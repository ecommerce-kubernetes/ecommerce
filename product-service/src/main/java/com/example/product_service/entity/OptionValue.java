package com.example.product_service.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String optionValue;

    protected void setOptionType(OptionType optionType){
        this.optionType = optionType;
    }

    public OptionValue(String optionValue){
        this.optionValue = optionValue;
    }
}
