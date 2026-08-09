package com.ngo.ngoapp.services;

import com.ngo.ngoapp.config.RazorpayConfig;
import com.ngo.ngoapp.models.Campaign;
import com.ngo.ngoapp.models.Donation;
import com.ngo.ngoapp.models.User;
import com.ngo.ngoapp.repositories.CampaignRepository;
import com.ngo.ngoapp.repositories.DonationRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DonationService {

    private static final Logger log = LoggerFactory.getLogger(DonationService.class);

    private final RazorpayConfig razorpayConfig;
    private final DonationRepository donationRepository;
    private final CampaignRepository campaignRepository;

    public DonationService(RazorpayConfig razorpayConfig, DonationRepository donationRepository, CampaignRepository campaignRepository) {
        this.razorpayConfig = razorpayConfig;
        this.donationRepository = donationRepository;
        this.campaignRepository = campaignRepository;
    }

    @Transactional
    public Donation createDonationOrder(Campaign campaign, User user, Double amount, String donorName, String donorEmail) throws Exception {
        Donation donation = new Donation();
        donation.setCampaign(campaign);
        donation.setUser(user);
        donation.setAmount(amount);
        donation.setDonorName(donorName);
        donation.setDonorEmail(donorEmail);
        donation.setStatus("PENDING");
        donation.setCreatedAt(LocalDateTime.now());

        if (razorpayConfig.isMockMode()) {
            String mockOrderId = "order_mock_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
            donation.setOrderId(mockOrderId);
            log.info("Mock Mode: Created order ID {}", mockOrderId);
        } else {
            RazorpayClient client = razorpayConfig.getClient();
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", (int) (amount * 100));
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "txn_" + System.currentTimeMillis());

            Order order = client.orders.create(orderRequest);
            donation.setOrderId(order.get("id"));
            log.info("Razorpay: Created order ID {}", donation.getOrderId());
        }

        return donationRepository.save(donation);
    }

    @Transactional
    public Donation verifyPayment(String orderId, String paymentId, String signature) throws Exception {
        Donation donation = donationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Donation order not found: " + orderId));

        if (!"PENDING".equals(donation.getStatus())) {
            log.warn("Donation order {} already processed with status: {}", orderId, donation.getStatus());
            return donation;
        }

        boolean isValidSignature = false;

        if (razorpayConfig.isMockMode()) {
            log.info("Mock Mode: verifying payment ID {} for order ID {}", paymentId, orderId);
            isValidSignature = paymentId != null && !paymentId.trim().isEmpty();
        } else {
            try {
                JSONObject options = new JSONObject();
                options.put("razorpay_order_id", orderId);
                options.put("razorpay_payment_id", paymentId);
                options.put("razorpay_signature", signature);
                isValidSignature = Utils.verifyPaymentSignature(options, razorpayConfig.getKeySecret());
            } catch (Exception e) {
                log.error("Signature verification error: {}", e.getMessage());
            }
        }

        if (isValidSignature) {
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
            log.error("Payment signature verification failed for order {}", orderId);
        }

        return donationRepository.save(donation);
    }
}
