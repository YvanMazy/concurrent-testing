# 🧵 concurrent-testing

Very small Java library for doing concurrency tests during unit testing. It's a tool that I use for personal projects,
it is not intended to be really maintained.

Here is a small example of use:

```java
@RepeatedTest(10) // To reduce randomness, use repeated test
void testAtomicCounter() {
    final Counter counter = new AtomicCounter();
    ConcurrentTester.start(() -> {
        // Code executed in different threads
        counter.increment();
    }, THREADS, ITERATIONS);
    assertEquals(THREADS * ITERATIONS, counter.get());
}
```

To use the project as a dependency, you can add it using [Maven](https://maven.apache.org/)
or [Gradle](https://gradle.org/).
<br>**Last version**: [![Release](https://jitpack.io/v/Darkkraft/concurrent-testing.svg)](https://jitpack.io/#Darkkraft/concurrent-testing)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.Darkkraft:concurrent-testing:VERSION'
}
```

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupId>com.github.Darkkraft</groupId>
        <artifactId>concurrent-testing</artifactId>
        <version>VERSION</version>
    </dependency>
</dependencies>
```