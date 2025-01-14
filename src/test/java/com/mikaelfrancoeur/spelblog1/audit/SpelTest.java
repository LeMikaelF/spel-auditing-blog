package com.mikaelfrancoeur.spelblog1.audit;

import java.util.List;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class SpelTest implements WithAssertions {

    @Test
    void basicSpelExpression() {
        String expression = "#mylist.size";

        Expression parsedExpression = new SpelExpressionParser()
                .parseExpression(expression);

        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("mylist", List.of(1, 2, 3));

        assertThat(parsedExpression.getValue(context))
                .isEqualTo(3);
    }

    private void doSomething(String myArg) {
    }

    @Test
    void methodParamBinding() throws NoSuchMethodException {
        String expression = "#myArg.length";

        Expression parsedExpression = new SpelExpressionParser()
                .parseExpression(expression);

        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(
                new Object(),
                getClass().getDeclaredMethod("doSomething", String.class),
                new Object[] { "the argument" },
                new DefaultParameterNameDiscoverer());

        assertThat(parsedExpression.getValue(context))
                .isEqualTo(12);
    }
}
