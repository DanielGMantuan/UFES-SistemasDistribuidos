import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClienteAgencia {
    public static void main(String[] args) throws Exception{
        int porta = 50002;
        try(Socket soc = new Socket("localhost", porta)){
            ObjectOutputStream out = new ObjectOutputStream(soc.getOutputStream());
                out.writeObject(new Evento(0, porta, "JUROS", true));
                out.flush();    
        }
        int porta2 = 50001;
        try(Socket socet = new Socket("localhost", porta2)){
            ObjectOutputStream out = new ObjectOutputStream(socet.getOutputStream());
                out.writeObject(new Evento(1, porta2, "DEPOSITAR", true));
                out.flush();
        }
        
        Thread.sleep(30000);
        try(Socket socet = new Socket("localhost", porta2)){
            ObjectOutputStream out = new ObjectOutputStream(socet.getOutputStream());
            out.writeObject(new Evento(0, porta2, "END", true));
            out.flush();
        }
    }
}
