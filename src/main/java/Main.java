import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Main {
    private static final Map<String, String> orderAliasMap = new HashMap<>();
    private static ShopService shopService;

    public static void main(String[] args) {
        ProductRepo productRepo = new ProductRepo();
        OrderRepo orderRepo = new OrderMapRepo();
        IdService idService = new IdService();

        shopService = new ShopService(productRepo, orderRepo, idService);

        productRepo.addProduct(new Product("2","Orange"));
        productRepo.addProduct(new Product("3","Kiwi"));
        productRepo.addProduct(new Product("4","Strawberry"));

//        Order order1 = shopService.addOrder(List.of("1","2"));
//        Order order2 = shopService.addOrder(List.of("3","4"));
//        Order order3 = shopService.addOrder(List.of("1","2","3","4"));

//        System.out.println("Orders in PROCESSING status:");
//        List<Order> processingOrders = shopService.getOrdersByStatus(OrderStatus.PROCESSING);
//        processingOrders.forEach(order -> System.out.println("Order Id: " + order.id() + ", Status: " + order.status() + ", Creating time: " + order.timestamp()));

//        System.out.println("\nFinding the oldest orders per status:");
//        shopService.updateOrder(order2.id(),OrderStatus.IN_DELIVERY);
//        Map<OrderStatus,Order> oldestOrders = shopService.getOldestOrderPerStatus();
//        oldestOrders.forEach((orderStatus, order) -> System.out.println("Oldest order Id: " + order.id() + ", Status: " + order.status() + ", Creating time: " + order.timestamp()));

        System.out.println("\nWorking with transaction file:");
        try (BufferedReader reader = new BufferedReader(new FileReader("transactions.txt"))) {
            String line;
            while ((line = reader.readLine()) != null){
                processFileLine(line.trim());
            }
        }
        catch (IOException e){
            System.out.println("Error while file reading: " + e.getMessage());
        }
    }

    public static void processFileLine(String line) {
        String[] words = line.split(" ");
        if (words.length == 0) {
            return;
        }
        switch (words[0]) {
            case "addOrder":
                executeAddOrder(words);
                break;
            case "setStatus":
                executeSetStatus(words);
                break;
            case "printOrders":
                executePrintOrders();
                break;
            default:
                System.out.println("Unknown command: " + words[0]);
        }
    }

    private static void executePrintOrders() {
        System.out.println("Printing all orders in shopService:");
        List<Order> orders = shopService.getAllOrders();
        orders.forEach(order ->
                System.out.println("Order ID: " + order.id() + ", Status: " + order.status() + ", Creating time: " + order.timestamp() + ", List of products: " + order.products())
        );
    }

    private static void executeAddOrder(String[] parts) {
        if (parts.length < 2) {
            System.err.println("Invalid addOrder command");
            return;
        }

        String alias = parts[1];
        List<String> productIds = List.of(parts).subList(2, parts.length);
        if (productIds.isEmpty()) {
            System.err.println("No product Ids provided for addOrder command");
            return;
        }

        Order newOrder = shopService.addOrder(productIds);
        if (newOrder != null) {
            orderAliasMap.put(alias, newOrder.id());
            System.out.println("Order added with alias: " + alias + " and Id: " + newOrder.id() + " List of products: " + newOrder.products());
        } else {
            System.err.println("Failed to add order for alias: " + alias);
        }
    }

    private static void executeSetStatus(String[] parts) {
        if (parts.length != 3) {
            System.err.println("Invalid setStatus command");
            return;
        }

        String alias = parts[1];
        String statusString = parts[2];
        OrderStatus status;
        try {
            status = OrderStatus.valueOf(statusString);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid status: " + statusString);
            return;
        }

        String orderId = orderAliasMap.get(alias);
        if (orderId == null) {
            System.err.println("No order found for alias: " + alias);
            return;
        }

        Optional<Order> orderOpt = shopService.getOrdersByStatus(OrderStatus.PROCESSING).stream()
                .filter(order -> order.id().equals(orderId))
                .findFirst();

        if (orderOpt.isPresent()) {
            shopService.updateOrder(orderId, status);
            System.out.println("Order with alias " + alias + " updated to status " + status);
        } else {
            System.err.println("Order with Id " + orderId + " not found");
        }
    }
}
