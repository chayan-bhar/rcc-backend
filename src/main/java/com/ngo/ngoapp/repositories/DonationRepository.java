package com.ngo.ngoapp.repositories;

import com.ngo.ngoapp.models.Donation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DonationRepository extends MongoRepository<Donation, String> {
    List<Donation> findByUserIdOrderByCreatedAtDesc(String userId);
    Optional<Donation> findByOrderId(String orderId);
    List<Donation> findByStatus(String status);
}
