package com.socialmedia.aspects;

import java.lang.reflect.Field;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TrimAspect {

	@Before("execution(* com.socialmedia..*(.., @com.socialmedia.annotations.Trimmed (*), ..)) || @annotation(com.socialmedia.annotations.Trimmed)")
	public void trimStrings(JoinPoint joinPoint) {
		for (Object arg : joinPoint.getArgs()) {
			if (arg == null)
				continue;

			Class<?> clazz = arg.getClass();

			// Check if the class has @Trimmed annotation
			if (clazz.isAnnotationPresent(com.socialmedia.annotations.Trimmed.class)) {
				for (Field field : clazz.getDeclaredFields()) {
					if (field.getType().equals(String.class)) {
						field.setAccessible(true);
						try {
							String value = (String) field.get(arg);
							if (value != null) {
								field.set(arg, value.trim());
							}
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
}
