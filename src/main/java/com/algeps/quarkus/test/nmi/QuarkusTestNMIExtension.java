package com.algeps.quarkus.test.nmi;

import io.quarkus.test.junit.QuarkusTestExtension;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Method;

public class QuarkusTestNMIExtension extends QuarkusTestExtension {

  @Override
  public void interceptTestTemplateMethod(
      Invocation<Void> invocation,
      ReflectiveInvocationContext<Method> invocationContext,
      ExtensionContext extensionContext)
      throws Throwable {

    invocation.proceed();
  }
}
