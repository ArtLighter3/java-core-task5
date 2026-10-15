import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleBlockingQueue<T> {

    private final Queue<T> queue;
    private final int capacity;

    public SimpleBlockingQueue(int capacity) {
        this.capacity = capacity;
        this.queue = new ArrayDeque<>(capacity);
    }

    public void enqueue(T item) throws InterruptedException {
        synchronized (queue) {
            while (queue.size() >= capacity) {
                queue.wait();
            }

            queue.add(item);
            queue.notifyAll();
        }
    }

    public T dequeue() throws InterruptedException {
        synchronized (queue) {
            while (queue.isEmpty()) {
                queue.wait();
            }

            T result = queue.poll();
            queue.notifyAll();
            return result;
        }
    }

    public int size() {
        return queue.size();
    }

    public static void main(String[] args) {
        try (ExecutorService executorService = Executors.newFixedThreadPool(2)) {
            SimpleBlockingQueue<Integer> queue = new SimpleBlockingQueue<>(2);

            executorService.execute(() -> {
                for (int i = 0; i < 5; i++) {
                    try {
                        queue.enqueue(i);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            executorService.execute(() -> {
                for (int i = 0; i < 5; i++) {
                    try {
                        System.out.println(queue.dequeue());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }

    }
}
