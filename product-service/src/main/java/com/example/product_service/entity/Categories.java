package com.example.product_service.entity;

import com.example.product_service.entity.base.BaseEntity;
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
public class Categories extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private String name;
    @Setter
    private String iconUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Categories parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Categories> children = new ArrayList<>();

    public Categories(String name, String iconUrl){
        this.name = name;
        this.iconUrl = iconUrl;
    }

    public void addChild(Categories child){
        children.add(child);
        child.parent = this;
    }

    public void removeChild(Categories child){
        children.remove(child);
        child.parent = null;
    }

    public void modifyParent(Categories newParent){
        if(newParent != null && newParent.equals(this)) {
            throw new IllegalArgumentException("An item cannot be set as its own parent");
        }
        if (this.parent != null){
            this.parent.getChildren().remove(this);
        }
        this.parent = newParent;

        if(newParent != null){
            newParent.getChildren().add(this);
        }
    }

    public boolean isLeaf(){
        return this.children.isEmpty();
    }
}
