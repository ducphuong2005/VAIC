package com.careercompass.controller;

import com.careercompass.dto.request.CreateChatSessionRequest;
import com.careercompass.dto.request.SendChatMessageRequest;
import com.careercompass.dto.response.ApiResponse;
import com.careercompass.dto.response.ChatMessageResponse;
import com.careercompass.dto.response.ChatSessionDetailResponse;
import com.careercompass.dto.response.ChatSessionResponse;
import com.careercompass.security.UserPrincipal;
import com.careercompass.service.ChatService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat/sessions")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ApiResponse<ChatSessionResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateChatSessionRequest request
    ) {
        return ApiResponse.success(chatService.create(principal.getId(), request));
    }

    @GetMapping
    public ApiResponse<List<ChatSessionResponse>> sessions(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(chatService.sessions(principal.getId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<ChatSessionDetailResponse> detail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        return ApiResponse.success(chatService.detail(principal.getId(), id));
    }

    @PostMapping("/{id}/messages")
    public ApiResponse<ChatMessageResponse> message(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody SendChatMessageRequest request
    ) {
        return ApiResponse.success(chatService.message(principal.getId(), id, request));
    }
}
