import lombok.With;

import java.time.Instant;
import java.util.List;


public record Order(
        String id,
        @With
        OrderStatus status,
        List<Product> products,
        Instant timestamp
) {
}
