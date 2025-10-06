package com.java2.tpi2SQLite.service;

import com.java2.tpi2SQLite.domain.Product;
import com.java2.tpi2SQLite.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ProductService {

    private final ProductRepository repository;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    @Autowired
    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }


    public Map<String, Object> getSync() {
        int total = findAll().size();
        double sumaPrecios = findAll().stream().mapToDouble(Product::getPrecio).sum();
        return Map.of(
                "total_productos", total,
                "suma_precios", sumaPrecios
        );
    }

    public CompletableFuture<String> getAsync(ExecutorService executor) {
        return CompletableFuture.supplyAsync(() -> {
            int total = findAll().size();
            return "Procesados " + total + " productos";
        }, executor);
    }

    public Product save(Product product) {
        return repository.save(product);
    }

    public Optional<Product> findById(Long id) {
        return repository.findById(id);
    }

    public List<Product> findAll() {
        return repository.findAll();
    }

    public Product update(Product product) {
        return repository.save(product);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public Page<Product> findAllPaged(Pageable pageable) {
        return repository.findAll(pageable);
    }

}
