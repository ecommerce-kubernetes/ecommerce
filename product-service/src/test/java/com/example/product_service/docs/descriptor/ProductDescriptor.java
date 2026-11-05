package com.example.product_service.docs.descriptor;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.request.ParameterDescriptor;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.snippet.Attributes.key;

public class ProductDescriptor {

    public static FieldDescriptor[] createProductRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("name").type(JsonFieldType.STRING).description("상품 이름")
                        .attributes(key("constraint").value("필수")),
                fieldWithPath("categoryId").type(JsonFieldType.NUMBER).description("카테고리 ID")
                        .attributes(key("constraint").value("필수")),
                fieldWithPath("description").type(JsonFieldType.STRING).description("상품 설명").optional()
        };
    }

    public static FieldDescriptor[] createProductResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("productId").type(JsonFieldType.STRING).description("생성된 상품 ID")
        };
    }

    public static FieldDescriptor[] updateProductRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("name").type(JsonFieldType.STRING).description("상품 이름")
                        .attributes(key("constraint").value("필수")),
                fieldWithPath("categoryId").type(JsonFieldType.NUMBER).description("카테고리 ID")
                        .attributes(key("constraint").value("필수")),
                fieldWithPath("description").type(JsonFieldType.STRING).description("상품 설명").optional()
        };
    }

    public static FieldDescriptor[] updateProductResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("productId").type(JsonFieldType.STRING).description("수정된 상품 ID")
        };
    }

    public static FieldDescriptor[] registerProductOptionsRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("optionTypeIds").type(JsonFieldType.ARRAY).description("등록할 옵션 타입 ID 리스트")
                        .attributes(key("constraint").value("중복 불가"))
        };
    }

    public static FieldDescriptor[] registerProductOptionsResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("productId").type(JsonFieldType.STRING).description("상품 ID")
        };
    }

    public static FieldDescriptor[] addProductVariantsRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("variants").type(JsonFieldType.ARRAY).description("추가할 상품 변형 목록"),
                fieldWithPath("variants[].originalPrice").type(JsonFieldType.NUMBER).description("상품 변형 원본 가격")
                        .attributes(key("constraint").value("100 이상")),
                fieldWithPath("variants[].discountRate").type(JsonFieldType.NUMBER).description("할인율")
                        .attributes(key("constraint").value("0~100")),
                fieldWithPath("variants[].stockQuantity").type(JsonFieldType.NUMBER).description("재고 수량")
                        .attributes(key("constraint").value("1 이상")),
                fieldWithPath("variants[].optionValueIds").type(JsonFieldType.ARRAY).description("옵션 값 ID 리스트")
                        .attributes(key("constraint").value("중복 불가"))
        };
    }

    public static FieldDescriptor[] addProductVariantsResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("productId").type(JsonFieldType.STRING).description("상품 ID")
        };
    }

    public static FieldDescriptor[] addProductImagesRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("images").type(JsonFieldType.ARRAY).description("등록할 이미지 경로 목록")
                        .attributes(key("constraint").value("최소 1개, 이미지 경로 패턴"))
        };
    }

    public static FieldDescriptor[] addProductImagesResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("productId").type(JsonFieldType.STRING).description("상품 ID")
        };
    }

    public static FieldDescriptor[] addProductDescriptionImagesRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("images").type(JsonFieldType.ARRAY).description("등록할 설명 이미지 경로 목록")
                        .attributes(key("constraint").value("최소 1개, 이미지 경로 패턴"))
        };
    }

    public static FieldDescriptor[] addProductDescriptionImagesResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("productId").type(JsonFieldType.STRING).description("상품 ID")
        };
    }

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

    public static ParameterDescriptor[] productSearchParams() {
        return new ParameterDescriptor[] {
                parameterWithName("page").description("페이지 번호 (기본값: 0)").optional(),
                parameterWithName("size").description("페이지 크기 (기본값: 20)").optional(),
                parameterWithName("sort").description("정렬 기준 (기본값: latest)").optional(),
                parameterWithName("categoryId").description("카테고리 ID").optional(),
                parameterWithName("name").description("상품명 검색 키워드").optional(),
                parameterWithName("rating").description("상품 평점 (이 점수 이상)").optional()
        };
    }

    public static FieldDescriptor[] productListResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("content[].productId").type(JsonFieldType.STRING).description("상품 ID"),
                fieldWithPath("content[].name").description("상품 이름"),
                fieldWithPath("content[].thumbnail").description("썸네일"),
                fieldWithPath("content[].originalPrice").description("원본 가격"),
                fieldWithPath("content[].discountRate").description("할인율"),
                fieldWithPath("content[].price").description("판매 가격"),
                fieldWithPath("content[].reviewCount").description("리뷰 개수"),

                fieldWithPath("currentPage").description("현재 페이지"),
                fieldWithPath("totalPage").description("총 페이지"),
                fieldWithPath("pageSize").description("페이지 크기"),
                fieldWithPath("totalElement").description("총 데이터 양")
        };
    }

    public static FieldDescriptor[] productDetailResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("productId").description("상품 ID"),
                fieldWithPath("name").description("상품 이름"),
                fieldWithPath("description").description("상품 설명"),
                fieldWithPath("status").description("상품 상태"),
                fieldWithPath("category.id").description("카테고리 ID"),
                fieldWithPath("category.name").description("카테고리 이름"),
                fieldWithPath("images").description("상품 이미지 URL 목록"),
                fieldWithPath("descriptionImages").description("상품 설명 이미지 URL 목록"),
                fieldWithPath("rating").description("평점"),
                fieldWithPath("reviewCount").description("리뷰 개수"),
                fieldWithPath("originalPrice").description("원본 가격"),
                fieldWithPath("discountRate").description("할인율"),
                fieldWithPath("price").description("판매 가격"),
                fieldWithPath("options[].optionTypeId").description("상품 옵션 타입 ID"),
                fieldWithPath("options[].optionTypeName").description("상품 옵션 타입 이름"),
                fieldWithPath("options[].values[].optionValueId").description("상품 옵션 값 ID"),
                fieldWithPath("options[].values[].name").description("상품 옵션 값 이름"),
                fieldWithPath("variants[].variantId").description("상품 변형 ID"),
                fieldWithPath("variants[].sku").description("상품 변형 SKU"),
                fieldWithPath("variants[].optionValueIds").description("상품 변형 옵션 값 ID 리스트"),
                fieldWithPath("variants[].originalPrice").description("상품 변형 원본 가격"),
                fieldWithPath("variants[].discountRate").description("상품 변형 할인율"),
                fieldWithPath("variants[].price").description("상품 변형 판매 가격"),
                fieldWithPath("variants[].stockQuantity").description("상품 변형 재고 수량")
        };
    }
}
