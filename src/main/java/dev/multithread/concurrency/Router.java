package dev.multithread.concurrency;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@RestController
@RequestMapping(value = "${api.base.url}")
public class Router {

    private final ConcurrentService service;

    public Router(ConcurrentService service) {
        this.service = service;
    }

    public record Concurrent(String name) {
    }

    private static final int duration = 60;
    private static final List<Concurrent> list = List.of(
            new Concurrent("concurrency one "),
            new Concurrent("concurrency two "),
            new Concurrent("concurrency three "),
            new Concurrent("concurrency four")
    );

    /**
     * Returns a list of {@link Concurrent} objects.
     * This method does not leverage concurrency.
     * Instead, it uses a single thread to make
     * individual calls to a third-party service and
     * waits for each response.
     *
     * @return A list of {@link Concurrent} objects,
     * each representing a response from the third-party
     * service.
     */
    @GetMapping(produces = "application/json")
    public List<Concurrent> single() {
        List<Concurrent> res = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            res.add(new Concurrent(list.get(i).name() + service.externalSource(duration + i)));
        }
        return res;
    }

    /**
     * Returns a {@link CompletableFuture} of a list of
     * {@link Concurrent} objects. This method utilizes
     * concurrency by returning a {@link CompletableFuture}
     * that completes when all asynchronous tasks have finished.
     *
     * @return A {@link CompletableFuture} containing a list of
     * {@link Concurrent} objects, each representing a response
     * from the third-party service.
     */
    @GetMapping("/future")
    public CompletableFuture<List<Concurrent>> future() {
        List<CompletableFuture<Concurrent>> futures = new ArrayList<>();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < list.size(); i++) {
                int temp = i;
                var future = CompletableFuture.supplyAsync(
                        () -> new Concurrent(list.get(temp).name()
                                .concat(" " + service
                                        .externalSource(duration + temp))
                        ),
                        executor
                );
                futures.add(future);
            }
        }

        return CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures
                        .stream()
                        .map(CompletableFuture::join)
                        .toList()
                );
    }

    /**
     * Returns a list of {@link Concurrent} objects using concurrency.
     * This method achieves concurrency by creating multiple
     * {@link Callable} tasks and then assigning them to an
     * {@link ExecutorService}. The {@link ExecutorService} uses the
     * {@code invokeAll} method to execute all {@link Callable} tasks
     * and waits for their completion.
     *
     * @return A list of {@link Concurrent} objects, each representing
     * a response from the third-party service.
     * @throws RuntimeException if any error occurs during the execution
     *                          of the {@link Callable} tasks.
     */
    @GetMapping("/callable")
    public List<Concurrent> callable() {
        List<Callable<String>> callables = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            int temp = i;
            callables
                    .add(() -> list.get(temp).name() + service.externalSource(duration + temp));
        }

        List<Future<String>> futures;
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            futures = executor.invokeAll(callables);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }

        List<Concurrent> res = new ArrayList<>();
        for (Future<String> future : futures) {
            try {
                res.add(new Concurrent(future.get()));
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        return res;
    }

}
