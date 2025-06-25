package com.management.event_management_system.service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.management.event_management_system.config.JwtUtil;
import com.management.event_management_system.dto.ApiResponseDTO;
import com.management.event_management_system.dto.BookingRequestDTO;
import com.management.event_management_system.dto.ConfirmPaymentRequestDTO;
import com.management.event_management_system.model.Booking;
import com.management.event_management_system.model.Event;
import com.management.event_management_system.model.User;
import com.management.event_management_system.repository.BookingRepository;
import com.management.event_management_system.repository.EventRepository;
import com.management.event_management_system.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;

import jakarta.mail.internet.MimeMessage;

@Service
public class BookingService {

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BookingRepository bookingRepository;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private JwtUtil jwtUtil;

	@Value("${razorpay.key}")
	private String key;
	@Value("${razorpay.secret}")
	private String secret;

	private BookingService() {
		System.out.println(key);
		System.out.println(secret);
	}

	public ResponseEntity<?> createBooking(BookingRequestDTO request, String token) {
		try {
			Event event = eventRepository.findById(request.getEventId())
					.orElseThrow(() -> new RuntimeException("Event not found"));

			RazorpayClient razorpay = new RazorpayClient(key, secret);
			JSONObject orderRequest = new JSONObject();
			orderRequest.put("amount", event.getPrice().multiply(BigDecimal.valueOf(100)).intValue());
			orderRequest.put("currency", "INR");
			orderRequest.put("receipt", UUID.randomUUID().toString());

			Order order = razorpay.orders.create(orderRequest);

			String email = jwtUtil.getEmailFromToken(token);

			String attendeeId = userRepository.findByEmail(email).map(User::getId)
					.orElseThrow(() -> new RuntimeException("User not found with email: " + email));

			Booking booking = new Booking();

			booking.setAttendeeId(attendeeId);
			booking.setEventId(event.getId());
			booking.setAttendeeName(request.getAttendeeName());
			booking.setAttendeeEmail(request.getAttendeeEmail());
			booking.setOrderId(order.get("id"));
			booking.setStatus("PENDING");

			bookingRepository.save(booking);

			return ResponseEntity.ok(new ApiResponseDTO<>("Order created", order.toString()));

		} catch (Exception e) {
			return ResponseEntity.internalServerError()
					.body(new ApiResponseDTO<>("Booking failed: " + e.getMessage(), null));
		}
	}

	public ResponseEntity<?> confirmBooking(ConfirmPaymentRequestDTO request) {
		try {
			String generatedSignature = generateRazorpaySignature(request.getOrderId() + "|" + request.getPaymentId(),
					secret);

			if (!generatedSignature.equals(request.getSignature())) {
				return ResponseEntity.badRequest().body(new ApiResponseDTO<>("Invalid payment signature", null));
			}

			Optional<Booking> optionalBooking = bookingRepository.findByOrderId(request.getOrderId());

			if (optionalBooking.isEmpty()) {
				return ResponseEntity.badRequest().body(new ApiResponseDTO<>("Booking not found", null));
			}

			Booking booking = optionalBooking.get();
			booking.setStatus("CONFIRMED");
			booking.setPaymentId(request.getPaymentId());
			bookingRepository.save(booking);

			String ticketData = "Ticket ID: " + booking.getId() + "\nEvent ID: " + booking.getEventId() + "\nName: "
					+ booking.getAttendeeName();
			byte[] qrCode = generateQRCode(ticketData);

			sendTicketEmail(booking.getAttendeeEmail(), booking, qrCode);

			return ResponseEntity.ok(new ApiResponseDTO<>("Booking confirmed and email sent", booking.getId()));

		} catch (Exception e) {
			return ResponseEntity.internalServerError()
					.body(new ApiResponseDTO<>("Failed to confirm booking: " + e.getMessage(), null));
		}
	}

	private String generateRazorpaySignature(String data, String secret) throws Exception {
		Mac mac = Mac.getInstance("HmacSHA256");
		SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
		mac.init(secretKeySpec);
		byte[] hash = mac.doFinal(data.getBytes());
		return bytesToHex(hash);
	}

	private String bytesToHex(byte[] bytes) {
		StringBuilder hexString = new StringBuilder();
		for (byte b : bytes) {
			String hex = Integer.toHexString(0xff & b);
			if (hex.length() == 1)
				hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}

	private byte[] generateQRCode(String text) throws Exception {
		BitMatrix matrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, 300, 300);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);
		return outputStream.toByteArray();
	}

	public void sendTicketEmail(String toEmail, Booking booking, byte[] qrCode) throws Exception {
		Optional<Event> optionalEvent = eventRepository.findById(booking.getEventId());
		if (optionalEvent.isEmpty()) {
			throw new RuntimeException("Event not found for ID: " + booking.getEventId());
		}

		Event event = optionalEvent.get();

		ZoneId utcZone = ZoneId.of("UTC");
		ZoneId istZone = ZoneId.of("Asia/Kolkata");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

		ZonedDateTime startTimeInIST = event.getStartTime().atZone(utcZone).withZoneSameInstant(istZone);
		ZonedDateTime endTimeInIST = event.getEndTime().atZone(utcZone).withZoneSameInstant(istZone);

		String formattedStartTime = startTimeInIST.format(formatter);
		String formattedEndTime = endTimeInIST.format(formatter);

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

		helper.setTo(toEmail);
		helper.setSubject("🎟️ Your Ticket for " + event.getTitle());

		String htmlContent = "<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;background-color:#f9f9f9;padding:20px;'>"
				+ "<div style='max-width:600px;margin:auto;background:white;border-radius:10px;box-shadow:0 0 10px rgba(0,0,0,0.1);padding:20px;'>"
				+

				"<img src='" + event.getImgUrl()
				+ "' alt='Event Banner' style='width:100%;border-radius:10px;margin-bottom:20px;'/>" +

				"<h2 style='color:#2e6c80;'>🎉 Your Event Ticket</h2>" + "<p>Hi <strong>" + booking.getAttendeeName()
				+ "</strong>,</p>" + "<p>Your booking for <strong>" + event.getTitle() + "</strong> is confirmed.</p>"
				+ "<p><strong>Event Timing:</strong> " + formattedStartTime + " to " + formattedEndTime + "<br/>"
				+ "<strong>Event ID:</strong> " + booking.getEventId() + "<br/>" + "<strong>Ticket ID:</strong> "
				+ booking.getId() + "</p>" +

				"<p style='margin-top:20px;'>Please present this QR code at the entrance:</p>"
				+ "<div style='text-align:center;margin:20px 0;'>"
				+ "<img src='cid:ticketQr' alt='QR Code' style='width:200px;height:200px;'/>" + "</div>" +

				"<p>We look forward to seeing you! 🎊</p>"
				+ "<p style='font-size:12px;color:gray;'>This is an automated message. Please do not reply.</p>"
				+ "</div></body></html>";

		helper.setText(htmlContent, true);
		helper.addInline("ticketQr", new ByteArrayResource(qrCode), "image/png");

		mailSender.send(message);
		System.out.println("✅ Email sent with converted IST timings and QR code.");
	}

	public ResponseEntity<ApiResponseDTO<?>> cancelBooking(String orderId) {
		try {
			Optional<Booking> optionalBooking = bookingRepository.findByOrderId(orderId);
			if (optionalBooking.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ApiResponseDTO<>("Booking not found for order ID", null));
			}

			Booking booking = optionalBooking.get();
			booking.setStatus("CANCELLED");
			bookingRepository.save(booking);

			return ResponseEntity.ok(new ApiResponseDTO<>("Booking cancelled successfully", null));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponseDTO<>("Error cancelling booking", null));
		}
	}

	public void sendTestTicketEmail() throws Exception {
		String toEmail = "abdulwasimsmech@gmail.com";
		String attendeeName = "Wasim";
		String bookingId = "TCKT12345";
		String eventName = "Tech Conference 2025";

		byte[] qrCodeBytes = generateQRCode("Ticket ID: " + bookingId);

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

		helper.setTo(toEmail);
		helper.setSubject("Your Ticket for " + eventName);

		String htmlContent = "<html><body>" + "<h2 style='color:#2e6c80;'>Event Ticket Confirmation</h2>"
				+ "<p>Dear <strong>" + attendeeName.toUpperCase() + "</strong>,</p>" + "<p>Your booking for <strong>"
				+ eventName + "</strong> is confirmed!</p>" + "<p><strong>Ticket ID:</strong> " + bookingId + "</p>"
				+ "<p>Scan this QR code at the entrance:</p>"
				+ "<img src='cid:qrCodeCid' style='width:200px;height:200px;'/>"
				+ "<p>Looking forward to seeing you at the event!</p>"
				+ "<p style='font-size:12px;color:gray;'>This is an auto-generated email. Please do not reply.</p>"
				+ "</body></html>";

		helper.setText(htmlContent, true);
		helper.addInline("qrCodeCid", new ByteArrayResource(qrCodeBytes), "image/png");

		mailSender.send(message);
		System.out.println("HTML email with inline QR code sent successfully!");
	}

	public ResponseEntity<?> getBookingsByEventPaginated(String eventId, int page, int limit) {
		try {
			List<Booking> allBookings = bookingRepository.findByEventId(eventId);
			int totalBookings = allBookings.size();

			Event event = eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));

			long confirmedCount = allBookings.stream().filter(b -> "CONFIRMED".equalsIgnoreCase(b.getStatus())).count();

			long pendingCount = allBookings.stream().filter(b -> "PENDING".equalsIgnoreCase(b.getStatus())).count();

			long cancelledCount = allBookings.stream().filter(b -> "CANCELLED".equalsIgnoreCase(b.getStatus())).count();

			BigDecimal totalRevenue = event.getPrice().multiply(BigDecimal.valueOf(confirmedCount));

			int start = Math.min(page * limit, totalBookings);
			int end = Math.min(start + limit, totalBookings);
			List<Booking> paginatedBookings = allBookings.subList(start, end);

			Map<String, Object> data = new HashMap<>();
			data.put("bookings", paginatedBookings);
			data.put("totalBookings", totalBookings);
			data.put("totalRevenue", totalRevenue);
			data.put("confirmedCount", confirmedCount);
			data.put("pendingCount", pendingCount);
			data.put("cancelledCount", cancelledCount);

			return ResponseEntity.ok(new ApiResponseDTO<>("Bookings fetched", data));
		} catch (Exception e) {
			return ResponseEntity.status(500).body(new ApiResponseDTO<>("Failed to fetch bookings", null));
		}
	}

}
