/***************************************************************** 
* Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 21/06/2026
* Ultima alteracao.: 28/06/2026
* Nome.............: ServidorUDP
* Funcao...........: Estabelece servidor UDP para APDU de CONFIRM e SEND
*************************************************************** */

package Network;

import java.net.*;
import java.io.*;
import Model.Grupos;
import Model.Usuario;
import Protocol.APDU;


//Classe de servidor UDP para mandar o SEND, CONFIRM
public class ServidorUDP extends Thread {
  private final int portaUDP = 7777;
  private final Grupos gerenciador;

  /********************************************************************
  * Metodo: ServidorUDP
  * Funcao: Construtor do servidor UDP
  * @param gerenciador gerenciador de grupos
  * @return void
  * ****************************************************************** */
  public ServidorUDP(Grupos gerenciador) {
    this.gerenciador = gerenciador;
  }//fim do metodo
  /********************************************************************
   * Metodo: run
   * Funcao: rodar a thread do servidorUDP
   * @param void
   * @return void
  * ****************************************************************** */
 @Override
  public void run() {
    try(DatagramSocket socket = new DatagramSocket(portaUDP)) {
      System.out.println("[UDP] Servidor de Mensagem escutando na porta " + portaUDP + "...");
      byte[] buffer = new byte[8192];
      //buffer grande para caber o objeto
      while(true) {
        DatagramPacket pacoteRecebido = new DatagramPacket(buffer, buffer.length);
        socket.receive(pacoteRecebido);
        //Remontar a APDU
        ByteArrayInputStream bais = new ByteArrayInputStream(pacoteRecebido.getData());
        ObjectInputStream in = new ObjectInputStream(bais);
        APDU apdu = (APDU) in.readObject();
        if(apdu.getOperacao().equals("SEND")) {
          //validacao para nao mandar mensagens para grupos que nao faz parte
          var usuarios = gerenciador.getUsuariosDoGrupo(apdu.getNomeGrupo());
          boolean pertenceAoGrupo = false;
          if(usuarios != null) {
            for(Usuario u : usuarios) {
              if(u.getNome().equals(apdu.getNomeUsuario())) {
                pertenceAoGrupo = true;
                break;
              }//fim do if
            }//fim do for
          }//fim do if

          if(!pertenceAoGrupo) {
            System.out.println("[SEND-BLOQUEADO] '" + apdu.getNomeUsuario() + "' tentou enviar mensagem para grupo inexistente ou sem permissao: '" + apdu.getNomeGrupo() + "'");
            continue; // Pula a execução e ignora o pacote do invasor
          }//fim do if

          System.out.println("[SEND] " + apdu.getNomeUsuario() + "enviou mensagem para o grupo '" + apdu.getNomeGrupo() + "'");
          //envia o CONFIRM como 1
          APDU confirm = new APDU("CONFIRM", apdu.getIdMensagem(), 1, "Servidor", 
          apdu.getNomeGrupo(), apdu.getNomeUsuario());
          enviarObjeto(socket, confirm, pacoteRecebido.getAddress(), apdu.getPortaClienteUDP());
          //encaminha a mensagem para os membros do grupo
          if(usuarios != null) {
            for(Usuario u : usuarios) {
              if(!u.getNome().equals(apdu.getNomeUsuario())) {
                enviarObjeto(socket, apdu, u.getIp(), u.getPortaUDP());
              }//fim do if
            }//fim do for
          }//fim do if
        } else if(apdu.getOperacao().equals("CONFIRM")) {
          String donoDaMensagem = apdu.getDonoDaMensagem();
          var usuarios = gerenciador.getUsuariosDoGrupo(apdu.getNomeGrupo());
          if(usuarios != null) {
            for(Usuario u : usuarios) {
              if(u.getNome().equals(donoDaMensagem)) {
                enviarObjeto(socket, apdu, u.getIp(), u.getPortaUDP());
                break;
              }//fim do if
            }//fim do for
          }//fim do if
        }//fim do if-elseif
      }//fim do while
    } catch(Exception e) {
      System.err.println("[UDP-ERRO] Falha no servidor UDP: " + e.getMessage());
    }//fim do try-catch
  }//fim do metodo
  /********************************************************************
   * Metodo: enviarObjeto
   * Funcao: Funcao auxiliar para separar a APDU em bytes
   * @param socket Socket para mandar a mensagem
   * @param apdu Objeto da apdu
   * @param ipDestino O ip destino do send
   * @param portaDestino A porta destino do send
   * @return void
  * ****************************************************************** */
 private void enviarObjeto(DatagramSocket socket, APDU apdu, InetAddress ipDestino, int portaDestino) {
  try {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(baos);
    out.writeObject(apdu);
    out.flush();
    byte[] dados = baos.toByteArray();
    //Depois dos dados fracionados, enviamos
    DatagramPacket pacote = new DatagramPacket(dados, dados.length, ipDestino, portaDestino);
    socket.send(pacote);
  } catch(Exception e) {
    System.err.println("Erro ao encaminhar UDP: " + e.getMessage());
  }//fim do try-catch
 }//fim do metodo
}//fim da classe
