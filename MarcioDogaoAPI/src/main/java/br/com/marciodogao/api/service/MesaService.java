package br.com.marciodogao.api.service;

import br.com.marciodogao.api.domain.Comanda;
import br.com.marciodogao.api.domain.Mesa;
import br.com.marciodogao.api.dto.ComandaDtos.ComandaResponse;
import br.com.marciodogao.api.exception.RegraNegocioException;
import br.com.marciodogao.api.repository.ComandaRepository;
import br.com.marciodogao.api.repository.MesaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REGRAS DAS MESAS:
 * - Mesa LIVRE = não tem comanda aberta. Mesa OCUPADA = tem uma comanda aberta (só pode ter uma).
 * - O número da mesa tem até 2 caracteres e não se repete.
 */
@Service
public class MesaService {

    public record MesaResponse(Integer codMesa, String numero, boolean livre, ComandaResponse comanda) {
    }

    private final MesaRepository mesas;
    private final ComandaRepository comandas;
    private final ComandaService comandaService;

    public MesaService(MesaRepository mesas, ComandaRepository comandas, ComandaService comandaService) {
        this.mesas = mesas;
        this.comandas = comandas;
        this.comandaService = comandaService;
    }

    @Transactional(readOnly = true)
    public List<MesaResponse> listar() {
        Map<String, Comanda> abertaPorMesa = new HashMap<>();
        for (Comanda c : comandas.findAbertas()) {
            if (c.getMesa() != null) {
                abertaPorMesa.putIfAbsent(c.getMesa(), c);
            }
        }
        return mesas.findAllByOrderByNumeroAsc().stream().map(m -> {
            Comanda aberta = abertaPorMesa.get(m.getNumero());
            return new MesaResponse(m.getCodMesa(), m.getNumero(), aberta == null,
                    aberta == null ? null : comandaService.resposta(aberta));
        }).toList();
    }

    @Transactional
    public MesaResponse criar(String numero) {
        String n = numero == null ? "" : numero.trim();
        if (n.isEmpty() || n.length() > 2) {
            throw new RegraNegocioException("O número da mesa deve ter 1 ou 2 caracteres (ex.: 01, 12).");
        }
        if (mesas.existsByNumero(n)) {
            throw new RegraNegocioException("A mesa " + n + " já existe.");
        }
        Mesa nova = mesas.save(new Mesa(n));
        return new MesaResponse(nova.getCodMesa(), nova.getNumero(), true, null);
    }
}
