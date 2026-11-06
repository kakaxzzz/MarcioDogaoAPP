/*
  Ajustes no banco MarcioDogaoAPP para a API REST (Spring Boot).
  Rode uma única vez, com as tabelas ainda vazias:

    sqlcmd -S localhost -E -C -f 65001 -i database\001_ajustes_para_api.sql

  O que muda:
    1. Valores em dinheiro passam a ter centavos (DECIMAL(10,2)).
       Antes: DECIMAL(18,0) não guardava R$ 12,50 e Valor_Pedido DECIMAL(4,0) ia só até 9999.
    2. Login.Senha passa para VARCHAR(100), para guardar o hash BCrypt (60 caracteres).
    3. Login.Email passa a ser único.
    4. Preco ganha CodCar (FK para Cardapio): cada produto tem o preço da manhã e da noite.
    5. Pagamento ganha Valor_Pago e CodCaix (FK para Caixa), para saber quanto foi pago
       e em qual caixa o dinheiro entrou.
*/
USE MarcioDogaoAPP;
SET XACT_ABORT ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;
BEGIN TRANSACTION;

-- 1. Dinheiro com centavos
ALTER TABLE Caixa              ALTER COLUMN Abertura_Caixa   DECIMAL(10,2) NOT NULL;
ALTER TABLE Caixa              ALTER COLUMN Fechamento_Caixa DECIMAL(10,2) NULL;
ALTER TABLE Comanda            ALTER COLUMN Valor_Pedido     DECIMAL(10,2) NULL;
ALTER TABLE DashBoard          ALTER COLUMN Relatorio_Caixa  DECIMAL(10,2) NULL;
ALTER TABLE Movimentacao_Caixa ALTER COLUMN Valores_Entrada  DECIMAL(10,2) NOT NULL;
ALTER TABLE Movimentacao_Caixa ALTER COLUMN Valores_Saida    DECIMAL(10,2) NULL;
ALTER TABLE Preco              ALTER COLUMN Manha            DECIMAL(10,2) NOT NULL;
ALTER TABLE Preco              ALTER COLUMN Noturno          DECIMAL(10,2) NULL;

-- 2 e 3. Login
ALTER TABLE Login ALTER COLUMN Senha VARCHAR(100) NULL;
ALTER TABLE Login ADD CONSTRAINT UQ_Login_Email UNIQUE (Email);

-- 4. Preço ligado ao produto do cardápio
ALTER TABLE Preco ADD CodCar INT NULL;
GO
ALTER TABLE Preco ADD CONSTRAINT FK_Preco_Cardapio FOREIGN KEY (CodCar) REFERENCES Cardapio(CodCar);
CREATE UNIQUE INDEX UQ_Preco_CodCar ON Preco(CodCar) WHERE CodCar IS NOT NULL;

-- 5. Pagamento com valor e caixa
ALTER TABLE Pagamento ADD Valor_Pago DECIMAL(10,2) NULL;
ALTER TABLE Pagamento ADD CodCaix INT NULL;
GO
ALTER TABLE Pagamento ADD CONSTRAINT FK_Pagamento_Caixa FOREIGN KEY (CodCaix) REFERENCES Caixa(CodCaix);

IF @@TRANCOUNT > 0 COMMIT TRANSACTION;
GO
PRINT 'Ajustes aplicados com sucesso.';
