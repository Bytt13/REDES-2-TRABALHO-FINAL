/***************************************************************** 
* Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 20/06/2026
* Ultima alteracao.: 28/06/2026
* Nome.............: APDU
* Funcao...........: Maneja as APDUs que vao ser utilizadas no projeto
*************************************************************** */

package Protocol;

import java.io.Serializable;
import java.util.UUID;

public class APDU implements Serializable{
  //Identificador da versao da APDU
  private static final long serialVersionUID = 1L;
  //Campos principais da APDU
  private String operacao; //SEND, JOIN, LEAVE & CONFRIM(202310282)
  private String nomeGrupo;
  private String nomeUsuario;
  private String textoMensagem;
  private String donoDaMensagem;
  //PortaUDP para o SEND
  private int portaClienteUDP;
  //Campos Extras para a APDU "CONFIRM" (Os ticks do zap)
  private String idMensagem; //ID unico gerado para cada send
  private int statusRecebido; // 0 = Criada (Saiu do cliente em direcao ao servidor) | 1 = Enviada (Servidor Recebeu) | 
  // 2 = Recebida (Outro Cliente Recebeu, ou em caso de grupo, todos os membros receberam) | 3 = Lida (Cliente focou na tela, ou no caso de grupos, todos os membros focaram)
  /********************************************************************
  * Metodo: APDU
  * Funcao: Construtor do objeto APDU
  * @param operacao O tipo de operação ("JOIN", "LEAVE", "SEND").
  * @param nomeGrupo O grupo alvo.
  * @param nomeUsuario O usuário que está enviando a requisição.
  * @param textoMensagem O conteúdo da mensagem (pode ser null para JOIN/LEAVE).
  * @param portaClienteUDP A porta UDP aberta no cliente para receber os broadcasts.
  * @return void
  * ****************************************************************** */
 public APDU(String operacao, String nomeGrupo, String nomeUsuario, String textoMensagem, int portaClienteUDP) {
  this.operacao = operacao;
  this.nomeGrupo = nomeGrupo;
  this.nomeUsuario = nomeUsuario;
  this.textoMensagem = textoMensagem;
  this.portaClienteUDP = portaClienteUDP;

  //Se for SEND, gera um ID único para rastrear os ticks do zap
  if(this.operacao.equals("SEND")) {
    this.idMensagem = UUID.randomUUID().toString();
    this.statusRecebido = 0; //nasce como criada, e só vira enviada, quando o SERVIDOR CONFIRMA QUE RECEBEU
  }//fim do if
 }//fim do metodo
  /********************************************************************
  * Metodo: APDU
  * Funcao: Construtor do objeto APDU para CONFRIM
  * @param operacao "CONFIRM".
  * @param idMensagem Id da mensagem enviada.
  * @param statusRecebido status atual de recebimento.
  * @param nomeUsuario usuario que esta confirmando o recebimento/leitura
  * @return void
  * ****************************************************************** */
 public APDU(String operacao, String idMensagem, int statusRecebido, String nomeUsuario) {
  this.operacao = operacao.toUpperCase();
  this.idMensagem = idMensagem;
  this.statusRecebido = statusRecebido;
  this.nomeUsuario = nomeUsuario;
 }//fim do metodo
   /********************************************************************
  * Metodo: APDU
  * Funcao: Construtor 2 do objeto APDU para CONFRIM
  * @param operacao "CONFIRM".
  * @param idMensagem Id da mensagem enviada.
  * @param statusRecebido status atual de recebimento.
  * @param nomeUsuario usuario que esta confirmando o recebimento/leitura
  * @param nomeGrupo grupo em que foi mandada a mensagem
  * @param donoDaMensagem usuario que mandou a mensagem
  * @return void
  * ****************************************************************** */
 public APDU(String operacao, String idMensagem, int statusRecebido, String nomeUsuario, String nomeGrupo, String donoDaMensagem) {
  this.operacao = operacao.toUpperCase();
  this.idMensagem = idMensagem;
  this.statusRecebido = statusRecebido;
  this.nomeUsuario = nomeUsuario;
  this.nomeGrupo = nomeGrupo;
  this.donoDaMensagem = donoDaMensagem;
 }//fim do metodo
  /********************************************************************
  * Metodo: getOperacao
  * Funcao: Retornar a operacao
  * @param void
  * @return operacao
  * ****************************************************************** */
 public String getOperacao() {
  return operacao;
 }//fim do metodo
  /********************************************************************
  * Metodo: getNomeGrupo
  * Funcao: Retornar o nome do grupo
  * @param void
  * @return nome do grupo
  * ****************************************************************** */
 public String getNomeGrupo() {
  return nomeGrupo;
 }//fim do metodo
  /********************************************************************
  * Metodo: getNomeUsuario
  * Funcao: Retornar o nome do usuario
  * @param void
  * @return nome do usuario
  * ****************************************************************** */
 public String getNomeUsuario() {
  return nomeUsuario;
 }//fim do metodo
  /********************************************************************
  * Metodo: getTextoMensagem
  * Funcao: Retornar o texto da mensagem
  * @param void
  * @return texto da mensagem
  * ****************************************************************** */
 public String getTextoMensagem() {
  return textoMensagem;
 }//fim do metodo
  /********************************************************************
  * Metodo: getPortaClienteUDP
  * Funcao: Retornar a portaClienteUDP
  * @param void
  * @return portaClienteUDP
  * ****************************************************************** */
 public int getPortaClienteUDP(){
  return portaClienteUDP;
 }//fim do metodo
  /********************************************************************
  * Metodo: getIdMensagem
  * Funcao: Retornar o id da mensagem
  * @param void
  * @return id da mensagem
  * ****************************************************************** */
 public String getIdMensagem() {
  return idMensagem;
 }//fim do metodo
  /********************************************************************
  * Metodo: getStatusRecebido
  * Funcao: Retornar o status de recebido
  * @param void
  * @return status de recebido
  * ****************************************************************** */
 public int getStatusRecebido() {
  return statusRecebido;
 }//fim do metodo
  /********************************************************************
  * Metodo: getDonoDaMensagem
  * Funcao: Retornar o dono da mensagem
  * @param void
  * @return dono da mensagem
  * ****************************************************************** */
 public String getDonoDaMensagem() {
  return donoDaMensagem;
 }//fim do metodo
  /********************************************************************
  * Metodo: toString
  * Funcao: Facilitar o debug alterando o log
  * @param void
  * @return void
  * ****************************************************************** */
 @Override
 public String toString() {
  //formata nosso log
  return String.format("APDU[%s | Grupo: %s | Usuario: %s | MensagemID: %s]",
    operacao,
    (nomeGrupo != null ? nomeGrupo: "N/A"),
    nomeUsuario,
    (idMensagem != null ? idMensagem: "N/A")
  );
 }//fim do metodo
}//fim da classe
