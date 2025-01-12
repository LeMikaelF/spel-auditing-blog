package com.mikaelfrancoeur.spelblog1.audit;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.Assert;

import lombok.RequiredArgsConstructor;

@Aspect
@RequiredArgsConstructor
public class AuditAspect {

    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final AuditService auditService;

    @After("@annotation(auditAnnotation)")
    void audit(JoinPoint joinPoint, Audit auditAnnotation) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Expression expression = parser.parseExpression(auditAnnotation.expression());
        evaluateExpression(joinPoint, signature, expression);

        Collection<String> auditableIds = evaluateExpression(joinPoint, signature, expression);
        auditableIds.forEach(id -> auditService.audit(auditAnnotation.action(), id));
    }

    private Collection<String> evaluateExpression(JoinPoint joinPoint, MethodSignature signature, Expression expression) {
        EvaluationContext context = new MethodBasedEvaluationContext(
                new Object(),
                signature.getMethod(),
                joinPoint.getArgs(),
                new DefaultParameterNameDiscoverer());

        Object result = expression.getValue(context);
        Objects.requireNonNull(result, "expression of @Audit evaluated to null");

        return switch (result) {
            case String string -> List.of(string);
            case Collection<?> collection -> {
                collection.forEach(element -> Assert.isInstanceOf(String.class, element,
                        () -> "@Audit expression evaluated to collection with non-string element"));
                //noinspection unchecked
                yield (Collection<String>) collection;
            }
            default -> throw new RuntimeException("@Audit expression evaluated to non-string type %s".formatted(result.getClass().getName()));
        };
    }
}
