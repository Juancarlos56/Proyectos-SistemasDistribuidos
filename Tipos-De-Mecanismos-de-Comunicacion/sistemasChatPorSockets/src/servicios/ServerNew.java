package servicios;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ServerNew {

	/* servidor escucha en este puerto- 9998 */
	private static int port = 9998;
	private static ServerSocket ss;
	private static Socket client;
	private static DataOutputStream dos;
	
	/* Arraylist para almacenar los nombres de usuario de los clientes en línea */
	private ArrayList<String> userNames;
	
	
	/* Arraylist para almacenar los flujos de salida de datos de los clientes en línea
	para que el servidor transmita mensajes */
	private ArrayList<DataOutputStream> streams;
	
	/* Estas 4 variables de lector / escritor de archivos utilizadas para el registro del servidor
	es decir, mantener los mensajes y mantener una copia de seguridad en caso de que el servidor
	apaga.*/
	private FileWriter fw;
	private BufferedWriter bw;
	private FileReader fr;
	private BufferedReader br;
	
	private JFrame frame;
	private static JTextArea textArea;
	private final static String host = "Host: localhost";
	private final static String userAgent = "User-Agent: MultiChat/2.0";
	private final static String contentType = "Content-Type: text/html";
	private final static String contentlength = "Content-Length: ";
	private final static String date = "Date: ";
	
	/* Botón para lista de usuarios en línea */
	private JButton btnOnlineUsers;
	private JButton btnClrScr;
	
	/**
	 * iniciar the application.
	 */
	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ServerNew window = new ServerNew();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
	}
	/**
	 * Create the application.
	 */
	public ServerNew() {
		initialize();
	}

	/**
	 * inicializar el contenido del frame del servidor
	 */
	private void initialize() {
		frame = new JFrame("Server Screen");
		frame.setBounds(500, 500, 501, 519);
	//	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		        	try {
						bw.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		            System.exit(0);
		    }
		});
		frame.getContentPane().setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 21, 464, 413);
		frame.getContentPane().add(scrollPane);
		
		textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		textArea.setEditable(false);
		
		btnOnlineUsers = new JButton("Online Users");

		/* Action Listener para implementar lo que sucede cuando hace clic en el botón "Usuarios en línea" */
		btnOnlineUsers.addActionListener(new ActionListener() {

			/* Muestra cualquier usuario disponible / en línea, más da un mensaje: No hay usuarios en línea */
			public void actionPerformed(ActionEvent arg0) {
				
				if(userNames.isEmpty()) {
					textArea.append("No User is online. \n");
				}
				
				else {
					textArea.append("Online users are: \n");
					for (String user : userNames) {
						textArea.append(user+"\n");
					}
				}
			}
		});
		btnOnlineUsers.setBounds(178, 446, 113, 23);
		frame.getContentPane().add(btnOnlineUsers);
		
		// - - - - 
		btnClrScr = new JButton("Clear Screen");

		/* Action Listener para implementar lo que sucede cuando hace clic en el botón "Usuarios en línea" */
		btnClrScr.addActionListener(new ActionListener() {
			
			/* Borra los mensajes de la pantalla */
			public void actionPerformed(ActionEvent arg0) {
				textArea.setText("");
			}
		});
		//btnClrScr.setBounds(178, 446, 113, 23);
		btnClrScr.setBounds(288, 446, 113, 23);
		frame.getContentPane().add(btnClrScr);
		//- - - - 
		
		
		/* Este único hilo está dedicado para iniciar y mantener la conexión del servidor.
		Llama al método startsServerConnection que realmente inicializa el servidor. */
		Thread t = new Thread () {
			
			@Override
			public void run() {
				startServerConnection();
			};
		};
		t.start();
	}
	

	/* Método para inicializar la conexión del servidor. */
	protected void startServerConnection() {

		try {
			ss = new ServerSocket(port);
			userNames = new ArrayList<>(12);
			streams = new ArrayList<>(12);
			textArea.append("-------SERVER STARTED------\n");
			
		//	String path = System.getProperty("user.dir");
		//	textArea.append(path);
			fr = new FileReader("ServerLog.txt");
			br = new BufferedReader(fr);
			
			String smh = "";
			while(!((smh = br.readLine())==null)) {
				
				textArea.append(smh);
				textArea.append("\n");
			}
			br.close();
			
			fw = new FileWriter("ServerLog.txt");
			bw = new BufferedWriter(fw);
			
			/* Este bucle se utiliza para escuchar las conexiones del cliente en el puerto del servidor */
			while(true) {
				

				/* El cliente se ha conectado al conector del servidor */
				client = ss.accept();
				
				dos = new DataOutputStream(client.getOutputStream());
				streams.add(dos);
				
				
				/* Creamos una instancia del controlador de este socket de cliente (que es una clase anidada dentro
						esta clase ServerNew java y pasar los parámetros: socket de cliente
						y el flujo de salida de datos de ese socket en el constructor de serverclienthandler */
				ServerClientHandler sch = new ServerClientHandler(client, dos);
				
				/* Iniciar el hilo para manejar esta sesión de cliente único */
				sch.start();
				
			}
			
		}
		
		/*IEn caso de que falte el archivo ServerLog.txt, esta aplicación no funcionará, ya que arrojará
		esta excepción */
		catch (FileNotFoundException fe) {
			textArea.append("No previous logs to fetch. \n");
		}
		catch (IOException e) {
			// TODO: handle exception
			e.getMessage();
		}
	}
	
	
	/* Método para DIFUSIÓN de mensajes de clientes y
	todos los eventos como inicio de sesión de cliente, cierre de sesión, etc.
	a todos los clientes en línea. */
	public void SendDataAllClients(String msg) {
		
		for (DataOutputStream dataOutputStream : streams) {
			try {
				dataOutputStream.writeUTF(msg);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
	
	
	/* Clase anidada usada para MultiThreading y Manejo de múltiples clientes al mismo tiempo. */
	public class ServerClientHandler extends Thread {
		
		private Socket csoc;
		private String cname;
		private DataInputStream diss;
		
		public ServerClientHandler(Socket client, DataOutputStream dosss) {
			// TODO Auto-generated constructor stub
			this.csoc = client;
			try {
				diss = new DataInputStream(csoc.getInputStream());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.getMessage();
			}
		}
		
		
		/* anulando el método de ejecución del hilo. este método se ejecuta tan pronto como enviamos el comando:
			thread.start (); */
		@Override
		public void run() {
			
			String line = "",msgin;
			String arr[];
			
			try {

				while(true) {
				
					line = diss.readUTF();
					
					arr = line.split("\n");
					
					
					/* Reconstruyendo el cuerpo del mensaje a partir del encabezado Http.
					Este código decodifica el cuerpo del mensaje Http. */
					if(arr[0].contains("POST")) {
						
						msgin = arr[0].split("/")[1];
						
						if(msgin.contains("{")) {
							cname = msgin.split("\\{")[1];
							cname = cname.replace(cname.substring(cname.length()-1),"");
							textArea.append(line);
							textArea.append("New Client connected: "+cname+"\n");
							SendDataAllClients("CONNECTED:"+cname);
							userNames.add(cname);
							bw.write(line);
							bw.write("New Client connected: "+cname);
							bw.newLine();
						}
						else {
							textArea.append(line);
							textArea.append(cname+": "+msgin+"\n");
							SendDataAllClients(cname+": "+msgin);
							bw.write(line);
							bw.write(cname+": "+msgin+"\n");
							bw.newLine();
						}
					}
					else {
							textArea.append(cname+" has LOGGED OUT\n");
							SendDataAllClients("LOGGEDOFF:"+cname);

							/* Eliminando el nombre de usuario del cliente de su memoria o lista de matrices */
							userNames.remove(cname);
							
							
							/* Eliminando el flujo de salida de datos del socket del cliente de su memoria o lista de matrices */
							//	streams.remove(dos);
							
							/* el flujo de entrada de datos del socket del cliente se establece en nulo para que
							no recibe ningún dato incluso después de que se haya cerrado la sesión del cliente. */
							this.diss = null;
							
							/* Registrando el evento de cierre de sesión en el archivo db / */
							bw.write(cname+" has LOGGED OUT\n");
							bw.newLine();
							break;
					}
					
				}
					
			} 
			
			/* En caso de que se desconecte la conexión del cliente, incluso si el cliente no presiona el botón LOGOUT,
			el servidor cerrará la conexión del cliente y cerrará la sesión */
			catch (IOException e) {

					textArea.append(cname+" has LOGGED OUT\n");
					SendDataAllClients("LOGGEDOFF:"+cname);
					
					/* Eliminando el nombre de usuario del cliente de su memoria o lista de matrices */
					userNames.remove(cname);

					/* Eliminando el flujo de salida de datos del socket del cliente de su memoria o lista de matrices */
					//	streams.remove(dos);
					try {
						/* escribiendo en el archivo para el registro del servidor */
						bw.write(cname+" has LOGGED OUT\n");
						bw.newLine();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}

	}
