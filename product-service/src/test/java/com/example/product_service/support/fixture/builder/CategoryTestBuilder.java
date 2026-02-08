package com.example.product_service.support.fixture.builder;

import com.example.product_service.api.category.domain.model.Category;
import org.springframework.test.util.ReflectionTestUtils;

public class CategoryTestBuilder {
    private Long id = 1L;
    private String name = "카테고리";
    private Integer depth = 1;
    private String imageUrl = "http://image.jpg";
    private Category parent = null;

    public static CategoryTestBuilder aCategory() {
        return new CategoryTestBuilder();
    }

    public CategoryTestBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public CategoryTestBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public CategoryTestBuilder withDepth(Integer depth) {
        this.depth = depth;
        return this;
    }

    public CategoryTestBuilder withImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public CategoryTestBuilder withParent(Category parent) {
        this.parent = parent;
        return this;
    }

    public Category build() {
        Category category = Category.create(name, parent, imageUrl);
        if (id != null) {
            ReflectionTestUtils.setField(category, "id", id);
            category.generatePath();
        }
        if (depth != null) {
            ReflectionTestUtils.setField(category, "depth", depth);
        }
        return category;
    }
}
