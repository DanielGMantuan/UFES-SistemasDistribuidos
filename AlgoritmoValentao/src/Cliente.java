import java.io.PrintStream;
import java.net.Socket;

public class Cliente {

    public static void main(String[] args) throws Exception {
        int porta = 40000;
        try( Socket s = new Socket("localhost", porta)){
            PrintStream prt = new PrintStream(s.getOutputStream());
            prt.println("processar");
        }
        Thread.sleep(1000);
        try( Socket s = new Socket("localhost", porta)){
            PrintStream prt = new PrintStream(s.getOutputStream());
            prt.println("desativar");
        }
    }
}