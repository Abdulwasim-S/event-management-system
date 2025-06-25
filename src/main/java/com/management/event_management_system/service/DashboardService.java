package com.management.event_management_system.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.management.event_management_system.dto.DashboardStatsDTO;
import com.management.event_management_system.dto.IncomePoint;
import com.management.event_management_system.model.Booking;
import com.management.event_management_system.model.Event;
import com.management.event_management_system.repository.BookingRepository;
import com.management.event_management_system.repository.EventRepository;
import com.management.event_management_system.repository.UserRepository;

@Service
public class DashboardService {

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private BookingRepository bookingRepository;

	@Autowired
	private UserRepository userRepository;

	public DashboardStatsDTO getDashboardStats() {
		try {
			long totalEvents = eventRepository.count();
			long totalBookings = bookingRepository.count();
			long totalUsers = userRepository.count();

			List<Booking> bookings = bookingRepository.findAll();
			long confirmedCount = bookings.stream().filter(b -> "CONFIRMED".equalsIgnoreCase(b.getStatus())).count();
			long cancelledCount = bookings.stream().filter(b -> "CANCELLED".equalsIgnoreCase(b.getStatus())).count();
			long pendingCount = bookings.stream().filter(b -> "PENDING".equalsIgnoreCase(b.getStatus())).count();

			double bookingCompletionRate = totalBookings == 0 ? 0 : (confirmedCount * 100.0) / totalBookings;

			double totalIncome = 0.0;
			Map<LocalDate, Double> dailyIncome = new HashMap<>();

			for (Booking booking : bookings) {
				if (!"CONFIRMED".equalsIgnoreCase(booking.getStatus()))
					continue;

				Event event = eventRepository.findById(booking.getEventId()).orElse(null);
				if (event == null || event.getPrice() == null)
					continue;

				double price = event.getPrice().doubleValue();
				totalIncome += price;

				LocalDate date = booking.getBookedAt().toLocalDate();
				dailyIncome.put(date, dailyIncome.getOrDefault(date, 0.0) + price);
			}

			List<IncomePoint> incomeOverTime = dailyIncome.entrySet().stream().sorted(Map.Entry.comparingByKey())
					.map(entry -> new IncomePoint(entry.getKey().toString(), entry.getValue()))
					.collect(Collectors.toList());

			return new DashboardStatsDTO(totalEvents, totalBookings, totalUsers, totalIncome, bookingCompletionRate,
					confirmedCount, cancelledCount, pendingCount, incomeOverTime);

		} catch (Exception e) {
			e.printStackTrace();
			return new DashboardStatsDTO();
		}
	}
}
