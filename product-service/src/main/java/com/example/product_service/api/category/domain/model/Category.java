package com.example.product_service.api.category.domain.model;

import com.example.product_service.api.common.entity.BaseEntity;
import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.CommonErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Category extends BaseEntity {

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

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> children = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    public Category(String name, int depth, String path, String imageUrl){
        this.name = name;
        this.depth = depth;
        this.path = path;
        this.imageUrl = imageUrl;
    }

    public static Category create(String name, Category parent, String imageUrl) {
        int depth = generateDepth(parent);
        Category category = create(name, depth, imageUrl);
        if (parent != null) {
            category.linkParent(parent);
        }
        return category;
    }

    private void linkParent(Category parent) {
        this.parent = parent;
        if (!parent.getChildren().contains(this)){
            parent.getChildren().add(this);
        }
    }

    public boolean isLeaf(){
        return this.children.isEmpty();
    }

    public void generatePath() {
        // id 가 생성되지 않은 상태이면 예외를 던짐
        if (this.id == null) {
            throw new BusinessException(CommonErrorCode.SYSTEM_ERROR);
        }
        // 부모가 존재하지 않으면 자신의 id 가 path, 부모가 존재하면 부모의 path/id 가 path
        if (this.parent == null) {
            this.path = String.valueOf(this.id);
        } else {
            this.path = parent.getPath() + "/" + this.id;
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
}
