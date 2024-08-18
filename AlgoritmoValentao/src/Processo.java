import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.*;

import java.io.*;

public class Processo implements Runnable {
    public int pId;
    public int porta;
    public Processo[] processos;
    ServerSocket serverSocket;
    public boolean running = true;
    public boolean active = true;
    public Processo coordenador;
    public boolean estaElegendo = false;
    
    public Processo(int pId, int porta) {
        this.pId = pId;
        this.porta = porta;
    }

    @Override
    public void run() {
        try{
            serverSocket = new ServerSocket(this.porta);
            System.out.println("Iniciando servidor na porta " + this.porta);
            while (running) {
                Socket socket = serverSocket.accept();
                if(active){
                    // O server aceita varias requisicoes simultaneas o que permite que ele processe e faca a eleicao
                    new Thread(() -> {
                        try{
                            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            String message = reader.readLine();

                            if(message.equals("terminei") || message.equals("ok")){
                                PrintStream outr = new PrintStream(socket.getOutputStream());
                                outr.println("ok");
                            }
                            else{
                                if(message.equals("eleicao")){
                                    // Participar da eleicao
                                    PrintStream outr = new PrintStream(socket.getOutputStream());
                                    outr.println("ok");
                                    iniciarEleicao();
                                }
                                else{
                                    if(message.equals("elegendo")){
                                        this.estaElegendo = true;
                                    }
                                    else{
                                        if(message.equals("processar")){
                                            todosProcessar();
                                            randomizeProcess();
                                            System.out.println("Processo " + this.pId + " terminou processamento");
                                            if(active && this.coordenador.pId != this.pId && !this.estaElegendo){
                                                notificarLider();
                                            }
                                        }
                                        else{
                                            if(message.equals("desativar")){
                                                this.active = false;
                                            }
                                            else{
                                                if(message.equals("TerminoEleicao")){
                                                    this.estaElegendo = false;
                                                }
                                                else{
                                                    try{
                                                       int novoOperador = Integer.parseInt(message);
                                                       System.out.println(this.pId + " - Coordernador settado para " + novoOperador);
                                                       this.coordenador = processos[novoOperador]; 
                                                    }
                                                    catch(NumberFormatException e){
                                                        System.out.println("mesagem nao entrou em nada :" + message);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        catch (Exception e) {
                            System.out.println("Erro na thread: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }).start();
                }
            }
            serverSocket.close();
        }
        catch (Exception exception) {
            System.out.println("Error running server: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    public void notificarLider() {
        //Notificar o Lider
        try{
            var leader = this.coordenador;
            Socket soc = new Socket("localhost", coordenador.porta);
            PrintStream outr = new PrintStream(soc.getOutputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            
            outr.println("terminei");

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<String> future = executor.submit(reader::readLine);
            try {
                // Aguarda a resposta por até 5 segundos
                String response = future.get(5, TimeUnit.SECONDS);
                if ("ok".equals(response)) {
                    System.out.println("O lider respondeu: " + response);
                } else {
                    System.out.println("O lider respondeu: " + response);
                }
            } catch (TimeoutException e) {
                if(!estaElegendo && leader.pId == this.coordenador.pId) {
                    System.out.println(this.pId + " - Lider " + leader.pId + " nao respondeu");
                    notificarInicioEleicao();
                    iniciarEleicao();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                executor.shutdownNow(); // Fecha o executor
                soc.close();
            }
        }
        catch(Exception exception){
            System.out.println("Error notificar leader: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    public void notificarInicioEleicao(){
        //Iniciar eleicao
        this.estaElegendo = true;
        System.out.println(this.pId + " - Notificando inicio eleicao ");
        for (Processo processo : processos) {
            if(processo.pId != this.pId ){
                try{
                    Socket soc = new Socket("localhost", processo.porta);
                    PrintStream outr = new PrintStream(soc.getOutputStream());
                    outr.println("elegendo");
                    soc.close();
                }
                catch (Exception exception){
                    System.out.println("Error ao notificar inicio eleicao: " + exception.getMessage());
                    exception.printStackTrace();
                }
           }
        }
    }

    public void notificarTerminoEleicao(){
        //Iniciar eleicao
        this.estaElegendo = false;
        System.out.println(this.pId + " - Notificando termino eleicao ");
        for (Processo processo : processos) {
            if(processo.pId != this.pId ){
                try{
                    Socket soc = new Socket("localhost", processo.porta);
                    PrintStream outr = new PrintStream(soc.getOutputStream());
                    outr.println("TerminoEleicao");
                    soc.close();
                }
                catch (Exception exception){
                    System.out.println("Error ao notificar inicio eleicao: " + exception.getMessage());
                    exception.printStackTrace();
                }
           }
        }
    }

    public void iniciarEleicao(){
        //Iniciar eleicao
        System.out.println("Iniciando eleição para o processo: " + this.pId);
        boolean flag = true;
        for (Processo processo : processos) {
            if(processo.pId > this.pId ){
                try{
                    Socket soc = new Socket("localhost", processo.porta);
                    PrintStream outr = new PrintStream(soc.getOutputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(soc.getInputStream()));

                    outr.println("eleicao");

                    System.out.println( this.pId  + " eleicao -> " + processo.pId);
                    
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Future<String> future = executor.submit(reader::readLine);
                    try {
                        // Aguarda a resposta por até 5 segundos
                        String response = future.get(5, TimeUnit.SECONDS);
                        if ("ok".equals(response)) {
                            System.out.println("Resposta recebida: " + response);
                        } else {
                            System.out.println("Resposta inesperada: " + response);
                        }
                        flag = false;
                        break;
                    } catch (TimeoutException e) {
                        System.out.println("Nao respondeu o processo: " + processo.pId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        executor.shutdownNow(); // Fecha o executor
                        soc.close();
                    }
                }
                catch (Exception exception){
                    System.out.println("Error start eleicao: " + exception.getMessage());
                    exception.printStackTrace();
                }
           }
        }
        if(flag){
            this.coordenador = this;
            notificarNovoLider();
            notificarTerminoEleicao();
        }
    }

    public void todosProcessar(){
        //Iniciar o processamento em todos os outros processos
        for (Processo processo : processos) {
            if(coordenador.pId == this.pId && processo.pId != this.pId ){
                try{
                    Socket soc = new Socket("localhost", processo.porta);
                    PrintStream outr = new PrintStream(soc.getOutputStream());
                    outr.println("processar");
                    soc.close();
                }
                catch (Exception exception){
                    System.out.println(processo.pId + " - Nao foi possivel processar : " + exception.getMessage());
                    exception.printStackTrace();
                }
            }
        }
    }

    public void notificarNovoLider(){
        //Notificar o novo Lider
        for (Processo processo : processos) {
            if(processo.pId != this.pId ){
                System.out.println(this.pId  + " notificar -> " + processo.pId);
                if(processo.pId != this.pId ){
                    try{
                        Socket soc = new Socket("localhost", processo.porta);
                        PrintStream outr = new PrintStream(soc.getOutputStream());
                        outr.println(String.valueOf(this.pId));
                        soc.close();
                    }
                    catch (Exception exception){
                        System.out.println("Error start eleicao: " + exception.getMessage());
                        exception.printStackTrace();
                    }
                }
            }
        }
    }

    private synchronized void randomizeProcess() throws InterruptedException{
        System.out.println("Processo " + this.pId + " processando");
        Random random = new Random();
        // A thread fica em sleep por um tempo aleatório entre 8 e 18 segundos
        if(this.coordenador.pId == this.pId){
            Thread.sleep(5000);
        }
        else{
            Thread.sleep((random.nextInt(10) + 8) * 1000 );
        }
    }
}
