import java.io.Serializable;

public class Evento implements Serializable{
    public int timestamp;
    public int portaProcesso;
    public String operacao;
    public boolean repassar;

    public Evento(int timestamp, int portaProcesso, String operacao, boolean repassar) {
        this.timestamp = timestamp;
        this.portaProcesso = portaProcesso;
        this.operacao = operacao;
        this.repassar = repassar;
    }
}
