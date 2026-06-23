/***************************************************************** 
* Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 23/06/2026
* Ultima alteracao.: 23/06/2026
* Nome.............: MensagemListener
* Funcao...........: Interface para ouvir mensagens recebidas
*************************************************************** */
package Network;

import Protocol.APDU;

public interface MensagemListener {
  /********************************************************************
  * Metodo: onMessageReceived
  * Funcao: chamado quando uma mensagem e recebida
  * @param apdu objeto apdu
  * @return void
  * ****************************************************************** */
  void onMessageReceived(APDU apdu);

  /********************************************************************
  * Metodo: onTickReceived
  * Funcao: chamado quando um tick e recebido
  * @param apdu objeto apdu
  * @return void
  * ****************************************************************** */
  void onTickReceived(APDU apdu);
}//fim da interface
