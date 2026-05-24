package com.mitrian.diploma.web;

import com.mitrian.diploma.auth.exception.InvalidCredentialsException;
import com.mitrian.diploma.auth.exception.LoginAlreadyExistsException;
import com.mitrian.diploma.auth.exception.UserNotFoundException;
import com.mitrian.diploma.history.exception.RoomHistoryAccessDeniedException;
import com.mitrian.diploma.history.exception.RoomHistoryNotAvailableException;
import com.mitrian.diploma.history.exception.RoomHistoryNotFoundException;
import com.mitrian.diploma.voting.room.exception.ActiveRoomMembershipException;
import com.mitrian.diploma.voting.room.exception.AlreadyRoomParticipantException;
import com.mitrian.diploma.voting.room.exception.NotRoomParticipantException;
import com.mitrian.diploma.voting.room.exception.OnlyRoomOwnerException;
import com.mitrian.diploma.voting.room.exception.OwnerCannotLeaveRoomException;
import com.mitrian.diploma.voting.room.exception.RoomFinishedException;
import com.mitrian.diploma.voting.room.exception.RoomGeoFilterNotAllowedException;
import com.mitrian.diploma.voting.room.exception.RoomLeaveNotAllowedDuringVotingException;
import com.mitrian.diploma.voting.room.exception.RoomLeaveNotAllowedWhileAwaitingStartException;
import com.mitrian.diploma.voting.room.exception.RoomNotFoundException;
import com.mitrian.diploma.voting.room.exception.RoomNotJoinableException;
import com.mitrian.diploma.voting.room.exception.RoomPasswordMismatchException;
import com.mitrian.diploma.voting.room.filter.exception.InvalidKitchenTagSlugException;
import com.mitrian.diploma.voting.catalog.exception.CandidateSelectionPrerequisitesException;
import com.mitrian.diploma.voting.stageone.exception.StageOneDataInvariantException;
import com.mitrian.diploma.voting.stageone.exception.StageOneNotActiveException;
import com.mitrian.diploma.voting.stageone.exception.StageOneVotingNotAllowedException;
import com.mitrian.diploma.voting.stagetwo.exception.StageTwoAlreadyVotedException;
import com.mitrian.diploma.voting.stagetwo.exception.StageTwoNotActiveException;
import com.mitrian.diploma.voting.stagetwo.exception.StageTwoRanksInvalidException;
import com.mitrian.diploma.voting.stagetwo.dto.StageTwoRankEntryDTO;
import com.mitrian.diploma.voting.stagetwo.dto.StageTwoSubmitRanksRequestDTO;
import com.mitrian.diploma.voting.room.exception.RoomKitchenTagsNotAllowedException;
import com.mitrian.diploma.voting.room.exception.RoomReadinessNotAllowedException;
import com.mitrian.diploma.voting.room.exception.RoomStartNotAllowedException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Pattern RANKS_FIELD_PATH = Pattern.compile("^ranks\\[(\\d+)]\\.(\\w+)$");

	private static ApiErrorResponse messageBody(String message) {
		return new ApiErrorResponse(message != null ? message : "");
	}

	@ExceptionHandler(LoginAlreadyExistsException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ApiErrorResponse handleLoginAlreadyExistsException(LoginAlreadyExistsException e) {
		return messageBody(e.getMessage());
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ApiErrorResponse handleInvalidCredentialsException(InvalidCredentialsException e) {
		return messageBody(e.getMessage());
	}

	@ExceptionHandler(UserNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ApiErrorResponse handleUserNotFoundException(UserNotFoundException e) {
		return messageBody(e.getMessage());
	}

	@ExceptionHandler(RoomNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ApiErrorResponse handleRoomNotFoundException(RoomNotFoundException e) {
		return messageBody(e.getMessage());
	}

	@ExceptionHandler(RoomHistoryNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ApiErrorResponse handleRoomHistoryNotFoundException(RoomHistoryNotFoundException e) {
		return messageBody(e.getMessage());
	}

	@ExceptionHandler(NotRoomParticipantException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ApiErrorResponse handleNotRoomParticipantException(NotRoomParticipantException e) {
		return messageBody(e.getMessage());
	}

	@ExceptionHandler(RoomHistoryAccessDeniedException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ApiErrorResponse handleRoomHistoryAccessDeniedException(RoomHistoryAccessDeniedException e) {
		return messageBody(e.getMessage());
	}

	@ExceptionHandler(RoomPasswordMismatchException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ApiErrorResponse handleRoomPasswordMismatchException(RoomPasswordMismatchException e) {
		return messageBody(e.getMessage());
	}

	@ExceptionHandler(OnlyRoomOwnerException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ApiErrorResponse handleOnlyRoomOwnerException(OnlyRoomOwnerException e) {
		return messageBody(e.getMessage());
	}

	@ExceptionHandler({
		AlreadyRoomParticipantException.class,
		RoomNotJoinableException.class,
		ActiveRoomMembershipException.class,
		RoomFinishedException.class,
		RoomGeoFilterNotAllowedException.class,
		RoomLeaveNotAllowedDuringVotingException.class,
		RoomLeaveNotAllowedWhileAwaitingStartException.class,
		OwnerCannotLeaveRoomException.class,
		RoomReadinessNotAllowedException.class,
		RoomKitchenTagsNotAllowedException.class,
		RoomStartNotAllowedException.class,
		StageOneNotActiveException.class,
		StageOneVotingNotAllowedException.class,
		StageOneDataInvariantException.class,
		StageTwoAlreadyVotedException.class,
		StageTwoNotActiveException.class,
		RoomHistoryNotAvailableException.class
	})
	@ResponseStatus(HttpStatus.CONFLICT)
	public ApiErrorResponse handleRoomConflict(RuntimeException e) {
		return messageBody(e.getMessage());
	}

	@ExceptionHandler({BadCredentialsException.class, InternalAuthenticationServiceException.class})
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ApiErrorResponse handleSpringSecurityAuthExceptions(Exception e) {
		String msg = e.getMessage();
		return messageBody(msg != null && !msg.isBlank() ? msg : "Invalid credentials");
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiErrorResponse handleValidationException(MethodArgumentNotValidException e) {
		Object target = e.getBindingResult().getTarget();
		String message = e.getBindingResult().getFieldErrors().stream()
			.findFirst()
			.map(fieldError -> formatValidationFieldError(fieldError, target))
			.orElse("Validation failed");
		return messageBody(message);
	}

	private static String formatValidationFieldError(FieldError fieldError, Object target) {
		String fieldPath = fieldError.getField();
		String detail = fieldError.getDefaultMessage();
		Matcher matcher = RANKS_FIELD_PATH.matcher(fieldPath);
		if (!matcher.matches()) {
			return fieldPath + ": " + detail;
		}
		int idx = Integer.parseInt(matcher.group(1));
		String prop = matcher.group(2);
		if (target instanceof StageTwoSubmitRanksRequestDTO dto) {
			Long restaurantId = resolveRestaurantIdForRankEntry(dto, idx);
			String scope = restaurantId != null
				? "Restaurant " + restaurantId
				: "List item #" + (idx + 1) + " (restaurantId not set)";
			return scope + " (" + prop + "): " + detail;
		}
		return "List item #" + (idx + 1) + " (" + prop + "): " + detail;
	}

	private static Long resolveRestaurantIdForRankEntry(StageTwoSubmitRanksRequestDTO dto, int idx) {
		List<StageTwoRankEntryDTO> ranks = dto.ranks();
		if (ranks == null || idx < 0 || idx >= ranks.size()) {
			return null;
		}
		StageTwoRankEntryDTO entry = ranks.get(idx);
		if (entry == null) {
			return null;
		}
		return entry.restaurantId();
	}

	@ExceptionHandler(InvalidKitchenTagSlugException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiErrorResponse handleInvalidKitchenTagSlugException(InvalidKitchenTagSlugException e) {
		return messageBody(e.getMessage());
	}

	@ExceptionHandler(CandidateSelectionPrerequisitesException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiErrorResponse handleCandidateSelectionPrerequisitesException(
		CandidateSelectionPrerequisitesException e
	) {
		return messageBody(e.getMessage());
	}

	@ExceptionHandler(StageTwoRanksInvalidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiErrorResponse handleStageTwoRanksInvalidException(StageTwoRanksInvalidException e) {
		return messageBody(e.getMessage());
	}
}
