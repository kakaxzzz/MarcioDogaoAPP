package br.com.marciodogao.api.service;

import br.com.marciodogao.api.domain.FormaPagamento;
import br.com.marciodogao.api.domain.Pagamento;
import br.com.marciodogao.api.domain.Turno;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Agrupa pagamentos por turno (manhã / noite) e por forma de pagamento. */
@Component
class RelatorioPagamentos {

    private final TurnoService turnos;

    RelatorioPagamentos(TurnoService turnos) {
        this.turnos = turnos;
    }

    Map<String, BigDecimal> porTurno(List<Pagamento> pagamentos) {
        Map<String, BigDecimal> mapa = new LinkedHashMap<>();
        for (Turno t : Turno.values()) {
            mapa.put(t.name(), Dinheiro.duasCasas(BigDecimal.ZERO));
        }
        for (Pagamento pg : pagamentos) {
            Turno turno = turnos.turnoDe(pg.getPedido().getComanda().getHorarioPedido());
            mapa.merge(turno.name(), pg.getValorPago(), BigDecimal::add);
        }
        mapa.replaceAll((k, v) -> Dinheiro.duasCasas(v));
        return mapa;
    }

    Map<String, BigDecimal> porForma(List<Pagamento> pagamentos) {
        Map<String, BigDecimal> mapa = new LinkedHashMap<>();
        for (FormaPagamento f : FormaPagamento.values()) {
            mapa.put(f.name(), Dinheiro.duasCasas(BigDecimal.ZERO));
        }
        for (Pagamento pg : pagamentos) {
            mapa.merge(pg.getFormaPagamento(), pg.getValorPago(), BigDecimal::add);
        }
        mapa.replaceAll((k, v) -> Dinheiro.duasCasas(v));
        return mapa;
    }

    BigDecimal total(List<Pagamento> pagamentos) {
        return Dinheiro.duasCasas(pagamentos.stream().map(Pagamento::getValorPago).reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
