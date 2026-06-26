/***************************************************************** 
* Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 20/06/2026
* Ultima alteracao.: 28/06/2026
* Nome.............: Grupos
* Funcao...........: Gerencia os grupos e define a ED usada no projeto
*************************************************************** */

package Model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

//Classe Grupos que define a Estrutura de dados do projeto e cria o gerenciamento de grupos
public class Grupos {
  //Chave: Grupo (O(1)), Valor: Set Ordenado (O(log n))
  private final ConcurrentHashMap<String, ConcurrentSkipListSet<Usuario>> grupos;

  /********************************************************************
   * Metodo: Grupos
   * Funcao: Construtor do objeto grupos
   * @param void
   * @return void
  * ****************************************************************** */
  public Grupos() {
    this.grupos = new ConcurrentHashMap<>();
  }//fim do construtor
  /********************************************************************
   * Metodo: adicionarUsuarios
   * Funcao: Adicionar um usuario a um grupo
   * @param nomeGrupo o nome do grupo representado por uma string
   * @param novoUsuario o objeto do novo usuario
   * @return true caso o usuario foi corretamente adicionado ao grupo, false caso contrario
  * ****************************************************************** */
  public synchronized boolean adicionarUsuarios(String nomeGrupo, Usuario novoUsuario) {
    //Cria a arvore balanceada caso o grupo nao exista
    ConcurrentSkipListSet<Usuario> usuariosDoGrupo = grupos.computeIfAbsent(
      nomeGrupo,
      k -> new ConcurrentSkipListSet<>()
    );

    //Verifica se o usuario existe, e elimina duplicatas
    if(usuariosDoGrupo.contains(novoUsuario)) {
      return false;
    } //fim do if

    //Insere o usuario na ordem alfabetica correta
    usuariosDoGrupo.add(novoUsuario);
    return true;
  }//fim do metodo
  /********************************************************************
   * Metodo: removerUsuario
   * Funcao: Remove um usuario
   * @param nomeGrupo o nome do grupo representado por uma string
   * @param nomeUsuario o nome do usuario representado por uma string
   * @return uma collection com os usuarios do grupo do parametro
  * ****************************************************************** */
  public synchronized boolean removerUsuario(String nomeGrupo, String nomeUsuario) {
    //Percorre os usuarios e remove o correto
    ConcurrentSkipListSet<Usuario> usuariosDoGrupo = grupos.get(nomeGrupo);
    
    //se existir um usuario no grupo
    if(usuariosDoGrupo != null){
      //Crio um usuario fantasma para fazer a busca binaria, so com nome, que eh o unico atributo que usamos em todas as funcoes
      Usuario usuarioFantasma = new Usuario(nomeUsuario, null, 0);
      boolean removido = usuariosDoGrupo.remove(usuarioFantasma);

      //checa se o grupo está vazio, se sim, libera memória fazendo o grupo deixar de existir
      if(usuariosDoGrupo.isEmpty()) {
        grupos.remove(nomeGrupo);
      }//fim do if
      return removido;
    } //fim do if
    return false;
  }//fim do metodo
  /********************************************************************
   * Metodo: getUsuariosDoGrupo
   * Funcao: Retorna a colecao de usuarios de um grupo
   * @param nomeGrupo o nome do grupo representado por uma string
   * @return uma collection com os usuarios do grupo do parametro
  * ****************************************************************** */
  public Collection<Usuario> getUsuariosDoGrupo(String nomeGrupo) {
    //usaremos futuramente para o SEND, e assim, ja sairao em ordem alfabetica
    return grupos.get(nomeGrupo);
  }//fim do metodo
  /********************************************************************
   * Metodo: getGrupos
   * Funcao: Retorna a colecao de grupos
   * @return uma collection com os grupos
  * ****************************************************************** */
  public Collection<String> getGrupos() {
    return grupos.keySet();
  }//fim do metodo
}//fim da classe