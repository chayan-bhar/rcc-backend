package com.ngo.ngoapp.controllers;

import com.ngo.ngoapp.models.Donation;
import com.ngo.ngoapp.repositories.DonationRepository;
import com.ngo.ngoapp.repositories.CampaignRepository;
import com.ngo.ngoapp.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AdminController {

    private final DonationRepository donationRepository;
    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;

    public AdminController(DonationRepository donationRepository, CampaignRepository campaignRepository, UserRepository userRepository) {
        this.donationRepository = donationRepository;
        this.campaignRepository = campaignRepository;
        this.userRepository = userRepository;
    }

    public static class AdminStats {
        private Double totalDonations;
        private Long donorCount;
        private Long campaignCount;
        private List<Donation> recentTransactions;

        public Double getTotalDonations() { return totalDonations; }
        public void setTotalDonations(Double totalDonations) { this.totalDonations = totalDonations; }
        public Long getDonorCount() { return donorCount; }
        public void setDonorCount(Long donorCount) { this.donorCount = donorCount; }
        public Long getCampaignCount() { return campaignCount; }
        public void setCampaignCount(Long campaignCount) { this.campaignCount = campaignCount; }
        public List<Donation> getRecentTransactions() { return recentTransactions; }
        public void setRecentTransactions(List<Donation> recentTransactions) { this.recentTransactions = recentTransactions; }
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStats> getDashboardStats() {
        List<Donation> successfulDonations = donationRepository.findByStatus("SUCCESS");

        Double totalDonations = successfulDonations.stream()
                .mapToDouble(Donation::getAmount)
                .sum();

        long donorCount = successfulDonations.stream()
                .map(d -> d.getDonorEmail() != null ? d.getDonorEmail() : (d.getUser() != null ? d.getUser().getEmail() : "anonymous"))
                .distinct()
                .count();

        long campaignCount = campaignRepository.count();

        List<Donation> allTransactions = donationRepository.findAll();
        allTransactions.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        List<Donation> recentTransactions = allTransactions.subList(0, Math.min(allTransactions.size(), 20));

        AdminStats stats = new AdminStats();
        stats.setTotalDonations(totalDonations);
        stats.setDonorCount(donorCount);
        stats.setCampaignCount(campaignCount);
        stats.setRecentTransactions(recentTransactions);

        return ResponseEntity.ok(stats);
    }
}
