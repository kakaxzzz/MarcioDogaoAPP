# API Márcio Dogão (Java 21 + Spring Boot 3.5)

API REST do restaurante, ligada ao banco **MarcioDogaoAPP** (SQL Server). Serve para o app Android, iOS ou web: todos falam com ela por HTTP + JSON, com login por token JWT.

## 1. Preparar o banco (uma vez só)

Os scripts em `database/` **já foram aplicados** no seu SQL Server local. Para outro servidor, rode na ordem:

```bash
sqlcmd -S localhost -E -C -f 65001 -i database/001_ajustes_para_api.sql
```

```bash
sqlcmd -S localhost -E -C -f 65001 -i database/002_pagamento_unico_por_pedido.sql
```

O que eles mudam: dinheiro passa a ter centavos (`DECIMAL(10,2)`), `Senha` cabe o hash BCrypt, `Email` é único, `Preco` ganha `CodCar` (preço por produto), `Pagamento` ganha `Valor_Pago` e `CodCaix`, e um pedido não pode ser pago duas vezes.

## 2. Ligar o TCP/IP no SQL Server (obrigatório para Java)

O Java (JDBC) só conecta por TCP, que hoje está **desligado** no seu servidor:

1. Abra **SQL Server Configuration Manager** (no Iniciar, procure `SQLServerManager17.msc`).
2. *Configuração de Rede do SQL Server* → *Protocolos para MSSQLSERVER* → **TCP/IP** → *Habilitar*.
3. Em *Propriedades do TCP/IP* → aba *Endereços IP* → *IPAll*: `Porta TCP` = **1433**.
4. *Serviços do SQL Server* → reinicie o **SQL Server (MSSQLSERVER)**.

Por padrão a API entra com seu **usuário do Windows** (`integratedSecurity=true`). A DLL necessária (`lib/mssql-jdbc_auth-*.dll`) é baixada automaticamente no build.

Para usar usuário/senha do SQL Server (por exemplo, num servidor na nuvem), defina:

```bash
set DB_URL=jdbc:sqlserver://SERVIDOR:1433;databaseName=MarcioDogaoAPP;encrypt=true;trustServerCertificate=true
set DB_USER=usuario
set DB_PASSWORD=senha
```

## 3. Rodar

Precisa do JDK 21. Você já tem um junto com o Android Studio:

```bash
set JAVA_HOME=C:\Program Files\Android\openjdk\jdk-21.0.8
```

```bash
mvnw.cmd spring-boot:run
```

- API: `http://localhost:8080`
- Documentação interativa (Swagger): `http://localhost:8080/swagger-ui.html`
- Testes automáticos: `mvnw.cmd test`

No **emulador Android**, `localhost` do PC é `http://10.0.2.2:8080`. No celular de verdade, use o IP do PC na rede (ex.: `http://192.168.0.10:8080`) e libere a porta 8080 no firewall do Windows.

Em produção, troque o segredo do token: `set JWT_SECRET=uma-frase-longa-com-mais-de-32-caracteres`.

## 4. Fluxo de uso no app

| # | Ação | Endpoint |
|---|------|----------|
| 1 | Cadastrar o primeiro funcionário (livre) | `POST /api/auth/registrar` `{"email","senha"}` |
| 2 | Entrar → recebe `token` | `POST /api/auth/login` |
| 3 | Mandar o token em toda chamada | header `Authorization: Bearer <token>` |
| 4 | Cadastrar produtos | `POST /api/cardapio` `{"produto":"Dogão Completo","precoManha":15.00,"precoNoite":18.50}` |
| 5 | Abrir o caixa | `POST /api/caixa/abrir` `{"valorAbertura":100.00}` |
| 6 | Abrir comanda | `POST /api/comandas` `{"numeroComanda":"7","mesa":"3"}` |
| 7 | Lançar pedido | `POST /api/comandas/{id}/pedidos` `{"observacao":"...","itens":[{"codCar":1,"quantidade":2,"variacao":"sem cebola"}]}` |
| 8 | Receber | `POST /api/comandas/{id}/pagamentos` `{"formaPagamento":"PIX"}` |
| 9 | Painel | `GET /api/dashboard` |
| 10 | Fechar o caixa | `POST /api/caixa/fechar` |

Outros: `GET /api/comandas?status=ABERTA|FECHADA`, `GET /api/comandas/{id}`, `DELETE /api/comandas/{id}` (cancelar), `DELETE /api/comandas/{id}/pedidos/{codPed}`, `DELETE /api/comandas/{id}/pedidos/{codPed}/itens/{codPedFei}`, `POST /api/comandas/{id}/pedidos/{codPed}/pagamento` (dividir a conta), `POST /api/caixa/movimentacoes` `{"tipo":"SAIDA","valor":20}`, `GET /api/caixa/atual`, `GET /api/caixa`, `GET /api/dashboard/historico`, `GET /api/auth/me`.

Formas de pagamento: `DINHEIRO`, `PIX`, `CARTAO_CREDITO`, `CARTAO_DEBITO`.

## 5. Regras do restaurante

**Login**
- O primeiro funcionário se cadastra sozinho; depois disso só quem está logado cadastra outro.
- E-mail único; senha guardada com BCrypt; token vale 12 horas.

**Cardápio e preço**
- Cada produto tem preço da **manhã** e, opcionalmente, da **noite** (sem preço da noite, vale o da manhã).
- Noite = das 18:00 às 05:00 (ajuste em `application.yml`).
- O turno é o do **horário de abertura da comanda**: comanda aberta às 17:50 paga preço da manhã até o fim.
- Nome de produto não se repete. Produto que já foi vendido não pode ser excluído.

**Comanda e pedidos**
- Não existem duas comandas **abertas** com o mesmo número.
- Comanda **fechada** = tem pedidos e todos foram pagos. Ela não recebe pedidos novos.
- O valor total é sempre calculado pela API, nunca enviado pelo app.
- Pedido pago não pode ser alterado nem removido; comanda com algum pagamento não pode ser cancelada.
- Cada unidade vira uma linha em `Pedido_Feito` (a tabela não tem coluna de quantidade).

**Pagamento e caixa**
- Só se recebe com **caixa aberto**, e só existe um caixa aberto por vez.
- Cada pedido é pago uma vez só. Dá para pagar tudo de uma vez ou pedido a pedido (parte no PIX, parte no dinheiro).
- Todo pagamento vira uma entrada em `Movimentacao_Caixa`.
- Saldo = abertura + entradas − saídas. Não dá para tirar mais do que o saldo.
- O caixa não fecha com pedido sem pagamento. No fechamento: `Fechamento_Caixa` = saldo final, `Relações_de_Pagamento_Final_da_Manha_e_Noite` = "Manhã: R$ … | Noite: R$ … | Saídas: R$ …", e uma linha é gravada em `DashBoard`.

**Erros** chegam sempre assim (fácil de mostrar no app):

```json
{ "status": 422, "title": "Regra do restaurante", "detail": "Nenhum caixa aberto. Abra o caixa antes de continuar." }
```

`400` dados inválidos (com `campos`), `401` sem login / token vencido, `403` sem permissão, `404` não encontrado, `409` conflito, `422` regra do restaurante.

## Estrutura

```
src/main/java/br/com/marciodogao/api
├── domain/       entidades mapeadas nas tabelas do banco
├── repository/   consultas (Spring Data JPA)
├── service/      REGRAS DO RESTAURANTE
├── controller/   endpoints REST
├── dto/          JSON de entrada e saída
├── config/       segurança (JWT), CORS, relógio
└── exception/    tratamento de erros
```
