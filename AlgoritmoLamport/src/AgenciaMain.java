public class AgenciaMain {
    public static void main(String[] args) throws Exception {
        Conta conta1 = new Conta();
        Conta conta2 = new Conta();
        Conta conta3 = new Conta();

        Agencia agencia1 = new Agencia(conta1, 50000);
        Agencia agencia2 = new Agencia(conta2, 50001);
        Agencia agencia3 = new Agencia(conta3, 50002);

        Thread threadAg1 = new Thread(agencia1);
        Thread threadAg2 = new Thread(agencia2);
        Thread threadAg3 = new Thread(agencia3);
        
        threadAg1.start();
        Thread.sleep(500);
        threadAg2.start();
        Thread.sleep(500);
        threadAg3.start();


        // // agencia1.fazerEventos(new int[]{50001, 50002}, "DEPOSITAR");
        // // agencia2.fazerEventos(new int[]{50000, 50002}, "DEPOSITAR");
        // // agencia3.fazerEventos(new int[]{50000, 50001}, "DEPOSITAR");

        // threadAg1.join();
        // threadAg2.join();
        // threadAg3.join();

        // agencia1.processar();
        // agencia2.processar();
        // agencia3.processar();
    }
}
