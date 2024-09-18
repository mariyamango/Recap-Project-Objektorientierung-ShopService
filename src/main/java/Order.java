import java.util.List;

public record Order(
        String id,
        OrderStatus status,
        List<Product> products
) {
}
