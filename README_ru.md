# QuarkusTestNMI (No Method Inject)

**[ВНИМАНИЕ]** Если имеется какие-либо замечания по поводу кода, остались вопросы или в процессе ознакомления остались
вопросы, то просьба их направить на почту: ***algeps@outlook.com***

Данная библиотека предоставляет возможность тестировать приложения Quarkus совместно с другими библиотеками,
реализующими интерфейс `InvocationInterceptor` от `JUnit` (Например,
[Jazzer](https://github.com/CodeIntelligenceTesting/jazzer))

### Использование

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

### Проблематика

У тестов `Quarkus` имеется возможность внедрять бины прямо в
методы (https://quarkus.io/guides/testing-components#injection). Quarkus в тестах перехватывает управление тестовым
фреймворком `JUnit` и не передаёт управления другим расширениям дальше по цепочке обработки.

Для перехвата управления расширение `QuarkusTestExtension` расширяет интерфейс
`org.junit.jupiter.api.extension.InvocationInterceptor`. В `QuarkusTestExtension` метод `interceptTestMethod`
перехватывает управление фреймворка тестирования `JUnit`.

Метод `interceptTestMethod`, переопределённый в классе `QuarkusTestExtension` передаёт дальнейшую обработку тестового
метода (вызов метода `InvocationInterceptor.Invocation.proceed()`) только в случае, если тестовый метод является
интеграционным или выполняется в нативной среде. В обычном сценарии
выполнения, передача дальнейшим обработчикам пропускается (вызов метода `InvocationInterceptor.Invocation.skip()`).
Реализованный интерфейс `InvocationInterceptor` в `QuarkusTestExtension`, используется для внедрения бинов прямо в
тестовый метод.

### Решение

Унаследываться от класса `QuarkusTestExtension`, переопределив поведения метода, чтобы он всегда вызывал метод
`InvocationInterceptor.Invocation.proceed()`.

Если создать свою аннотацию со своим расширением, то `Quarkus` не будет внедрять тестовый класс в контекст приложения.
Для внедрения бина в контекст необходимо добавить аннотацию бина над классом, например `@Singleton` из `Jakarta`.

Можно добавить в цепочку запуска приложения `Quarkus` свою аннотацию. Тогда все аннотированные с помощью кастомной
аннотации бины будут загружены в контекст и аннотация бина из `jakarta` не потребуется.
Класс [QuarkusTestExtension.TestBuildChainFunction](https://github.com/quarkusio/quarkus/blob/3.15.0/test-framework/junit5/src/main/java/io/quarkus/test/junit/QuarkusTestExtension.java#L1323)
добавляет дополнительные шаги сборки приложения
через [ServiceLoader](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/ServiceLoader.html).
Необходимо выполнить следующие шаги:

1) Реализовать интерфейс `io.quarkus.test.junit.buildchain.TestBuildChainCustomizerProducer`, добавив в метод `produce`
   шаг сборки, с указанием своей аннотации. Интерфейс
   `io.quarkus.test.junit.buildchain.TestBuildChainCustomizerProducer`
   называется [классом SPI](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html).

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

2) Добавить в директорию ресурсов сервис. Директория сервисов (`META-INF/services`) находится в директории ресурсов.
   Создать файл `io.quarkus.test.junit.buildchain.TestBuildChainCustomizerProducer`  и записать туда одну строку:
   свою реализацию интерфейса `io.quarkus.test.junit.buildchain.TestBuildChainCustomizerProducer`.
