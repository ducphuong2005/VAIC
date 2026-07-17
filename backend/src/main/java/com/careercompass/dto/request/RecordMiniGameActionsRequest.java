package com.careercompass.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record RecordMiniGameActionsRequest(
        @NotEmpty List<@Valid MiniGameActionRequest> actions
) {
}
