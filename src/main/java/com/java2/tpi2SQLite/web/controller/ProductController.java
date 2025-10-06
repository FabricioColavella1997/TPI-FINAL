package com.java2.tpi2SQLite.web.controller;

import com.java2.tpi2SQLite.domain.Product;
import com.java2.tpi2SQLite.service.ProductService;
import com.java2.tpi2SQLite.web.dto.ProductRequestDTO;
import com.java2.tpi2SQLite.web.dto.ProductResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService service;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    @Autowired
    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping("/sync")
    public ResponseEntity<Map<String, Object>> sync() {
        Map<String, Object> stats = service.getSync();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/async")
    public CompletableFuture<ResponseEntity<String>> async() {
        return service.getAsync(executor)
                .thenApply(ResponseEntity::ok);
    }
    @GetMapping("/demo")
    public ResponseEntity<Map<String, Object>> demoSyncAsync() {
        long t0 = System.currentTimeMillis();

        long syncStart = System.currentTimeMillis();
        Map<String, Object> syncStats = service.getSync();
        long syncEnd = System.currentTimeMillis();

        long asyncStart = System.currentTimeMillis();
        String asyncResult = service.getAsync(executor).join();
        long asyncEnd = System.currentTimeMillis();

        return ResponseEntity.ok(Map.of(
                "sync", Map.of(
                        "start_millis", syncStart,
                        "end_millis", syncEnd,
                        "elapsed_ms", (syncEnd - syncStart),
                        "result", syncStats
                ),
                "async", Map.of(
                        "start_millis", asyncStart,
                        "end_millis", asyncEnd,
                        "elapsed_ms", (asyncEnd - asyncStart),
                        "result", asyncResult
                ),
                "t0_millis", t0
        ));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getById(@PathVariable Long id) {
        Optional<Product> product = service.findById(id);
        if (product.isPresent()) {
            return ResponseEntity.ok(convertToResponseDTO(product.get()));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> create(@RequestBody ProductRequestDTO dto) {
        Product product = new Product(dto.getNombre(), dto.getPrecio());
        Product savedProduct = service.save(product);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(convertToResponseDTO(savedProduct));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> update(@PathVariable Long id, @RequestBody ProductRequestDTO dto) {
        Optional<Product> existingProduct = service.findById(id);
        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            product.setNombre(dto.getNombre());
            product.setPrecio(dto.getPrecio());
            Product updatedProduct = service.update(product);
            return ResponseEntity.ok(convertToResponseDTO(updatedProduct));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Optional<Product> product = service.findById(id);
        if (product.isPresent()) {
            service.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private ProductResponseDTO convertToResponseDTO(Product product) {
        return new ProductResponseDTO(product.getId(), product.getNombre(), product.getPrecio());
    }

    @GetMapping("/paged")
    public Page<Product> getProductsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return service.findAllPaged(pageable);
    }


}
