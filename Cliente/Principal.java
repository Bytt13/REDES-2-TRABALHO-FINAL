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
import Network.Mensagens;

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
  RecebedorUDP recebedor = new RecebedorUDP(portaUDPCliente, ipServidor, 7777);
  recebedor.start();
  
  Cliente cliente = new Cliente(ipServidor, 6789);
  System.out.println("\nComandos disponíveis:");
  System.out.println("  -entrar <nomeGrupo> <nomeUsuario>");
  System.out.println("  -sair <nomeGrupo> <nomeUsuario>");
  System.out.println("  -enviar <nomeGrupo> <nomeUsuario> <Texto>");
  System.out.println("  -lido");
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

    if (partes.length < 1) {
        System.out.println("Formato incorreto. Utilize: -comando <grupo> <usuario>");
        continue;
    }//fim do if

    String grupo = partes[1];
    String usuario = partes[2];

    switch (comando) {
      case "-entrar": {
        APDU joinMsg = new APDU("JOIN", grupo, usuario, null, portaUDPCliente);
        Mensagens.meuNome = usuario;
        cliente.enviarComandoTCP(joinMsg);
        break;
      }//fim do case
      case "-sair": {
        APDU leaveMsg = new APDU("LEAVE", grupo, usuario, null, portaUDPCliente);
        cliente.enviarComandoTCP(leaveMsg);
        break;
      }//fim do case
      case "-enviar": {
        if(partes.length < 4) {
          System.out.println("Formato incorreto, utilize: -comando <Grupo> <Usuario> <Mensagem>");
          continue;
        } else if (comando.equals("-lido")) {
          if(Mensagens.ultimaMensagemId != null) {
            APDU lido = new APDU("CONFIRM", Mensagens.ultimaMensagemId, 3,
            Mensagens.meuNome, Mensagens.ultimoGrupo, Mensagens.ultimoRemetente);
            cliente.enviarMensagemUDP(lido, 7777);
            System.out.println(" ✓✓ Mensagem marcada como lida com sucesso!");
          } else {
            System.out.println(" Nenhuma mensagem recebida recentemente para marcar como lida.");
          }//fim do if-else
        }//fim do if

        //Concatena tudo que o usuario escreveu para formar o texto
        StringBuilder texto = new StringBuilder();
        for(int i = 3; i < partes.length; i++) {
          texto.append(partes[i]).append(" ");
        }//fim do for
        APDU sendMsg = new APDU("SEND", grupo, usuario, texto.toString().trim(), portaUDPCliente);
        cliente.enviarMensagemUDP(sendMsg,7777);
        break;
      }//fim do case
      case "-lido" : {
        if(Mensagens.ultimaMensagemId != null) {
        // Envia o Status 3
        APDU confirm3 = new APDU("CONFIRM", Mensagens.ultimaMensagemId, 3, Mensagens.meuNome, Mensagens.ultimoGrupo, Mensagens.ultimoRemetente);
        cliente.enviarMensagemUDP(confirm3, 7777);
        System.out.println(" ✓✓ Mensagem marcada como lida com sucesso!");
        } else {
          System.out.println(" Nenhuma mensagem recebida recentemente para marcar como lida.");
        }//fim do if-else
        break;
      }//fim do case
      default: {
        System.out.println("Comando Desconhecido: " + comando);
        break;
      }//fim do case
    }//fim do switch
  }//fim do while
  scanner.close();
 }//fim do metodo
}//fim da classe
