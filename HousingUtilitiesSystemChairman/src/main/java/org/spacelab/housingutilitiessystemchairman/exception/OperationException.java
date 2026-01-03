package org.spacelab.housingutilitiessystemchairman.exception;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@Getter
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class OperationException extends RuntimeException {
    private final HttpStatus status;
    public OperationException(String operation, String message) {
        super("Ошибка при " + operation + ": " + message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }
    public OperationException(String operation, String message, HttpStatus status) {
        super("Ошибка при " + operation + ": " + message);
        this.status = status;
    }
}
