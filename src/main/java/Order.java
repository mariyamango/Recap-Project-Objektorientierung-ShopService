import lombok.With;

import java.util.List;


public record Order(
        String id,
        @With
        OrderStatus status,
        List<Product> products
) {
}
