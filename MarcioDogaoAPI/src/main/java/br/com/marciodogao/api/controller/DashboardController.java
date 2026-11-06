package br.com.marciodogao.api.controller;

import br.com.marciodogao.api.dto.DashboardDtos.DashboardHistoricoResponse;
import br.com.marciodogao.api.dto.DashboardDtos.DashboardResponse;
import br.com.marciodogao.api.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard")
public class DashboardController {

    private final DashboardService dashboard;

    public DashboardController(DashboardService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping
    @Operation(summary = "Painel em tempo real do caixa aberto")
    public DashboardResponse painel() {
        return dashboard.painel();
    }

    @GetMapping("/historico")
    @Operation(summary = "Faturamento de cada caixa fechado (tabela DashBoard)")
    public List<DashboardHistoricoResponse> historico() {
        return dashboard.historico();
    }
}
