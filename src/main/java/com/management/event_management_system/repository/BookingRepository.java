package com.management.event_management_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.management.event_management_system.model.Booking;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {

	Optional<Booking> findByPaymentId(String paymentId);

	Optional<Booking> findByOrderId(String orderId);

	List<Booking> findByEventId(String eventId);

	long countByStatus(String status);

	List<Booking> findByStatus(String status);

	List<Booking> findAllByAttendeeIdAndStatus(String attendeeId, String status);

}
