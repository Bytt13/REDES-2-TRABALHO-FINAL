/***************************************************************** 
* Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 23/06/2026
* Ultima alteracao.: 28/06/2026
* Nome.............: ChatController
* Funcao...........: Controlador da interface grafica
*************************************************************** */
package Controller;

import Protocol.APDU;
import Network.Enviador;
import Network.RecebedorUDP;
import Network.MensagemListener;
import Network.Mensagens;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Optional;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javafx.stage.Stage;

public class ChatController implements MensagemListener {
  private Enviador cliente;
  private RecebedorUDP recebedor;
  private String nomeUsuario;
  private int portaUDPCliente;
  private String grupoAtual;

  private Map<String, List<MensagemWrapper>> mensagensGrupo;
  private Map<String, MensagemWrapper> mapaMensagensEnviadas;
  
  @FXML private ListView<String> listaGrupos;
  @FXML private ListView<javafx.scene.text.TextFlow> areaChat;
  @FXML private TextField campoMensagem;
  @FXML private Button botaoEnviar;
  @FXML private Button botaoEntrar;
  @FXML private Button botaoSair;
  @FXML private Button botaoVerMembros;
  @FXML private TextField campoEntrarGrupo;

  private Stage stage;

  public static class MensagemWrapper {
    public APDU mensagem;
    public boolean eMinha;
    public int statusTick;

    /********************************************************************
    * Metodo: MensagemWrapper
    * Funcao: Construtor do wrapper da mensagem
    * @param msg mensagem APDU
    * @param eMinha booleano
    * @return void
    * ****************************************************************** */
    public MensagemWrapper(APDU msg, boolean eMinha) {
      this.mensagem = msg;
      this.eMinha = eMinha;
      this.statusTick = 0;
    }//fim do metodo
  }//fim da classe

  /********************************************************************
  * Metodo: ChatController
  * Funcao: Construtor do Controlador
  * @param cliente objeto cliente
  * @param recebedor recebedor UDP
  * @param nomeUsuario nome do usuario
  * @param portaUDPCliente porta UDP
  * @return void
  * ****************************************************************** */
  public ChatController(Enviador cliente, RecebedorUDP recebedor, String nomeUsuario, int portaUDPCliente) {
    this.cliente = cliente;
    this.recebedor = recebedor;
    this.nomeUsuario = nomeUsuario;
    this.portaUDPCliente = portaUDPCliente;
    this.mensagensGrupo = new HashMap<>();
    this.mapaMensagensEnviadas = new HashMap<>();

    recebedor.setListener(this);
  }//fim do metodo

  /********************************************************************
  * Metodo: setStage
  * Funcao: Define a janela principal
  * @param stage stage do JavaFX
  * @return void
  * ****************************************************************** */
  public void setStage(Stage stage) {
    this.stage = stage;
    atualizarTitulo(this.nomeUsuario);
    stage.setOnCloseRequest(e -> encerrar());
  }//fim do metodo

  /********************************************************************
  * Metodo: initialize
  * Funcao: Inicializa os componentes FXML
  * @param void
  * @return void
  * ****************************************************************** */
  @FXML
  public void initialize() {
    listaGrupos.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
      selecionarGrupo(newVal);
    });

    // WebView stuff removed

    renderizarChat(null, null);
  }//fim do metodo

  /********************************************************************
  * Metodo: handleEntrarGrupo
  * Funcao: Handler do botao Entrar
  * @param void
  * @return void
  * ****************************************************************** */
  @FXML
  private void handleEntrarGrupo() {
    entrarGrupo(campoEntrarGrupo.getText().trim());
  }//fim do metodo

  /********************************************************************
  * Metodo: handleSairGrupo
  * Funcao: Handler do botao Sair
  * @param void
  * @return void
  * ****************************************************************** */
  @FXML
  private void handleSairGrupo() {
    sairGrupo(listaGrupos.getSelectionModel().getSelectedItem());
  }//fim do metodo

  /********************************************************************
  * Metodo: handleEnviarMensagem
  * Funcao: Handler do botao Enviar
  * @param void
  * @return void
  * ****************************************************************** */
  @FXML
  private void handleEnviarMensagem() {
    enviarMensagem(campoMensagem.getText().trim());
  }//fim do metodo

  /********************************************************************
  * Metodo: entrarGrupo
  * Funcao: entra num grupo do chat
  * @param g nome do grupo
  * @return void
  * ****************************************************************** */
  public void entrarGrupo(String g) {
    if (!g.isEmpty() && !listaGrupos.getItems().contains(g)) {
      APDU mensagemJoin = new APDU("JOIN", g, nomeUsuario, null, portaUDPCliente);
      Mensagens.meuNome = nomeUsuario;
      String resposta = cliente.enviarComandoTCP(mensagemJoin);
      
      if (resposta != null && (resposta.startsWith("ERRO") || resposta.startsWith("[JOIN-ERRO]"))) {
        if (resposta.contains("uso")) {
          mostrarErro("Usuario ja existente no grupo", "Erro de Usuario");
          String novoNome = pedirNovoNome();
          if (novoNome != null && !novoNome.trim().isEmpty()) {
            this.nomeUsuario = novoNome.trim();
            atualizarTitulo(this.nomeUsuario);
            Mensagens.meuNome = this.nomeUsuario;
          }//fim do if
        } else {
          mostrarErro(resposta, "Erro ao Entrar");
        }//fim do if-else
      } else {
        listaGrupos.getItems().add(g);
        listaGrupos.getSelectionModel().select(g);
        campoEntrarGrupo.clear();

        // Avisar no chat que o usuario entrou
        APDU mensagemAviso = new APDU("SEND", g, nomeUsuario, "[Aviso] " + nomeUsuario + " entrou no grupo!", portaUDPCliente);
        MensagemWrapper mwAviso = new MensagemWrapper(mensagemAviso, true);
        mensagensGrupo.putIfAbsent(g, new ArrayList<>());
        mensagensGrupo.get(g).add(mwAviso);
        mapaMensagensEnviadas.put(mensagemAviso.getIdMensagem(), mwAviso);
        cliente.enviarMensagemUDP(mensagemAviso, 7777);
      }//fim do if-else
    }//fim do if
  }//fim do metodo

  /********************************************************************
  * Metodo: sairGrupo
  * Funcao: sai de um grupo do chat
  * @param g nome do grupo
  * @return void
  * ****************************************************************** */
  public void sairGrupo(String g) {
    if (g != null) {
      listaGrupos.getItems().remove(g);
      APDU mensagemLeave = new APDU("LEAVE", g, nomeUsuario, null, portaUDPCliente);
      cliente.enviarComandoTCP(mensagemLeave);
      if (listaGrupos.getItems().isEmpty()) {
        selecionarGrupo(null);
      } else {
        listaGrupos.getSelectionModel().selectFirst();
      }//fim do if-else
    }//fim do if
  }//fim do metodo

  /********************************************************************
  * Metodo: selecionarGrupo
  * Funcao: seleciona o grupo atual
  * @param grupo nome do grupo
  * @return void
  * ****************************************************************** */
  public void selecionarGrupo(String grupo) {
    grupoAtual = grupo;
    if (grupo == null) {
      campoMensagem.setDisable(true);
      botaoEnviar.setDisable(true);
    } else {
      campoMensagem.setDisable(false);
      botaoEnviar.setDisable(false);
      
      if (mensagensGrupo.containsKey(grupo)) {
        for (MensagemWrapper mw : mensagensGrupo.get(grupo)) {
          if (!mw.eMinha && mw.statusTick < 3) {
            mw.statusTick = 3;
            APDU lido = new APDU("CONFIRM", mw.mensagem.getIdMensagem(), 3, nomeUsuario, grupo, mw.mensagem.getNomeUsuario());
            cliente.enviarMensagemUDP(lido, 7777);
          }//fim do if
        }//fim do for
      }//fim do if
    }//fim do if-else
    renderizarChat(grupoAtual, mensagensGrupo.getOrDefault(grupoAtual, new ArrayList<>()));
  }//fim do metodo

  /********************************************************************
  * Metodo: handleVerMembros
  * Funcao: Handler do botao Ver membros
  * @param void
  * @return void
  * ****************************************************************** */
  @FXML
  private void handleVerMembros() {
    if (grupoAtual != null) {
      APDU req = new APDU("MEMBERS", grupoAtual, nomeUsuario, null, portaUDPCliente);
      String resposta = cliente.enviarComandoTCP(req);
      if (resposta != null && resposta.startsWith("OK: ")) {
        String membros = resposta.substring(4);
        String[] lista = membros.split(",");
        StringBuilder sb = new StringBuilder("Membros do grupo " + grupoAtual + ":\n");
        for (String m : lista) {
          if (!m.isEmpty()) {
            sb.append("- ").append(m).append("\n");
          }//fim do if
        }//fim do for
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Membros do Grupo");
        alert.setHeaderText(null);
        alert.setContentText(sb.toString());
        alert.showAndWait();
      } else {
        mostrarErro("Nao foi possivel obter a lista de membros.", "Erro");
      }//fim do if-else
    }//fim do if
  }//fim do metodo

  /********************************************************************
  * Metodo: enviarMensagem
  * Funcao: envia uma mensagem
  * @param texto texto da mensagem
  * @return void
  * ****************************************************************** */
  public void enviarMensagem(String texto) {
    if (!texto.isEmpty() && grupoAtual != null) {
      APDU mensagemSend = new APDU("SEND", grupoAtual, nomeUsuario, texto, portaUDPCliente);
      MensagemWrapper mw = new MensagemWrapper(mensagemSend, true);
      
      mensagensGrupo.putIfAbsent(grupoAtual, new ArrayList<>());
      mensagensGrupo.get(grupoAtual).add(mw);
      mapaMensagensEnviadas.put(mensagemSend.getIdMensagem(), mw);
      
      cliente.enviarMensagemUDP(mensagemSend, 7777);
      campoMensagem.clear();
      renderizarChat(grupoAtual, mensagensGrupo.getOrDefault(grupoAtual, new ArrayList<>()));
    }//fim do if
  }//fim do metodo

  /********************************************************************
  * Metodo: onMessageReceived
  * Funcao: callback quando recebe mensagem
  * @param apdu objeto apdu
  * @return void
  * ****************************************************************** */
  @Override
  public void onMessageReceived(APDU apdu) {
    Platform.runLater(() -> {
      String grupo = apdu.getNomeGrupo();
      if (grupo != null) grupo = grupo.trim();
      
      mensagensGrupo.putIfAbsent(grupo, new ArrayList<>());
      MensagemWrapper mw = new MensagemWrapper(apdu, false);
      mensagensGrupo.get(grupo).add(mw);
      
      if (grupoAtual != null && grupo.equals(grupoAtual.trim())) {
        mw.statusTick = 3;
        String remetente = apdu.getNomeUsuario() != null ? apdu.getNomeUsuario().trim() : null;
        APDU lido = new APDU("CONFIRM", apdu.getIdMensagem(), 3, nomeUsuario, grupo, remetente);
        cliente.enviarMensagemUDP(lido, 7777);
        renderizarChat(grupoAtual, mensagensGrupo.getOrDefault(grupoAtual, new ArrayList<>()));
      }//fim do if
    });
  }//fim do metodo

  /********************************************************************
  * Metodo: onTickReceived
  * Funcao: callback quando recebe tick
  * @param apdu objeto apdu
  * @return void
  * ****************************************************************** */
  @Override
  public void onTickReceived(APDU apdu) {
    Platform.runLater(() -> {
      String id = apdu.getIdMensagem();
      if (mapaMensagensEnviadas.containsKey(id)) {
        MensagemWrapper mw = mapaMensagensEnviadas.get(id);
        if (apdu.getStatusRecebido() > mw.statusTick) {
          mw.statusTick = apdu.getStatusRecebido();
          if (grupoAtual != null && grupoAtual.equals(mw.mensagem.getNomeGrupo())) {
            renderizarChat(grupoAtual, mensagensGrupo.getOrDefault(grupoAtual, new ArrayList<>()));
          }//fim do if
        }//fim do if
      }//fim do if
    });
  }//fim do metodo

  /********************************************************************
  * Metodo: mostrarErro
  * Funcao: mostra tela de erro
  * @param mensagem erro
  * @param titulo titulo do erro
  * @return void
  * ****************************************************************** */
  public void mostrarErro(String mensagem, String titulo) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(titulo);
    alert.setHeaderText(null);
    alert.setContentText(mensagem);
    alert.showAndWait();
  }//fim do metodo

  /********************************************************************
  * Metodo: pedirNovoNome
  * Funcao: pede nome de novo para o usuario
  * @param void
  * @return String
  * ****************************************************************** */
  public String pedirNovoNome() {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Nome de Usuario");
    dialog.setHeaderText("Escolha outro nome de usuario:");
    dialog.setContentText("Nome:");
    Optional<String> result = dialog.showAndWait();
    return result.orElse(null);
  }//fim do metodo

  /********************************************************************
  * Metodo: atualizarTitulo
  * Funcao: atualiza o titulo da aba do programa
  * @param nome nome
  * @return void
  * ****************************************************************** */
  public void atualizarTitulo(String nome) {
    if (stage != null) {
      stage.setTitle("WhatsApp - " + nome);
    }//fim do if
  }//fim do metodo

  /********************************************************************
  * Metodo: renderizarChat
  * Funcao: atualiza as mensagens na tela
  * @param grupoAtual nome do grupo atual
  * @param msgs lista de mensagens
  * @return void
  * ****************************************************************** */
  public void renderizarChat(String grupoAtual, List<MensagemWrapper> msgs) {
    areaChat.getItems().clear();
    if (grupoAtual == null) {
      javafx.scene.text.Text msg = new javafx.scene.text.Text("Nenhum grupo selecionado. Entre ou selecione um grupo.");
      msg.setStyle("-fx-fill: gray; -fx-font-style: italic;");
      areaChat.getItems().add(new javafx.scene.text.TextFlow(msg));
      return;
    }//fim do if

    if (msgs != null) {
      for (MensagemWrapper mw : msgs) {
        String remetente = mw.eMinha ? "Voce" : mw.mensagem.getNomeUsuario();
        String texto = mw.mensagem.getTextoMensagem();
        String cor = mw.eMinha ? "#0066cc" : "#009933";
        
        javafx.scene.text.Text remetenteText = new javafx.scene.text.Text(remetente + ": ");
        remetenteText.setStyle("-fx-font-weight: bold; -fx-fill: " + cor + ";");
        
        javafx.scene.text.Text conteudoText = new javafx.scene.text.Text(texto);
        
        javafx.scene.text.Text tickText = new javafx.scene.text.Text("");
        if (mw.eMinha) {
          if (mw.statusTick == 1) tickText.setText(" \u2713");
          else if (mw.statusTick >= 2) tickText.setText(" \u2713\u2713");
          else tickText.setText(" \u231A"); // reloginho
          
          if (mw.statusTick == 3) {
            tickText.setStyle("-fx-fill: #34B7F1; -fx-font-weight: bold;"); // Azul do WhatsApp
          } else {
            tickText.setStyle("-fx-fill: gray; -fx-font-weight: bold;");
          }
        }//fim do if
        
        areaChat.getItems().add(new javafx.scene.text.TextFlow(remetenteText, conteudoText, tickText));
      }//fim do for
    }//fim do if
    
    // Rola para a ultima mensagem
    int lastIndex = areaChat.getItems().size() - 1;
    if (lastIndex >= 0) {
      areaChat.scrollTo(lastIndex);
    }
  }//fim do metodo

  /********************************************************************
  * Metodo: encerrar
  * Funcao: encerra o programa
  * @param void
  * @return void
  * ****************************************************************** */
  public void encerrar() {
    System.out.println("A encerrar cliente...");
    System.exit(0);
  }//fim do metodo
}//fim da classe
