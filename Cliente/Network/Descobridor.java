/***************************************************************** 
* Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 20/06/2026
* Ultima alteracao.: 28/06/2026
* Nome.............: Descobridor
* Funcao...........: Descobrir o IP do servidor
*************************************************************** */

package Cliente.Network;

import java.net.*;

public class Descobridor {
  /********************************************************************
  * Metodo: buscarIPServidor
  * Funcao: Gritar para o servidor ouvir
  * @param void
  * @return o ip descoberto
  * ****************************************************************** */
 public static String buscarIPServidor() {
  try(DatagramSocket socket = new DatagramSocket()) {
    socket.setBroadcast(true);//autoriza broadcast

    byte[] pedido = "SERVIDOR_IP".getBytes();

    DatagramPacket pacoteGrito = new DatagramPacket(pedido, pedido.length, InetAddress.getByName("255.255.255.255"),8888);
    System.out.println("Procurando servidor na rede local");
    socket.send(pacoteGrito);
    
    socket.setSoTimeout(5000);
    byte[] buffer = new byte[256];
    DatagramPacket pacoteResposta = new DatagramPacket(buffer, buffer.length);
    
    socket.receive(pacoteResposta);
    String resposta = new String(pacoteResposta.getData(), 0, pacoteResposta.getLength());

    if(resposta.equals("IP")) {
      return pacoteResposta.getAddress().getHostAddress();
    }//fim do if

  } catch(SocketTimeoutException e) {
    System.err.println("Tempo Esgotado! Nenhum servidor respondeu");
  } catch(Exception e) {
    System.err.println("Erro ao tentar descobrir o servidor: " + e.getMessage());
  }//fim do try-catch
  return null;
 }//fim do metodo
}//fim da classe
