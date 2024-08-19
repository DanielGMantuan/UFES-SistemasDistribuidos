import java.util.ArrayList;
import java.util.Arrays;

public class ExclusaoDescentralizada {
    public static void main(String[] args) throws Exception {
        ProcessoDescentralizado p0 = new ProcessoDescentralizado(50000, 0);
        ProcessoDescentralizado p1 = new ProcessoDescentralizado(50001, 1);
        ProcessoDescentralizado p2 = new ProcessoDescentralizado(50002, 2);

        p0.processos = new ArrayList<ProcessoDescentralizado>(Arrays.asList(p0, p1, p2));
        p1.processos = new ArrayList<ProcessoDescentralizado>(Arrays.asList(p0, p1, p2));
        p2.processos = new ArrayList<ProcessoDescentralizado>(Arrays.asList(p0, p1, p2));
        
        Thread tp0 = new Thread(p0);
        Thread tp1 = new Thread(p1);
        Thread tp2 = new Thread(p2);

        tp0.start();
        tp1.start();
        tp2.start();

        Thread.sleep(2000);
        
        p2.mensagemBroadcast("critica");
        Thread.sleep(200);
        p0.mensagemBroadcast("critica");

        Thread.sleep(2000);

        p1.mensagemBroadcast("critica");
    }
}
