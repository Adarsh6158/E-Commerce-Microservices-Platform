package com.ecommerce.order_service.Controller;

import com.ecommerce.order_service.Domain.Order;
import com.ecommerce.order_service.Dto.Response.OrderResponse;
import com.ecommerce.order_service.Mapper.OrderMapper;
import com.ecommerce.order_service.Service.InvoiceService;
import com.ecommerce.order_service.Service.OrderService;
import com.ecommerce.order_service.Validator.OrderRequestValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderService orderService;

    @MockBean
    private InvoiceService invoiceService;

    @MockBean
    private OrderMapper orderMapper;

    @MockBean
    private OrderRequestValidator validator;

    @Test
    void createOrder_shouldReturn201() {
        Order order = buildOrder();
        OrderResponse response = buildResponse();

        when(orderService.createOrder(eq("user-1"), any())).thenReturn(Mono.just(order));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(response);

        webTestClient.post().uri("/orders")
                .header("X-User-Id", "user-1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(
                        "{\"items\":[{\"productId\":\"p1\",\"sku\":\"SKU-1\",\"productName\":\"Test\",\"quantity\":1,\"unitPrice\":10.00}]}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("order-1")
                .jsonPath("$.status").isEqualTo("CONFIRMED");
    }

    @Test
    void getOrder_shouldReturn200() {
        Order order = buildOrder();
        OrderResponse response = buildResponse();

        when(orderService.getOrder("order-1")).thenReturn(Mono.just(order));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(response);

        webTestClient.get().uri("/orders/order-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("order-1");
    }

    @Test
    void getUserOrders_shouldReturnList() {
        Order order = buildOrder();
        OrderResponse response = buildResponse();

        when(orderService.getOrdersByUserId("user-1")).thenReturn(Flux.just(order));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(response);

        webTestClient.get().uri("/orders")
                .header("X-User-Id", "user-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("order-1");
    }

    @Test
    void cancelOrder_shouldReturn200() {
        Order order = buildOrder();
        OrderResponse response = buildResponse();

        when(orderService.cancelOrder("order-1", "user-1")).thenReturn(Mono.just(order));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(response);

        webTestClient.put().uri("/orders/order-1/cancel")
                .header("X-User-Id", "user-1")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getInvoice_shouldReturnPdf() {
        byte[] pdfBytes = "fake-pdf".getBytes();
        when(invoiceService.generateInvoice("order-1")).thenReturn(Mono.just(pdfBytes));

        webTestClient.get().uri("/orders/order-1/invoice")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF);
    }

    @Test
    void verifyPurchase_shouldReturnResult() {
        when(orderService.hasUserPurchasedProduct("user-1", "prod-1"))
                .thenReturn(Mono.just(true));

        webTestClient.get().uri("/orders/verify-purchase?userId=user-1&productId=prod-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.userId").isEqualTo("user-1")
                .jsonPath("$.productId").isEqualTo("prod-1")
                .jsonPath("$.purchased").isEqualTo(true);
    }

    @Test
    void verifyPurchase_shouldReturnFalse() {
        when(orderService.hasUserPurchasedProduct("user-1", "prod-999"))
                .thenReturn(Mono.just(false));

        webTestClient.get().uri("/orders/verify-purchase?userId=user-1&productId=prod-999")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.purchased").isEqualTo(false);
    }

    private Order buildOrder() {
        Order order = new Order();
        order.setId("order-1");
        order.setUserId("user-1");
        order.setStatus("CONFIRMED");
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setCorrelationId("corr-1");
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        order.setItems(List.of());
        return order;
    }

    private OrderResponse buildResponse() {
        return new OrderResponse(
                "order-1", "user-1", "CONFIRMED",
                new BigDecimal("100.00"), null, null,
                "corr-1", null, List.of(),
                Instant.now(), Instant.now());
    }
}
