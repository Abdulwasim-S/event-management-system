package com.management.event_management_system.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.management.event_management_system.model.Event;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {

}
