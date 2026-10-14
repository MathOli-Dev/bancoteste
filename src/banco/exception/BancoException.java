package banco.exception;

/** Erro de regra de negócio (saldo insuficiente, senha inválida, etc). */
public class BancoException extends RuntimeException {
    public BancoException(String mensagem) {
        super(mensagem);
    }
}
