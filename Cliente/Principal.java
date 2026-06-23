/***************************************************************** 
* Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 20/06/2026
* Ultima alteracao.: 28/06/2026
* Nome.............: Principal
* Funcao...........: Executa o Cliente
*************************************************************** */

import javax.swing.JOptionPane;
import Network.Descobridor;
import Network.RecebedorUDP;
import Protocol.APDU;
import Network.Mensagens;

public class Principal {
  /********************************************************************
   * Metodo: main
   * Funcao: inicia a execucao do programa
   * @param void
   * @return void
  * ****************************************************************** */
 public static void main(String[] args) {

  System.out.println("====================================");
  System.out.println("   BEM-VINDO AO CHAT");
  System.out.println("====================================");

  String ipServidor = Descobridor.buscarIPServidor();

  if(ipServidor == null) {
    System.out.println("Falha ao encontrar o servidor automaticamente.");
    ipServidor = JOptionPane.showInputDialog(null, 
        "Falha ao encontrar o servidor automaticamente.\nDigite o IP do servidor manualmente (ou 'localhost'):", 
        "IP do Servidor", 
        JOptionPane.QUESTION_MESSAGE);
    
    if (ipServidor != null) ipServidor = ipServidor.trim();
    if (ipServidor == null || ipServidor.isEmpty()) {
      System.out.println("IP nao informado. Encerrando...");
      System.exit(0);
    }//fim do if
  } else {
    System.out.println("Servidor encontrado automaticamente no IP: " + ipServidor);
  }//fim do if-else
  
  int portaUDPCliente = 5000 + (int)(Math.random() * 1000);
  RecebedorUDP recebedor = new RecebedorUDP(portaUDPCliente, ipServidor, 7777);
  recebedor.start();
  
  Cliente cliente = new Cliente(ipServidor, 6789);

  String nomeUsuarioUI = JOptionPane.showInputDialog(null, 
      "Digite seu nome de usuario para o chat:", 
      "Nome de Usuário", 
      JOptionPane.QUESTION_MESSAGE);
      
  if (nomeUsuarioUI != null) nomeUsuarioUI = nomeUsuarioUI.trim();
  if (nomeUsuarioUI == null || nomeUsuarioUI.isEmpty()) {
      System.out.println("Nome de usuario nao informado. Encerrando...");
      System.exit(0);
  }//fim do if

  final String nomeUsuarioFinal = nomeUsuarioUI;

  java.awt.EventQueue.invokeLater(() -> {
      Interface.ChatUI gui = new Interface.ChatUI(cliente, recebedor, nomeUsuarioFinal, portaUDPCliente);
      gui.setVisible(true);
  });
 }//fim do metodo
}//fim da classe
