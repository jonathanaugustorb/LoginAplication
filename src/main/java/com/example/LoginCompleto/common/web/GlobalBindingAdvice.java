package com.example.LoginCompleto.common.web;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.core.ResolvableType;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

import java.lang.reflect.Field;
import java.util.Locale;

@ControllerAdvice
public class GlobalBindingAdvice {

    private static final String PASSWORD_FIELD_MARKER = "password";

    @InitBinder
    public void trimTextFields(WebDataBinder binder) {
        Class<?> formType = resolveFormType(binder);
        if (formType == null) return;

        ReflectionUtils.doWithFields(formType,
                field -> binder.registerCustomEditor(String.class, field.getName(), new StringTrimmerEditor(false)),
                this::isTrimmable);
    }

    private Class<?> resolveFormType(WebDataBinder binder) {
        if (binder.getTarget() != null) return binder.getTarget().getClass();

        ResolvableType targetType = binder.getTargetType();
        return targetType != null ? targetType.resolve() : null;
    }

    private boolean isTrimmable(Field field) {
        return field.getType() == String.class
                && !field.getName().toLowerCase(Locale.ROOT).contains(PASSWORD_FIELD_MARKER);
    }
}
