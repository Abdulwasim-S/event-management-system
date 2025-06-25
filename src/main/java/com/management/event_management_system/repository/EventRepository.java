package com.management.event_management_system.repository;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.management.event_management_system.model.Event;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {
	@Query(value = "{ 'title': { $regex: ?0, $options: 'i' }, 'category': { $regex: ?1, $options: 'i' }, 'startTime': { $gte: ?2 } }")
	Page<Event> searchEvents(String titleRegex, String categoryRegex, Date currentTime, Pageable pageable);

}
