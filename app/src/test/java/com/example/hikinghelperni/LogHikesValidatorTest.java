package com.example.hikinghelperni;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

public class LogHikesValidatorTest {

    private final String REQ_FIELD_ERROR = "This field is required";
    private final String REQ_TIME_ERROR = "At least one of Hours or Minutes must be filled";
    private final String INVALID_NAME = "Please enter a valid name of less than 30 characters";
    private final String INVALID_DATE = "Please enter a valid date that is before today's date";
    private final String INVALID_LENGTH = "Please enter a valid trail length";
    private final String INVALID_TIME_TAKEN = "Please enter a valid time taken in hours and minutes";
    private final String INVALID_NAME_VALUE = "Invalid name as is way too long";

    private LogHikesValidator logHikesValidator;

    @Before
    public void SetUp() {
        logHikesValidator = new LogHikesValidator();
    }

    @Test
    public void returnsErrorsForCustomLogWithEmptyFields() {
        Map<Integer, String> result = logHikesValidator.validateCustomLog("", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)*1000, "", "", "");
        assert(result.equals(getExpectedResultEmptyFields(true)));
    }

    @Test
    public void returnsErrorsForCustomLogWithIncorrectFields() {
        Map<Integer, String> result = logHikesValidator.validateCustomLog(INVALID_NAME_VALUE, (LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)*1000) +1000, "-1", "1.5", "1.5");
        assert(result.equals(getExpectedResultIncorrectFields(true)));
    }

    @Test
    public void returnsEmptyMapCustomLogWithCorrectFields() {
        Map<Integer, String> result = logHikesValidator.validateCustomLog("Trail Name", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)*1000 , "5", "1", "15");
        Map<Integer, String> expectedResult = new HashMap<>();
        assert(result.equals(expectedResult));
    }

    @Test
    public void returnsErrorsForTrailLogWithEmptyFields() {
        Map<Integer, String> result = logHikesValidator.validateTrailLog("", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)*1000, "", "");
        assert(result.equals(getExpectedResultEmptyFields(false)));
    }

    @Test
    public void returnsErrorsForTrailLogWithIncorrectFields() {
        Map<Integer, String> result = logHikesValidator.validateTrailLog(INVALID_NAME_VALUE, (LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)*1000) +1000, "1", "100");
        assert(result.equals(getExpectedResultIncorrectFields(false)));
    }

    @Test
    public void returnsEmptyMapForTrailLogWithCorrectFields() {
        Map<Integer, String> result = logHikesValidator.validateTrailLog("Trail Name", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)*1000, "1", "15");
        Map<Integer, String> expectedResult = new HashMap<>();
        assert(result.equals(expectedResult));
    }

    private Map<Integer, String> getExpectedResultEmptyFields(boolean isCustom) {
        Map<Integer, String> expectedResult = new HashMap<>();
        expectedResult.put(R.id.edit_text_hike_name, REQ_FIELD_ERROR);
        if(isCustom) {
            expectedResult.put(R.id.edit_text_number_trail_length, REQ_FIELD_ERROR);
        }
        expectedResult.put(R.id.edit_text_number_hours, REQ_TIME_ERROR);
        expectedResult.put(R.id.edit_text_number_minutes, REQ_TIME_ERROR);
        return expectedResult;
    }

    private Map<Integer, String> getExpectedResultIncorrectFields(boolean isCustom) {
        Map<Integer, String> expectedResult = new HashMap<>();
        expectedResult.put(R.id.edit_text_hike_name, INVALID_NAME);
        expectedResult.put(R.id.edit_text_date_field, INVALID_DATE);
        if(isCustom) {
            expectedResult.put(R.id.edit_text_number_trail_length, INVALID_LENGTH);
        }
        expectedResult.put(R.id.edit_text_number_minutes, INVALID_TIME_TAKEN);
        return expectedResult;
    }
}
