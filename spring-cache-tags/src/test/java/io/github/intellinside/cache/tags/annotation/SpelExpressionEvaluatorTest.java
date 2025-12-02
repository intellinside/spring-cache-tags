package io.github.intellinside.cache.tags.annotation;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpelExpressionEvaluatorTest {
    private final SpelExpressionEvaluator evaluator = new SpelExpressionEvaluator();

    static class Sample {
        public void foo(String userId, int count) {
        }
    }

    static class Result {
        private final int id;

        Result(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    @Test
    void evaluateWithArgsIndex() throws Exception {
        Method method = Sample.class.getMethod("foo", String.class, int.class);
        String out = evaluator.evaluate("'cache:' + #args[0]", method, null, "123", 5);
        assertEquals("cache:123", out);
    }

    @Test
    void evaluateWithResultProperty() throws Exception {
        Method method = Sample.class.getMethod("foo", String.class, int.class);
        Result result = new Result(42);
        String out = evaluator.evaluate("'result:' + #result.id", method, result, "x", 1);
        assertEquals("result:42", out);
    }

    @Test
    void evaluateWithTemplateParserContext() throws Exception {
        Method method = Sample.class.getMethod("foo", String.class, int.class);
        String out = evaluator.evaluate("User: #{#args[0]}", method, null, "7", 2);
        assertEquals("User: 7", out);
    }

    @Test
    void evaluateWithParamName() throws Exception {
        Method method = Sample.class.getMethod("foo", String.class, int.class);
        String out = evaluator.evaluate("'cache:' + #userId", method, null, "abc", 3);
        assertEquals("cache:abc", out);
    }

    @Test
    void evaluateWithParamNameTemplate() throws Exception {
        Method method = Sample.class.getMethod("foo", String.class, int.class);
        String out = evaluator.evaluate("User: #{#userId}", method, null, "xyz", 4);
        assertEquals("User: xyz", out);
    }

    @Test
    void evaluateMethodVariable() throws Exception {
        Method method = Sample.class.getMethod("foo", String.class, int.class);
        String out = evaluator.evaluate("#method.name", method, null, "a", 0);
        assertEquals(method.getName(), out);
    }
}
