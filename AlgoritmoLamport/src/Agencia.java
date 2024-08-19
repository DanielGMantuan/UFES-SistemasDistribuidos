import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class Agencia implements Runnable {
    private Conta conta;
    private int timestamp;
    private List<Evento> eventos;
    private int porta;
    public boolean running = true;
    private ServerSocket serverSocket;

    public Agencia(Conta conta, int porta) {
        this.conta = conta;
        this.timestamp = -1;
        this.eventos = new ArrayList<>();
        this.porta = porta;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(this.porta);
            System.out.println("Iniciando servidor na porta " + this.porta);
            while (running) {
                Socket socket = serverSocket.accept();
                new Thread(() -> {
                    try {
                        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                        Evento evento = (Evento) in.readObject();
                        processarEvento(evento);
                    } catch (Exception e) {
                        System.out.println("Erro: " + e.getMessage());
                        e.printStackTrace();
                    } finally {
                        try {
                            socket.close();
                        } catch (Exception e) {
                            System.out.println("Erro ao fechar o socket: " + e.getMessage());
                        }
                    }
                }).start();
            }
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (Exception e) {
                    System.out.println("Erro ao fechar o ServerSocket: " + e.getMessage());
                }
            }
        }
    }

    private synchronized void processarEvento(Evento evento) throws Exception {
        if (evento.repassar) {
            // System.out.println(this.porta + " - Repassando evento: " + evento.operacao);
            if(this.timestamp == -1){
                this.timestamp = evento.timestamp;
            }
            fazerEventos(new int[]{50000, 50001, 50002}, evento.operacao);            
            if(evento.operacao.equals("END")) {
                processar();
            }
        } else {
            if(evento.operacao.equals("END")) {
                processar();
                return;
            }
            synchronized (this) {
                atulizarTimestamp(evento);
                eventos.add(evento);
                eventos.sort(Comparator.comparingInt(e -> e.timestamp));
            }
            if (!evento.operacao.equals("CONFIRMACAO")) {
                // System.out.println(this.porta + " - Recebido evento: " + evento.operacao);
                // Enviar confirmação se necessário
                // Evento confirmacao = new Evento(this.timestamp, this.porta, "CONFIRMACAO", false);
                // outStream.writeObject(confirmacao);
                // outStream.flush();
            }
        }
    }

    public synchronized void processar() {
        
        for (Evento evento : eventos) {
            System.out.println("relatorio Agencia: " + this.porta + "\t - Evento: " + evento.operacao);
            switch (evento.operacao) {
                case "DEPOSITAR":
                    depositar();
                    break;
                case "JUROS":
                    aplicarJuros();
                    break;
            }
        }
        
        System.out.println("Agencia: " + this.porta + " - Saldo: " + this.conta.saldo);
    }

    private synchronized void atulizarTimestamp(Evento evento) {
        this.timestamp = Math.max(this.timestamp, evento.timestamp) + 1;
    }

    private void depositar() {
        this.conta.saldo += 100;
    }

    private void aplicarJuros() {
        this.conta.saldo *= 1.05f;
    }

    public synchronized void fazerEventos(int[] portas, String operacao) {
        Evento novoEvento = new Evento(this.timestamp, this.porta, operacao, false);
        eventos.add(novoEvento);

        for (int porta : portas) {
            if (porta != this.porta) {
                new Thread(() -> {
                    try (Socket socket = new Socket("localhost", porta);
                         ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

                        out.writeObject(novoEvento);
                    } catch (Exception e) {
                        System.out.println("Erro ao enviar evento para porta " + porta + ": " + e.getMessage());
                    }
                }).start();
            }
        }
    }
}
