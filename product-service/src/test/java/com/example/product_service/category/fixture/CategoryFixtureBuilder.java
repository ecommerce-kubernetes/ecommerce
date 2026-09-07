package com.example.product_service.category.fixture;

import com.example.product_service.category.domain.Category;
import com.example.product_service.common.util.IdGenerator;

import java.util.concurrent.atomic.AtomicLong;


public class CategoryFixtureBuilder {

    private static final AtomicLong idSeq = new AtomicLong(100L);
    private static final IdGenerator ID_GENERATOR = idSeq::getAndIncrement;

    private String name = "카테고리";
    private String imagePath = "/category/image.jpg";

    public static CategoryFixtureBuilder given() {
        return new CategoryFixtureBuilder();
    }

    public CategoryFixtureBuilder name(String name) {
        this.name = name;
        return this;
    }

    public Category build() {
        return Category.createRoot(ID_GENERATOR.generate(), name, imagePath);
    }

    public Category buildMaxDepth() {
        Category currentParent = Category.createRoot(ID_GENERATOR.generate(), "더미 뎁스1", "/dummy.jpg");
        currentParent = Category.createChild(ID_GENERATOR.generate(), "더미 뎁스2", "/dummy.jpg", currentParent);
        currentParent = Category.createChild(ID_GENERATOR.generate(), "더미 뎁스3", "/dummy.jpg", currentParent);
        currentParent = Category.createChild(ID_GENERATOR.generate(), "더미 뎁스4", "/dummy.jpg", currentParent);

        return Category.createChild(ID_GENERATOR.generate(), this.name, this.imagePath, currentParent);
    }
}
