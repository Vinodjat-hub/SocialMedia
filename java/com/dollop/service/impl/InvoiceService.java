package com.dollop.service.impl;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dollop.entity.Order;
import com.dollop.entity.Users;
import com.dollop.repository.UserRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;

@Service
public class InvoiceService {

	@Autowired
	private UserRepository userRepository;

	public byte[] generateInvoice(Order order, Long userId) {
		Optional<Users> user = userRepository.findById(userId);

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			PdfWriter writer = new PdfWriter(baos);
			PdfDocument pdf = new PdfDocument(writer);
			Document document = new Document(pdf);

			// Company Header
			Paragraph company = new Paragraph("My E-Commerce Pvt. Ltd.").setFontSize(18).setBold()
					.setTextAlignment(TextAlignment.CENTER);
			document.add(company);

			Paragraph address = new Paragraph(
					"123 Business Street, Indore, India\nPhone: +91-8818847240 | Email: vinodjat8818@gmail.com")
					.setFontSize(10).setTextAlignment(TextAlignment.CENTER);
			document.add(address);

			document.add(new LineSeparator(new SolidLine()));

			// Invoice Title
			document.add(new Paragraph("INVOICE").setFontSize(16).setBold().setTextAlignment(TextAlignment.CENTER)
					.setMarginTop(15));

			document.add(new Paragraph("\n"));

			// Invoice Metadata
			Table invoiceTable = new Table(2).useAllAvailableWidth();
			invoiceTable.addCell(new Cell().add(new Paragraph("Invoice No: INV-" + order.getOrderId())));
			invoiceTable.addCell(new Cell().add(new Paragraph("Order Date: " + order.getOrderDate())));
			invoiceTable.addCell(new Cell().add(new Paragraph("Payment Mode: " + "Case On Delivery")));
			invoiceTable.addCell(new Cell().add(new Paragraph("Status: " + order.getStatus())));
			document.add(invoiceTable);

			document.add(new Paragraph("\n"));

			// Customer Info
			Paragraph customerTitle = new Paragraph("Bill To:").setBold().setUnderline();
			document.add(customerTitle);

			Paragraph customer = new Paragraph("Name : " + user.get().getName() + "\n" + "Email : "
					+ user.get().getEmail() + "\n" + "Invoice download date : " + LocalDate.now() + "\n")
					.setFontSize(11);
			document.add(customer);

			document.add(new Paragraph("\n"));

			// Order Items Table
			Table table = new Table(new float[] { 4, 1, 2, 2 }).useAllAvailableWidth();
			table.addHeaderCell(new Cell().add(new Paragraph("Product")).setBold());
			table.addHeaderCell(new Cell().add(new Paragraph("Qty")).setBold());
			table.addHeaderCell(new Cell().add(new Paragraph("Price (₹)")).setBold());
			table.addHeaderCell(new Cell().add(new Paragraph("Total (₹)")).setBold());

			order.getOrderItems().forEach(item -> {
				table.addCell(item.getProduct().getProductName());
				table.addCell(String.valueOf(item.getQuantity()));
				table.addCell("₹" + item.getPrice());
				table.addCell("₹" + (item.getQuantity() * item.getPrice()));
			});

			document.add(table);

			// Grand Total
			document.add(new Paragraph("\n"));
			Paragraph total = new Paragraph("Grand Total: ₹" + order.getTotalAmount()).setFontSize(14).setBold()
					.setTextAlignment(TextAlignment.RIGHT);
			document.add(total);

			document.add(new Paragraph("\n"));

			// Footer
			document.add(new LineSeparator(new SolidLine()));
			Paragraph footer = new Paragraph("Thank you for your purchase!\nVisit us again.").setFontSize(10)
					.setTextAlignment(TextAlignment.CENTER).setMarginTop(15);
			document.add(footer);

			document.close();
			return baos.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException("Failed to generate invoice PDF", e);
		}
	}

}
