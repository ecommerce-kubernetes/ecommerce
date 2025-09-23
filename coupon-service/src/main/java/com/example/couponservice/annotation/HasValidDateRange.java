package com.example.couponservice.annotation;

import java.time.LocalDateTime;

public interface HasValidDateRange {
    LocalDateTime getValidFrom();
    LocalDateTime getValidTo();
}
