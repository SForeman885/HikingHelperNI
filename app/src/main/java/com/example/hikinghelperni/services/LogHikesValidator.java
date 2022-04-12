package com.example.hikinghelperni.services;

import com.example.hikinghelperni.R;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

public class LogHikesValidator {

    private final int MAX_NAME_LENGTH = 30;
    private final int MAX_MINUTES = 60;
    private final String REQ_FIELD_ERROR = "This field is required";
    private final String REQ_TIME_ERROR = "At least one of Hours or Minutes must be filled";
    private final String INVALID_NAME = "Please enter a valid name of less than 30 characters";
    private final String INVALID_DATE = "Please enter a valid date that is before today's date";
    private final String INVALID_LENGTH = "Please enter a valid trail length";
    private final String INVALID_TIME_TAKEN = "Please enter a valid time taken in hours and minutes";

    private Map<Integer, String> validationResult = new HashMap<>();

    public Map<Integer, String> validateCustomLog(String name, long date, String length, String hours, String minutes) {
        if(name.isEmpty()) {
            validationResult.put(R.id.edit_text_hike_name, REQ_FIELD_ERROR);
        }
        else if(!validateName(name)) {
            validationResult.put(R.id.edit_text_hike_name, INVALID_NAME);
        }
        if(!validateDate(date)) {
            validationResult.put(R.id.edit_text_date_field, INVALID_DATE);
        }
        if(length.isEmpty()) {
            validationResult.put(R.id.edit_text_number_trail_length, REQ_FIELD_ERROR);
        }
        else if(!validateTrailLength(Double.parseDouble(length))) {
            validationResult.put(R.id.edit_text_number_trail_length, INVALID_LENGTH);
        }
        String hoursValue = hours.isEmpty() ? "0" : hours; //this will handle an empty hours/minutes input from user
        String minsValue = minutes.isEmpty() ? "0" : minutes;
        if(hoursValue.equals("0") && minsValue.equals("0")) {
            validationResult.put(R.id.edit_text_number_hours, REQ_TIME_ERROR);
            validationResult.put(R.id.edit_text_number_minutes, REQ_TIME_ERROR);
        }
        else if(!validateTimeTaken(Double.parseDouble(hoursValue), Double.parseDouble(minsValue))) {
            validationResult.put(R.id.edit_text_number_minutes, INVALID_TIME_TAKEN);
        }
        return validationResult;
    }

    public Map<Integer, String> validateTrailLog(String name, long date, String hours, String minutes) {
        if(name.isEmpty()) {
            validationResult.put(R.id.edit_text_hike_name, REQ_FIELD_ERROR);
        }
        else if(!validateName(name)) {
            validationResult.put(R.id.edit_text_hike_name, INVALID_NAME);
        }
        if(!validateDate(date)) {
            validationResult.put(R.id.edit_text_date_field, INVALID_DATE);
        }
        String hoursValue = hours.isEmpty() ? "0" : hours; //this will handle an empty hours/minutes input from user
        String minsValue = minutes.isEmpty() ? "0" : minutes;
        if(hoursValue.equals("0") && minsValue.equals("0")) {
            validationResult.put(R.id.edit_text_number_hours, REQ_TIME_ERROR);
            validationResult.put(R.id.edit_text_number_minutes, REQ_TIME_ERROR);
        }
        else if(!validateTimeTaken(Double.parseDouble(hoursValue), Double.parseDouble(minsValue))) {
            validationResult.put(R.id.edit_text_number_minutes, INVALID_TIME_TAKEN);
        }
        return validationResult;
    }

    private boolean validateName(String name) {
        return name.length() <= MAX_NAME_LENGTH;
    }

    private boolean validateDate(long date) {//using LocalDateTime as Date and DateTime are older outdated libraries
        LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(date/1000, 0, ZoneOffset.UTC);
        LocalDateTime currentDate = LocalDateTime.now();
        return !localDateTime.isAfter(currentDate);
    }

    private boolean validateTrailLength(double length) {
        return !(length < 1);
    }

    private boolean validateTimeTaken(double hours, double minutes) {
        return hours % 1 == 0 && minutes % 1 == 0 && !(minutes >= MAX_MINUTES);
    }
}
