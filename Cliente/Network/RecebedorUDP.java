/***************************************************************** 
* Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 20/06/2026
* Ultima alteracao.: 28/06/2026
* Nome.............: RecebidorUDP
* Funcao...........: Ouve o servidor UDP para APDU de CONFIRM e SEND
*************************************************************** */

package Network;

import java.net.*;
import java.io.*;
import Protocol.APDU;

public class RecebedorUDP extends Thread{
  private final int minhaPorta;

  /********************************************************************
  * Metodo: RecebedorUDP
  * Funcao: Construtor do recebidor UDP
  * @param minhaPorta porta que esta escutando
  * @return void
  * ****************************************************************** */
 public RecebedorUDP(int minhaPorta) {
  this.minhaPorta = minhaPorta;
 }//fim do metodo
  /********************************************************************
   * Metodo: run
   * Funcao: rodar a thread do recebidorUDP
   * @param void
   * @return void
  * ****************************************************************** */
 public void run() {
  try(DatagramSocket socket = new DatagramSocket(minhaPorta)) {
    byte[] buffer = new byte[8192];

    while(true) {
      DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
      socket.receive(pacote);

      ByteArrayInputStream bais = new ByteArrayInputStream(pacote.getData());
      ObjectInputStream in = new ObjectInputStream(bais);
      APDU apdu = (APDU) in.readObject();
      
      //roteamento na interface do cliente
      if(apdu.getOperacao().equals("SEND")) {
        System.out.println("\n[" + apdu.getNomeGrupo() + "]" + apdu.getNomeUsuario() + " diz: " + apdu.getTextoMensagem());
        System.out.print("\n> "); //Repinta o cursor

      } else if(apdu.getOperacao().equals("CONFIRM")) {
        System.out.println("\n[Tick] Mensagem (" + apdu.getIdMensagem().substring(0,4) + "...) Status: " + 
        apdu.getStatusRecebido());
        System.out.print("\n> ");
      }//fim do if-elseif-else
    }//fim do while
  } catch(Exception e) {
    System.err.println("Erro no Recebidor UDP: " + e.getMessage());
  }//fim do try-catch
 }//fim do metodo
}//fim da classe
