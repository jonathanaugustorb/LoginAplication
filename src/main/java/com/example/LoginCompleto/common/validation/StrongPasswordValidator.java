package com.example.LoginCompleto.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 64;
    private static final Pattern LETTER = Pattern.compile("\\p{L}");
    private static final Pattern DIGIT = Pattern.compile("\\d");

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) return true;

        return password.length() >= MIN_LENGTH
                && password.length() <= MAX_LENGTH
                && LETTER.matcher(password).find()
                && DIGIT.matcher(password).find();
    }
}
