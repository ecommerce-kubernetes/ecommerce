package com.example.product_service.docs.descriptor;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.request.ParameterDescriptor;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

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
                fieldWithPath("optionTypeIds").description("옵션 타입 Id 리스트")
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
                fieldWithPath("images").description("이미지 경로")
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
                fieldWithPath("images").description("이미지 경로")
        };
    }

    public static FieldDescriptor[] getAddDescriptionImageResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("productId").description("상품 Id"),
                fieldWithPath("descriptionImages[].imageId").description("상품 설명 이미지 Id"),
                fieldWithPath("descriptionImages[].imagePath").description("상품 이미지 URL"),
                fieldWithPath("descriptionImages[].sortOrder").description("상품 이미지 순서")
        };
    }

    public static FieldDescriptor[] getPublishResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("productId").description("상품 Id"),
                fieldWithPath("status").description("상품 상태"),
                fieldWithPath("publishedAt").description("게시일")
        };
    }

    public static FieldDescriptor[] getCloseResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("productId").description("상품 Id"),
                fieldWithPath("status").description("상품 상태"),
                fieldWithPath("saleStoppedAt").description("판매 중지일")
        };
    }

    public static FieldDescriptor[] getUpdateRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("name").description("변경할 상품 이름"),
                fieldWithPath("categoryId").description("변경할 카테고리 Id"),
                fieldWithPath("description").description("변경할 상품 설명")
        };
    }

    public static FieldDescriptor[] getUpdateResponse() {
        return new FieldDescriptor[]{
                fieldWithPath("productId").description("상품 ID"),
                fieldWithPath("name").description("상품 이름"),
                fieldWithPath("categoryId").description("카테고리 Id"),
                fieldWithPath("description").description("상품 설명")
        };
    }

    public static ParameterDescriptor[] getSearchParams() {
        return new ParameterDescriptor[] {
                parameterWithName("page").description("페이지 번호 (기본값: 1)").optional(),
                parameterWithName("size").description("페이지 크기 (기본값: 20, 최대: 100)").optional(),
                parameterWithName("sort").description("정렬 기준 (latest: 최신순 등)").optional(),
                parameterWithName("categoryId").description("카테고리 ID").optional(),
                parameterWithName("name").description("상품명 검색 키워드").optional(),
                parameterWithName("rating").description("상품 평점 (이 점수 이상)").optional()
        };
    }

    public static FieldDescriptor[] getSummaryResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("content[].productId").description("상품 번호"),
                fieldWithPath("content[].name").description("상품 이름"),
                fieldWithPath("content[].thumbnail").description("썸네일"),
                fieldWithPath("content[].displayPrice").description("대표 상품 판매 가격"),

                fieldWithPath("content[].originalPrice").description("대표 상품 원본 가격"),
                fieldWithPath("content[].maxDiscountRate").description("최대 할인율"),
                fieldWithPath("content[].categoryId").description("카테고리 Id"),
                fieldWithPath("content[].publishedAt").description("게시일"),
                fieldWithPath("content[].rating").description("평점"),
                fieldWithPath("content[].reviewCount").description("리뷰 개수"),
                fieldWithPath("content[].status").description("상품 상태"),

                fieldWithPath("currentPage").description("현재 페이지"),
                fieldWithPath("totalPage").description("총 페이지"),
                fieldWithPath("pageSize").description("페이지 크기"),
                fieldWithPath("totalElement").description("총 데이터 양")
        };
    }

    public static FieldDescriptor[] getDetailResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("productId").description("상품 ID"),
                fieldWithPath("name").description("상품 이름"),
                fieldWithPath("status").description("상품 상태"),
                fieldWithPath("categoryId").description("카테고리 Id"),
                fieldWithPath("description").description("상품 설명"),
                fieldWithPath("displayPrice").description("대표 상품 판매 가격"),
                fieldWithPath("originalPrice").description("대표 상품 원본 가격"),
                fieldWithPath("maxDiscountRate").description("최대 할인율"),
                fieldWithPath("rating").description("평점"),
                fieldWithPath("reviewCount").description("리뷰 갯수"),
                fieldWithPath("popularityScore").description("인기 점수"),
                fieldWithPath("optionGroups[].optionTypeId").description("상품 옵션 타입 Id"),
                fieldWithPath("optionGroups[].name").description("상품 옵션 타입 이름"),
                fieldWithPath("optionGroups[].priority").description("상품 옵션 우선순위"),
                fieldWithPath("optionGroups[].values[].optionValueId").description("상품 옵션 값 ID"),
                fieldWithPath("optionGroups[].values[].name").description("상품 옵션 값 이름"),
                fieldWithPath("images[].imageId").description("상품 이미지 Id"),
                fieldWithPath("images[].imagePath").description("상품 이미지 URL"),
                fieldWithPath("images[].sortOrder").description("상품 이미지 순서"),
                fieldWithPath("images[].isThumbnail").description("썸네일 여부"),
                fieldWithPath("descriptionImages[].imageId").description("상품 설명 이미지 ID"),
                fieldWithPath("descriptionImages[].imagePath").description("상품 설명 이미지 URL"),
                fieldWithPath("descriptionImages[].sortOrder").description("상품 설명 이미지 순서"),
                fieldWithPath("variants[].variantId").description("상품 변형 ID"),
                fieldWithPath("variants[].sku").description("상품 변형 SKU"),
                fieldWithPath("variants[].optionValueIds").description("상품 변형 옵션 값 Id 리스트"),
                fieldWithPath("variants[].originalPrice").description("상품 변형 원본 가격"),
                fieldWithPath("variants[].discountedPrice").description("상품 변형 할인 가격"),
                fieldWithPath("variants[].discountRate").description("상품 변형 할인율"),
                fieldWithPath("variants[].stockQuantity").description("상품 변형 재고 수량")
        };
    }
}
