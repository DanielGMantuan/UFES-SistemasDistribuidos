import java.io.Serializable;
import java.sql.Date;

public class Mensagem implements Serializable {
    public String nomeRegiao;
    public int idProcesso;
    public Date timestamp;

    public Mensagem(String nomeRegiao, int idProcesso) {
        this.nomeRegiao = nomeRegiao;
        this.idProcesso = idProcesso;
        this.timestamp = new Date(System.currentTimeMillis());
    }
}
