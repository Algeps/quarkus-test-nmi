# QuarkusTestNMI (No Method Inject)

**[WARNING]** If you have any remarks about the code, questions remaining, or any inquiries during your review, please
send them to the email: ***algeps@outlook.com***

This library provides the ability to test Quarkus applications in conjunction with other libraries that implement the
[InvocationInterceptor](https://junit.org/junit5/docs/snapshot/api/org.junit.jupiter.api/org/junit/jupiter/api/extension/InvocationInterceptor.html)
interface from `JUnit` (Example, [Jazzer](https://github.com/CodeIntelligenceTesting/jazzer)).

### Use

```java

import com.algeps.quarkus.test.nmi.QuarkusTestNMI;
import org.junit.jupiter.api.Test;

@QuarkusTestNMI
public class TestClass {

    @Test
    void test() {
        assertTrue();
    }
}
```

### Problems

`Quarkus` tests have the capability to inject beans directly into
methods (https://quarkus.io/guides/testing-components#injection). In tests, `Quarkus` intercepts control from the
`JUnit` testing framework and does not pass control to other extensions down the processing chain.

To intercept control, the `QuarkusTestExtension` extends the interface
`org.junit.jupiter.api.extension.InvocationInterceptor`. In `QuarkusTestExtension`, the `interceptTestMethod` method
intercepts control the `JUnit` testing framework.

The `interceptTestMethod` method, implemented in the `QuarkusTestExtension` class, proceeds with further processing of
the test method (invoking `InvocationInterceptor.Invocation.proceed()`) only if the test method is an integration test
or is executed in a native environment. In a normal execution scenario, passing control to subsequent handlers is
skipped (invoking `Invocation Interceptor.Invocation.skip()`). The implemented Invocation Interceptor interface in
`QuarkusTestExtension` is used for injecting beans directly into the test method.

### Solution

Inherit from the `QuarkusTestExtension` class and override the behavior of the method to always call the
`InvocationInterceptor.Invocation.proceed()` method.

If you create your custom annotation with your extension, `Quarkus` will not inject the test class into the application
context. To inject the bean into the context, you need to add a bean annotation above the class, such as `@Singleton`
from `Jakarta`.

You can add your custom annotation to the `Quarkus` application startup chain. Then, all beans annotated with the custom
annotation will be loaded into the context, and the `Jakarta` bean annotation will not be required. The
class [QuarkusTestExtension.TestBuildChainFunction](https://github.com/quarkusio/quarkus/blob/3.15.0/test-framework/junit5/src/main/java/io/quarkus/test/junit/QuarkusTestExtension.java#L1323)
adds additional steps to the application build process
using [ServiceLoader](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/ServiceLoader.html). The
following steps are necessary:

1) Implement the `io.quarkus.test.junit.build chain.TestBuildChainCustomizedProducer` interface, adding a build step in
   the produce method, specifying your custom annotation. The
   `io.quarkus.test.junit.build.chain.TestBuildChainCustomizerProducer` interface is known as
   an [SPI class](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html).

```java
import io.quarkus.builder.BuildChainBuilder;
import io.quarkus.builder.BuildStep;
import io.quarkus.deployment.builditem.TestAnnotationBuildItem;
import io.quarkus.test.junit.buildchain.TestBuildChainCustomizerProducer;
import org.jboss.jandex.Index;

import java.util.function.Consumer;

public class YourCustomBuildChainCustomizerProducer implements TestBuildChainCustomizerProducer {
    @Override
    public Consumer<BuildChainBuilder> produce(Index testClassesIndex) {

        return buildChainBuilder -> {
            BuildStep buildStep =
                    (buildContext) ->
                            buildContext.produce(
                                    new TestAnnotationBuildItem(YourCustomAnnotation.class.getName()));

            buildChainBuilder.addBuildStep(buildStep).produces(TestAnnotationBuildItem.class).build();
        };
    }
}

```

2) Add a service in the resources' directory. The services directory (META-INF/services) is located in the resources'
   directory. Create a file named `io.quarkus.test.junit.buildchain.TestBuildChainCustomizedProducer` and write one
   line into it: your implementation of the `io.quarkus.test.junit.buildchain.TestBuildChainCustomizerProducer`
   interface.