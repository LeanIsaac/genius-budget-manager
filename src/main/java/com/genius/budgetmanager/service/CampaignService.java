package com.genius.budgetmanager.service;

import com.genius.budgetmanager.model.BudgetSummary;
import com.genius.budgetmanager.model.Campaign;
import com.genius.budgetmanager.model.CampaignBudgetSummary;
import com.genius.budgetmanager.model.Expense;
import com.genius.budgetmanager.model.GlobalBudgetSummary;
import com.genius.budgetmanager.repository.CampaignRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Service
public class CampaignService {

    @Autowired
    private CampaignRepository repository;

    // LISTAR CAMPAÑAS

    public List<Campaign> getAllCampaigns() {
        return repository.findAll();
    }

    // FILTRAR POR STATUS Y CLIENTE

    public List<Campaign> searchCampaigns(String status, String client) {

        return repository.findAll().stream()

                .filter(c -> status == null || status.isBlank()
                        || (c.getStatus() != null
                        && c.getStatus().equalsIgnoreCase(status.trim())))

                .filter(c -> client == null || client.isBlank()
                        || (c.getClient() != null
                        && c.getClient().equalsIgnoreCase(client.trim())))

                .collect(Collectors.toList());
    }

    public List<Campaign> getCampaignsByStatus(String status) {

        return repository.findAll().stream()
                .filter(c -> c.getStatus() != null
                        && c.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }

    // BUSCAR CAMPAÑA POR ID

    public Campaign getCampaignById(Long id) {

        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Campaign not found: " + id
                ));
    }

    // PORCENTAJE DE CONSUMO

    private double percentage(double spent, double budget) {

        if (budget <= 0) {
            return 0.0;
        }

        return Math.round((spent / budget) * 10000.0) / 100.0;
    }

    // RESUMEN INDIVIDUAL DE CAMPAÑA

    public CampaignBudgetSummary getBudgetSummary(Long campaignId) {

        Campaign campaign = getCampaignById(campaignId);

        CampaignBudgetSummary summary = new CampaignBudgetSummary();

        summary.setCampaignId(campaign.getId());
        summary.setCampaignName(campaign.getName());
        summary.setClient(campaign.getClient());
        summary.setTotalBudget(campaign.getBudget());
        summary.setSpent(campaign.getSpent());

        summary.setRemaining(
                campaign.getBudget() - campaign.getSpent()
        );

        summary.setPercentageUsed(
                percentage(campaign.getSpent(), campaign.getBudget())
        );

        return summary;
    }

    // RESUMEN DE TODOS LOS CLIENTES

    public List<BudgetSummary> getBudgetSummaryAllClients() {

        Map<String, List<Campaign>> byClient =
                repository.findAll().stream()

                        .collect(Collectors.groupingBy(
                                Campaign::getClient,
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        return byClient.entrySet().stream()

                .map(e -> buildClientSummary(
                        e.getKey(),
                        e.getValue()
                ))

                .collect(Collectors.toList());
    }

    // RESUMEN DE UN CLIENTE

    public BudgetSummary getBudgetSummaryByClient(String client) {

        List<Campaign> campaigns = repository.findAll().stream()

                .filter(c -> c.getClient() != null
                        && c.getClient().equalsIgnoreCase(client.trim()))

                .collect(Collectors.toList());

        if (campaigns.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No campaigns found for client: " + client
            );
        }

        return buildClientSummary(client, campaigns);
    }

    // CONSTRUIR RESUMEN DEL CLIENTE

    private BudgetSummary buildClientSummary(
            String client,
            List<Campaign> campaigns) {

        BudgetSummary summary = new BudgetSummary();

        int totalCampaigns = campaigns.size();

        int activeCampaigns = (int) campaigns.stream()
                .filter(c -> "active".equalsIgnoreCase(c.getStatus()))
                .count();

        double totalBudget = campaigns.stream()
                .mapToDouble(Campaign::getBudget)
                .sum();

        double totalSpent = campaigns.stream()
                .mapToDouble(Campaign::getSpent)
                .sum();

        double totalAvailable = totalBudget - totalSpent;

        double consumptionPercentage =
                percentage(totalSpent, totalBudget);

        summary.setClient(client);
        summary.setTotalCampaigns(totalCampaigns);
        summary.setActiveCampaigns(activeCampaigns);
        summary.setTotalBudget(totalBudget);
        summary.setTotalSpent(totalSpent);
        summary.setTotalAvailable(totalAvailable);
        summary.setConsumptionPercentage(consumptionPercentage);

        return summary;
    }

    // GASTOS POR CAMPAÑA

    public List<Expense> getExpensesByCampaign(Long campaignId) {

        getCampaignById(campaignId);

        return repository.findExpensesByCampaignId(campaignId);
    }

    // REGISTRAR GASTO

    public Expense addExpense(Long campaignId, Expense expense) {

        Campaign campaign = getCampaignById(campaignId);

        if (expense == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Expense is required"
            );
        }

        if (expense.getDescription() == null
                || expense.getDescription().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Description is required"
            );
        }

        if (expense.getAmount() == null
                || !Double.isFinite(expense.getAmount())
                || expense.getAmount() <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Expense amount must be greater than zero"
            );
        }

        expense.setCampaignId(campaignId);

        campaign.setSpent(
                campaign.getSpent() + expense.getAmount()
        );

        return repository.saveExpense(expense);
    }

    // RESUMEN GLOBAL

    public GlobalBudgetSummary getGlobalBudgetSummary() {

        List<Campaign> active = repository.findAll().stream()

                .filter(c -> "active".equalsIgnoreCase(c.getStatus()))

                .collect(Collectors.toList());

        double totalBudget = active.stream()
                .mapToDouble(Campaign::getBudget)
                .sum();

        double totalSpent = active.stream()
                .mapToDouble(Campaign::getSpent)
                .sum();

        double totalAvailable = totalBudget - totalSpent;

        double pct = percentage(totalSpent, totalBudget);

        GlobalBudgetSummary summary = new GlobalBudgetSummary();

        summary.setActiveCampaigns(active.size());
        summary.setTotalBudget(totalBudget);
        summary.setTotalSpent(totalSpent);
        summary.setTotalAvailable(totalAvailable);
        summary.setConsumptionPercentage(pct);

        return summary;
    }

    // ACTUALIZAR PRESUPUESTO

    public Campaign updateBudget(Long campaignId, Double newBudget) {

        Campaign campaign = getCampaignById(campaignId);

        if (newBudget == null
                || !Double.isFinite(newBudget)
                || newBudget <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Budget must be greater than zero"
            );
        }

        if (newBudget < campaign.getSpent()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New budget cannot be lower than current spent amount"
            );
        }

        // No reiniciamos el gasto acumulado.
        campaign.setBudget(newBudget);

        return campaign;
    }

    // CREAR CAMPAÑA

    public Campaign createCampaign(Campaign campaign) {

        if (campaign == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Campaign is required"
            );
        }

        if (campaign.getName() == null
                || campaign.getName().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Campaign name is required"
            );
        }

        if (campaign.getClient() == null
                || campaign.getClient().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Client is required"
            );
        }

        if (campaign.getBudget() == null
                || !Double.isFinite(campaign.getBudget())
                || campaign.getBudget() <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Budget must be greater than zero"
            );
        }

        campaign.setSpent(0.0);

        if (campaign.getStatus() == null
                || campaign.getStatus().isBlank()) {

            campaign.setStatus("draft");
        }

        if (campaign.getCurrency() == null
                || campaign.getCurrency().isBlank()) {

            campaign.setCurrency("ARS");
        }

        return repository.saveCampaign(campaign);
    }
}