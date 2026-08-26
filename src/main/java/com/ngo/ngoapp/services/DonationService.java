package com.ngo.ngoapp.services;

import com.ngo.ngoapp.models.Campaign;
import com.ngo.ngoapp.models.Donation;
import com.ngo.ngoapp.models.User;
import com.ngo.ngoapp.repositories.CampaignRepository;
import com.ngo.ngoapp.repositories.DonationRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class DonationService {

    private static final Logger log = LoggerFactory.getLogger(DonationService.class);

    private final DonationRepository donationRepository;
    private final CampaignRepository campaignRepository;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    public DonationService(DonationRepository donationRepository, CampaignRepository campaignRepository) {
        this.donationRepository = donationRepository;
        this.campaignRepository = campaignRepository;
    }

    public String getRazorpayKeyId() {
        return razorpayKeyId;
    }

    @Transactional
    public Donation createDonationOrder(Campaign campaign, User user, Double amount, String donorName, String donorEmail) throws RazorpayException {
        // Razorpay requires amount in paise (1 INR = 100 paise), minimum 100 paise (₹1)
        long amountInPaise = Math.round(amount * 100);
        if (amountInPaise < 100) {
            throw new IllegalArgumentException("Minimum donation amount is ₹1 (100 paise).");
        }

        // Call Razorpay Orders API to create a real order
        RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "rcpt_" + System.currentTimeMillis());
        orderRequest.put("payment_capture", true);

        Order razorpayOrder = client.orders.create(orderRequest);
        String razorpayOrderId = razorpayOrder.get("id");

        log.info("Razorpay order created: id={}, amount={} paise", razorpayOrderId, amountInPaise);

        // Persist a PENDING donation record with the real Razorpay order id
        Donation donation = new Donation();
        donation.setCampaign(campaign);
        donation.setUser(user);
        if (user != null) {
            donation.setUserId(user.getId()); // flat field for efficient querying
        }
        donation.setAmount(amount);           // stored as INR
        donation.setDonorName(donorName);
        donation.setDonorEmail(donorEmail);
        donation.setStatus("PENDING");
        donation.setOrderId(razorpayOrderId); // real Razorpay order_id
        donation.setCreatedAt(LocalDateTime.now());

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

        // Validate inputs
        if (paymentId == null || paymentId.trim().isEmpty()
                || signature == null || signature.trim().isEmpty()) {
            donation.setStatus("FAILED");
            donationRepository.save(donation);
            throw new IllegalArgumentException("Missing paymentId or signature.");
        }

        // HMAC-SHA256 signature verification
        // Expected payload: "<orderId>|<paymentId>"
        boolean signatureValid = verifySignature(orderId, paymentId, signature);

        if (!signatureValid) {
            donation.setStatus("FAILED");
            donationRepository.save(donation);
            log.error("Signature mismatch for order {}. Payment NOT marked as SUCCESS.", orderId);
            throw new SecurityException("Razorpay signature verification failed.");
        }

        // Signature valid — mark SUCCESS
        donation.setPaymentId(paymentId);
        donation.setSignature(signature);
        donation.setStatus("SUCCESS");

        Campaign campaign = donation.getCampaign();
        if (campaign != null) {
            double newRaisedAmount = (campaign.getRaisedAmount() != null ? campaign.getRaisedAmount() : 0.0)
                    + donation.getAmount();
            campaign.setRaisedAmount(newRaisedAmount);
            campaignRepository.save(campaign);
            log.info("Campaign {} raised amount updated to {}", campaign.getId(), newRaisedAmount);
        }

        log.info("Donation order {} verified and marked as SUCCESS.", orderId);
        return donationRepository.save(donation);
    }

    /**
     * Verifies Razorpay payment signature using HMAC-SHA256.
     * Algorithm: HMAC_SHA256(key=KEY_SECRET, data="<orderId>|<paymentId>")
     * Compare hex digest with razorpay_signature.
     */
    private boolean verifySignature(String orderId, String paymentId, String receivedSignature) {
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hashBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String generatedSignature = HexFormat.of().formatHex(hashBytes);
            return generatedSignature.equals(receivedSignature);
        } catch (Exception e) {
            log.error("Error during signature verification: ", e);
            return false;
        }
    }
}
