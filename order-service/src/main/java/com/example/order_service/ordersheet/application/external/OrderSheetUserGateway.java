package com.example.order_service.ordersheet.application.external;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.UserAdaptor;
import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetUserResult;
import com.example.order_service.ordersheet.application.mapper.OrderSheetUserMapper;
import com.example.order_service.ordersheet.exception.OrderSheetErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 주문서 유저 도메인 통신을 담당하는 Gateway 서비스
 * <p>
 * 유저 도메인의 응답을 서비스 레이어의 Result로 매핑하여 반환
 * 유저 도메인 통신중 발생하는 예외를 비지니스 예외로 변환
 * </p>
 *
 * @author 최민식
 * @since 2026. 05. 22
 */
@Service
@RequiredArgsConstructor
public class OrderSheetUserGateway {
    private final UserAdaptor userAdaptor;
    private final OrderSheetUserMapper mapper;

    /**
     * 유저 도메인에 유저 프로필 정보(유저 기본정보, 유저 배송 정보)를 조회
     *
     * @param userId 조회 대상 유저 아이디
     * @return 유저 기본정보, 배송 정보 결과 반환
     */
    public OrderSheetUserResult.Profile getUserProfile(Long userId) {
        UserClientResponse.Profile profile = fetchUserProfileWithTranslation(userId);
        return mapper.toResult(profile);
    }

    /**
     * 유저 도메인에 유저 포인트 정보(포인트 잔액, 적용 가능 포인트)를 조회
     *
     * @param userId      조회 대상 유저 아이디
     * @param orderAmount 주문 가격
     * @return 포인트 잔액, 적용 가능 포인트
     * @throws BusinessException 유저 도메인 통신중 발생한 예외를 비지니스 예외로 변환
     */
    public OrderSheetUserResult.UserPoint getUserPoints(Long userId, Money orderAmount) {
        UserClientResponse.UserPoints userPoints = fetchUserPointsWithTranslation(userId, orderAmount.longValue());
        return mapper.toResult(userPoints);
    }

    private UserClientResponse.UserPoints fetchUserPointsWithTranslation(Long userId, Long orderAmount) {
        try {
            return userAdaptor.getUserPoints(userId, orderAmount);
        } catch (ExternalClientException e) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_USER_CLIENT_ERROR);
        } catch (ExternalServerException e) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_USER_SERVER_ERROR);
        } catch (ExternalSystemUnavailableException e) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_USER_UNAVAILABLE_SERVER_ERROR);
        }
    }

    /**
     * 유저 도메인에 적용 포인트를 검증하고 결과를 반환
     *
     * @param userId      조회 대상 유저 아이디
     * @param orderAmount 주문 가격
     * @param usedPoints  적용 포인트
     * @return 포인트 잔액, 적용 가능 포인트
     * @throws BusinessException 유저 도메인 통신중 발생한 예외를 비지니스 예외로 변환
     */
    public OrderSheetUserResult.UserPoint getUserPointsForOrder(Long userId, Money orderAmount, Money usedPoints) {
        UserClientResponse.UserPoints userPoints = fetchUserPointsForOrderWithTranslation(userId, orderAmount.longValue(), usedPoints.longValue());
        return mapper.toResult(userPoints);
    }

    private UserClientResponse.UserPoints fetchUserPointsForOrderWithTranslation(Long userId, Long orderAmount, Long usedPoints) {
        try {
            return userAdaptor.getUserPointsForOrder(userId, orderAmount, usedPoints);
        } catch (ExternalClientException e) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_USER_CLIENT_ERROR);
        } catch (ExternalServerException e) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_USER_SERVER_ERROR);
        } catch (ExternalSystemUnavailableException e) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_USER_UNAVAILABLE_SERVER_ERROR);
        }
    }

    private UserClientResponse.Profile fetchUserProfileWithTranslation(Long userId) {
        try {
            return userAdaptor.getUserProfile(userId);
        } catch (ExternalClientException e) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_USER_CLIENT_ERROR);
        } catch (ExternalServerException e) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_USER_SERVER_ERROR);
        } catch (ExternalSystemUnavailableException e) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_USER_UNAVAILABLE_SERVER_ERROR);
        }
    }

}
