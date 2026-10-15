import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ParallelRunner {

    enum Status { PASSED, FAILED }

    record TestResult(String name, Status status, long durationMs) { }

    static Callable<TestResult> fakeTest(String name, long durationMs, Status status) {
        return () -> {
            Thread.sleep(durationMs);
            return new TestResult(name, status, durationMs);
        };
    }

    public static void main(String[] args) throws InterruptedException {
        List<Callable<TestResult>> tasks = List.of(
                fakeTest("LoginTest", 356, Status.PASSED),
                fakeTest("PostTest", 450, Status.PASSED),
                fakeTest("CommentAddTest", 320, Status.PASSED),
                fakeTest("CommentDeleteTest", 415, Status.FAILED),
                fakeTest("PostDeleteTest", 309, Status.PASSED),
                fakeTest("CommentEditTest", 468, Status.FAILED),
                fakeTest("LikeTest", 302, Status.PASSED),
                fakeTest("LogoutTest", 390, Status.PASSED)
        );

        long startTime = System.nanoTime();

        List<TestResult> results = null;
        try (ExecutorService executor = Executors.newFixedThreadPool(4)) {
            results = executor.invokeAll(tasks).stream()
                    .filter(future -> future.state() == Future.State.SUCCESS)
                    .map(Future::resultNow)
                    .toList();
        }

        long parallelTestsDuration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        long serialTestsDuration = results.stream().mapToLong(TestResult::durationMs).sum();
        long diff = serialTestsDuration - parallelTestsDuration;

        Map<Status, List<TestResult>> testsByStatus = results.stream()
                .collect(Collectors.groupingBy(TestResult::status));
        int passed = testsByStatus.getOrDefault(Status.PASSED, Collections.emptyList()).size();
        int failed = testsByStatus.getOrDefault(Status.FAILED, Collections.emptyList()).size();

        System.out.printf("Прошло: %d, упало: %d%n", passed, failed);
        System.out.printf("Суммарное время тестов: %d ms%n", serialTestsDuration);
        System.out.printf("Реальное время прогона: %d ms%n", parallelTestsDuration);
        System.out.printf("Параллельность сэкономила: %d ms%n", diff);
    }

}
