package com.example.product_service.api.option.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OptionType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    @Setter
    private String name;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "optionType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OptionValue> optionValues = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private OptionType(String name){
        this.name = name;
    }

    public static OptionType create(String name, List<String> valueNames) {
        OptionType optionType = OptionType.builder().name(name).build();
        valueNames.forEach(optionType::addOptionValue);
        return optionType;
    }

    public void update(String name, List<String> values) {
        this.name = name;
        this.optionValues.removeIf(exist -> !values.contains(exist.getName()));

        for (String newValue : values) {
            boolean isExist = this.optionValues.stream()
                    .anyMatch(exist -> exist.getName().equals(newValue));
            if (!isExist) {
                this.addOptionValue(newValue);
            }
        }
    }

    public void addOptionValue(String name) {
        OptionValue optionValue = OptionValue.create(name);
        this.optionValues.add(optionValue);
        optionValue.setOptionType(this);
    }
}
