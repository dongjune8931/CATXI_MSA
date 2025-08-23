package org.example.chatservice.dto;

import java.util.List;

public record ParticipantsUpdateMessage(
	Long roomId,
	List<String> nicknames
) {}