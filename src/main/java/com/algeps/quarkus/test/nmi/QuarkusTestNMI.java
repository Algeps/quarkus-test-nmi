package com.algeps.quarkus.test.nmi;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

@ExtendWith({QuarkusTestNMIExtension.class})
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Tag("com.algeps.quarkus.test.nmi.QuarkusTestNMI")
public @interface QuarkusTestNMI {}
