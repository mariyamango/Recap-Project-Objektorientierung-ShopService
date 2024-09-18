import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
}
