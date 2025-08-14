package com.example.product_service.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OptionTypes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    @Setter
    private String name;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "optionType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OptionValues> optionValues = new ArrayList<>();

    public OptionTypes(String name){
        this.name = name;
    }

    public void addOptionValue(OptionValues optionValue){
        this.optionValues.add(optionValue);
        optionValue.setOptionType(this);
    }

    public void removeOptionValue(OptionValues optionValue){
        this.optionValues.remove(optionValue);
        optionValue.setValueName(null);
    }
}
