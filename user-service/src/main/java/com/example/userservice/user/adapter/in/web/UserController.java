package com.example.userservice.user.adapter.in.web;

import com.example.userservice.common.security.model.UserPrincipal;
import com.example.userservice.user.adapter.in.web.dto.AddShippingAddressRequest;
import com.example.userservice.user.adapter.in.web.dto.AddShippingAddressResponse;
import com.example.userservice.user.adapter.in.web.dto.EmailAvailableResponse;
import com.example.userservice.user.adapter.in.web.dto.UserCreateRequest;
import com.example.userservice.user.adapter.in.web.dto.UserCreateResponse;
import com.example.userservice.user.application.service.UserService;
import com.example.userservice.user.application.service.dto.command.AddShippingAddressCommand;
import com.example.userservice.user.application.service.dto.command.UserCreateCommand;
import com.example.userservice.user.application.service.dto.result.AddShippingAddressResult;
import com.example.userservice.user.application.service.dto.result.EmailAvailableResult;
import com.example.userservice.user.application.service.dto.result.UserCreateResult;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserCreateResponse> createUser(@Validated @RequestBody UserCreateRequest request) {
        UserCreateCommand command = request.toCommand();
        UserCreateResult result = userService.createUser(command);
        UserCreateResponse response = UserCreateResponse.from(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/email-availability")
    public ResponseEntity<EmailAvailableResponse> checkEmailAvailable(@RequestParam(name = "email")
                                                                          @NotBlank(message = "email 파라미터는 필수값 입니다")
                                                                          @Email(message = "올바른 이메일 형식이 아닙니다") String email){
        EmailAvailableResult result = userService.checkAvailableEmail(email);
        EmailAvailableResponse response = EmailAvailableResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/shipping-addresses")
    public ResponseEntity<AddShippingAddressResponse> addShippingAddress(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                          @Validated @RequestBody AddShippingAddressRequest request) {
        AddShippingAddressCommand command = request.toCommand(userPrincipal.getUserId());
        AddShippingAddressResult result = userService.addShippingAddress(command);
        AddShippingAddressResponse response = AddShippingAddressResponse.from(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/shipping-addresses/{shippingAddressId}")
    public ResponseEntity<Void> deleteShippingAddress(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                       @PathVariable Long shippingAddressId) {
        userService.deleteShippingAddress(userPrincipal.getUserId(), shippingAddressId);
        return ResponseEntity.noContent().build();
    }
}
