import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        ProductRepo productRepo = new ProductRepo();
        OrderRepo orderRepo = new OrderMapRepo();
        IdService idService = new IdService();

        ShopService shopService = new ShopService(productRepo, orderRepo, idService);

        productRepo.addProduct(new Product("2","Orange"));
        productRepo.addProduct(new Product("3","Kiwi"));
        productRepo.addProduct(new Product("4","Strawberry"));

        Order order1 = shopService.addOrder(List.of("1","2"));
        Order order2 = shopService.addOrder(List.of("3","4"));
        Order order3 = shopService.addOrder(List.of("1","2","3","4"));

        System.out.println("Orders in PROCESSING status:");
        List<Order> processingOrders = shopService.getOrdersByStatus(OrderStatus.PROCESSING);
        processingOrders.forEach(order -> System.out.println("Order Id: " + order.id() + ", Status: " + order.status() + ", Creating time: " + order.timestamp()));

        System.out.println("\nFinding the oldest orders per status:");
        shopService.updateOrder(order2.id(),OrderStatus.IN_DELIVERY);
        Map<OrderStatus,Order> oldestOrders = shopService.getOldestOrderPerStatus();
        oldestOrders.forEach((orderStatus, order) -> System.out.println("Oldest order Id: " + order.id() + ", Status: " + order.status() + ", Creating time: " + order.timestamp()));
    }
}
