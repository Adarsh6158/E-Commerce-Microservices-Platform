package com.ecommerce.order_service.Service;

import reactor.core.publisher.Mono;

public interface InvoiceService {

    Mono<byte[]> generateInvoice(String orderId);
}
