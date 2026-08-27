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
            c1.setImageUrl("https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?q=80&w=800&auto=format&fit=crop");
            c1.setCreatedAt(LocalDateTime.now().minusDays(10));
            campaignRepository.save(c1);

            Campaign c2 = new Campaign();
            c2.setTitle("Clean Water Initiative");
            c2.setDescription("Fund the drilling of community tube-wells, water-purification stations, and distribution pipelines in rural drylands to curb waterborne diseases.");
            c2.setTargetAmount(800000.0);
            c2.setRaisedAmount(450000.0);
            c2.setImageUrl("https://images.unsplash.com/photo-1541888946425-d0fbb186a5b3?q=80&w=800&auto=format&fit=crop");
            c2.setCreatedAt(LocalDateTime.now().minusDays(8));
            campaignRepository.save(c2);

            Campaign c3 = new Campaign();
            c3.setTitle("Bright Minds Scholars");
            c3.setDescription("Cover annual school fees, uniforms, textbooks, and computer lab access for orphan children and girls from agricultural working communities.");
            c3.setTargetAmount(300000.0);
            c3.setRaisedAmount(95000.0);
            c3.setImageUrl("https://images.unsplash.com/photo-1509062522246-3755977927d7?q=80&w=800&auto=format&fit=crop");
            c3.setCreatedAt(LocalDateTime.now().minusDays(6));
            campaignRepository.save(c3);

            Campaign c4 = new Campaign();
            c4.setTitle("Blood Donation & Health Drive");
            c4.setDescription("Organizing regional blood donation camps, mobile health checkup vans, and life-saving medical supplies for rural healthcare centers.");
            c4.setTargetAmount(250000.0);
            c4.setRaisedAmount(85000.0);
            c4.setImageUrl("https://images.unsplash.com/photo-1615461066841-6116e61058f4?q=80&w=800&auto=format&fit=crop");
            c4.setCreatedAt(LocalDateTime.now().minusDays(4));
            campaignRepository.save(c4);

            Campaign c5 = new Campaign();
            c5.setTitle("Disaster Relief & Shelter");
            c5.setDescription("Providing emergency shelter kits, waterproof tents, blankets, and essential survival gear for families displaced by floods and severe weather.");
            c5.setTargetAmount(600000.0);
            c5.setRaisedAmount(210000.0);
            c5.setImageUrl("https://images.unsplash.com/photo-1518398046578-8cca57782e17?q=80&w=800&auto=format&fit=crop");
            c5.setCreatedAt(LocalDateTime.now().minusDays(2));
            campaignRepository.save(c5);

            Campaign c6 = new Campaign();
            c6.setTitle("Senior Citizen Care & Dignity");
            c6.setDescription("Supporting impoverished senior citizens with monthly medical checkups, essential medicines, warm clothes, and nutritious daily meals.");
            c6.setTargetAmount(400000.0);
            c6.setRaisedAmount(150000.0);
            c6.setImageUrl("https://images.unsplash.com/photo-1581579438747-1dc8d1e377c8?q=80&w=800&auto=format&fit=crop");
            c6.setCreatedAt(LocalDateTime.now().minusDays(1));
            campaignRepository.save(c6);

            log.info("Seed campaigns successfully created.");
        }
    }
}
