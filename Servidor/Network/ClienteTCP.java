/***************************************************************** 
* Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 20/06/2026
* Ultima alteracao.: 28/06/2026
* Nome.............: ClienteTCP
* Funcao...........: Trata a conexao do cliente ao servidor
*************************************************************** */

package Network;

import java.net.*;
import java.io.*;
import Model.Usuario;
import Model.Grupos;
import Protocol.APDU;

//Classe ClienteTCP trata individualmente a requisicao TCP de um cliente utilizando threads
public class ClienteTCP extends Thread{
  private final Socket socket;
  private final Grupos gerenciador;
  /********************************************************************
   * Metodo: ClienteTCP
   * Funcao: Construtor do objeto clienteTCP
   * @param socket socket
   * @param gerenciador objeto grupos
   * @return void
  * ****************************************************************** */
  public ClienteTCP(Socket socket, Grupos gerenciador) {
    this.socket = socket;
    this.gerenciador = gerenciador;
  }//fim do metodo
  /********************************************************************
   * Metodo: run
   * Funcao: rodar a thread do clienteTCP
   * @param void
   * @return void
  * ****************************************************************** */
 @Override
 public void run() {
  try(
    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
  ) {
    //Le o objeto que o cliente enviou pela rede
    APDU apdu = (APDU) in.readObject();
    String operacao = apdu.getOperacao().toUpperCase();

    //Logica de roteamento da APDU
    if(operacao.equals("JOIN")) {
      Usuario novoUsuario = new Usuario(apdu.getNomeUsuario(), socket.getInetAddress(), apdu.getPortaClienteUDP());
      boolean sucesso = gerenciador.adicionarUsuarios(apdu.getNomeGrupo(), novoUsuario);
      if(sucesso) {
        System.out.println("[JOIN]" + apdu.getNomeUsuario() + "entrou no grupo '" + apdu.getNomeGrupo() + "'.");
        out.writeObject("OK: entrou no grupo com sucesso");
      } else {
        System.out.println("[JOIN-ERRO] " + apdu.getNomeUsuario() + "tentou entrar mas nome já existe no grupo.");
        out.writeObject("ERRO: Nome de usuario ja em uso neste grupo");
      }//fim do if-else
    }//fim do if
    else if(operacao.equals("LEAVE")) {
      boolean sucesso = gerenciador.removerUsuario(apdu.getNomeGrupo(), apdu.getNomeUsuario());
      if(sucesso) {
        System.out.println("[LEAVE] " + apdu.getNomeUsuario() + " saiu do grupo '" + apdu.getNomeGrupo() + "'.");
        out.writeObject("OK: Saiu do grupo com sucesso");
      } else {
        out.writeObject("ERRO: Usuario nao encontrado no grupo.");
      }//fim do if-else
    } else {
      System.out.println("[AVISO] Comando TCP desconhecido: " + operacao);
    }// fim do if-elseif-else

    out.flush();

  } catch(Exception e) {
    System.err.println("[TCP-ERRO] Falha ao processar requisicao do cliente: " + e.getMessage());
  } finally {
    try {
      socket.close();
    } catch (IOException e){
      e.printStackTrace();
    }//fim do try-catch
  }//fim do try-catch-finally
 }//fim do metodo
}//fim da classe