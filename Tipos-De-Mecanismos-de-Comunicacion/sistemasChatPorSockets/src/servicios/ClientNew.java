
package servicios;

import java.awt.EventQueue;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

public class ClientNew {

	private JTextField textMessage;
	private int port = 9998;
	private String ip = "localhost";
	private DataOutputStream dos;
	private static DataInputStream dis;
	private Socket clientsoc;
	private static String clientname;
	
	/* ArrayList para almacenar nombres de todos los clientes en línea,
	incluido el nombre de usuario del cliente actual */
	private static ArrayList<String> otherusers;
	
	/* HashMap para almacenar el mapeo del nombre de usuario del cliente y la marca de tiempo de su último mensaje.
	Se utiliza para implementar el temporizador. */
	private static HashMap<String, String> usertimestamp;
	
	/* Bandera booleana para indicar si el cliente está conectado o desconectado */
	private boolean connected = false;
	
	/* Regex para filtrar nombres de usuario incorrectos (no alfanuméricos) */
	public String regex = "^[a-zA-Z0-9]+$";
	
	/* Variables Http estáticas utilizadas para construir encabezados de solicitud http */
	private final static String host = "Host: localhost";
	private final static String userAgent = "User-Agent: MultiChat/2.0";
	private final static String contentType = "Content-Type: text/html";
	private final static String contentlength = "Content-Length: ";
	private final static String date = "Date: ";
	private final static String connection = "Connection: close";
	
	private JFrame frame;
	private JTextField textRegName;
	private static JTextArea chatArea;
	private JScrollPane scrollPane;
	/**
	 * Iniciar la aplicacion
	 */
	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientNew window = new ClientNew();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		String readChat = ""; String arr[];
		SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
		
		while(true) {
			
			try {
				readChat = dis.readUTF();
				arr = readChat.split(":");
				
				if(arr[0].equals("CONNECTED")) {
					otherusers.add(arr[1]);
					readChat = arr[1]+" has Connected";
				}
				else if(arr[0].equals("LOGGEDOFF")) {
					readChat = arr[1]+" has LOGGED OUT.";
					otherusers.remove(arr[1]);
					usertimestamp.remove(arr[1]);
				}
				else {
					if(!usertimestamp.containsKey(arr[0])) {
						String time1 = sdf.format(Calendar.getInstance().getTime());
						usertimestamp.put(arr[0], time1);
						
						readChat = arr[0]+" :(00:00) - "+arr[1];
					}
					else {
						
						Calendar cl = Calendar.getInstance();
						String curtime = sdf.format(cl.getTime());
						
						String timedif = subtractTime(curtime , usertimestamp.get(arr[0]));
						readChat = arr[0]+" :("+timedif+") - "+arr[1];
						usertimestamp.put(arr[0], curtime);
					}
					
				}
				chatArea.append(readChat+"\n");
				
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		
	}

	/* Método para calcular la diferencia de tiempo entre el mensaje actual y el último para ese cliente en particular */
	private static String subtractTime(String curtime, String string) {

		String ff = "";
		
		int m2 = Integer.parseInt(curtime.substring(0,2));
		int m1 = Integer.parseInt(string.substring(0,2));
		
		int s2 = Integer.parseInt(curtime.substring(3,5));
		int s1 = Integer.parseInt(string.substring(3,5));
		
		if(s2<s1) {
			ff = ((m2-m1)<0?Math.abs(m2-m1+59):m2-m1-1)+":"+Math.abs(s2-s1+60);
		}
		else {
			ff = ((m2-m1)<0?Math.abs(m2-m1+60):m2-m1)+":"+(s2-s1);
		}
		return ff;
	}

	/**
	 * Create the application.
	 */
	public ClientNew() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(500, 500, 500, 500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String msgg = textMessage.getText();
				StringBuilder sbpostreq = new StringBuilder();
				
				try {
					
					/* Construyendo el encabezado del método HTTP Post para enviarlo al cliente */
					sbpostreq.append("POST /").append(msgg).append("/ HTTP/1.1\r\n").append(host).append("\r\n").
					append(userAgent).append("\r\n").append(contentType).append("\r\n").append(contentlength).append(msgg.length()).append("\r\n").
					append(date).append(new Date()).append("\r\n");
					
					dos.writeUTF(sbpostreq.toString());
				} catch (IOException e1) {
					e1.getMessage();
				}
				textMessage.setText("");
			}
		});
		btnSend.setBounds(380, 328, 89, 40);
		frame.getContentPane().add(btnSend);
		
		textMessage = new JTextField();
		textMessage.setBounds(25, 320, 345, 57);
		frame.getContentPane().add(textMessage);
		textMessage.setColumns(10);
		
		JButton btnlogin = new JButton("Connect");
		btnlogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				/* Código para asegurarse de que el cliente que ya inició sesión no pueda volver a conectarse mediante el uso de la bandera booleana
				*/
				if(connected == true) {
					JOptionPane.showMessageDialog(null, "You are already connected !");
				}

				
				/* Solo si el cliente aún no ha iniciado sesión, podrá
				enviar solicitud de conexión al servidor */
				else if(connected == false) {
					
					clientname = textRegName.getText();
					
					/* Comprobando nombres de usuario de clientes incorrectos y aceptando solo nombres alfanuméricos */
					if(clientname.equals(null)||clientname.trim().isEmpty()||(!Pattern.matches(regex, clientname)))
					{
						JOptionPane.showMessageDialog(null, "Please enter an alphanumeric username to connect to server! ");
					}
					
					else {
						
						/* llamando al método para iniciar la conexión del cliente. */
						startClientConnection();
					}
				}
			}
		});
		btnlogin.setBounds(270, 60, 89, 23);
		frame.getContentPane().add(btnlogin);
		
		/* Implementación de la función LogOut del cliente haciendo clic en el botón LogOut */
		JButton btnlogout = new JButton("LOGOUT");
		btnlogout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					
					StringBuilder sbconnreq = new StringBuilder();
					
					/* Construir la solicitud HTTP para LogOut, es decir, cerrar la conexión */
					sbconnreq.append("GET /").append(" ").append("/ HTTP/1.1\r\n").append(host).append("\r\n").
					append(userAgent).append("\r\n").append(contentType).append("\r\n").append(contentlength).append(clientname.length()).append("\r\n").
					append(date).append(new Date()).append("\r\n").append(connection).append("\r\n");
					
					dos.writeUTF(sbconnreq.toString());
					
					JOptionPane.showMessageDialog(null, "You have logged out.");
					
					/* Indicador booleano establecido en falso para indicar que este cliente puede iniciar sesión nuevamente */
					connected = false;
					
					clientsoc.close();
					
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnlogout.setBounds(177, 406, 89, 23);
		frame.getContentPane().add(btnlogout);
		
		textRegName = new JTextField();
		textRegName.setBounds(64, 61, 188, 20);
		frame.getContentPane().add(textRegName);
		textRegName.setColumns(10);
		
		/* Botón para registrar el nombre de usuario en el servidor */
		JLabel lblRegisterMsg = new JLabel("Register username with server");
		lblRegisterMsg.setBounds(65, 31, 195, 14);
		frame.getContentPane().add(lblRegisterMsg);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(25, 96, 428, 193);
		frame.getContentPane().add(scrollPane);
		
		chatArea = new JTextArea();
		scrollPane.setViewportView(chatArea);
		chatArea.setEditable(false);
		
	}
	/* Solicita la conexión al servidor creando un socket de flujo para el número de puerto del servidor: 9998 */
	private void startClientConnection() {
			
	//	StringBuilder sbreq = new StringBuilder();
		
		try {
			
			/* solicitud de conexión */
			clientsoc = new Socket(ip,port);
			
			/* Flujos de entrada y salida para envío y recepción de datos a través de sockets de cliente y servidor. */
			dis = new DataInputStream(clientsoc.getInputStream());	
			dos = new DataOutputStream(clientsoc.getOutputStream());
			
			StringBuilder sbconnreq = new StringBuilder();

			/* Construyendo la solicitud de conexión Http y pasando el nombre del cliente como cuerpo. Por lo tanto, el encabezado Http
			están codificados alrededor de los datos del nombre del cliente. */
			sbconnreq.append("POST /").append("{"+clientname+"}").append("/ HTTP/1.1\r\n").append(host).append("\r\n").
			append(userAgent).append("\r\n").append(contentType).append("\r\n").append(contentlength).append(clientname.length()).append("\r\n").
			append(date).append(new Date()).append("\r\n");
			
			dos.writeUTF(sbconnreq.toString());

			otherusers = new ArrayList<>(10);
			usertimestamp = new HashMap<>(10);
			JOptionPane.showMessageDialog(null, "You have logged in. You can start Chatting: " + clientname);
			connected = true;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
