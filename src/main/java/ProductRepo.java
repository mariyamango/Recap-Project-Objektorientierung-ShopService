import java.util.*;

public class ProductRepo {
    private Map<Product,Double> products;

    public ProductRepo() {
        products = new HashMap<>();
        products.put(new Product("1", "Apfel"), 10.0);
    }

    public List<Product> getProducts() {
        return products.keySet().stream().toList();
    }

    public Optional<Product> getProductById(String id) {
        for (Product product : products.keySet()) {
            if (product.id().equals(id)) {
                return Optional.of(product);
            }
        }
        return Optional.empty();
    }

    public Product addProduct(Product newProduct, double quantity) {
        products.put(newProduct, quantity);
        return newProduct;
    }

    public void removeProduct(String id) {
        for (Product product : products.keySet()) {
           if (product.id().equals(id)) {
               products.remove(product);
               return;
           }
        }
    }

    public boolean reduceProductQuantity(Product product, double quantity) {
        double currentQuantity = products.getOrDefault(product, 0.0);
        if (currentQuantity >= quantity) {
            products.put(product, currentQuantity - quantity);
            return true;
        }
        return false;
    }
}
