package com.firstclub.membership.config;

import com.firstclub.membership.benefit.domain.BenefitDefinition;
import com.firstclub.membership.benefit.domain.BenefitType;
import com.firstclub.membership.benefit.domain.TierBenefit;
import com.firstclub.membership.benefit.repository.BenefitDefinitionRepository;
import com.firstclub.membership.benefit.repository.TierBenefitRepository;
import com.firstclub.membership.deal.domain.Deal;
import com.firstclub.membership.deal.domain.DealPlanAccess;
import com.firstclub.membership.deal.domain.DealType;
import com.firstclub.membership.deal.repository.DealPlanAccessRepository;
import com.firstclub.membership.deal.repository.DealRepository;
import com.firstclub.membership.discount.domain.BenefitEligibility;
import com.firstclub.membership.discount.domain.EligibilityScopeType;
import com.firstclub.membership.discount.repository.BenefitEligibilityRepository;
import com.firstclub.membership.plan.domain.BillingCycle;
import com.firstclub.membership.plan.domain.MembershipPlan;
import com.firstclub.membership.plan.repository.MembershipPlanRepository;
import com.firstclub.membership.tier.domain.Tier;
import com.firstclub.membership.tier.domain.TierRule;
import com.firstclub.membership.tier.repository.TierRepository;
import com.firstclub.membership.tier.repository.TierRuleRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds a ready-to-demo catalog on first start.
 * Idempotent: checks whether plans already exist and skips if so.
 * Delete the ./data/ directory to force a full re-seed after schema changes.
 */
@Component
@Profile("!test")
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final MembershipPlanRepository planRepository;
    private final TierRepository tierRepository;
    private final BenefitDefinitionRepository benefitRepository;
    private final TierBenefitRepository tierBenefitRepository;
    private final TierRuleRepository tierRuleRepository;
    private final BenefitEligibilityRepository eligibilityRepository;
    private final DealRepository dealRepository;
    private final DealPlanAccessRepository dealPlanAccessRepository;

    public DataSeeder(MembershipPlanRepository planRepository,
                      TierRepository tierRepository,
                      BenefitDefinitionRepository benefitRepository,
                      TierBenefitRepository tierBenefitRepository,
                      TierRuleRepository tierRuleRepository,
                      BenefitEligibilityRepository eligibilityRepository,
                      DealRepository dealRepository,
                      DealPlanAccessRepository dealPlanAccessRepository) {
        this.planRepository = planRepository;
        this.tierRepository = tierRepository;
        this.benefitRepository = benefitRepository;
        this.tierBenefitRepository = tierBenefitRepository;
        this.tierRuleRepository = tierRuleRepository;
        this.eligibilityRepository = eligibilityRepository;
        this.dealRepository = dealRepository;
        this.dealPlanAccessRepository = dealPlanAccessRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (planRepository.count() > 0) {
            log.info("Catalog already seeded — skipping DataSeeder.");
            return;
        }
        log.info("Seeding membership catalog...");

        // ── Plans ────────────────────────────────────────────────────────────
        MembershipPlan monthly = planRepository.save(
                MembershipPlan.builder().code("MONTHLY").name("Monthly Membership")
                        .billingCycle(BillingCycle.MONTHLY).price(new BigDecimal("199.00")).build());
        MembershipPlan quarterly = planRepository.save(
                MembershipPlan.builder().code("QUARTERLY").name("Quarterly Membership")
                        .billingCycle(BillingCycle.QUARTERLY).price(new BigDecimal("499.00")).build());
        MembershipPlan yearly = planRepository.save(
                MembershipPlan.builder().code("YEARLY").name("Yearly Membership")
                        .billingCycle(BillingCycle.YEARLY).price(new BigDecimal("1499.00")).build());

        // ── Tiers ────────────────────────────────────────────────────────────
        Tier silver = tierRepository.save(new Tier("SILVER", "Silver", 1, true));
        Tier gold = tierRepository.save(new Tier("GOLD", "Gold", 2, true));
        Tier platinum = tierRepository.save(new Tier("PLATINUM", "Platinum", 3, true));

        // ── Benefit definitions ──────────────────────────────────────────────
        BenefitDefinition freeDelivery = benefitRepository.save(new BenefitDefinition(
                "FREE_DELIVERY", BenefitType.FREE_DELIVERY, "Free delivery on eligible orders", true));
        BenefitDefinition extraDiscount = benefitRepository.save(new BenefitDefinition(
                "EXTRA_DISCOUNT", BenefitType.EXTRA_DISCOUNT, "Extra discount on selected items", true));
        BenefitDefinition exclusiveDeals = benefitRepository.save(new BenefitDefinition(
                "EXCLUSIVE_DEALS", BenefitType.EXCLUSIVE_DEALS, "Access to exclusive deals", true));
        BenefitDefinition earlyAccess = benefitRepository.save(new BenefitDefinition(
                "EARLY_ACCESS", BenefitType.EARLY_ACCESS, "Early access to sales", true));
        BenefitDefinition prioritySupport = benefitRepository.save(new BenefitDefinition(
                "PRIORITY_SUPPORT", BenefitType.PRIORITY_SUPPORT, "Priority customer support", true));

        // ── Tier-benefit assignments ─────────────────────────────────────────
        // SILVER
        tierBenefitRepository.save(new TierBenefit(silver, freeDelivery, new BigDecimal("500"), null, true));
        tierBenefitRepository.save(new TierBenefit(silver, extraDiscount, new BigDecimal("5"), null, true));
        // GOLD
        tierBenefitRepository.save(new TierBenefit(gold, freeDelivery, BigDecimal.ZERO, null, true));
        tierBenefitRepository.save(new TierBenefit(gold, extraDiscount, new BigDecimal("10"), null, true));
        tierBenefitRepository.save(new TierBenefit(gold, exclusiveDeals, null, null, true));
        tierBenefitRepository.save(new TierBenefit(gold, earlyAccess, null, null, true));
        // PLATINUM
        tierBenefitRepository.save(new TierBenefit(platinum, freeDelivery, BigDecimal.ZERO, null, true));
        tierBenefitRepository.save(new TierBenefit(platinum, extraDiscount, new BigDecimal("15"), null, true));
        tierBenefitRepository.save(new TierBenefit(platinum, exclusiveDeals, null, null, true));
        tierBenefitRepository.save(new TierBenefit(platinum, earlyAccess, null, null, true));
        tierBenefitRepository.save(new TierBenefit(platinum, prioritySupport, null, null, true));

        // ── Plan-scoped discount eligibilities ───────────────────────────────
        // MONTHLY plan: 5% across all product categories.
        eligibilityRepository.save(new BenefitEligibility(
                monthly, EligibilityScopeType.ALL, null, new BigDecimal("5.00"), true));

        // QUARTERLY plan: 10% on ELECTRONICS category, 15% on a specific product item.
        eligibilityRepository.save(new BenefitEligibility(
                quarterly, EligibilityScopeType.PRODUCT_CATEGORY, "ELECTRONICS", new BigDecimal("10.00"), true));
        eligibilityRepository.save(new BenefitEligibility(
                quarterly, EligibilityScopeType.PRODUCT_ITEM, "SKU-IPHONE", new BigDecimal("15.00"), true));

        // YEARLY plan: 5% ALL, 15% on ELECTRONICS, 25% on a specific product item.
        eligibilityRepository.save(new BenefitEligibility(
                yearly, EligibilityScopeType.ALL, null, new BigDecimal("5.00"), true));
        eligibilityRepository.save(new BenefitEligibility(
                yearly, EligibilityScopeType.PRODUCT_CATEGORY, "ELECTRONICS", new BigDecimal("15.00"), true));
        eligibilityRepository.save(new BenefitEligibility(
                yearly, EligibilityScopeType.PRODUCT_ITEM, "SKU-IPHONE", new BigDecimal("25.00"), true));

        // ── Tier progression rules ───────────────────────────────────────────
        tierRuleRepository.save(new TierRule(gold, 5, null, null, 10, true));
        tierRuleRepository.save(new TierRule(gold, null, new BigDecimal("5000"), null, 10, true));
        tierRuleRepository.save(new TierRule(platinum, 15, null, null, 20, true));
        tierRuleRepository.save(new TierRule(platinum, null, new BigDecimal("20000"), null, 20, true));
        tierRuleRepository.save(new TierRule(platinum, null, null, "VIP", 30, true));

        // ── Deals and plan access ────────────────────────────────────────────
        seedDeals(monthly, quarterly, yearly);

        log.info("Seeded {} plans, {} tiers, {} benefit definitions, {} tier rules, {} deals",
                planRepository.count(), tierRepository.count(),
                benefitRepository.count(), tierRuleRepository.count(), dealRepository.count());
    }

    private void seedDeals(MembershipPlan monthly, MembershipPlan quarterly, MembershipPlan yearly) {
        Instant now = Instant.now();

        // Exclusive deal: only QUARTERLY and YEARLY plan holders can see it.
        Deal vipDeal = dealRepository.save(new Deal("VIP-WEEKEND", "VIP Weekend Deal",
                "Members-only exclusive deal for quarterly and yearly subscribers", DealType.EXCLUSIVE_DEAL,
                null, now.plus(30, ChronoUnit.DAYS), true, true));
        dealPlanAccessRepository.save(new DealPlanAccess(vipDeal.getId(), quarterly.getId(), now));
        dealPlanAccessRepository.save(new DealPlanAccess(vipDeal.getId(), yearly.getId(), now));

        // Sale: public in 2 days; YEARLY gets early access now, QUARTERLY in 1 day.
        Deal summerSale = dealRepository.save(new Deal("SUMMER-SALE", "Summer Mega Sale",
                "Seasonal sale — yearly subscribers get the earliest access", DealType.SALE,
                now.plus(2, ChronoUnit.DAYS), now.plus(12, ChronoUnit.DAYS), false, true));
        dealPlanAccessRepository.save(new DealPlanAccess(summerSale.getId(), yearly.getId(), now));
        dealPlanAccessRepository.save(new DealPlanAccess(
                summerSale.getId(), quarterly.getId(), now.plus(1, ChronoUnit.DAYS)));
        // Monthly plan gets access at the public start time (no special early access).
        dealPlanAccessRepository.save(new DealPlanAccess(
                summerSale.getId(), monthly.getId(), now.plus(2, ChronoUnit.DAYS)));
    }
}
