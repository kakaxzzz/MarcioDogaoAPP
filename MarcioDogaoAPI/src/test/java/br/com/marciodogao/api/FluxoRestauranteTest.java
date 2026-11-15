package br.com.marciodogao.api;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Um dia de trabalho completo, às 19:00 (turno da noite), passando por todas as regras principais. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FluxoRestauranteTest {

    @TestConfiguration
    static class RelogioDasSeteDaNoite {
        @Bean
        @Primary
        Clock relogioFixo() {
            ZoneId sp = ZoneId.of("America/Sao_Paulo");
            // Hoje às 19:00: com uma data fixa no passado o token JWT do teste já nasceria vencido.
            return Clock.fixed(ZonedDateTime.now(sp).withHour(19).withMinute(0).withSecond(0).withNano(0).toInstant(), sp);
        }
    }

    @Autowired
    MockMvc mvc;

    private String token;

    @Test
    void diaCompletoNoRestaurante() throws Exception {
        // --- Login ---
        enviar(post("/api/auth/registrar"), "{\"email\":\"Marcio@Dogao.com\",\"senha\":\"segredo1\"}", false)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codLog").value(1))
                .andExpect(jsonPath("$.email").value("marcio@dogao.com"));
        enviar(post("/api/auth/registrar"), "{\"email\":\"intruso@x.com\",\"senha\":\"segredo1\"}", false)
                .andExpect(status().isForbidden());
        enviar(post("/api/auth/login"), "{\"email\":\"marcio@dogao.com\",\"senha\":\"errada\"}", false)
                .andExpect(status().isUnauthorized());
        String login = enviar(post("/api/auth/login"), "{\"email\":\"marcio@dogao.com\",\"senha\":\"segredo1\"}", false)
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        token = JsonPath.read(login, "$.token");
        mvc.perform(get("/api/comandas")).andExpect(status().isUnauthorized());

        // --- Cardápio (preço noturno às 19h) ---
        int dogao = id(enviar(post("/api/cardapio"), "{\"produto\":\"Dogão Completo\",\"precoManha\":15.00,\"precoNoite\":18.50}", true)
                .andExpect(status().isCreated()), "$.codCar");
        int refri = id(enviar(post("/api/cardapio"), "{\"produto\":\"Refrigerante Lata\",\"precoManha\":6.00}", true)
                .andExpect(status().isCreated()), "$.codCar");
        enviar(post("/api/cardapio"), "{\"produto\":\"dogão completo\",\"precoManha\":1}", true)
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(get("/api/cardapio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].produto").value("Dogão Completo"))
                .andExpect(jsonPath("$[0].turnoAtual").value("NOITE"))
                .andExpect(jsonPath("$[0].precoAtual").value(18.50))
                .andExpect(jsonPath("$[1].precoAtual").value(6.00));

        // --- Comanda e pedido ---
        int comanda = id(enviar(post("/api/comandas"), "{\"numeroComanda\":\"7\",\"mesa\":\"3\"}", true)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.turno").value("NOITE"))
                .andExpect(jsonPath("$.nomeFuncionario").value("marcio")), "$.codComan");
        enviar(post("/api/comandas"), "{\"numeroComanda\":\"7\"}", true)
                .andExpect(status().isUnprocessableEntity());

        String pedido = "{\"observacao\":\"capricha\",\"itens\":[{\"codCar\":" + dogao + ",\"quantidade\":2,\"variacao\":\"sem cebola\"},{\"codCar\":" + refri + "}]}";
        enviar(post("/api/comandas/" + comanda + "/pedidos"), pedido, true)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valorTotal").value(43.00))
                .andExpect(jsonPath("$.pedidos[0].itens", hasSize(3)))
                .andExpect(jsonPath("$.pedidos[0].itens[0].variacao").value("sem cebola"))
                .andExpect(jsonPath("$.pedidos[0].itens[2].variacao").value("Padrão"));

        // --- Pagamento exige caixa aberto ---
        enviar(post("/api/comandas/" + comanda + "/pagamentos"), "{\"formaPagamento\":\"PIX\"}", true)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail", containsString("Nenhum caixa aberto")));

        enviar(post("/api/caixa/abrir"), "{\"valorAbertura\":100.00}", true).andExpect(status().isCreated());
        enviar(post("/api/caixa/abrir"), "{\"valorAbertura\":50}", true).andExpect(status().isUnprocessableEntity());
        enviar(post("/api/caixa/fechar"), "", true)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail", containsString("7")));

        enviar(post("/api/comandas/" + comanda + "/pagamentos"), "{\"formaPagamento\":\"PIX\"}", true)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valorPago").value(43.00))
                .andExpect(jsonPath("$.statusComanda").value("FECHADA"));
        enviar(post("/api/comandas/" + comanda + "/pagamentos"), "{\"formaPagamento\":\"PIX\"}", true)
                .andExpect(status().isUnprocessableEntity());
        enviar(post("/api/comandas/" + comanda + "/pedidos"), pedido, true)
                .andExpect(status().isUnprocessableEntity());
        enviar(delete("/api/comandas/" + comanda), "", true)
                .andExpect(status().isUnprocessableEntity());

        // --- Caixa: sangria ---
        enviar(post("/api/caixa/movimentacoes"), "{\"tipo\":\"SAIDA\",\"valor\":500}", true)
                .andExpect(status().isUnprocessableEntity());
        enviar(post("/api/caixa/movimentacoes"), "{\"tipo\":\"SAIDA\",\"valor\":20}", true)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldo").value(123.00))
                .andExpect(jsonPath("$.recebidoPorFormaPagamento.PIX").value(43.00))
                .andExpect(jsonPath("$.recebidoPorTurno.NOITE").value(43.00));

        // --- Dashboard ---
        enviar(get("/api/dashboard"), "", true)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.faturamento").value(43.00))
                .andExpect(jsonPath("$.comandasAtendidas").value(1))
                .andExpect(jsonPath("$.ticketMedio").value(43.00))
                .andExpect(jsonPath("$.maisVendidos[0].produto").value("Dogão Completo"))
                .andExpect(jsonPath("$.maisVendidos[0].quantidade").value(2));

        // --- Fechamento ---
        enviar(post("/api/caixa/fechar"), "", true)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aberto").value(false))
                .andExpect(jsonPath("$.valorFechamento").value(123.00))
                .andExpect(jsonPath("$.relacoesPagamento", containsString("Noite: R$ 43,00")));
        enviar(get("/api/dashboard/historico"), "", true)
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].faturamento").value(43.00));

        // --- Produto já vendido não pode ser excluído ---
        enviar(delete("/api/cardapio/" + dogao), "", true).andExpect(status().isUnprocessableEntity());

        // --- Validação ---
        enviar(post("/api/comandas"), "{\"numeroComanda\":\"123\"}", true)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.numeroComanda").exists());
    }

    private ResultActions enviar(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder req,
                                 String corpo, boolean autenticado) throws Exception {
        req.contentType(MediaType.APPLICATION_JSON);
        if (!corpo.isEmpty()) {
            req.content(corpo);
        }
        if (autenticado) {
            req.header("Authorization", "Bearer " + token);
        }
        return mvc.perform(req);
    }

    private static int id(ResultActions r, String caminho) throws Exception {
        return JsonPath.read(r.andReturn().getResponse().getContentAsString(), caminho);
    }
}
