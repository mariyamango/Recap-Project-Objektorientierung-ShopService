import java.util.List;

public class Main {
    public static void main(String[] args) {
        ProductRepo productRepo = new ProductRepo();
        OrderRepo orderRepo = new OrderMapRepo();

        ShopService shopService = new ShopService(productRepo, orderRepo);

        productRepo.addProduct(new Product("2","Orange"));
        productRepo.addProduct(new Product("3","Kiwi"));
        productRepo.addProduct(new Product("4","Strawberry"));

        shopService.addOrder(List.of("1","2"));
        shopService.addOrder(List.of("3","4"));
        shopService.addOrder(List.of("1","2","3","4"));

        System.out.println(shopService.getOrdersByStatus(OrderStatus.PROCESSING));
    }
}
