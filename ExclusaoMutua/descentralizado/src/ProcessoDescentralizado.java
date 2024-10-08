import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class ProcessoDescentralizado implements Runnable {
    public List<ProcessoDescentralizado> processos;
    public List<Mensagem> regiaoCriticaQueue;
    public int porta;
    public int pId;
    public boolean estaRegiaoCritica = false;
    public boolean queroEntrarNaRegiaoCritica = false;
    public int liberado = 0;


    public ProcessoDescentralizado(int porta, int id) {
        this.regiaoCriticaQueue = new ArrayList<Mensagem>();
        this.porta = porta;
        this.pId = id;
    }
    //TODO: fica melhor transformar todas as entradas em stirng e so serializar e deserializar quando precisar
    @Override
    public void run() {
        try{
            System.out.println("Iniciando servidor na porta " + this.porta);
            ServerSocket serverSocket = new ServerSocket(this.porta);
            while (true) {
                Socket s = serverSocket.accept();
                new Thread(() -> {
                    try{
                        try{
                            ObjectInputStream in = new ObjectInputStream(s.getInputStream());
                            PrintStream outr = new PrintStream(s.getOutputStream());
                            Mensagem m = (Mensagem) in.readObject();
                            if(m.nomeRegiao.equals("critica")){
                                if(!this.estaRegiaoCritica){
                                    if(this.queroEntrarNaRegiaoCritica){
                                        // Se a operacao e critica verifica qual tem prioridade pelo timestamp
                                        this.regiaoCriticaQueue.add(m);
                                        this.regiaoCriticaQueue.sort(Comparator.comparing(me -> me.timestamp.getTime()));

                                        // Se o processo for o primeiro da fila
                                        if(this.regiaoCriticaQueue.get(0).idProcesso == this.pId){
                                            if(liberado == processos.size()-1){
                                                processar();
                                            }
                                        }
                                        else{
                                            outr.println("OK");
                                        }
                                    }
                                    else{
                                        // Retornar OK, se nao quiser participar da regiao critica
                                        outr.println("OK");
                                    }
                                }
                                else{
                                    //Esta na regiao cricita entao so enfileira
                                    this.regiaoCriticaQueue.add(m);
                                    this.regiaoCriticaQueue.sort(Comparator.comparing(me -> me.timestamp.getTime()));
                                }
                            }
                        }
                        catch(Exception e){
                            // System.err.println();
                            // this.regiaoCriticaQueue.remove(0);
                            // processar();
                            liberado++;
                            if(liberado == processos.size()-1){
                                processar();
                            }

                        }
                    }
                    catch (Exception e) {
                        System.out.println("Erro na thread do servidor: " + e.getMessage());
                        e.printStackTrace();
                    }
                }).start();
            }
        }
        catch (Exception e) {
            System.out.println("Erro ao iniciar o servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void mensagemBroadcast(String nomeRegiao){
        Mensagem mes = new Mensagem(nomeRegiao, this.pId);
        this.regiaoCriticaQueue.add(mes);
        this.queroEntrarNaRegiaoCritica = true;

        for (ProcessoDescentralizado processo : processos) {
            if(processo.pId != this.pId){
                new Thread(() -> {
                    try{
                        Socket soc = new Socket("localhost", processo.porta);
                        ObjectOutputStream out = new ObjectOutputStream(soc.getOutputStream());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(soc.getInputStream()));
                        out.writeObject(mes);

                        // Reader
                        String response = reader.readLine();

                        if (response.equals("OK")) {
                            System.out.println(processo.pId + " liberou " + this.pId + " para uso da area critica");
                        }
                        liberado++;

                        soc.close();
                    }
                    catch (Exception e) {
                        System.out.println("Erro ao enviar mensagem de broadcast: " + e.getMessage());
                        e.printStackTrace();
                    }
                }).start();
            }
        }
    }

    public void processar() throws InterruptedException{
        liberado = 0;
        this.regiaoCriticaQueue.remove(0);
        this.estaRegiaoCritica = true;
        this.queroEntrarNaRegiaoCritica = false;
        Random random = new Random();

        System.out.println("Processo " + this.pId + " processando na regiao critica");
        Thread.sleep((random.nextInt(10) + 2) * 1000);
        System.out.println("Processo " + this.pId + " terminou");
        this.estaRegiaoCritica = false;

        for (Mensagem mes : regiaoCriticaQueue) {            
            new Thread(() -> {
                try{
    
                        System.out.println("Processo " + this.pId + " notificando "+ mes.idProcesso);
                        Socket soc = new Socket("localhost", processos.get(mes.idProcesso).porta);
                        PrintStream outr = new PrintStream(soc.getOutputStream());
                        outr.println("OK");
                        soc.close();
                    }
                        catch (Exception e) {
                            System.out.println("Erro ao notificar o proximo da fila: " + e.getMessage());
                            e.printStackTrace();
                        }
            }).start();
        }
        regiaoCriticaQueue.clear();
    }
}