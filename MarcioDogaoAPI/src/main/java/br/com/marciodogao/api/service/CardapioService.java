package br.com.marciodogao.api.service;

import br.com.marciodogao.api.domain.Cardapio;
import br.com.marciodogao.api.domain.Preco;
import br.com.marciodogao.api.domain.Turno;
import br.com.marciodogao.api.dto.CardapioDtos.ProdutoRequest;
import br.com.marciodogao.api.dto.CardapioDtos.ProdutoResponse;
import br.com.marciodogao.api.exception.NaoEncontradoException;
import br.com.marciodogao.api.exception.RegraNegocioException;
import br.com.marciodogao.api.repository.CardapioRepository;
import br.com.marciodogao.api.repository.PedidoFeitoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * REGRAS:
 * - Nome do produto único no cardápio.
 * - Todo produto tem preço da manhã (> 0); preço da noite é opcional.
 * - Produto que já apareceu em algum pedido não pode ser excluído (o histórico seria perdido).
 */
@Service
public class CardapioService {

    private final CardapioRepository cardapio;
    private final PedidoFeitoRepository itens;
    private final TurnoService turnos;

    public CardapioService(CardapioRepository cardapio, PedidoFeitoRepository itens, TurnoService turnos) {
        this.cardapio = cardapio;
        this.itens = itens;
        this.turnos = turnos;
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listar() {
        Turno turno = turnos.turnoAtual();
        return cardapio.findAllByOrderByProdutoAsc().stream().map(c -> resposta(c, turno)).toList();
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscar(Integer codCar) {
        return resposta(obter(codCar), turnos.turnoAtual());
    }

    @Transactional
    public ProdutoResponse criar(ProdutoRequest req) {
        String nome = req.produto().trim();
        if (cardapio.existsByProdutoIgnoreCase(nome)) {
            throw new RegraNegocioException("Já existe o produto \"" + nome + "\" no cardápio.");
        }
        Cardapio produto = new Cardapio(nome);
        produto.definirPreco(new Preco(req.precoManha(), req.precoNoite()));
        return resposta(cardapio.save(produto), turnos.turnoAtual());
    }

    @Transactional
    public ProdutoResponse atualizar(Integer codCar, ProdutoRequest req) {
        Cardapio produto = obter(codCar);
        String nome = req.produto().trim();
        if (cardapio.existsByProdutoIgnoreCaseAndCodCarNot(nome, codCar)) {
            throw new RegraNegocioException("Já existe o produto \"" + nome + "\" no cardápio.");
        }
        produto.setProduto(nome);
        if (produto.getPreco() == null) {
            produto.definirPreco(new Preco(req.precoManha(), req.precoNoite()));
        } else {
            produto.getPreco().setManha(req.precoManha());
            produto.getPreco().setNoturno(req.precoNoite());
        }
        return resposta(produto, turnos.turnoAtual());
    }

    @Transactional
    public void excluir(Integer codCar) {
        Cardapio produto = obter(codCar);
        if (itens.existsByCardapio(produto)) {
            throw new RegraNegocioException("\"" + produto.getProduto() + "\" já foi vendido e não pode ser excluído. Altere o nome ou o preço.");
        }
        cardapio.delete(produto);
    }

    Cardapio obter(Integer codCar) {
        return cardapio.findById(codCar)
                .orElseThrow(() -> new NaoEncontradoException("Produto " + codCar + " não existe no cardápio."));
    }

    private ProdutoResponse resposta(Cardapio c, Turno turno) {
        Preco p = c.getPreco();
        return new ProdutoResponse(c.getCodCar(), c.getProduto(),
                p == null ? null : p.getManha(),
                p == null ? null : p.getNoturno(),
                turno,
                p == null ? null : p.valorNoTurno(turno));
    }
}
