package banco.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Registro imutável de uma movimentação (um "record" nunca muda depois de criado). */
public record Transacao(LocalDateTime data, Tipo tipo, BigDecimal valor, String descricao) {

    public enum Tipo {
        DEPOSITO, TRANSFERENCIA_ENVIADA, TRANSFERENCIA_RECEBIDA, PIX_ENVIADO, PIX_RECEBIDO
    }

    public boolean ehSaida() {
        return tipo == Tipo.TRANSFERENCIA_ENVIADA || tipo == Tipo.PIX_ENVIADO;
    }
}
