package io.github.intellinside.cache.tags.annotation;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Utility class for evaluating Spring Expression Language (SpEL) templates in
 * the context
 * of cached method invocations.
 *
 * <p>
 * This evaluator provides methods to parse and evaluate SpEL expressions with
 * access to:
 * <ul>
 * <li>Method arguments by name (requires -parameters compiler flag)</li>
 * <li>Method return value via {@code #result}</li>
 * <li>All method arguments via {@code #args} array</li>
 * <li>Method metadata via {@code #method}</li>
 * </ul>
 *
 * <p>
 * <b>Example expressions:</b>
 * 
 * <pre>
 * \"'user:' + #userId\"           // Concatenates literal with argument
 * \"'result:' + #result.id\"       // Uses property of returned object
 * \"'cache:' + #args[0]\"          // Uses first argument
 * \"#result.type.toString()\"      // Calls methods on result
 * </pre>
 *
 * <p>
 * <b>Thread Safety:</b>
 * This class is thread-safe and uses static instances of
 * {@code ParameterNameDiscoverer}
 * and {@code ExpressionParser}.
 *
 * @author intellinside
 * @see org.springframework.expression.spel.standard.SpelExpressionParser
 * @see org.springframework.core.DefaultParameterNameDiscoverer
 */
public class SpelExpressionEvaluator {
    private static final ParameterNameDiscoverer paramNameDiscoverer = new DefaultParameterNameDiscoverer();
    private static final ExpressionParser parser = new SpelExpressionParser();

    /**
     * Evaluates a SpEL expression within the context of a join point and method
     * result.
     *
     * <p>
     * The expression can reference:
     * <ul>
     * <li>Method parameters by name</li>
     * <li>Method result via {@code #result}</li>
     * <li>All arguments via {@code #args}</li>
     * <li>Method object via {@code #method}</li>
     * </ul>
     *
     * @param template the SpEL expression to evaluate
     * @param jp       the join point providing method and arguments context
     * @param result   the return value of the method
     * @return the string result of the expression evaluation
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
     * The expression can reference method parameters by name, result object,
     * arguments array, and method metadata.
     *
     * @param template the SpEL expression to evaluate
     * @param method   the method being invoked
     * @param result   the return value of the method
     * @param args     the arguments passed to the method
     * @return the string result of the expression evaluation
     */
    public String evaluate(String template, Method method, Object result, Object... args) {
        String[] paramNames = paramNameDiscoverer.getParameterNames(method);

        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("args", args);
        context.setVariable("result", result);
        context.setVariable("method", method);

        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        return parser.parseExpression(template).getValue(context, String.class);
    }
}
