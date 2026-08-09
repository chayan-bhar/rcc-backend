package com.ngo.ngoapp.controllers;

import com.ngo.ngoapp.auth.UserPrincipal;
import com.ngo.ngoapp.models.Campaign;
import com.ngo.ngoapp.models.Donation;
import com.ngo.ngoapp.repositories.CampaignRepository;
import com.ngo.ngoapp.repositories.DonationRepository;
import com.ngo.ngoapp.services.DonationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/donations")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DonationController {

    private static final Logger log = LoggerFactory.getLogger(DonationController.class);

    private final DonationService donationService;
    private final DonationRepository donationRepository;
    private final CampaignRepository campaignRepository;

    public DonationController(DonationService donationService, DonationRepository donationRepository, CampaignRepository campaignRepository) {
        this.donationService = donationService;
        this.donationRepository = donationRepository;
        this.campaignRepository = campaignRepository;
    }

    public static class OrderRequest {
        private String campaignId;
        private Double amount;
        private String donorName;
        private String donorEmail;

        public String getCampaignId() { return campaignId; }
        public void setCampaignId(String campaignId) { this.campaignId = campaignId; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        public String getDonorName() { return donorName; }
        public void setDonorName(String donorName) { this.donorName = donorName; }
        public String getDonorEmail() { return donorEmail; }
        public void setDonorEmail(String donorEmail) { this.donorEmail = donorEmail; }
    }

    public static class VerifyRequest {
        private String orderId;
        private String paymentId;
        private String signature;

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
        public String getSignature() { return signature; }
        public void setSignature(String signature) { this.signature = signature; }
    }

    @PostMapping("/order")
    public ResponseEntity<?> createOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody OrderRequest request) {
        try {
            Campaign campaign = campaignRepository.findById(request.getCampaignId())
                    .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));

            if (request.getAmount() == null || request.getAmount() <= 0) {
                return ResponseEntity.badRequest().body("Invalid donation amount");
            }

            Donation donation = donationService.createDonationOrder(
                    campaign,
                    principal != null ? principal.getUser() : null,
                    request.getAmount(),
                    request.getDonorName() != null ? request.getDonorName() : (principal != null ? principal.getUser().getName() : "Anonymous"),
                    request.getDonorEmail() != null ? request.getDonorEmail() : (principal != null ? principal.getUser().getEmail() : "")
            );

            return ResponseEntity.ok(donation);
        } catch (Exception e) {
            log.error("Error creating donation order: ", e);
            return ResponseEntity.internalServerError().body("Error generating payment order: " + e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody VerifyRequest request) {
        try {
            Donation verifiedDonation = donationService.verifyPayment(
                    request.getOrderId(),
                    request.getPaymentId(),
                    request.getSignature()
            );

            if ("SUCCESS".equals(verifiedDonation.getStatus())) {
                return ResponseEntity.ok(verifiedDonation);
            } else {
                return ResponseEntity.badRequest().body("Signature verification failed or payment failed");
            }
        } catch (Exception e) {
            log.error("Error verifying payment: ", e);
            return ResponseEntity.internalServerError().body("Error verifying payment: " + e.getMessage());
        }
    }

    @GetMapping("/my-donations")
    public ResponseEntity<List<Donation>> getMyDonations(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(donationRepository.findByUserIdOrderByCreatedAtDesc(principal.getUid()));
    }
}
