/***************************************************************** 
* Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 20/06/2026
* Ultima alteracao.: 28/06/2026
* Nome.............: Principal
* Funcao...........: Executa o Cliente
*************************************************************** */

import java.util.Scanner;
import Network.Descobridor;
import Network.RecebedorUDP;
import Protocol.APDU;

public class Principal {
  /********************************************************************
   * Metodo: main
   * Funcao: inicia a execucao do programa
   * @param void
   * @return void
  * ****************************************************************** */
 public static void main(String[] args) {
  Scanner scanner = new Scanner(System.in);

  System.out.println("====================================");
  System.out.println("   BEM-VINDO AO TERMINAL DO CHAT");
  System.out.println("====================================");

  String ipServidor = Descobridor.buscarIPServidor();

  if(ipServidor == null) {
    System.out.println("Falha ao encontrar o servidor automaticamente.");
    System.out.print("Digite o IP do servidor manualmente (ou 'localhost'): ");
    ipServidor = scanner.nextLine().trim();
  } else {
    System.out.println("Servidor encontrado automaticamente no IP: " + ipServidor);
  }//fim do if-else
  
  int portaUDPCliente = 5000 + (int)(Math.random() * 1000);
  RecebedorUDP recebedor = new RecebedorUDP(portaUDPCliente);
  recebedor.start();
  
  Cliente cliente = new Cliente(ipServidor, 6789);
  System.out.println("\nComandos disponíveis:");
  System.out.println("  -entrar <nomeGrupo> <nomeUsuario>");
  System.out.println("  -sair <nomeGrupo> <nomeUsuario>");
  System.out.println(". -enviar <nomeGrupo> <nomeUsuario> <Texto>");
  System.out.println("  -fechar");

  while(true) {
    System.out.print("\n> ");
    String entrada = scanner.nextLine().trim();

    if (entrada.isEmpty()) continue;

    String[] partes = entrada.split("\\s+");
    String comando = partes[0].toLowerCase();

    if (comando.equals("-fechar")) {
        System.out.println("A encerrar o cliente...");
        break;
    }//fim do if

    if (partes.length < 3) {
        System.out.println("Formato incorreto. Utilize: -comando <grupo> <usuario>");
        continue;
    }//fim do if

    String grupo = partes[1];
    String usuario = partes[2];

    if(comando.equals("-entrar")) {
      APDU joinMsg = new APDU("JOIN", grupo, usuario, null, portaUDPCliente);
      cliente.enviarComandoTCP(joinMsg);
    } else if (comando.equals("-sair")) {
      APDU leaveMsg = new APDU("LEAVE", grupo, usuario, null, portaUDPCliente);
      cliente.enviarComandoTCP(leaveMsg);
    } else if (comando.equals("-enviar")) {
      if(partes.length < 4) {
        System.out.println("Formato incorreto, utilize: -comando <Grupo> <Usuario> <Mensagem>");
        continue;
      }//fim do if

      //Concatena tudo que o usuario escreveu para formar o texto
      StringBuilder texto = new StringBuilder();
      for(int i = 3; i < partes.length; i++) {
        texto.append(partes[i]).append(" ");
      }//fim do for
      APDU sendMsg = new APDU("SEND", grupo, usuario, texto.toString().trim(), portaUDPCliente);
      cliente.enviarMensagemUDP(sendMsg,7777);
    }else {
      System.out.println("Comando Desconhecido: " + comando);
    }//fim do if-elseif-elseif-else
  }//fim do while
  scanner.close();
 }//fim do metodo
}//fim da classe
