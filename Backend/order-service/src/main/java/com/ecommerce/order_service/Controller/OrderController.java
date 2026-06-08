package com.ecommerce.order_service.Controller;

import com.ecommerce.order_service.Dto.Request.CreateOrderRequest;
import com.ecommerce.order_service.Dto.Response.OrderResponse;
import com.ecommerce.order_service.Dto.Response.VerifyPurchaseResponse;
import com.ecommerce.order_service.Mapper.OrderMapper;
import com.ecommerce.order_service.Service.InvoiceService;
import com.ecommerce.order_service.Service.OrderService;
import com.ecommerce.order_service.Validator.OrderRequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.ecommerce.order_service.Constant.AppConstants.HEADER_USER_ID;

@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Order management, cancellation, invoice generation, and purchase verification")
public class OrderController {

    private final OrderService orderService;
    private final InvoiceService invoiceService;
    private final OrderMapper orderMapper;
    private final OrderRequestValidator validator;

    public OrderController(OrderService orderService,
                           InvoiceService invoiceService,
                           OrderMapper orderMapper,
                           OrderRequestValidator validator) {
        this.orderService = orderService;
        this.invoiceService = invoiceService;
        this.orderMapper = orderMapper;
        this.validator = validator;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create order", description = "Create a new order for the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    public Mono<OrderResponse> createOrder(
            @Parameter(description = "Authenticated user ID", required = true)
            @RequestHeader(HEADER_USER_ID) String userId,
            @Valid @RequestBody CreateOrderRequest request) {
        validator.validateUserId(userId);
        return orderService.createOrder(userId, request.items())
                .map(orderMapper::toResponse);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order", description = "Retrieve an order by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public Mono<OrderResponse> getOrder(
            @Parameter(description = "Order ID") @PathVariable String orderId) {
        validator.validateOrderId(orderId);
        return orderService.getOrder(orderId).map(orderMapper::toResponse);
    }

    @GetMapping
    @Operation(summary = "Get user orders", description = "List all orders for the authenticated user, newest first")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders retrieved")
    })
    public Flux<OrderResponse> getUserOrders(
            @Parameter(description = "Authenticated user ID", required = true)
            @RequestHeader(HEADER_USER_ID) String userId) {
        validator.validateUserId(userId);
        return orderService.getOrdersByUserId(userId).map(orderMapper::toResponse);
    }

    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel a pending or inventory-reserved order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order cancelled"),
            @ApiResponse(responseCode = "409", description = "Order cannot be cancelled")
    })
    public Mono<OrderResponse> cancelOrder(
            @Parameter(description = "Order ID") @PathVariable String orderId,
            @Parameter(description = "Authenticated user ID", required = true)
            @RequestHeader(HEADER_USER_ID) String userId) {
        validator.validateOrderId(orderId);
        validator.validateUserId(userId);
        return orderService.cancelOrder(orderId, userId).map(orderMapper::toResponse);
    }

    @GetMapping("/{orderId}/invoice")
    @Operation(summary = "Download invoice", description = "Generate and download a PDF invoice for the order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invoice PDF generated"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public Mono<ResponseEntity<byte[]>> getInvoice(
            @Parameter(description = "Order ID") @PathVariable String orderId) {
        validator.validateOrderId(orderId);
        return invoiceService.generateInvoice(orderId)
                .map(pdf -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=invoice-" + orderId.substring(0, Math.min(8, orderId.length())) + ".pdf")
                        .contentType(MediaType.APPLICATION_PDF)
                        .contentLength(pdf.length)
                        .body(pdf));
    }

    @GetMapping("/verify-purchase")
    @Operation(summary = "Verify purchase", description = "Check whether a user has purchased a specific product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verification result returned"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public Mono<VerifyPurchaseResponse> verifyPurchase(
            @Parameter(description = "User ID to check") @RequestParam String userId,
            @Parameter(description = "Product ID to verify") @RequestParam String productId) {
        validator.validateUserId(userId);
        validator.validateProductId(productId);
        return orderService.hasUserPurchasedProduct(userId, productId)
                .map(purchased -> new VerifyPurchaseResponse(userId, productId, purchased));
    }
}
