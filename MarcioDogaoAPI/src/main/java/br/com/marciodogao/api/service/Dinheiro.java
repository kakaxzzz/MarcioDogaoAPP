package br.com.marciodogao.api.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

final class Dinheiro {

    private static final Locale PT_BR = Locale.of("pt", "BR");

    private Dinheiro() {
    }

    static BigDecimal duasCasas(BigDecimal valor) {
        return (valor == null ? BigDecimal.ZERO : valor).setScale(2, RoundingMode.HALF_UP);
    }

    /** 1234.5 -> "R$ 1.234,50" */
    static String formatar(BigDecimal valor) {
        return "R$ " + String.format(PT_BR, "%,.2f", duasCasas(valor));
    }
}
