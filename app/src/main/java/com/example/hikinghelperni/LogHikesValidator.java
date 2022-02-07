package com.example.hikinghelperni;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.sql.Time;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
            validationResult.put(R.id.editTextTrailName, REQ_FIELD_ERROR);
        }
        else if(!validateName(name)) {
            validationResult.put(R.id.editTextTrailName, INVALID_NAME);
        }
        if(!validateDate(date)) {
            validationResult.put(R.id.editTextDateField, INVALID_DATE);
        }
        if(length.isEmpty()) {
            validationResult.put(R.id.editTextNumberTrailLength, REQ_FIELD_ERROR);
        }
        else if(!validateTrailLength(Double.parseDouble(length))) {
            validationResult.put(R.id.editTextNumberTrailLength, INVALID_LENGTH);
        }
        if(hours.isEmpty() && minutes.isEmpty()) {
            validationResult.put(R.id.editTextNumberHours, REQ_TIME_ERROR);
            validationResult.put(R.id.editTextNumberMinutes, REQ_TIME_ERROR);
        }
        else if(!validateTimeTaken(Double.parseDouble(hours), Double.parseDouble(minutes))) {
            validationResult.put(R.id.editTextNumberMinutes, INVALID_TIME_TAKEN);
        }
        return validationResult;
    }

    private boolean validateName(String name) {
        if(name.length() > MAX_NAME_LENGTH){
            return false;
        }
        return true;
    }

    private boolean validateDate(long date) {//using Time as Date and DateTime are older outdated libraries
        Time formattedDate = new Time(date);
        Time currentDate = new Time(System.currentTimeMillis());
        if(formattedDate.after(currentDate)) {
            return false;
        }
        return true;
    }

    private boolean validateTrailLength(double length) {
        if(length < 1) {
            return false;
        }
        return true;
    }

    private boolean validateTimeTaken(double hours, double minutes) {
        if(hours%1 != 0 || minutes%1 != 0 || minutes >= MAX_MINUTES) {
            return false;
        }
        return true;
    }
}
