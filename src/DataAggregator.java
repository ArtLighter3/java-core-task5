import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class DataAggregator {

    record ProductInfo(String name, double price, String description, double rating) {

    }

    Random random = new Random();

    ProductInfo aggregateProductInfo(String productName) {
        CompletableFuture<Double> priceFuture = CompletableFuture
                .supplyAsync(this::fetchPrice)
                .exceptionally(ex -> 0.0);
        CompletableFuture<String> descFuture = CompletableFuture
                .supplyAsync(this::fetchDescription)
                .exceptionally(ex -> "Нет данных");
        CompletableFuture<Double> ratingFuture = CompletableFuture
                .supplyAsync(this::fetchRating)
                .exceptionally(ex -> 0.0);

        CompletableFuture<ProductInfo> productFuture = CompletableFuture
                .allOf(priceFuture, descFuture, ratingFuture)
                .thenApply(v ->
                        new ProductInfo(productName,
                                priceFuture.resultNow(),
                                descFuture.resultNow(),
                                ratingFuture.resultNow()
                        )
                );

        return productFuture.join();
    }

    private double fetchPrice() {
        imitateFetching(1000, 3000, 0.2);
        return Math.round((random.nextDouble(2000.0) + 10.0) * 100.0) / 100.0;
    }

    private String fetchDescription() {
        imitateFetching(1000, 3000, 0.2);
        return "Описание";
    }

    private double fetchRating() {
        imitateFetching(1000, 3000, 0.2);
        return Math.round(random.nextDouble(10) * 10.0) / 10.0;
    }


    private void imitateFetching(int lowerSleepBound, int upperSleepBound, double exceptionChance) {
        try {
            Thread.sleep(random.nextInt(upperSleepBound - lowerSleepBound) + lowerSleepBound);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException();
        }

        if (random.nextDouble() < exceptionChance) {
            throw new RuntimeException();
        }
    }

    public static void main(String[] args) {
        DataAggregator dataAggregator = new DataAggregator();

        System.out.println(dataAggregator.aggregateProductInfo("Ноутбук"));
        System.out.println(dataAggregator.aggregateProductInfo("Ноутбук2"));
        System.out.println(dataAggregator.aggregateProductInfo("Ноутбук3"));
    }

}
