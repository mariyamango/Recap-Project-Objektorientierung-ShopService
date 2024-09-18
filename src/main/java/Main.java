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

        productRepo.addProduct(new Product("2","Orange"), 10);
        productRepo.addProduct(new Product("3","Kiwi"), 15);
        productRepo.addProduct(new Product("4","Strawberry"), 5.5);

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
//        if (parts.length < 3 || (parts.length - 2) % 2 != 0) {
//            System.out.println("Invalid addOrder command. Expected format: addOrder <alias> <productId,quantity> ...");
//            return;
//        }

        String orderAlias = parts[1];
        Map<Product, Double> productQuantities = new HashMap<>();

        for (int i = 2; i < parts.length; i++) {
            String[] productQuantity = parts[i].split(",");
            if (productQuantity.length != 2) {
                System.out.println("Invalid product-quantity format for: " + parts[i]);
                return;
            }
            try {
                String productId = productQuantity[0];
                double quantity = Double.parseDouble(productQuantity[1]);
                Product product = shopService.getProductRepo().getProductById(productId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid product Id: " + productId));
                productQuantities.put(product, quantity);
            } catch (NumberFormatException e) {
                System.out.println("Invalid quantity format for productId " + productQuantity[0] + ": " + productQuantity[1]);
                return;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                return;
            }
        }

        Order newOrder = shopService.addOrder(productQuantities);
        orderAliasMap.put(orderAlias, newOrder.id());
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
