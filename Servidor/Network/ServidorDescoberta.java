/***************************************************************** 
* Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 20/06/2026
* Ultima alteracao.: 28/06/2026
* Nome.............: ServidorDescoberta.java
* Funcao...........: Fica escutando a porta 8888 para descobrirmos o servidor automaticamente
*************************************************************** */

package Network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

//Thread que fica escutando gritos no broadcast na rede local
public class ServidorDescoberta extends Thread{
  private final int portaUDP = 8888;
  /********************************************************************
   * Metodo: run
   * Funcao: rodar a thread da descoberta de servidor
   * @param void
   * @return void
  * ****************************************************************** */
  @Override
  public void run() {
    //Socket sem conexao do UDP
    try(DatagramSocket socket = new DatagramSocket(portaUDP)) {
      System.out.println("[DISCOVERY] Servidor pronto para ser descoberto na porta UDP " + portaUDP);
      
      byte[] buffer = new byte[256];

      while(true) {
        //Prepara o pacote vazio para receber o grito do cliente
        DatagramPacket pacoteRecebido = new DatagramPacket(buffer, buffer.length);
        socket.receive(pacoteRecebido);

        //Le o que o cliente mandou
        String mensagem = new String(pacoteRecebido.getData(), 0, pacoteRecebido.getLength());

        if(mensagem.equals("SERVIDOR_IP")) {
          System.out.println("[DISCOVERY] Grito Recebido de: " + pacoteRecebido.getAddress().getHostAddress());
          byte[] resposta = "IP".getBytes();
          DatagramPacket pacoteEnvio = new DatagramPacket(
            resposta,
            resposta.length,
            pacoteRecebido.getAddress(),
            pacoteRecebido.getPort()
          );
          socket.send(pacoteEnvio);
        }//fim do if
      }//fim do while
    } catch(Exception e) {
      System.err.println("[DISCOVERY-ERRO] Falha na descoberta UDP: " + e.getMessage());
    }//fim do try-catch
  }//fim do metodo
}//fim da classe