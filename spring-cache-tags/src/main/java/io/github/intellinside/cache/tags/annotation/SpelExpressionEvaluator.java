package io.github.intellinside.cache.tags.annotation;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Simple helper to evaluate SpEL expressions or templates for a method call.
 *
 * <p>
 * The evaluator makes method arguments, the full argument array, the method
 * object and the return value available to expressions (for example,
 * {@code #userId}, {@code #args[0]}, {@code #method}, {@code #result}). It
 * supports both plain SpEL and templates containing "#{...}".
 *
 * <p>
 * Thread-safe: parser and utilities are shared.
 *
 * @author intellinside
 */
public class SpelExpressionEvaluator {
    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();
    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private static final TemplateParserContext TEMPLATE_PARSER_CONTEXT = new TemplateParserContext();

    /**
     * Evaluates a SpEL expression within the context of a join point and method
     * result.
     *
     * <p>
     * Evaluate the given expression or template using information from the
     * provided join point and the method result. Available variables include
     * parameter names, {@code #args}, {@code #method} and {@code #result}.
     *
     * @param template expression or template to evaluate
     * @param jp       the join point for the call
     * @param result   the method return value
     * @return evaluated string (may be {@code null} if expression produces null)
     */
    public String evaluate(String template, JoinPoint jp, Object result) {
        Method method = Optional.ofNullable(jp.getSignature())
                .filter(MethodSignature.class::isInstance)
                .map(MethodSignature.class::cast)
                .map(MethodSignature::getMethod)
                .orElse(null);

        return evaluate(template, method, result, jp.getArgs());
    }

    /**
     * Evaluates a SpEL expression with explicit method and arguments context.
     *
     * <p>
     * Evaluate the expression or template with explicit {@code method} and
     * {@code args}. When parameter names are available they are bound to their
     * corresponding argument values.
     *
     * @param template expression or template to evaluate
     * @param method   the invoked method (may be {@code null})
     * @param result   the method return value
     * @param args     argument values for the method
     * @return evaluated string (may be {@code null} if expression produces null)
     */
    public String evaluate(String template, Method method, Object result, Object... args) {
        String[] paramNames = PARAMETER_NAME_DISCOVERER.getParameterNames(method);

        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("args", args);
        context.setVariable("result", result);
        context.setVariable("method", method);

        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        boolean isTemplate = template.contains("#{");

        if (isTemplate) {
            return PARSER.parseExpression(template, TEMPLATE_PARSER_CONTEXT).getValue(context, String.class);
        }
        return PARSER.parseExpression(template).getValue(context, String.class);
    }
}
