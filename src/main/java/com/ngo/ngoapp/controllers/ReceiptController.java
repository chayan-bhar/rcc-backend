package com.ngo.ngoapp.controllers;

import com.ngo.ngoapp.models.Donation;
import com.ngo.ngoapp.repositories.DonationRepository;
import com.ngo.ngoapp.services.ReceiptService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/api/donations")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ReceiptController {

    private final DonationRepository donationRepository;
    private final ReceiptService receiptService;

    public ReceiptController(DonationRepository donationRepository, ReceiptService receiptService) {
        this.donationRepository = donationRepository;
        this.receiptService = receiptService;
    }

    @GetMapping("/{id}/receipt")
    public ResponseEntity<?> downloadReceipt(@PathVariable String id) {
        return donationRepository.findById(id)
                .map(donation -> {
                    if (!"SUCCESS".equals(donation.getStatus())) {
                        return ResponseEntity.badRequest().body("Receipt is only available for successful donations.");
                    }

                    ByteArrayInputStream bis = receiptService.generateDonationReceipt(donation);

                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Content-Disposition", "attachment; filename=NGO_Donation_Receipt_" + id + ".pdf");

                    return ResponseEntity.ok()
                            .headers(headers)
                            .contentType(MediaType.APPLICATION_PDF)
                            .body(new InputStreamResource(bis));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
