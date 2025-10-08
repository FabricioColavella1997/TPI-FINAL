package com.java2.tpi2SQLite;

import com.java2.tpi2SQLite.domain.Product;
import com.java2.tpi2SQLite.repository.ProductRepository;
import com.java2.tpi2SQLite.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

class ProductServiceTest {

    private ProductRepository repository;
    private ProductService service;

    @BeforeEach
    void setUp() {
        repository = mock(ProductRepository.class);
        service = new ProductService(repository);
    }

    @Test
    void testSave() {
        Product product = new Product();
        when(repository.save(product)).thenReturn(product);
        assertEquals(product, service.save(product));
    }

    @Test
    void testFindById() {
        Product product = new Product();
        when(repository.findById(1L)).thenReturn(Optional.of(product));
        assertTrue(service.findById(1L).isPresent());
    }

    @Test
    void testFindAll() {
        List<Product> products = Arrays.asList(new Product(), new Product());
        when(repository.findAll()).thenReturn(products);
        assertEquals(2, service.findAll().size());
    }

    @Test
    void testUpdate() {
        Product product = new Product();
        when(repository.save(product)).thenReturn(product);
        assertEquals(product, service.update(product));
    }

    @Test
    void testDeleteById() {
        service.deleteById(1L);
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void testFindAllPaged() {
        List<Product> products = Arrays.asList(new Product(), new Product());
        Page<Product> page = new PageImpl<>(products);
        Pageable pageable = mock(Pageable.class);
        when(repository.findAll(pageable)).thenReturn(page);
        assertEquals(2, service.findAllPaged(pageable).getContent().size());
    }

    @Test
    void testGetSync() {
        Product p1 = new Product();
        p1.setPrecio(10.0);
        Product p2 = new Product();
        p2.setPrecio(20.0);
        when(repository.findAll()).thenReturn(Arrays.asList(p1, p2));
        var result = service.getSync();
        assertEquals(2, result.get("total_productos"));
        assertEquals(30.0, result.get("suma_precios"));
    }

    @Test
    void testGetAsync() throws Exception {
        Product p1 = new Product();
        Product p2 = new Product();
        when(repository.findAll()).thenReturn(Arrays.asList(p1, p2));
        ExecutorService testExecutor = java.util.concurrent.Executors.newSingleThreadExecutor();
        CompletableFuture<String> future = service.getAsync(testExecutor);
        assertEquals("Procesados 2 productos", future.get());
        testExecutor.shutdown();
    }
}
