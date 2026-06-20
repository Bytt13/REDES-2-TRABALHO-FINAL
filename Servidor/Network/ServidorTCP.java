/***************************************************************** 
* Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 20/06/2026
* Ultima alteracao.: 28/06/2026
* Nome.............: ServidorTCP
* Funcao...........: Estabelece conexao tcp para as APDUs padrao
*************************************************************** */

package Network;

import java.net.*;
import java.io.*;
import Model.Grupos;

//Classe ServidorTCP, estabelece a conexao TCP necessaria para as APDU JOIN e LEAVE
public class ServidorTCP extends Thread {
  //Cria a porta e gerenciador de grupos necessario
  private final int porta; 
  private final Grupos gerenciador;
  /********************************************************************
   * Metodo: ServidorTCP
   * Funcao: Construtor do objeto servidorTCP
   * @param porta porta estabelecida
   * @param gerenciador Grupos estabelecidos
   * @return void
  * ****************************************************************** */
  public ServidorTCP(int porta, Grupos gerenciador) {
    this.porta = porta;
    this.gerenciador = gerenciador;
  }//fim do construtor
  /********************************************************************
   * Metodo: run
   * Funcao: rodar a thread do servidorTCP
   * @param void
   * @return void
  * ****************************************************************** */
  @Override
  public void run() {
    //try-catch para caso de errado, o server socket sera fechado corretamente
    try(ServerSocket serverSocket = new ServerSocket(porta)) {
      System.out.println("[TCP] Servidor escutando a porta " + porta + "...");

      //Loop infinito para nunca parar de aceitar novos clientes e nunca fechar a conexao
      while(true) {
        //accept trava tudo até alguém conectar
        Socket socketCliente = serverSocket.accept();
        System.out.println("[TCP] Nova conexão recebida do IP: " + socketCliente.getInetAddress().getHostAddress());
        
        //Terceiriza o trabalho de tratar o cliente para outra classe
        ClienteTCP tratador = new ClienteTCP(socketCliente, gerenciador);
        tratador.start();
      }//fim do while
    } catch (IOException e) {
      System.err.println("[TCP] Erro fatal no servidor TCP: " + e.getMessage());
    } //fim do try-catch
  }//fim do metodo
} //fim da classe
