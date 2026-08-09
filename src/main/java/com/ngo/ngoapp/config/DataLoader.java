package com.ngo.ngoapp.config;

import com.ngo.ngoapp.models.Campaign;
import com.ngo.ngoapp.repositories.CampaignRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final CampaignRepository campaignRepository;

    public DataLoader(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (campaignRepository.count() == 0) {
            log.info("Database empty. Seeding default NGO campaigns...");

            Campaign c1 = new Campaign();
            c1.setTitle("Nourish the Needy");
            c1.setDescription("Provide warm meals, weekly nutrition kits, and emergency food supplies to families in low-income neighborhoods experiencing acute food shortages.");
            c1.setTargetAmount(500000.0);
            c1.setRaisedAmount(120000.0);
            c1.setImageUrl("https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?q=80&w=600&auto=format&fit=crop");
            c1.setCreatedAt(LocalDateTime.now().minusDays(10));
            campaignRepository.save(c1);

            Campaign c2 = new Campaign();
            c2.setTitle("Clean Water Initiative");
            c2.setDescription("Fund the drilling of community tube-wells, water-purification stations, and distribution pipelines in rural drylands to curb waterborne diseases.");
            c2.setTargetAmount(800000.0);
            c2.setRaisedAmount(450000.0);
            c2.setImageUrl("https://images.unsplash.com/photo-1541252260730-0412e8e2108e?q=80&w=600&auto=format&fit=crop");
            c2.setCreatedAt(LocalDateTime.now().minusDays(5));
            campaignRepository.save(c2);

            Campaign c3 = new Campaign();
            c3.setTitle("Bright Minds Scholars");
            c3.setDescription("Cover annual school fees, uniforms, textbooks, and computer lab access for orphan children and girls from agricultural working communities.");
            c3.setTargetAmount(300000.0);
            c3.setRaisedAmount(95000.0);
            c3.setImageUrl("https://images.unsplash.com/photo-1503676260728-1c00da094a0b?q=80&w=600&auto=format&fit=crop");
            c3.setCreatedAt(LocalDateTime.now().minusDays(2));
            campaignRepository.save(c3);

            log.info("Seed campaigns successfully created.");
        }
    }
}
