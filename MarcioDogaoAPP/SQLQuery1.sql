use MarcioDogaoAPP;

ALTER TABLE Pedido_Feito
ADD CodPed INT NULL;

ALTER TABLE Pedido_Feito
ADD CONSTRAINT FK_Pedido_Feito_Pedido
FOREIGN KEY (CodPed) REFERENCES Pedido(CodPed);