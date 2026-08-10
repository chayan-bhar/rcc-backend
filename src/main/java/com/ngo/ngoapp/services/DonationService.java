package com.ngo.ngoapp.services;

import com.ngo.ngoapp.models.Campaign;
import com.ngo.ngoapp.models.Donation;
import com.ngo.ngoapp.models.User;
import com.ngo.ngoapp.repositories.CampaignRepository;
import com.ngo.ngoapp.repositories.DonationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DonationService {

    private static final Logger log = LoggerFactory.getLogger(DonationService.class);

    private final DonationRepository donationRepository;
    private final CampaignRepository campaignRepository;

    public DonationService(DonationRepository donationRepository, CampaignRepository campaignRepository) {
        this.donationRepository = donationRepository;
        this.campaignRepository = campaignRepository;
    }

    @Transactional
    public Donation createDonationOrder(Campaign campaign, User user, Double amount, String donorName, String donorEmail) {
        Donation donation = new Donation();
        donation.setCampaign(campaign);
        donation.setUser(user);
        donation.setAmount(amount);
        donation.setDonorName(donorName);
        donation.setDonorEmail(donorEmail);
        donation.setStatus("PENDING");
        donation.setCreatedAt(LocalDateTime.now());

        String orderId = "order_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
        donation.setOrderId(orderId);
        log.info("Created donation order ID {}", orderId);

        return donationRepository.save(donation);
    }

    @Transactional
    public Donation verifyPayment(String orderId, String paymentId, String signature) {
        Donation donation = donationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Donation order not found: " + orderId));

        if (!"PENDING".equals(donation.getStatus())) {
            log.warn("Donation order {} already processed with status: {}", orderId, donation.getStatus());
            return donation;
        }

        if (paymentId != null && !paymentId.trim().isEmpty()) {
            donation.setPaymentId(paymentId);
            donation.setSignature(signature);
            donation.setStatus("SUCCESS");

            Campaign campaign = donation.getCampaign();
            if (campaign != null) {
                double newRaisedAmount = (campaign.getRaisedAmount() != null ? campaign.getRaisedAmount() : 0.0) + donation.getAmount();
                campaign.setRaisedAmount(newRaisedAmount);
                campaignRepository.save(campaign);
                log.info("Campaign ID {} raised amount updated to {}", campaign.getId(), newRaisedAmount);
            }
            log.info("Donation order {} marked as SUCCESS.", orderId);
        } else {
            donation.setStatus("FAILED");
            log.error("Payment verification failed for order {}", orderId);
        }

        return donationRepository.save(donation);
    }
}
