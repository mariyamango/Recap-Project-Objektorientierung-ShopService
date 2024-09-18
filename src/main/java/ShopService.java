import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class ShopService {
    private final ProductRepo productRepo;
    private final OrderRepo orderRepo;

    public Order addOrder(List<String> productIds) {
        List<Product> products = new ArrayList<>();
        for (String productId : productIds) {
            Optional<Product> productToOrder = productRepo.getProductById(productId);
            if (productToOrder.isEmpty()) {
                throw new ProductNotFoundException("Product with ID: " + productId + " could not be found.");
            }
            products.add(productToOrder.get());
        }

        Order newOrder = new Order(UUID.randomUUID().toString(), OrderStatus.PROCESSING, products, Instant.now());

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
}
