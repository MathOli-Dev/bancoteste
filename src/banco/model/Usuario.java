package banco.model;

/** Guarda apenas o HASH da senha, nunca a senha em texto puro. */
public class Usuario {
    private final String cpf;
    private final String nome;
    private final String senhaHash;
    private int tentativasFalhas = 0;
    private boolean bloqueado = false;

    public Usuario(String cpf, String nome, String senhaHash) {
        this.cpf = cpf;
        this.nome = nome;
        this.senhaHash = senhaHash;
    }

    public String getCpf() { return cpf; }
    public String getNome() { return nome; }
    public String getSenhaHash() { return senhaHash; }
    public synchronized boolean isBloqueado() { return bloqueado; }

    public synchronized void registrarFalha(int maximo) {
        tentativasFalhas++;
        if (tentativasFalhas >= maximo) bloqueado = true;
    }

    public synchronized void zerarFalhas() { tentativasFalhas = 0; }
}
