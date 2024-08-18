public class App {
    public static void main(String[] args) throws Exception {
        Processo p0 = new Processo(0, 40000);
        Processo p1 = new Processo(1, 40001);
        Processo p2 = new Processo(2, 40002);
        Processo p3 = new Processo(3, 40003);
        Processo p4 = new Processo(4, 40004);
        Processo p5 = new Processo(5, 40005);

        Processo[] processos = new Processo[]{p0, p1, p2, p3, p4, p5};
        
        //Todos tem conhecimento de todos
        for (Processo processo : processos) {
            processo.processos = processos;
            processo.coordenador = p0;    
        }

        Thread tp0 = new Thread(p0);
        Thread tp1 = new Thread(p1);
        Thread tp2 = new Thread(p2);
        Thread tp3 = new Thread(p3);
        Thread tp4 = new Thread(p4);
        Thread tp5 = new Thread(p5);


        tp0.start();
        tp1.start();
        tp2.start();
        tp3.start();
        tp4.start();
        tp5.start();

        tp0.join();
        tp1.join();
        tp2.join();
        tp3.join();
        tp4.join();
        tp5.join();
    }
}
