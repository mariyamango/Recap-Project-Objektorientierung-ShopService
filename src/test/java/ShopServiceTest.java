import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShopServiceTest {

    @Test
    void addOrderTest() {
        //GIVEN
        ShopService shopService = new ShopService();
        List<String> productsIds = List.of("1");

        //WHEN
        Order actual = shopService.addOrder(productsIds);

        //THEN
        Order expected = new Order("-1", OrderStatus.PROCESSING, List.of(new Product("1", "Apfel")));
        assertEquals(expected.products(), actual.products());
        assertNotNull(expected.id());
    }

    @Test
    void addOrderTest_whenInvalidProductId_expectException() {
        //GIVEN
        ShopService shopService = new ShopService();
        List<String> productsIds = List.of("1", "2");

        //WHEN

        //THEN
        assertThrows(ProductNotFoundException.class, () -> shopService.addOrder(productsIds));
    }

    @Test
    void getOrdersByStatus_whenProcessingStatus_expectTwoOrders() {
        //GIVEN
        ShopService shopService = new ShopService();
        List<String> productsIds = List.of("1");
        Order order1 = shopService.addOrder(productsIds);
        Order order2 = shopService.addOrder(productsIds);

        // WHEN
        List<Order> result = shopService.getOrdersByStatus(OrderStatus.PROCESSING);

        // THEN
        assertEquals(2, result.size());
        assertEquals(OrderStatus.PROCESSING, result.get(0).status());
        assertEquals(OrderStatus.PROCESSING, result.get(1).status());
    }
}
