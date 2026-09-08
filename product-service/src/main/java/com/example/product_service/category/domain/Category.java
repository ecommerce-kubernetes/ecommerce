package com.example.product_service.category.domain;

import com.example.product_service.common.entity.BaseEntity;
import com.example.product_service.common.exception.BusinessException;
import com.example.product_service.category.exception.CategoryErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {

    private final static int MAX_DEPTH = 5;
    private final static int ROOT_DEPTH = 1;

    @Id
    private Long id;
    private String name;
    private Integer depth;
    private String path;
    private String imagePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Category> children = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Category(Long id, String name, int depth, String path, String imagePath) {
        Assert.notNull(id, "카테고리 아이디는 필수이다.");
        Assert.hasText(name, "카테고리 이름은 필수이다.");
        Assert.hasText(path, "카테고리 경로는 필수이다.");
        this.id = id;
        this.name = name;
        this.depth = depth;
        this.path = path;
        this.imagePath = imagePath;
    }

    public static Category createRoot(Long id, String name, String imagePath) {
        return Category.builder()
                .id(id)
                .name(name)
                .depth(ROOT_DEPTH)
                .path(String.valueOf(id))
                .imagePath(imagePath)
                .build();
    }

    public static Category createChild(Long id, String name, String imagePath, Category parent) {
        Assert.notNull(parent, "하위 카테고리 생성시 부모는 필수이다");

        if (parent.getDepth() == MAX_DEPTH) {
            throw new BusinessException(CategoryErrorCode.EXCEED_MAX_DEPTH);
        }

        Category child = Category.builder()
                .id(id)
                .name(name)
                .depth(parent.getDepth() + 1)
                .path(parent.getPath() + "/" + id)
                .imagePath(imagePath)
                .build();

        child.assignParent(parent);
        return child;
    }

    public void update(String name, String imagePath) {
        this.name = name;
        this.imagePath = imagePath;
    }

    public boolean isRoot() {
        return this.parent == null;
    }

    public boolean isLeaf() {
        return this.children.isEmpty();
    }

    public void moveParent(Category parent) {
        if (parent == null) {
            this.parent.getChildren().remove(this);
            this.parent = null;
            return;
        }

        if (parent.getDepth() == MAX_DEPTH) {
            throw new BusinessException(CategoryErrorCode.EXCEED_MAX_DEPTH);
        }

        if (parent.getPath().startsWith(this.getPath()+"/")){
            throw new BusinessException(CategoryErrorCode.CANNOT_SET_SELF_AS_PARENT);
        }
    }

    private void assignParent(Category parent) {
        this.parent = parent;
        parent.getChildren().add(this);
    }
}
