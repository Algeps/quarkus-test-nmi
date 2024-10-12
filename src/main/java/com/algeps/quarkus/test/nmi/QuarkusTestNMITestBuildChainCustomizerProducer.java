package com.algeps.quarkus.test.nmi;

import io.quarkus.builder.BuildChainBuilder;
import io.quarkus.builder.BuildStep;
import io.quarkus.deployment.builditem.TestAnnotationBuildItem;
import io.quarkus.test.junit.buildchain.TestBuildChainCustomizerProducer;
import org.jboss.jandex.Index;

import java.util.function.Consumer;

/**
 * This class adds a stage to the build and launch step of the Quarkus application, in which classes
 * annotated with @QuarkusTestNMI become beans (part of the application context)
 */
public class QuarkusTestNMITestBuildChainCustomizerProducer
    implements TestBuildChainCustomizerProducer {
  @Override
  public Consumer<BuildChainBuilder> produce(Index testClassesIndex) {

    return buildChainBuilder -> {
      BuildStep buildStep =
          (buildContext) ->
              buildContext.produce(new TestAnnotationBuildItem(QuarkusTestNMI.class.getName()));

      buildChainBuilder.addBuildStep(buildStep).produces(TestAnnotationBuildItem.class).build();
    };
  }
}
