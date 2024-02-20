package dev.multithread.concurrency;

import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class ConcurrentService {

    /**
     * Simulates making a request to a third
     * party service which will take approximately
     * `i` seconds.
     *
     * @param i The duration in seconds for each request
     *          to simulate.
     * @return a unique string or an error message when a
     * thread is waiting, sleeping, or otherwise occupied,
     * and the thread is interrupted, either before or
     * during the activity.
     * */
    public String externalSource(int i) {
        try {
            Thread.sleep(Duration.ofSeconds(i));
            return "source " + i;
        } catch (InterruptedException e) {
            return e.getMessage();
        }
    }

}
