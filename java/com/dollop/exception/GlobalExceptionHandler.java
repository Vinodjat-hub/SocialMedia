package com.dollop.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
	
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException ex){
		Map<String, Object> response = new HashMap<>();
        response.put("status", "ERROR");
        response.put("message", ex.getMessage());
        response.put("errorCode", "RESOURCE_NOT_FOUND");
        System.out.println(ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);	}
	
	 @ExceptionHandler(DuplicateEntryExecption.class)
	    public ResponseEntity<Map<String, Object>> handleDuplicateEntry(DuplicateEntryExecption ex) {
	        Map<String, Object> response = new HashMap<>();
	        response.put("status", "ERROR");
	        response.put("message", ex.getMessage());
	        response.put("errorCode", "DUPLICATE_EMAIL");
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	    }
	 
	 @ExceptionHandler(InvalidLoginException.class)
	    public ResponseEntity<Map<String, String>> handleInvalidLogin(InvalidLoginException ex) {
	        Map<String, String> response = new HashMap<>();
	        response.put("status", "ERROR");
	        response.put("message", ex.getMessage());
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response); // 401
	    }
	 @ExceptionHandler(HttpMessageNotReadableException.class)
	    public ResponseEntity<Map<String, String>> HttpMessageException(HttpMessageNotReadableException ex) {
	        Map<String, String> response = new HashMap<>();
	        response.put("status", "ERROR");
	        response.put("message", ex.getMessage());
	        System.out.println(ex.getMessage());
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // 401
	    }
	 
	 @ExceptionHandler(UserBlockedException.class)
	 public ResponseEntity<Map<String, Object>> handleUserBlockedException(UserBlockedException ex) {
	     Map<String, Object> response = new HashMap<>();
	     response.put("status", "ERROR");
	     response.put("message", ex.getMessage());
	     response.put("errorCode", "USER_BLOCKED");
	     return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
	 }

}
