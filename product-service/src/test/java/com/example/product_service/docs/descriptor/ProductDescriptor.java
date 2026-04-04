package com.example.product_service.docs.descriptor;

import org.springframework.restdocs.payload.FieldDescriptor;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class ProductDescriptor {

    public static FieldDescriptor[] getCreateRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("name").description("상품 이름"),
                fieldWithPath("categoryId").description("카테고리 ID"),
                fieldWithPath("description").description("상품 설명").optional()
        };
    }

    public static FieldDescriptor[] getRegisterOptionRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("options[].optionTypeId").description("옵션 타입 Id 리스트"),
                fieldWithPath("options[].priority").description("옵션 타입 우선순위")
        };
    }

    public static FieldDescriptor[] getCreateResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("productId").description("상품 Id")
        };
    }

    public static FieldDescriptor[] getRegisterOptionResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("productId").description("상품 Id"),
                fieldWithPath("options[].optionTypeId").description("옵션 타입 Id"),
                fieldWithPath("options[].optionTypeName").description("옵션 타입 이름"),
                fieldWithPath("options[].priority").description("상품 옵션 순서")
        };
    }

    public static FieldDescriptor[] getAddVariantRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("variants[].originalPrice").description("상품 변형 가격"),
                fieldWithPath("variants[].discountRate").description("할인율"),
                fieldWithPath("variants[].stockQuantity").description("재고 수량"),
                fieldWithPath("variants[].optionValueIds").description("옵션 값 Id 리스트")
        };
    }

    public static FieldDescriptor[] getAddVariantResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("productId").description("상품 Id"),
                fieldWithPath("variants[].variantId").description("상품 변형 Id"),
                fieldWithPath("variants[].sku").description("상품 SKU"),
                fieldWithPath("variants[].optionValueIds").description("상품 변형 옵션 값 ID 리스트"),
                fieldWithPath("variants[].originalPrice").description("상품 변형 원본 가격"),
                fieldWithPath("variants[].discountedPrice").description("상품 변형 할인 가격"),
                fieldWithPath("variants[].discountRate").description("상품 변형 할인율"),
                fieldWithPath("variants[].stockQuantity").description("상품 변형 재고 수량")
        };
    }

    public static FieldDescriptor[] getAddImageRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("images[].imagePath").description("이미지 경로"),
                fieldWithPath("images[].isThumbnail").description("썸네일 여부"),
                fieldWithPath("images[].sortOrder").description("정렬 순서")
        };
    }

    public static FieldDescriptor[] getAddImageResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("productId").description("상품 Id"),
                fieldWithPath("images[].imageId").description("상품 이미지 ID"),
                fieldWithPath("images[].imagePath").description("상품 이미지 URL"),
                fieldWithPath("images[].sortOrder").description("상품 이미지 순서"),
                fieldWithPath("images[].isThumbnail").description("썸네일 여부")
        };
    }

    public static FieldDescriptor[] getAddDescriptionImageRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("images[].imagePath").description("이미지 경로"),
                fieldWithPath("images[].sortOrder").description("정렬 순서")
        };
    }

    public static FieldDescriptor[] getAddDescriptionImageResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("productId").description("상품 Id"),
                fieldWithPath("descriptionImages[].imageUrl").description("상품 이미지 URL"),
                fieldWithPath("descriptionImages[].sortOrder").description("상품 이미지 순서")
        };
    }
}
