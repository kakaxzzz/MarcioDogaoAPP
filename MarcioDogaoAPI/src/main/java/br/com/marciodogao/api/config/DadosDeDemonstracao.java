package br.com.marciodogao.api.config;

import br.com.marciodogao.api.domain.FormaPagamento;
import br.com.marciodogao.api.dto.AuthDtos.RegistrarRequest;
import br.com.marciodogao.api.dto.CaixaDtos.AbrirCaixaRequest;
import br.com.marciodogao.api.dto.CardapioDtos.ProdutoRequest;
import br.com.marciodogao.api.dto.ComandaDtos.AbrirComandaRequest;
import br.com.marciodogao.api.dto.ComandaDtos.ItemRequest;
import br.com.marciodogao.api.dto.ComandaDtos.NovoPedidoRequest;
import br.com.marciodogao.api.service.AuthService;
import br.com.marciodogao.api.service.CaixaService;
import br.com.marciodogao.api.service.CardapioService;
import br.com.marciodogao.api.service.ComandaService;
import br.com.marciodogao.api.service.MesaService;
import br.com.marciodogao.api.service.PagamentoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Só no perfil "demo": enche o banco em memória com dados de EXEMPLO (produtos e preços inventados),
 * passando pelas mesmas regras da API. Nunca roda contra o SQL Server.
 */
@Component
@Profile("demo")
public class DadosDeDemonstracao implements CommandLineRunner {

    private final AuthService auth;
    private final CardapioService cardapio;
    private final MesaService mesas;
    private final CaixaService caixa;
    private final ComandaService comandas;
    private final PagamentoService pagamentos;
    private final String email;
    private final String senha;

    public DadosDeDemonstracao(AuthService auth, CardapioService cardapio, MesaService mesas, CaixaService caixa,
                               ComandaService comandas, PagamentoService pagamentos,
                               @Value("${demo.email}") String email, @Value("${demo.senha}") String senha) {
        this.auth = auth;
        this.cardapio = cardapio;
        this.mesas = mesas;
        this.caixa = caixa;
        this.comandas = comandas;
        this.pagamentos = pagamentos;
        this.email = email;
        this.senha = senha;
    }

    @Override
    public void run(String... args) {
        int garcom = auth.registrar(new RegistrarRequest(email, senha), null).codLog();

        int dogao = produto("Dogão Sagrado", "24.90", "27.90");
        int brutao = produto("Brutão", "32.90", "36.90");
        int combo = produto("Combo Casal", "64.90", "72.90");
        int executivo = produto("Executivo da Casa", "24.90", null);
        int parmegiana = produto("Parmegiana", "34.90", "38.90");
        int xSalada = produto("X-Salada", "19.90", "22.90");
        int fritas = produto("Batata Frita", "18.00", "21.00");
        int refri = produto("Refrigerante Lata", "6.00", "7.00");
        int suco = produto("Suco Natural", "9.00", "10.00");
        produto("Água Mineral", "4.00", null);

        for (int i = 1; i <= 12; i++) {
            mesas.criar(String.format("%02d", i));
        }

        caixa.abrir(new AbrirCaixaRequest(new BigDecimal("100.00")));

        // Mesas que já pagaram
        int c1 = comanda("02", garcom, item(combo, 1, null), item(refri, 2, null));
        pagamentos.pagarComanda(c1, FormaPagamento.PIX);
        int c2 = comanda("05", garcom, item(dogao, 3, "sem cebola"), item(suco, 3, null), item(fritas, 1, null));
        pagamentos.pagarComanda(c2, FormaPagamento.CARTAO_CREDITO);
        int c3 = comanda("09", garcom, item(executivo, 2, null), item(refri, 2, null));
        pagamentos.pagarComanda(c3, FormaPagamento.DINHEIRO);

        // Mesas em atendimento
        comanda("03", garcom, item(brutao, 2, "bem passado"), item(fritas, 1, null), item(refri, 2, null));
        comanda("07", garcom, item(parmegiana, 2, null), item(combo, 1, null), item(suco, 4, null));
        comanda("10", garcom, item(xSalada, 2, "sem tomate"), item(refri, 2, null));
    }

    private int produto(String nome, String dia, String noite) {
        return cardapio.criar(new ProdutoRequest(nome, new BigDecimal(dia), noite == null ? null : new BigDecimal(noite))).codCar();
    }

    private static ItemRequest item(int codCar, int quantidade, String variacao) {
        return new ItemRequest(codCar, quantidade, variacao);
    }

    private int comanda(String mesa, int garcom, ItemRequest... itens) {
        int cod = comandas.abrir(new AbrirComandaRequest(mesa, mesa, null), garcom).codComan();
        comandas.adicionarPedido(cod, new NovoPedidoRequest(null, List.of(itens)));
        return cod;
    }
}
