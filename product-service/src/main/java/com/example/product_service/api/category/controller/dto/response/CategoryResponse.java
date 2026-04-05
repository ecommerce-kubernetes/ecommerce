package com.example.product_service.api.category.controller.dto.response;

import com.example.product_service.api.category.service.dto.result.CategoryNavigationResult;
import com.example.product_service.api.category.service.dto.result.CategoryResult;
import com.example.product_service.api.category.service.dto.result.CategoryTreeResult;
import lombok.Builder;

import java.util.List;

public class CategoryResponse {

    @Builder
    public record Detail (
            Long id,
            String name,
            Long parentId,
            Integer depth,
            String imagePath
    ) {
        public static Detail from(CategoryResult result) {
            return Detail.builder()
                    .id(result.getId())
                    .name(result.getName())
                    .parentId(result.getParentId())
                    .depth(result.getDepth())
                    .imagePath(result.getImagePath())
                    .build();
        }

        public static Detail from(CategoryResult.Detail result) {
            return Detail.builder()
                    .id(result.id())
                    .name(result.name())
                    .parentId(result.parentId())
                    .depth(result.depth())
                    .imagePath(result.imagePath())
                    .build();
        }
    }

    @Builder
    public record Tree (
            Long id,
            String name,
            Long parentId,
            Integer depth,
            String imagePath,
            List<Tree> children
    ) {
        public static Tree from (CategoryTreeResult result) {
            return Tree.builder()
                    .id(result.getId())
                    .name(result.getName())
                    .parentId(result.getParentId())
                    .depth(result.getDepth())
                    .imagePath(result.getImagePath())
                    .children(result.getChildren().stream().map(Tree::from).toList())
                    .build();
        }
    }

    @Builder
    public record Navigation (
            Detail current,
            List<Detail> path,
            List<Detail> siblings,
            List<Detail> children
    ) {
        public static Navigation from(CategoryNavigationResult result) {
            return Navigation.builder()
                    .current(Detail.from(result.getCurrent()))
                    .path(result.getPath().stream().map(Detail::from).toList())
                    .siblings(result.getSiblings().stream().map(Detail::from).toList())
                    .children(result.getChildren().stream().map(Detail::from).toList())
                    .build();
        }
    }
}
