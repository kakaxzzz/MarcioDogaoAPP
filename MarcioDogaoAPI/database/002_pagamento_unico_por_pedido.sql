/*
  Garante no banco que um pedido nunca é pago duas vezes
  (ex.: dois celulares pagando a mesma comanda ao mesmo tempo).

    sqlcmd -S localhost -E -C -f 65001 -i database\002_pagamento_unico_por_pedido.sql
*/
USE MarcioDogaoAPP;
SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;
GO
CREATE UNIQUE INDEX UQ_Pagamento_CodPed ON Pagamento(CodPed) WHERE CodPed IS NOT NULL;
GO
