/***************************************************************** 
* Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 23/06/2026
* Ultima alteracao.: 23/06/2026
* Nome.............: ChatUI
* Funcao...........: Interface grafica para o chat
*************************************************************** */
package Interface;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import Protocol.APDU;
import Network.*;

public class ChatUI extends JFrame implements MensagemListener {
  private JList<String> listaGrupos;
  private DefaultListModel<String> modeloListaGrupos;
  private JTextPane areaChat;
  private JTextField campoMensagem;
  private JButton botaoEnviar;
  private JButton botaoEntrar;
  private JButton botaoSair;
  private JTextField campoEntrarGrupo;

  private Enviador cliente;
  private RecebedorUDP recebedor;
  private String nomeUsuario;
  private int portaUDPCliente;
  private String grupoAtual;

  private Map<String, List<MensagemWrapper>> mensagensGrupo;
  private Map<String, MensagemWrapper> mapaMensagensEnviadas;

  static class MensagemWrapper {
    APDU mensagem;
    boolean eMinha;
    int statusTick;

    /********************************************************************
    * Metodo: MensagemWrapper
    * Funcao: Construtor do wrapper da mensagem
    * @param msg mensagem APDU
    * @param eMinha booleano
    * @return void
    * ****************************************************************** */
    MensagemWrapper(APDU msg, boolean eMinha) {
      this.mensagem = msg;
      this.eMinha = eMinha;
      this.statusTick = 0;
    }//fim do metodo
  }//fim da classe

  /********************************************************************
  * Metodo: ChatUI
  * Funcao: Construtor da interface
  * @param cliente objeto cliente
  * @param recebedor recebedor UDP
  * @param nomeUsuario nome do usuario
  * @param portaUDPCliente porta UDP
  * @return void
  * ****************************************************************** */
  public ChatUI(Enviador cliente, RecebedorUDP recebedor, String nomeUsuario, int portaUDPCliente) {
    this.cliente = cliente;
    this.recebedor = recebedor;
    this.nomeUsuario = nomeUsuario;
    this.portaUDPCliente = portaUDPCliente;
    this.mensagensGrupo = new HashMap<>();
    this.mapaMensagensEnviadas = new HashMap<>();

    recebedor.setListener(this);

    setTitle("WhatsApp - " + nomeUsuario);
    setSize(800, 600);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);

    iniciarUI();

    addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            System.out.println("A encerrar cliente...");
            System.exit(0);
        }//fim do metodo
    });
  }//fim do metodo

  /********************************************************************
  * Metodo: iniciarUI
  * Funcao: inicializa a interface
  * @param void
  * @return void
  * ****************************************************************** */
  private void iniciarUI() {
    setLayout(new BorderLayout());

    // Painel Esquerdo para Grupos
    JPanel painelEsquerdo = new JPanel(new BorderLayout());
    painelEsquerdo.setPreferredSize(new Dimension(250, 0));
    painelEsquerdo.setBorder(BorderFactory.createTitledBorder("Meus Grupos"));

    modeloListaGrupos = new DefaultListModel<>();
    listaGrupos = new JList<>(modeloListaGrupos);
    listaGrupos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    painelEsquerdo.add(new JScrollPane(listaGrupos), BorderLayout.CENTER);

    JPanel painelEntrar = new JPanel(new BorderLayout());
    campoEntrarGrupo = new JTextField();
    botaoEntrar = new JButton("Entrar");
    botaoSair = new JButton("Sair");

    JPanel botoes = new JPanel(new GridLayout(1, 2));
    botoes.add(botaoEntrar);
    botoes.add(botaoSair);

    painelEntrar.add(campoEntrarGrupo, BorderLayout.CENTER);
    painelEntrar.add(botoes, BorderLayout.SOUTH);
    painelEsquerdo.add(painelEntrar, BorderLayout.SOUTH);

    add(painelEsquerdo, BorderLayout.WEST);

    // Painel Central para o Chat
    JPanel painelCentral = new JPanel(new BorderLayout());
    painelCentral.setBorder(BorderFactory.createTitledBorder("Chat"));

    areaChat = new JTextPane();
    areaChat.setContentType("text/html");
    areaChat.setEditable(false);
    JScrollPane scrollChat = new JScrollPane(areaChat);
    painelCentral.add(scrollChat, BorderLayout.CENTER);

    JPanel painelMensagem = new JPanel(new BorderLayout());
    campoMensagem = new JTextField();
    campoMensagem.setEnabled(false);
    botaoEnviar = new JButton("Enviar");
    botaoEnviar.setEnabled(false);

    painelMensagem.add(campoMensagem, BorderLayout.CENTER);
    painelMensagem.add(botaoEnviar, BorderLayout.EAST);
    painelCentral.add(painelMensagem, BorderLayout.SOUTH);

    add(painelCentral, BorderLayout.CENTER);

    // Eventos
    listaGrupos.addListSelectionListener(new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                selecionarGrupo(listaGrupos.getSelectedValue());
            }//fim do if
        }//fim do metodo
    });

    botaoEntrar.addActionListener(e -> {
        String g = campoEntrarGrupo.getText().trim();
        if (!g.isEmpty() && !modeloListaGrupos.contains(g)) {
            APDU mensagemJoin = new APDU("JOIN", g, nomeUsuario, null, portaUDPCliente);
            Mensagens.meuNome = nomeUsuario;
            String resposta = cliente.enviarComandoTCP(mensagemJoin);
            
            if (resposta != null && (resposta.startsWith("ERRO") || resposta.startsWith("[JOIN-ERRO]"))) {
                if (resposta.contains("uso")) {
                    JOptionPane.showMessageDialog(this, "Usuário já existente no grupo", "Erro de Usuário", JOptionPane.ERROR_MESSAGE);
                    String novoNome = JOptionPane.showInputDialog(this, "Escolha outro nome de usuário:", "Nome de Usuário", JOptionPane.QUESTION_MESSAGE);
                    if (novoNome != null && !novoNome.trim().isEmpty()) {
                        this.nomeUsuario = novoNome.trim();
                        setTitle("WhatsApp - " + this.nomeUsuario);
                        Mensagens.meuNome = this.nomeUsuario;
                    }//fim do if
                } else {
                    JOptionPane.showMessageDialog(this, resposta, "Erro ao Entrar", JOptionPane.ERROR_MESSAGE);
                }//fim do if-else
            } else {
                modeloListaGrupos.addElement(g);
                listaGrupos.setSelectedValue(g, true);
                campoEntrarGrupo.setText("");
            }//fim do if-else
        }//fim do if
    });

    botaoSair.addActionListener(e -> {
        String g = listaGrupos.getSelectedValue();
        if (g != null) {
            modeloListaGrupos.removeElement(g);
            APDU mensagemLeave = new APDU("LEAVE", g, nomeUsuario, null, portaUDPCliente);
            cliente.enviarComandoTCP(mensagemLeave);
            if (modeloListaGrupos.isEmpty()) {
                selecionarGrupo(null);
            } else {
                listaGrupos.setSelectedIndex(0);
            }//fim do if-else
        }//fim do if
    });

    ActionListener acaoEnviar = e -> {
        String texto = campoMensagem.getText().trim();
        if (!texto.isEmpty() && grupoAtual != null) {
            APDU mensagemSend = new APDU("SEND", grupoAtual, nomeUsuario, texto, portaUDPCliente);
            MensagemWrapper mw = new MensagemWrapper(mensagemSend, true);
            
            mensagensGrupo.putIfAbsent(grupoAtual, new ArrayList<>());
            mensagensGrupo.get(grupoAtual).add(mw);
            mapaMensagensEnviadas.put(mensagemSend.getIdMensagem(), mw);
            
            cliente.enviarMensagemUDP(mensagemSend, 7777);
            campoMensagem.setText("");
            renderizarChat();
        }//fim do if
    };

    botaoEnviar.addActionListener(acaoEnviar);
    campoMensagem.addActionListener(acaoEnviar);
    
    renderizarChat();
  }//fim do metodo

  /********************************************************************
  * Metodo: selecionarGrupo
  * Funcao: seleciona o grupo atual
  * @param grupo nome do grupo
  * @return void
  * ****************************************************************** */
  private void selecionarGrupo(String grupo) {
    grupoAtual = grupo;
    if (grupo == null) {
        campoMensagem.setEnabled(false);
        botaoEnviar.setEnabled(false);
    } else {
        campoMensagem.setEnabled(true);
        botaoEnviar.setEnabled(true);
        
        // Marcar nao lidas como lidas
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
    renderizarChat();
  }//fim do metodo

  /********************************************************************
  * Metodo: renderizarChat
  * Funcao: atualiza o chat visual
  * @param void
  * @return void
  * ****************************************************************** */
  private void renderizarChat() {
    if (grupoAtual == null) {
        areaChat.setText("<html><body style='font-family: sans-serif; padding: 10px; color: gray;'>Nenhum grupo selecionado. Entre ou selecione um grupo para enviar mensagens.</body></html>");
        return;
    }//fim do if

    StringBuilder sb = new StringBuilder("<html><body style='font-family: sans-serif; padding: 10px;'>");
    List<MensagemWrapper> msgs = mensagensGrupo.getOrDefault(grupoAtual, new ArrayList<>());
    for (MensagemWrapper mw : msgs) {
        String remetente = mw.eMinha ? "Voce" : mw.mensagem.getNomeUsuario();
        String texto = mw.mensagem.getTextoMensagem();
        
        String tick = "";
        if (mw.eMinha) {
            if (mw.statusTick == 1) tick = " <span style='color:gray'>✓</span>";
            else if (mw.statusTick == 2) tick = " <span style='color:gray'>✓✓</span>";
            else if (mw.statusTick == 3) tick = " <span style='color:blue'>✓✓</span>";
            else tick = " <span style='color:silver'>◷</span>";
        }//fim do if
        
        String cor = mw.eMinha ? "#0066cc" : "#009933";
        sb.append("<div style='margin-bottom: 8px;'>")
            .append("<b style='color:").append(cor).append(";'>").append(remetente).append(":</b> ")
            .append(texto).append(tick)
            .append("</div>");
    }//fim do for
    sb.append("</body></html>");
    areaChat.setText(sb.toString());

    // Scroll para baixo
    SwingUtilities.invokeLater(() -> {
        Container pai = SwingUtilities.getUnwrappedParent(areaChat);
        if (pai instanceof JViewport) {
            JViewport viewport = (JViewport) pai;
            Rectangle viewRect = viewport.getViewRect();
            viewRect.y = areaChat.getHeight() - viewRect.height;
            if (viewRect.y < 0) viewRect.y = 0;
            areaChat.scrollRectToVisible(viewRect);
        }//fim do if
    });
  }//fim do metodo

  /********************************************************************
  * Metodo: onMessageReceived
  * Funcao: callback quando recebe mensagem
  * @param apdu objeto apdu
  * @return void
  * ****************************************************************** */
  @Override
  public void onMessageReceived(APDU apdu) {
      SwingUtilities.invokeLater(() -> {
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
              renderizarChat();
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
    SwingUtilities.invokeLater(() -> {
      String id = apdu.getIdMensagem();
      if (mapaMensagensEnviadas.containsKey(id)) {
        MensagemWrapper mw = mapaMensagensEnviadas.get(id);
        if (apdu.getStatusRecebido() > mw.statusTick) {
          mw.statusTick = apdu.getStatusRecebido();
          if (grupoAtual != null && grupoAtual.equals(mw.mensagem.getNomeGrupo())) {
              renderizarChat();
          }//fim do if
        }//fim do if
      }//fim do if
  });
  }//fim do metodo
}//fim da classe
