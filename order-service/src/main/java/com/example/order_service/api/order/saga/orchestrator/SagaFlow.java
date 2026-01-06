package com.example.order_service.api.order.saga.orchestrator;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.CommonErrorCode;
import com.example.order_service.api.order.saga.domain.model.SagaStep;
import com.example.order_service.api.order.saga.domain.model.vo.Payload;

import java.util.Arrays;

public enum SagaFlow {

    PRODUCT(SagaStep.PRODUCT) {
        @Override
        public SagaStep next(Payload payload) {
            if (payload.hasCoupon()) return SagaStep.COUPON;
            if (payload.hasPoints()) return SagaStep.USER;
            return SagaStep.PAYMENT;
        }

        @Override
        public SagaStep nextCompensation(Payload payload) {
            return null;
        }
    },
    COUPON(SagaStep.COUPON) {
        @Override
        public SagaStep next(Payload payload){
            if (payload.hasPoints()) return SagaStep.USER;
            return SagaStep.PAYMENT;
        }

        @Override
        public SagaStep nextCompensation(Payload payload) {
            return SagaStep.PRODUCT;
        }

    },
    USER(SagaStep.USER) {
        @Override
        public SagaStep next(Payload payload) {
            return SagaStep.PAYMENT;
        }

        @Override
        public SagaStep nextCompensation(Payload payload) {
            if (payload.hasCoupon()) return SagaStep.COUPON;
            return SagaStep.PRODUCT;
        }
    },
    PAYMENT(SagaStep.PAYMENT) {
        @Override
        public SagaStep next(Payload payload) {
            return null;
        }

        @Override
        public SagaStep nextCompensation(Payload payload) {
            if (payload.hasPoints()) return SagaStep.USER;
            if (payload.hasCoupon()) return SagaStep.COUPON;
            return SagaStep.PRODUCT;
        }
    };

    private final SagaStep step;
    SagaFlow(SagaStep step) {
        this.step = step;
    }

    public abstract SagaStep next(Payload payload);
    public abstract SagaStep nextCompensation(Payload payload);
    public static SagaFlow from(SagaStep step) {
        return Arrays.stream(values())
                .filter(flow -> flow.step == step)
                .findFirst()
                .orElseThrow(() -> new BusinessException(CommonErrorCode.INTERNAL_ERROR, "정의되지 않은 SagaStep " + step));
    }

    public static SagaStep initialStep(Payload payload) {
        return SagaStep.PRODUCT;
    }
}
