package com.ngo.ngoapp.controllers;

import com.ngo.ngoapp.models.Campaign;
import com.ngo.ngoapp.repositories.CampaignRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CampaignController {

    private final CampaignRepository campaignRepository;

    public CampaignController(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    @GetMapping
    public List<Campaign> getAllCampaigns() {
        return campaignRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Campaign> getCampaignById(@PathVariable String id) {
        return campaignRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Campaign createCampaign(@RequestBody Campaign campaign) {
        campaign.setRaisedAmount(0.0);
        campaign.setCreatedAt(LocalDateTime.now());
        return campaignRepository.save(campaign);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Campaign> updateCampaign(@PathVariable String id, @RequestBody Campaign campaignDetails) {
        return campaignRepository.findById(id)
                .map(campaign -> {
                    campaign.setTitle(campaignDetails.getTitle());
                    campaign.setDescription(campaignDetails.getDescription());
                    campaign.setTargetAmount(campaignDetails.getTargetAmount());
                    if (campaignDetails.getImageUrl() != null) {
                        campaign.setImageUrl(campaignDetails.getImageUrl());
                    }
                    return ResponseEntity.ok(campaignRepository.save(campaign));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCampaign(@PathVariable String id) {
        return campaignRepository.findById(id)
                .map(campaign -> {
                    campaignRepository.delete(campaign);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
