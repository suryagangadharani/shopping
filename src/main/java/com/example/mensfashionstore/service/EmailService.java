package com.example.mensfashionstore.service;

import com.example.mensfashionstore.model.Order;
import com.example.mensfashionstore.model.OrderItem;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Async
    public void sendOrderConfirmationEmail(Order order) {
        try {
            System.out.println("=== EMAIL SENDING STARTED ===");
            System.out.println("Order ID: " + order.getId());
            System.out.println("Recipient: " + order.getUser().getEmail());
            System.out.println("Sender Email: " + senderEmail);
            
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(order.getUser().getEmail());
            helper.setSubject("Order Confirmation - Order #" + order.getId());

            String content = buildEmailContent(order);
            helper.setText(content, true);

            // Generate PDF Invoice
            System.out.println("Generating PDF invoice...");
            byte[] pdfBytes = generatePdfInvoice(order);
            helper.addAttachment("Invoice_" + order.getId() + ".pdf", new ByteArrayResource(pdfBytes));

            System.out.println("Sending email...");
            javaMailSender.send(message);
            System.out.println("=== EMAIL SENT SUCCESSFULLY ===");

        } catch (MessagingException e) {
            System.err.println("=== EMAIL SENDING FAILED - MessagingException ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("=== EMAIL SENDING FAILED - IOException ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } catch (DocumentException e) {
            System.err.println("=== EMAIL SENDING FAILED - DocumentException ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("=== EMAIL SENDING FAILED - Unexpected Error ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String buildEmailContent(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>")
          .append("<head>")
          .append("<style>")
          .append("body { font-family: Arial, sans-serif; }")
          .append(".header { background-color: #f8f9fa; padding: 20px; text-align: center; }")
          .append(".content { padding: 20px; }")
          .append(".table { width: 100%; border-collapse: collapse; margin-top: 20px; }")
          .append(".table th, .table td { border: 1px solid #ddd; padding: 8px; text-align: left; }")
          .append(".table th { background-color: #f2f2f2; }")
          .append(".summary { margin-top: 20px; text-align: right; }")
          .append("</style>")
          .append("</head>")
          .append("<body>")
          
          .append("<div class='header'>")
          .append("<h2>Order Confirmation</h2>")
          .append("<p>Order #").append(order.getId()).append("</p>")
          .append("</div>")
          
          .append("<div class='content'>")
          .append("<p>Hi ").append(order.getUser().getFullName()).append(",</p>")
          .append("<p>Thank you for your order! It has been successfully placed.</p>")
          
          // Helper method or inline logic for address
          .append("<h3>Shipment Details</h3>")
          .append("<p><strong>Shipping Address:</strong><br/>")
          .append(order.getShippingAddress().getStreet()).append("<br/>")
          .append(order.getShippingAddress().getCity()).append(", ")
          .append(order.getShippingAddress().getState()).append(" - ")
          .append(order.getShippingAddress().getZipCode()).append("</p>")
          .append("<p><strong>Mobile:</strong> ").append(order.getMobileNumber()).append("</p>")
          
          .append("<h3>Billing Details</h3>")
          .append("<p><strong>Payment Method:</strong> ").append(order.getPaymentMethod()).append("</p>")
          .append("<p><strong>Payment Status:</strong> ").append(order.getPaymentStatus() != null ? order.getPaymentStatus() : "PENDING").append("</p>")
          
          .append("<h4>Order Summary</h4>")
          .append("<table class='table'>")
          .append("<thead><tr><th>Product</th><th>Quantity</th><th>Price</th><th>Total</th></tr></thead>")
          .append("<tbody>");

        for (OrderItem item : order.getItems()) {
            sb.append("<tr>")
              .append("<td>").append(item.getProduct().getName()).append("</td>")
              .append("<td>").append(item.getQuantity()).append("</td>")
              .append("<td>$").append(String.format("%.2f", item.getPrice())).append("</td>") 
              .append("<td>$").append(String.format("%.2f", item.getSubtotal())).append("</td>")
              .append("</tr>");
        }

        sb.append("</tbody>")
          .append("</table>")
          
          .append("<div class='summary'>")
          .append("<h3>Total Amount: $").append(String.format("%.2f", order.getTotalAmount())).append("</h3>")
          .append("</div>")
          
          .append("<p>Please find the attached invoice for your reference.</p>")
          .append("<br/>")
          .append("<p>Best Regards,<br/>Men's Fashion Store Team</p>")
          
          .append("</div>") // content
          .append("</body>")
          .append("</html>");
          
        return sb.toString();
    }

    private byte[] generatePdfInvoice(Order order) throws DocumentException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);

        document.open();

        // Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
        Paragraph title = new Paragraph("INVOICE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);

        // Order Details
        Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        document.add(new Paragraph("Order ID: " + order.getId(), bodyFont));
        document.add(new Paragraph(
                "Order Date: " + order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                bodyFont));
        document.add(new Paragraph("Customer: " + order.getUser().getFullName(),
                bodyFont));
        document.add(new Paragraph("Email: " + order.getUser().getEmail(), bodyFont));
        document.add(new Paragraph("Payment Method: " + order.getPaymentMethod(), bodyFont));
        document.add(new Paragraph(
                "Payment Status: " + (order.getPaymentStatus() != null ? order.getPaymentStatus() : "Pending"),
                bodyFont));
        document.add(Chunk.NEWLINE);

        // Address
        document.add(new Paragraph("Shipping Address:", headFont));
        document.add(new Paragraph(order.getShippingAddress().getStreet(), bodyFont));
        document.add(new Paragraph(order.getShippingAddress().getCity() + ", " + order.getShippingAddress().getState()
                + " - " + order.getShippingAddress().getZipCode(), bodyFont));
        document.add(new Paragraph("Phone: " + order.getMobileNumber(), bodyFont));
        document.add(Chunk.NEWLINE);

        // Table
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 4, 1, 2, 2 });

        // Table Header
        addTableHeader(table, headFont, "Product");
        addTableHeader(table, headFont, "Qty");
        addTableHeader(table, headFont, "Price");
        addTableHeader(table, headFont, "Total");

        // Table Body
        for (OrderItem item : order.getItems()) {
            table.addCell(new Phrase(item.getProduct().getName(), bodyFont));
            table.addCell(new Phrase(String.valueOf(item.getQuantity()), bodyFont));
            table.addCell(new Phrase("$" + String.format("%.2f", item.getPrice()), bodyFont)); // Using purchase price
            table.addCell(new Phrase("$" + String.format("%.2f", item.getSubtotal()), bodyFont));
        }

        document.add(table);
        document.add(Chunk.NEWLINE);

        // Total
        Paragraph total = new Paragraph("Grand Total: $" + String.format("%.2f", order.getTotalAmount()), headFont);
        total.setAlignment(Element.ALIGN_RIGHT);
        document.add(total);

        document.close();
        return out.toByteArray();
    }

    private void addTableHeader(PdfPTable table, Font font, String title) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
        header.setPadding(5);
        header.setPhrase(new Phrase(title, font));
        table.addCell(header);
    }
}
