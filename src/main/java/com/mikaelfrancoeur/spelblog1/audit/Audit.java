package com.mikaelfrancoeur.spelblog1.audit;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {
    AuditAction action();

    String expression();
}
