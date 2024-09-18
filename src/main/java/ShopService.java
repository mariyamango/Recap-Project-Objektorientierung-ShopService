import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ShopService {
    @Getter
    private final ProductRepo productRepo;
    private final OrderRepo orderRepo;
    private final IdService idService;

    public Order addOrder(Map<Product,Double> productIdsAndQuantities) {
        List<Product> products = new ArrayList<>();
        for (Product productFromMap : productIdsAndQuantities.keySet()) {
            Optional<Product> productToOrder = productRepo.getProductById(productFromMap.id());
            if (productToOrder.isEmpty()) {
                throw new ProductNotFoundException("Product with ID: " + productFromMap.id() + " could not be found.");
            }
            Product product = productToOrder.get();

            if (!productRepo.reduceProductQuantity(product, productIdsAndQuantities.get(product))) {
                System.out.println("Not enough quantity on stock for product: " + product.name());
                return null;
            }
            products.add(product);
        }
        Order newOrder = new Order(idService.generateId(), OrderStatus.PROCESSING, products, Instant.now());
        return orderRepo.addOrder(newOrder);
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepo.getOrders().stream()
                .filter(order -> order.status().equals(status))
                .toList();
        return orders;
    }

    public void updateOrder(String orderId, OrderStatus status) {
        orderRepo.getOrders().stream()
                .filter(order -> order.id().equals(orderId))
                .findFirst()
                .ifPresent(order -> {
                    orderRepo.removeOrder(orderId);
                    Order updatedOrder = order.withStatus(status);
                    orderRepo.addOrder(updatedOrder);
                });
    }

    public Map<OrderStatus,Order> getOldestOrderPerStatus() {
        return orderRepo.getOrders().stream()
                .collect(Collectors.groupingBy(
                        Order::status,
                        Collectors.minBy(Comparator.comparing(Order::timestamp))))
                .entrySet().stream()
                .filter(element -> element.getValue().isPresent())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().get()
                ));
    }

    public List<Order> getAllOrders() {
        return orderRepo.getOrders();
    }
}
