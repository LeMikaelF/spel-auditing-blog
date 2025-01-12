package com.mikaelfrancoeur.spelblog1.audit;

import java.util.Collection;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
public class AuditService {

    public void audit(AuditAction action, String auditableId) {
        // ...
    }
}
