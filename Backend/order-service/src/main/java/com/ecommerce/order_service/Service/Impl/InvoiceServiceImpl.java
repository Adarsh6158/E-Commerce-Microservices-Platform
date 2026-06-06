package com.ecommerce.order_service.Service.Impl;

import com.ecommerce.order_service.Domain.Order;
import com.ecommerce.order_service.Domain.OrderItem;
import com.ecommerce.order_service.Exception.OrderNotFoundException;
import com.ecommerce.order_service.Service.InvoiceService;
import com.ecommerce.order_service.Service.OrderService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm").withZone(ZoneId.systemDefault());

    private final OrderService orderService;

    public InvoiceServiceImpl(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public Mono<byte[]> generateInvoice(String orderId) {
        return orderService.getOrder(orderId)
                .switchIfEmpty(Mono.error(new OrderNotFoundException("Order not found: " + orderId)))
                .flatMap(order -> Mono.fromCallable(() -> buildPdf(order))
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    private byte[] buildPdf(Order order) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(doc, baos);
        doc.open();

        Font titleFont = new Font(Font.HELVETICA, 22, Font.BOLD, new Color(26, 26, 46));
        Font headerFont = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
        Font labelFont = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(100, 100, 120));
        Font valueFont = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(45, 55, 72));
        Font totalFont = new Font(Font.HELVETICA, 14, Font.BOLD, new Color(26, 26, 46));

        PdfPTable headerBar = new PdfPTable(2);
        headerBar.setWidthPercentage(100);
        headerBar.setWidths(new float[]{1, 1});

        PdfPCell brandCell = new PdfPCell(new Phrase("ShopFlux", titleFont));
        brandCell.setBorder(Rectangle.NO_BORDER);
        brandCell.setPaddingBottom(15);
        headerBar.addCell(brandCell);

        PdfPCell invoiceLabel = new PdfPCell(new Phrase("INVOICE",
                new Font(Font.HELVETICA, 28, Font.BOLD, new Color(233, 69, 96))));
        invoiceLabel.setBorder(Rectangle.NO_BORDER);
        invoiceLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        invoiceLabel.setPaddingBottom(15);
        headerBar.addCell(invoiceLabel);

        doc.add(headerBar);

        PdfPTable divider = new PdfPTable(1);
        divider.setWidthPercentage(100);
        PdfPCell divCell = new PdfPCell();
        divCell.setBorder(Rectangle.BOTTOM);
        divCell.setBorderColor(new Color(233, 69, 96));
        divCell.setBorderWidth(2);
        divCell.setFixedHeight(2);
        divider.addCell(divCell);
        doc.add(divider);
        doc.add(new Paragraph(" "));

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1, 1});

        addInfoRow(infoTable, "Order ID", order.getId(), labelFont, valueFont);
        addInfoRow(infoTable, "Status", order.getStatus(), labelFont, valueFont);
        addInfoRow(infoTable, "Date",
                order.getCreatedAt() != null ? DATE_FMT.format(order.getCreatedAt()) : "-",
                labelFont, valueFont);
        addInfoRow(infoTable, "Customer ID", order.getUserId(), labelFont, valueFont);

        doc.add(infoTable);
        doc.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 1.5f, 1.2f, 1.5f, 1.5f});

        Color headerBg = new Color(26, 26, 46);
        String[] headers = {"Product", "SKU", "Qty", "Unit Price", "Subtotal"};
        for (String h : headers) {
            PdfPCell c = new PdfPCell(new Phrase(h, headerFont));
            c.setBackgroundColor(headerBg);
            c.setPadding(8);
            c.setBorderColor(headerBg);
            table.addCell(c);
        }

        Color altRow = new Color(247, 248, 250);
        int row = 0;

        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                Color bg = (row % 2 == 0) ? Color.WHITE : altRow;

                addItemCell(table,
                        item.getProductName() != null ? item.getProductName() : "-",
                        valueFont, bg);
                addItemCell(table,
                        item.getSku() != null ? item.getSku() : "-",
                        valueFont, bg);
                addItemCell(table,
                        String.valueOf(item.getQuantity()),
                        valueFont, bg);
                addItemCell(table,
                        "$" + formatMoney(item.getUnitPrice()),
                        valueFont, bg);
                addItemCell(table,
                        "$" + formatMoney(item.getSubtotal()),
                        valueFont, bg);

                row++;
            }
        }

        doc.add(table);
        doc.add(new Paragraph(" "));

        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(40);
        totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

        PdfPCell totalLabelCell = new PdfPCell(new Phrase("Total Amount", labelFont));
        totalLabelCell.setBorder(Rectangle.TOP);
        totalLabelCell.setBorderColor(new Color(200, 200, 210));
        totalLabelCell.setPadding(8);
        totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalTable.addCell(totalLabelCell);

        PdfPCell totalValue = new PdfPCell(new Phrase(
                "$" + formatMoney(order.getTotalAmount()), totalFont));
        totalValue.setBorder(Rectangle.TOP);
        totalValue.setBorderColor(new Color(200, 200, 210));
        totalValue.setPadding(8);
        totalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalTable.addCell(totalValue);

        doc.add(totalTable);
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph(" "));

        Paragraph footer = new Paragraph(
                "Thank you for shopping with ShopFlux!",
                new Font(Font.HELVETICA, 11, Font.ITALIC, new Color(142, 154, 175)));
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);

        doc.close();
        return baos.toByteArray();
    }

    private void addInfoRow(PdfPTable table, String label, String value,
                            Font labelFont, Font valueFont) {
        PdfPCell lc = new PdfPCell(new Phrase(label, labelFont));
        lc.setBorder(Rectangle.NO_BORDER);
        lc.setPaddingBottom(6);
        table.addCell(lc);

        PdfPCell vc = new PdfPCell(new Phrase(value != null ? value : "-", valueFont));
        vc.setBorder(Rectangle.NO_BORDER);
        vc.setPaddingBottom(6);
        table.addCell(vc);
    }

    private void addItemCell(PdfPTable table, String text, Font font, Color bg) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBackgroundColor(bg);
        c.setPadding(7);
        c.setBorderColor(new Color(230, 230, 235));
        table.addCell(c);
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0.00";
        return amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
