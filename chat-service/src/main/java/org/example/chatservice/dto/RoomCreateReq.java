package org.example.chatservice.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.*;
import org.antlr.v4.runtime.misc.NotNull;
import org.example.chatservice.domain.ENUM.Location;

public record RoomCreateReq(
	@NotNull Location startPoint,
	@NotNull Location endPoint,
	@NotNull LocalDateTime departAt,
	@Min(1) @Max(4) Long recruitSize
) {}