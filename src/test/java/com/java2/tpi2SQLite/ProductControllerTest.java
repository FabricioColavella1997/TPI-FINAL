package com.java2.tpi2SQLite;

import com.java2.tpi2SQLite.domain.Product;
import com.java2.tpi2SQLite.service.ProductService;
import com.java2.tpi2SQLite.web.controller.ProductController;
import com.java2.tpi2SQLite.web.dto.ProductRequestDTO;
import com.java2.tpi2SQLite.web.dto.ProductResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.*;
        import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

class ProductControllerTest {

    private ProductService service;
    private ProductController controller;

    @BeforeEach
    void setUp() {
        service = mock(ProductService.class);
        controller = new ProductController(service);
    }

    @Test
    void testSync() {
        Map<String, Object> stats = Map.of("total_productos", 2, "suma_precios", 30.0);
        when(service.getSync()).thenReturn(stats);
        ResponseEntity<Map<String, Object>> response = controller.sync();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(stats, response.getBody());
    }

    @Test
    void testAsync() throws Exception {
        when(service.getAsync(any())).thenReturn(CompletableFuture.completedFuture("Procesados 2 productos"));
        CompletableFuture<ResponseEntity<String>> future = controller.async();
        assertEquals("Procesados 2 productos", future.get().getBody());
    }

    @Test
    void testGetByIdNotFound() {
        when(service.findById(1L)).thenReturn(Optional.empty());
        ResponseEntity<ProductResponseDTO> response = controller.getById(1L);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testCreate() {
        ProductRequestDTO dto = new ProductRequestDTO("Nuevo", 15.0);
        Product product = new Product("Nuevo", 15.0);
        Product saved = new Product("Nuevo", 15.0);
        when(service.save(any())).thenReturn(saved);
        ResponseEntity<ProductResponseDTO> response = controller.create(dto);
        assertEquals(201, response.getStatusCodeValue());
        assertEquals("Nuevo", response.getBody().getNombre());
    }

    @Test
    void testUpdateFound() {
        ProductRequestDTO dto = new ProductRequestDTO("Actualizado", 20.0);
        Product product = new Product("Viejo", 10.0);
        Product updated = new Product("Actualizado", 20.0);
        when(service.findById(1L)).thenReturn(Optional.of(product));
        when(service.update(any())).thenReturn(updated);
        ResponseEntity<ProductResponseDTO> response = controller.update(1L, dto);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Actualizado", response.getBody().getNombre());
    }

    @Test
    void testUpdateNotFound() {
        ProductRequestDTO dto = new ProductRequestDTO("Actualizado", 20.0);
        when(service.findById(1L)).thenReturn(Optional.empty());
        ResponseEntity<ProductResponseDTO> response = controller.update(1L, dto);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testDeleteFound() {
        Product product = new Product("Test", 10.0);
        when(service.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(service).deleteById(1L);
        ResponseEntity<Void> response = controller.delete(1L);
        assertEquals(204, response.getStatusCodeValue());
    }

    @Test
    void testDeleteNotFound() {
        when(service.findById(1L)).thenReturn(Optional.empty());
        ResponseEntity<Void> response = controller.delete(1L);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testGetProductsPaged() {
        List<Product> products = Arrays.asList(new Product(), new Product());
        Page<Product> page = new PageImpl<>(products);
        when(service.findAllPaged(any(Pageable.class))).thenReturn(page);
        Page<Product> result = controller.getProductsPaged(0, 2);
        assertEquals(2, result.getContent().size());
    }

    @Test
    void testDemoSyncAsync() {
        Map<String, Object> syncStats = Map.of("total_productos", 2, "suma_precios", 30.0);
        when(service.getSync()).thenReturn(syncStats);
        when(service.getAsync(any())).thenReturn(CompletableFuture.completedFuture("Procesados 2 productos"));

        ResponseEntity<Map<String, Object>> response = controller.demoSyncAsync();

        assertEquals(200, response.getStatusCodeValue());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        assertTrue(body.containsKey("sync"));
        assertTrue(body.containsKey("async"));
        assertTrue(body.containsKey("t0_millis"));

        Map<String, Object> syncMap = (Map<String, Object>) body.get("sync");
        Map<String, Object> asyncMap = (Map<String, Object>) body.get("async");

        assertEquals(syncStats, syncMap.get("result"));
        assertEquals("Procesados 2 productos", asyncMap.get("result"));
    }
    @Test
    void testDefaultConstructor() {
        ProductResponseDTO dto = new ProductResponseDTO();
        assertNull(dto.getId());
        assertNull(dto.getNombre());
        assertEquals(0.0, dto.getPrecio());
    }
    @Test
    void testSetId() {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(123L);
        assertEquals(123L, dto.getId());
    }
    @Test
    void testSetNombre() {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setNombre("Producto A");
        assertEquals("Producto A", request.getNombre());
        ProductResponseDTO response = new ProductResponseDTO();
        response.setNombre("Producto A");
        assertEquals("Producto A", response.getNombre());
    }

    @Test
    void testSetPrecio() {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setPrecio(77.7);
        assertEquals(77.7, request.getPrecio());
        ProductResponseDTO response = new ProductResponseDTO();
        response.setPrecio(99.99);
        assertEquals(99.99, response.getPrecio());
    }

    @Test
    void testConstructorWithParams() {
        ProductRequestDTO dto = new ProductRequestDTO("Producto B", 50.5);
        assertEquals("Producto B", dto.getNombre());
        assertEquals(50.5, dto.getPrecio());
    }
}