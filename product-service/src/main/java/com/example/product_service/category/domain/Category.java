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
import java.util.Arrays;
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

        validateDepth(parent);

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

    public void moveParent(Category newParent) {
        validateNotSelfOrDescendant(newParent);
        if (newParent != null) {
            validateDepth(newParent);
        }

        detachFromCurrentParent();

        if (newParent == null) {
            this.depth = ROOT_DEPTH;
            this.path = String.valueOf(this.id);
            return;
        }

        assignParent(newParent);
        this.depth = newParent.getDepth() + 1;
        this.path = newParent.getPath() + "/" + this.id;
    }

    public void relocatePrefix(String oldPathPrefix, String newPathPrefix, int depthDelta) {
        Assert.isTrue(this.path.startsWith(oldPathPrefix),
                "path가 예상한 조상 경로로 시작하지 않는다: " + this.path);
        this.path = newPathPrefix + this.path.substring(oldPathPrefix.length());
        this.depth += depthDelta;
    }

    private void validateNotSelfOrDescendant(Category newParent) {
        if (newParent == null) {
            return;
        }
        if (newParent.getId().equals(this.id)) {
            throw new BusinessException(CategoryErrorCode.CANNOT_SET_SELF_AS_PARENT);
        }
        if (newParent.getPath().startsWith(this.path + "/")) {
            throw new BusinessException(CategoryErrorCode.CANNOT_SET_DESCENDANT);
        }
    }

    private static void validateDepth(Category parent) {
        if (parent.getDepth() == MAX_DEPTH) {
            throw new BusinessException(CategoryErrorCode.EXCEED_MAX_DEPTH);
        }
    }

    private void detachFromCurrentParent() {
        if (this.parent != null) {
            this.parent.getChildren().remove(this);
            this.parent = null;
        }
    }

    private void assignParent(Category parent) {
        this.parent = parent;
        parent.getChildren().add(this);
    }

    public List<Long> getAncestorIds() {
        return Arrays.stream(this.path.split("/"))
                .map(Long::valueOf)
                .toList();
    }

}
