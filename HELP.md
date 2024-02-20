# Overview
LEVERAGING MULTITHREADING WITH THE NEW JAVA 21 VIRTUAL THREAD

## About
A simple RESTful Springboot project to test out this new feature and
to see the power of multithreading compared to single threading.

We have three end points in this project:

1. `api/v1/concurrency`: This route doesn't leverage concurrency in java. It
simply makes a call to a third party service and waits for the response in the
same thread.
2. `api/v1/concurrency/future`: Leverages concurrency. It utilizes
CompletableFuture to perform asynchronous calls to the third party service.
3. `api/v1/concurrency/callable`: Leverages concurrency by creating multiple
Callable tasks and assigns them to an executor service.

## Test
To run the test in your terminal, enter `mvn clean spring-boot:test-run`. This
should produce a result showing the time difference between the 3 endpoints.

Single thread time

![single.png](/image/single.png)

Multithreading (ComparableFuture)
![future](/image/comparable.png)

Multithreading (Callable)
![callable](/image/callable.png)

