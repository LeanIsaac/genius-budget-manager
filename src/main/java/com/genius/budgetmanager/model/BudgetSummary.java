
package com.genius.budgetmanager.model;

public class BudgetSummary {

    private String client;
    private int totalCampaigns;
    private int activeCampaigns;
    private double totalBudget;
    private double totalSpent;
    private double totalAvailable;
    private double consumptionPercentage;

    public BudgetSummary() {}

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public int getTotalCampaigns() {
        return totalCampaigns;
    }

    public void setTotalCampaigns(int totalCampaigns) {
        this.totalCampaigns = totalCampaigns;
    }

    public int getActiveCampaigns() {
        return activeCampaigns;
    }

    public void setActiveCampaigns(int activeCampaigns) {
        this.activeCampaigns = activeCampaigns;
    }

    public double getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(double totalBudget) {
        this.totalBudget = totalBudget;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
    }

    public double getTotalAvailable() {
        return totalAvailable;
    }

    public void setTotalAvailable(double totalAvailable) {
        this.totalAvailable = totalAvailable;
    }

    public double getConsumptionPercentage() {
        return consumptionPercentage;
    }

    public void setConsumptionPercentage(double consumptionPercentage) {
        this.consumptionPercentage = consumptionPercentage;
    }
}