package com.genius.budgetmanager.controller;

import com.genius.budgetmanager.model.BudgetSummary;
import com.genius.budgetmanager.model.BudgetUpdateRequest;
import com.genius.budgetmanager.model.Campaign;
import com.genius.budgetmanager.model.CampaignBudgetSummary;
import com.genius.budgetmanager.model.Expense;
import com.genius.budgetmanager.model.GlobalBudgetSummary;
import com.genius.budgetmanager.service.CampaignService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
@Tag(name = "Campaigns", description = "Gestion de campanas y presupuestos")
public class CampaignController {

    @Autowired
    private CampaignService campaignService;

    @GetMapping
    @Operation(summary = "Listar campanas",
            description = "Filtra opcionalmente por status y client")
    public ResponseEntity<List<Campaign>> getCampaigns(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String client) {

        return ResponseEntity.ok(
                campaignService.searchCampaigns(status, client)
        );
    }

    @PostMapping
    @Operation(summary = "Crear una campana")
    public ResponseEntity<Campaign> createCampaign(
            @RequestBody Campaign campaign) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(campaignService.createCampaign(campaign));
    }

    @GetMapping("/summary")
    @Operation(summary = "Resumen global de presupuesto")
    public ResponseEntity<GlobalBudgetSummary> getGlobalBudgetSummary() {

        return ResponseEntity.ok(
                campaignService.getGlobalBudgetSummary()
        );
    }

    @GetMapping("/clients/summary")
    @Operation(summary = "Resumen de todos los clientes")
    public ResponseEntity<List<BudgetSummary>> getAllClientsSummary() {

        return ResponseEntity.ok(
                campaignService.getBudgetSummaryAllClients()
        );
    }

    @GetMapping("/clients/{client}/summary")
    @Operation(summary = "Resumen de presupuesto por cliente")
    public ResponseEntity<BudgetSummary> getClientSummary(
            @PathVariable String client) {

        return ResponseEntity.ok(
                campaignService.getBudgetSummaryByClient(client)
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener campana por ID")
    public ResponseEntity<Campaign> getCampaignById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                campaignService.getCampaignById(id)
        );
    }

    @GetMapping("/{id}/summary")
    @Operation(summary = "Resumen individual de campaña")
    public ResponseEntity<CampaignBudgetSummary> getBudgetSummary(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                campaignService.getBudgetSummary(id)
        );
    }

    @GetMapping("/{id}/expenses")
    @Operation(summary = "Listar gastos de una campana")
    public ResponseEntity<List<Expense>> getExpenses(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                campaignService.getExpensesByCampaign(id)
        );
    }

    @PostMapping("/{id}/expenses")
    @Operation(summary = "Registrar un gasto")
    public ResponseEntity<Expense> addExpense(
            @PathVariable Long id,
            @RequestBody Expense expense) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(campaignService.addExpense(id, expense));
    }

    @PutMapping("/{id}/budget")
    @Operation(summary = "Actualizar presupuesto")
    public ResponseEntity<Campaign> updateBudget(
            @PathVariable Long id,
            @RequestBody BudgetUpdateRequest request) {

        return ResponseEntity.ok(
                campaignService.updateBudget(id, request.getBudget())
        );
    }
}
