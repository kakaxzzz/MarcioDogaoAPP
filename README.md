# Mdogão · App de atendimento para garçom

Sistema de atendimento do restaurante Márcio Dogão: o garçom lança pedidos por mesa, recebe pagamentos e a gerência acompanha o caixa e as vendas.

## O que tem aqui

| Pasta | O que é |
|---|---|
| [`MarcioDogaoAPI/`](MarcioDogaoAPI/) | API REST em **Java 21 + Spring Boot** com as regras do restaurante, e o **app web** (telas Pedido, Mesas e Dashboard) servido pela própria API |
| [`MarcioDogaoAPP/`](MarcioDogaoAPP/) | Projeto do banco de dados **SQL Server** |
| [`MarcioDogaoAPI/database/`](MarcioDogaoAPI/database/) | Scripts de ajuste do banco usados pela API |

## Funcionalidades

- **Pedido:** escolher a mesa, cardápio do dia e da noite (preços diferentes por turno), observação por item.
- **Mesas:** mesas livres e em atendimento, itens da comanda, recebimento em dinheiro, Pix, débito ou crédito.
- **Dashboard:** faturamento, ticket médio, vendas por turno, produtos mais vendidos e formas de pagamento.
- **Caixa:** abertura, sangria e suprimento, fechamento com resumo do dia e da noite.
- **Login** de funcionários com senha criptografada (BCrypt) e token JWT.

## Ver funcionando em 1 minuto (modo demonstração)

Não precisa do SQL Server: usa um banco em memória com dados de exemplo. Requer o JDK 21.

```powershell
cd MarcioDogaoAPI
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo"
```

Abra `http://localhost:8080/` e clique em **Entrar na demonstração**.

A documentação da API (Swagger) fica em `http://localhost:8080/swagger-ui.html`.

## Rodar com o SQL Server

O passo a passo (banco, TCP/IP, configuração) e todas as regras do restaurante estão no [README da API](MarcioDogaoAPI/README.md).

## Tecnologias

Java 21 · Spring Boot 3.5 · Spring Security (JWT) · Spring Data JPA · SQL Server · HTML, CSS e JavaScript puros no app · JUnit 5
