package com.example.userservice.user.adapter.in.web;

import com.example.userservice.common.security.model.UserPrincipal;
import com.example.userservice.user.adapter.in.web.dto.AddShippingAddressRequest;
import com.example.userservice.user.adapter.in.web.dto.EmailAvailableResponse;
import com.example.userservice.user.adapter.in.web.dto.UserCreateRequest;
import com.example.userservice.user.adapter.in.web.dto.UserCreateResponse;
import com.example.userservice.user.application.service.UserCommandService;
import com.example.userservice.user.application.service.UserQueryService;
import com.example.userservice.user.application.service.dto.command.AddShippingAddressCommand;
import com.example.userservice.user.application.service.dto.command.UserCreateCommand;
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
public class UserController {
    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    @PostMapping
    public ResponseEntity<UserCreateResponse> createUser(@Validated @RequestBody UserCreateRequest request) {
        UserCreateCommand command = request.toCommand();
        UserCreateResult result = userCommandService.createUser(command);
        UserCreateResponse response = UserCreateResponse.from(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/email-availability")
    public ResponseEntity<EmailAvailableResponse> checkEmailAvailable(@RequestParam(name = "email")
                                                                          @NotBlank(message = "{user.email.notBlank}")
                                                                          @Email(message = "{user.email.pattern}") String email){
        EmailAvailableResult result = userQueryService.checkAvailableEmail(email);
        EmailAvailableResponse response = EmailAvailableResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/shipping-addresses")
    public ResponseEntity<Void> addShippingAddress(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                    @Validated @RequestBody AddShippingAddressRequest request) {
        AddShippingAddressCommand command = request.toCommand(userPrincipal.getUserId());
        userCommandService.addShippingAddress(command);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/shipping-addresses/{shippingAddressId}")
    public ResponseEntity<Void> deleteShippingAddress(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                       @PathVariable Long shippingAddressId) {
        userCommandService.deleteShippingAddress(userPrincipal.getUserId(), shippingAddressId);
        return ResponseEntity.noContent().build();
    }
}
