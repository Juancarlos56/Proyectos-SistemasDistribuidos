package ec.edu.ups.cliente;

import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import javax.swing.JOptionPane;
import ec.edu.ups.server.ChatServerInterface;

public class ChatClient extends UnicastRemoteObject implements ChatClienteInterface{

	private static final long serialVersionUID = 7468891722773409712L;
	VentanaClienteChat chatGUI;
	private String hostName = "localhost";
	private String serviceName = "GroupChatService";
	private String clientServiceName;
	private String name;
	protected ChatServerInterface serverIF;
	protected boolean connectionProblem = false;
	
	public ChatClient(VentanaClienteChat aChatGUI, String userName) throws RemoteException {
		super();
		this.chatGUI = aChatGUI;
		this.name = userName;
		this.clientServiceName = "ClientListenService_" + userName;
	}
	
	public void startClient() throws RemoteException {		
		String[] details = {name, hostName, clientServiceName};	

		try {
			Naming.rebind("rmi://" + hostName + "/" + clientServiceName, this);
			serverIF = ( ChatServerInterface )Naming.lookup("rmi://" + hostName + "/" + serviceName);	
		} 
		catch (ConnectException  e) {
			JOptionPane.showMessageDialog(
					chatGUI.frame, "El servidor parece no estar disponible \\ Inténtelo más tarde",
					"Connection problem", JOptionPane.ERROR_MESSAGE);
			connectionProblem = true;
			e.printStackTrace();
		}
		catch(NotBoundException | MalformedURLException me){
			connectionProblem = true;
			me.printStackTrace();
		}
		if(!connectionProblem){
			registerWithServer(details);
		}	
		System.out.println("El servidor RMI del cliente se está ejecutando...\n");
	}
	
	public void registerWithServer(String[] details) {		
		try{
			serverIF.passIDentity(this.ref);//now redundant ??
			serverIF.registerListener(details);			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void messageFromServer(String message) throws RemoteException {
		System.out.println( message );
		chatGUI.textArea.append( message );
		//make the gui display the last appended text, ie scroll to bottom
		chatGUI.textArea.setCaretPosition(chatGUI.textArea.getDocument().getLength());
	}

	@Override
	public void updateUserList(String[] currentUsers) throws RemoteException {
		
		chatGUI.userPanel.remove(chatGUI.clientPanel);
		chatGUI.setClientPanel(currentUsers);
		chatGUI.clientPanel.repaint();
		chatGUI.clientPanel.revalidate();
	}
}
