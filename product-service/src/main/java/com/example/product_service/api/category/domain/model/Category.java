package com.example.product_service.api.category.domain.model;

import com.example.product_service.api.common.entity.BaseEntity;
import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.CategoryErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Category extends BaseEntity {

    private final static int MAX_DEPTH = 5;
    private final static int ROOT_DEPTH = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Integer depth;
    private String path;
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Category> children = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    public Category(String name, int depth, String path, String imageUrl){
        this.name = name;
        this.depth = depth;
        this.path = path;
        this.imageUrl = imageUrl;
    }

    public static Category create(String name, Category parent, String imageUrl) {
        if (parent == null) {
            return create(name, ROOT_DEPTH, imageUrl);
        }
        //부모 카테고리의 depth 가 최대인지 검증
        parent.validateCanAddChild();
        Category category = create(name.trim(), parent.getNextDepth(), imageUrl.trim());
        category.linkParent(parent);
        return category;
    }

    public void generatePath() {
        // id 가 생성되지 않은 상태이면 예외를 던짐
        if (this.id == null) {
            throw new BusinessException(CategoryErrorCode.CATEGORY_ID_IS_NULL);
        }
        // 부모가 존재하지 않으면 자신의 id 가 path, 부모가 존재하면 부모의 path/id 가 path
        if (this.parent == null) {
            this.path = String.valueOf(this.id);
            return;
        }
        this.path = parent.getPath() + "/" + this.id;
    }

    public void rename(String newName) {
        this.name = newName.trim();
    }

    public void changeImage(String newImage) {
        this.imageUrl = newImage.trim();
    }

    public boolean isRoot() {
        return this.parent == null;
    }

    public void moveParent(Category parent){
        // 부모 카테고리의 depth 가 최대라면 예외
        if (parent != null) {
            parent.validateCanAddChild();
        }

        if (this.parent != null) {
            this.parent.getChildren().remove(this);
        }
        this.parent = parent;
        if (parent != null) {
            parent.getChildren().add(this);
        }
        this.depth = parent.getNextDepth();
        generatePath();
        updateChildrenPath(this.children);
    }

    public List<Long> getPathIds() {
        return Arrays.stream(this.path.split("/"))
                .map(Long::parseLong)
                .toList();
    }

    public boolean isLeaf() {
        return this.children.isEmpty();
    }

    private void linkParent(Category parent) {
        this.parent = parent;
        if (!parent.getChildren().contains(this)){
            parent.getChildren().add(this);
        }
    }

    private void updateChildrenPath(List<Category> children) {
        if (children == null || children.isEmpty()) {
            return;
        }

        for (Category child : children) {
            child.depth = generateDepth(child.getParent());
            child.generatePath();

            updateChildrenPath(child.getChildren());
        }
    }

    // 부모가 존재하지 않으면 depth=1 , 부모가 존재하면 부모의 depth + 1
    private static int generateDepth(Category parent) {
        return (parent == null) ? 1 : parent.getDepth() + 1;
    }

    private static Category create(String name, int depth, String imageUrl){
        return Category.builder()
                .name(name)
                .depth(depth)
                .path(null)
                .imageUrl(imageUrl)
                .build();
    }

    private void validateCanAddChild() {
        if (this.depth >= MAX_DEPTH) {
            throw new BusinessException(CategoryErrorCode.EXCEED_MAX_DEPTH);
        }
    }

    private int getNextDepth() {
        return this.depth + 1;
    }
}
