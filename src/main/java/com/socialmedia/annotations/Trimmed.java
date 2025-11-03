package com.socialmedia.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 🔹 Can be used on class level
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Trimmed {
}
