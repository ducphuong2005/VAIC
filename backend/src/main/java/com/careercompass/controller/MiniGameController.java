package com.careercompass.controller;

import com.careercompass.dto.request.CompleteMiniGameRequest;
import com.careercompass.dto.request.RecordMiniGameActionsRequest;
import com.careercompass.dto.response.ApiResponse;
import com.careercompass.dto.response.MiniGameActionResponse;
import com.careercompass.dto.response.MiniGameDetailResponse;
import com.careercompass.dto.response.MiniGameResultResponse;
import com.careercompass.dto.response.MiniGameSessionResponse;
import com.careercompass.dto.response.MiniGameSummaryResponse;
import com.careercompass.security.UserPrincipal;
import com.careercompass.service.MiniGameService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MiniGameController {

    private final MiniGameService miniGameService;

    @GetMapping("/api/v1/minigames")
    public ApiResponse<List<MiniGameSummaryResponse>> miniGames() {
        return ApiResponse.success(miniGameService.list());
    }

    @GetMapping("/api/v1/minigames/latest-result")
    public ApiResponse<MiniGameResultResponse> latestResult(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(miniGameService.latestCompletedResult(principal.getId()));
    }

    @GetMapping("/api/v1/minigames/{id}")
    public ApiResponse<MiniGameDetailResponse> detail(@PathVariable UUID id) {
        return ApiResponse.success(miniGameService.detail(id));
    }

    @PostMapping("/api/v1/minigames/{id}/start")
    public ApiResponse<MiniGameSessionResponse> start(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        return ApiResponse.success(miniGameService.start(principal.getId(), id));
    }

    @PostMapping("/api/v1/minigame-sessions/{sessionId}/actions")
    public ApiResponse<List<MiniGameActionResponse>> actions(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID sessionId,
            @Valid @RequestBody RecordMiniGameActionsRequest request
    ) {
        return ApiResponse.success(miniGameService.recordActions(principal.getId(), sessionId, request));
    }

    @PostMapping("/api/v1/minigame-sessions/{sessionId}/complete")
    public ApiResponse<MiniGameResultResponse> complete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID sessionId,
            @Valid @RequestBody CompleteMiniGameRequest request
    ) {
        return ApiResponse.success(miniGameService.complete(principal.getId(), sessionId, request));
    }

    @GetMapping("/api/v1/minigame-sessions/{sessionId}/result")
    public ApiResponse<MiniGameResultResponse> result(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID sessionId
    ) {
        return ApiResponse.success(miniGameService.result(principal.getId(), sessionId));
    }
}
