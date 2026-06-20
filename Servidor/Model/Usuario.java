/***************************************************************** 
* Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 20/06/2026
* Ultima alteracao.: 28/06/2026
* Nome.............: Usuario
* Funcao...........: Define o que eh o usuario, seus atributos
*************************************************************** */

package Model;

import java.util.Objects;
import java.net.InetAddress;

//Classe Usuario com Comparable, para garantir ordenacao, O(log n)
public class Usuario implements Comparable<Usuario> {
  private String nome; //nome do usuario
  private InetAddress ip; //ip do usuario
  private int portaUDP; //portaUDP do usuario
  /********************************************************************
   * Metodo: Usuario
   * Funcao: Construtor do objeto usuario
   * @param nome String do nome do usuario
   * @param ip InetAddress que pega o ip do usuario
   * @param portaUDP portaUDP do usario (int)
   * @return void
  * ****************************************************************** */
  public Usuario(String nome, InetAddress ip, int portaUDP) {
    this.nome = nome;
    this.ip = ip;
    this.portaUDP = portaUDP;
  }//fim do construtor
  /********************************************************************
   * Metodo: getNome
   * Funcao: retornar o nome do usuario
   * @param void
   * @return nome do usuario
  * ****************************************************************** */
  public String getNome() {
    return nome;
  }//fim do metodo
  /********************************************************************
   * Metodo: getIp
   * Funcao: retornar o ip do usuario
   * @param void
   * @return ip do usuario
  * ****************************************************************** */
  public InetAddress getIp() {
    return ip;
  }//fim do metodo
  /********************************************************************
   * Metodo: getPortaUDP
   * Funcao: retornar a portaUDP do usuario 
   * @param void
   * @return portaUDP do usuario 
  * ****************************************************************** */
  public int getPortaUDP() {
    return portaUDP;
  }//fim do metodo
  /********************************************************************
   * Metodo: compareTo
   * Funcao: comparar este usuario a outro, a fim de ordenar a lista de usuarios de maneira mais facil
   * @param outro um usuario qualquer, para compararmos o nome, e poder ordenar alfabeticamente para reduzir a complexidade da busca para O(log n)
   * @return resultado da comparacao da string (um int negativo para menor que a string comparada, e um int positivo para maior)
  * ****************************************************************** */
  @Override
  public int compareTo(Usuario outro) {
      //Funcao para comparar o nome desse usuario com outro
      return this.nome.compareToIgnoreCase(outro.getNome());
  }//fim do metodo
  /********************************************************************
   * Metodo: hashCode
   * Funcao: retornar o hash do usuario, com base no nome
   * @param void
   * @return o hash do usuario
  * ****************************************************************** */
  @Override
  public int hashCode() {
    //Funcao para retornar o hash do nome
    return Objects.hash(nome);
  }//fim do metodo
  /********************************************************************
   * Metodo: equals
   * Funcao: conferir se: o objeto é aquele que buscamos mesmo, e se realmente existe
   * @param o Objeto do usuario
   * @return true para o usuario ser aquele que buscamos, ou ele mesmo, false para um usuario diferente ou nulo
  * ****************************************************************** */
  @Override
  public boolean equals(Object o) {
    if(o == null || getClass() != o.getClass()) return false; //se nulo, ou diferente, falso
    if(this == o) return true; //se for o proprio usuario true

    Usuario usuario = (Usuario) o;
    return nome.equals(usuario.nome); //usa a propria funcao, para comparar o usuario passado pelo Object o e retornar true ou false
  }//fim do metodo
}//fim da classe