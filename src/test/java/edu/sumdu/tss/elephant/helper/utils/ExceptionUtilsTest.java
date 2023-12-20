package edu.sumdu.tss.elephant.helper.utils;
import io.javalin.core.validation.ValidationError;
import io.javalin.core.validation.ValidationException;
import io.javalin.http.Context;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

class ExceptionUtilsTest {

    @Test
    void testValidationMessages() {
        // Creating ValidationError with required parameters
        ValidationError<Object> validationError = new ValidationError<>(
                "Error message", // message
                Collections.emptyMap(), // args
                null // value (can be null if not needed)
        );

        Map<String, List<ValidationError<Object>>> errors = Map.of(
                "field", List.of(validationError)
        );

        ValidationException ve = new ValidationException(errors);

        String expected = "<ul><li><b>field</b>&nbsp;Error message </li></ul>";
        String actual = ExceptionUtils.validationMessages(ve);

        assertEquals(expected, actual, "The validation message formatting is incorrect");
    }

    @Test
    void testIsSQLUniqueException() {
        Throwable sqlException = new Throwable("duplicate key value violates unique constraint");
        assertTrue(ExceptionUtils.isSQLUniqueException(sqlException),
                "Should return true for SQL unique constraint violation message");

        Throwable nonSqlException = new Throwable("some other error");
        assertFalse(ExceptionUtils.isSQLUniqueException(nonSqlException),
                "Should return false for non-SQL unique constraint violation message");
    }

    @Test
    void testStacktrace() {
        Exception e = new Exception("test exception");
        String stackTrace = ExceptionUtils.stacktrace(e);

        assertNotNull(stackTrace, "Stacktrace should not be null");
        assertTrue(stackTrace.contains("test exception"),
                "Stacktrace should contain the exception message");
    }

    @Test
    void testWrapError() {
        Context mockContext = Mockito.mock(Context.class);

        ValidationError<Object> validationError = new ValidationError<>(
                "Error message", // message
                Collections.emptyMap(), // args
                null // value (can be null if not needed)
        );

        Map<String, List<ValidationError<Object>>> errors = Map.of(
                "field", List.of(validationError)
        );

        ValidationException ve = new ValidationException(errors);

        ExceptionUtils.wrapError(mockContext, ve);

        verify(mockContext).sessionAttribute(anyString(), any());
    }
}