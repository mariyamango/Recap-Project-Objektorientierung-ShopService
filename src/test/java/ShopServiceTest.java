import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ShopServiceTest {

    @Test
    void addOrderTest() {
        //GIVEN
        ShopService shopService = new ShopService(new ProductRepo(), new OrderMapRepo(), new IdService());
        List<String> productsIds = List.of("1");

        //WHEN
        Order actual = shopService.addOrder(productsIds);

        //THEN
        Order expected = new Order("-1", OrderStatus.PROCESSING, List.of(new Product("1", "Apfel")), Instant.now());
        assertEquals(expected.products(), actual.products());
        assertNotNull(expected.id());
    }

    @Test
    void addOrderTest_whenInvalidProductId_expectException() {
        //GIVEN
        ShopService shopService = new ShopService(new ProductRepo(), new OrderMapRepo(), new IdService());
        List<String> productsIds = List.of("1", "2");

        //WHEN

        //THEN
        assertThrows(ProductNotFoundException.class, () -> shopService.addOrder(productsIds));
    }

    @Test
    void getOrdersByStatus_whenProcessingStatus_expectTwoOrders() {
        //GIVEN
        ShopService shopService = new ShopService(new ProductRepo(), new OrderMapRepo(), new IdService());
        List<String> productsIds = List.of("1");
        shopService.addOrder(productsIds);
        shopService.addOrder(productsIds);

        // WHEN
        List<Order> result = shopService.getOrdersByStatus(OrderStatus.PROCESSING);

        // THEN
        assertEquals(2, result.size());
        assertEquals(OrderStatus.PROCESSING, result.get(0).status());
        assertEquals(OrderStatus.PROCESSING, result.get(1).status());
    }

    @Test
    void updateOrder_whenUpdateToInDelivery_expectChangeStatus() {
        //GIVEN
        ShopService shopService = new ShopService(new ProductRepo(), new OrderMapRepo(), new IdService());
        List<String> productsIds = List.of("1");
        Order order1 = shopService.addOrder(productsIds);

        // WHEN
        shopService.updateOrder(order1.id(), OrderStatus.IN_DELIVERY);

        // THEN
        List<Order> listWithUpdatedOrder = shopService.getOrdersByStatus(OrderStatus.IN_DELIVERY);
        Order actual = listWithUpdatedOrder.stream().filter(order -> order.id().equals(order1.id())).findFirst().get();
        assertEquals(OrderStatus.IN_DELIVERY, actual.status());
    }

    @Test
    void getOldestOrderPerStatus_whenGettingOldOrdersPerStatus_expectMapWithOldestOrders() {
        //GIVEN
        ProductRepo productRepo = new ProductRepo();
        OrderRepo orderRepo = new OrderMapRepo();
        IdService idService = new IdService();
        ShopService shopService = new ShopService(productRepo, orderRepo, idService);
        productRepo.addProduct(new Product("2","Orange"));
        productRepo.addProduct(new Product("3","Kiwi"));
        orderRepo.addOrder(new Order(
                idService.generateId(),
                OrderStatus.PROCESSING,
                List.of(productRepo.getProductById("1").orElseThrow()),
                Instant.parse("2024-09-01T10:00:00Z")
        ));
        orderRepo.addOrder(new Order(
                idService.generateId(),
                OrderStatus.PROCESSING,
                List.of(productRepo.getProductById("2").orElseThrow()),
                Instant.parse("2024-09-02T10:00:00Z")
        ));
        orderRepo.addOrder(new Order(
                idService.generateId(),
                OrderStatus.IN_DELIVERY,
                List.of(productRepo.getProductById("3").orElseThrow()),
                Instant.parse("2024-09-03T10:00:00Z")
        ));
        orderRepo.addOrder(new Order(
                idService.generateId(),
                OrderStatus.IN_DELIVERY,
                List.of(productRepo.getProductById("1").orElseThrow()),
                Instant.parse("2024-09-04T10:00:00Z")
        ));

        // WHEN
        Map<OrderStatus,Order> actualOldestOrders = shopService.getOldestOrderPerStatus();

        // THEN
        assertEquals(2, actualOldestOrders.size());
        Order oldestProcessingOrder = actualOldestOrders.get(OrderStatus.PROCESSING);
        assertEquals("2024-09-01T10:00:00Z", oldestProcessingOrder.timestamp().toString());
        Order oldestInDeliveryOrder = actualOldestOrders.get(OrderStatus.IN_DELIVERY);
        assertEquals("2024-09-03T10:00:00Z", oldestInDeliveryOrder.timestamp().toString());
    }
}
