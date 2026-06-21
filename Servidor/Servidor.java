/***************************************************************** 
* Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 20/06/2026
* Ultima alteracao.: 28/06/2026
* Nome.............: Servidor
* Funcao...........: Maestro que coordena os outros arquivos da pasta servidor
*************************************************************** */

import Network.ServidorTCP;
import Network.ServidorUDP;
import Network.ServidorDescoberta;
import Model.Grupos;

public class Servidor {
  private static final int PORTA_TCP = 6789;
  /********************************************************************
   * Metodo: iniciar
   * Funcao: inicar a conexao do servidor
   * @param void
   * @return void
  * ****************************************************************** */
  public void iniciar() {
    System.out.println("Iniciando o servidor...");
    //Cria a memoria central
    Grupos gerenciador = new Grupos();
    //Inicia o Ouvido UDP para descoberta automatica
    ServidorDescoberta descoberta = new ServidorDescoberta();
    descoberta.start();
    //Instancia e inicia a thread do servidor TCP
    ServidorTCP servidorTCP = new ServidorTCP(PORTA_TCP, gerenciador);
    servidorTCP.start();
    //Instancia e inicia a thread do servidor UDP
    ServidorUDP servidorUDP = new ServidorUDP(gerenciador);
    servidorUDP.start();
  }//fim do metodo
}//fim da classe
