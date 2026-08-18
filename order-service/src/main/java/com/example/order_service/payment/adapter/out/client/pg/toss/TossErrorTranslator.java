package com.example.order_service.payment.adapter.out.client.pg.toss;

import com.example.order_service.payment.adapter.out.client.pg.PGErrorTranslator;
import com.example.order_service.payment.exception.PaymentPGPortErrorCode;
import org.springframework.stereotype.Component;

@Component
public class TossErrorTranslator implements PGErrorTranslator {
    @Override
    public PaymentPGPortErrorCode translate(String code) {
        return switch (code) {
            case "REJECT_ACCOUNT_PAYMENT", "REJECT_CARD_PAYMENT", "EXCEED_MAX_DAILY_PAYMENT_COUNT",
                 "EXCEED_MAX_PAYMENT_AMOUNT", "EXCEED_MAX_ONE_DAY_WITHDRAW_AMOUNT",
                 "EXCEED_MAX_ONE_TIME_WITHDRAW_AMOUNT", "EXCEED_MAX_AMOUNT",
                 "EXCEED_MAX_MONTHLY_PAYMENT_AMOUNT", "EXCEED_MAX_ONE_DAY_AMOUNT" ->
                    PaymentPGPortErrorCode.PG_INSUFFICIENT_BALANCE;

            case "INVALID_REJECT_CARD", "INVALID_CARD_EXPIRATION", "INVALID_STOPPED_CARD",
                 "INVALID_CARD_LOST_OR_STOLEN", "INVALID_CARD_NUMBER", "INVALID_ACCOUNT_INFO_RE_REGISTER",
                 "REJECT_TOSSPAY_INVALID_ACCOUNT", "NOT_ALLOWED_POINT_USE", "INVALID_AUTHORIZE_AUTH",
                 "INVALID_PASSWORD", "EXCEED_MAX_AUTH_COUNT", "EXCEED_MAX_CARD_INSTALLMENT_PLAN",
                 "NOT_SUPPORTED_INSTALLMENT_PLAN_CARD_OR_MERCHANT", "INVALID_CARD_INSTALLMENT_PLAN",
                 "NOT_SUPPORTED_MONTHLY_INSTALLMENT_PLAN",
                 "NOT_SUPPORTED_MONTHLY_INSTALLMENT_PLAN_BELOW_AMOUNT" -> PaymentPGPortErrorCode.PG_METHOD_REJECTED;

            case "NOT_AVAILABLE_PAYMENT", "NOT_AVAILABLE_BANK", "FDS_ERROR",
                 "REJECT_CARD_COMPANY", "FORBIDDEN_REQUEST", "RESTRICTED_TRANSFER_ACCOUNT",
                 "NOT_REGISTERED_BUSINESS", "INVALID_UNREGISTERED_SUBMALL", "NOT_FOUND_TERMINAL_ID" ->
                    PaymentPGPortErrorCode.PG_POLICY_RESTRICTED;

            case "INVALID_REQUEST", "BELOW_MINIMUM_AMOUNT", "UNAPPROVED_ORDER_ID" ->
                    PaymentPGPortErrorCode.PG_INVALID_REQUEST;

            case "ALREADY_PROCESSED_PAYMENT" ->
                    PaymentPGPortErrorCode.PG_ALREADY_PROCESSED;

            case "ALREADY_CANCELED_PAYMENT", "ALREADY_REFUND_PAYMENT" ->
                    PaymentPGPortErrorCode.PG_ALREADY_CANCELED;

            case "INVALID_REFUND_ACCOUNT_INFO", "INVALID_REFUND_ACCOUNT_NUMBER", "INVALID_BANK", "FORBIDDEN_BANK_REFUND_REQUEST" ->
                    PaymentPGPortErrorCode.PG_INVALID_REFUND_ACCOUNT;

            case "EXCEED_CANCEL_AMOUNT_DISCOUNT_AMOUNT", "NOT_MATCHES_REFUNDABLE_AMOUNT",
                 "REFUND_REJECTED", "NOT_CANCELABLE_AMOUNT", "NOT_CANCELABLE_PAYMENT",
                 "EXCEED_MAX_REFUND_DUE", "NOT_ALLOWED_PARTIAL_REFUND_WAITING_DEPOSIT",
                 "NOT_ALLOWED_PARTIAL_REFUND", "NOT_CANCELABLE_PAYMENT_FOR_DORMANT_USER",
                 "EXCEED_CANCEL_LIMIT" ->
                    PaymentPGPortErrorCode.PG_CANCEL_REJECTED;

            case "UNAUTHORIZED_KEY", "INCORRECT_BASIC_AUTH_FORMAT", "INVALID_API_KEY" ->
                    PaymentPGPortErrorCode.PG_AUTH_ERROR;

            case "NOT_FOUND_PAYMENT", "NOT_FOUND", "NOT_FOUND_PAYMENT_SESSION" ->
                    PaymentPGPortErrorCode.PG_NOT_FOUND;

            case "PROVIDER_ERROR", "CARD_PROCESSING_ERROR", "FORBIDDEN_CONSECUTIVE_REQUEST",
                 "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", "FAILED_INTERNAL_SYSTEM_PROCESSING",
                 "UNKNOWN_PAYMENT_ERROR", "FAILED_REFUND_PROCESS", "FAILED_METHOD_HANDLING_CANCEL",
                 "FAILED_PARTIAL_REFUND", "COMMON_ERROR" ->
                    PaymentPGPortErrorCode.PG_SERVER_ERROR;

            default -> PaymentPGPortErrorCode.PG_SERVER_ERROR;
        };
    }
}
