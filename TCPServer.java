import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This is the class that initializes the TomP2P middle-ware with 3 peers.
 * @author Duong Pham
 *
 */
public class TCPServer {
	static Method json;
	private static final Random RND = new Random(42L);
	static PeerDHT[] peers;
	final static int serverSocketAddress = 6777, clientSocketAddress = 6778, nrPeers = 3;
	final static String search = "search", song = "getSong";

	/**
	 * Runs Main to set up P2P, create the peers, initialize metadata, puts songs in peers, and connects to UDP Server.
	 * @param args - args
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		// create metadata to JSON indexes
		Metadata meta = new Metadata();

		PeerDHT master = null;
		final int port = 4001;

		try { // creating the P2P
			peers = createAndAttachPeersDHT(nrPeers, port);
			printPeers(peers);
			bootstrap(peers);
			master = peers[0];

			String [] dfs = {"METADATA.txt", "artistIndex.txt", "albumIndex.txt", "songIndex.txt"};

			// Put metadata and index into master peer
			for (String file : dfs) {
				String md = readFile(file);
				put (master, Number160.createHash(file), md);
			}

			/**
             *	String metaData = readFile("METADATA.txt");
             *	put(master, Number160.createHash("METADATA.txt"), metaData);
             *	String artistIndex = readFile("artistIndex.txt");
             *	put(master, Number160.createHash("artistIndex.txt"), artistIndex);
             *	String albumIndex = readFile("albumIndex.txt");
             *	put(master, Number160.createHash("albumIndex.txt"), albumIndex);
             *	String songIndex = readFile("songIndex.txt");
             *	put(master, Number160.createHash("songIndex.txt"), songIndex);
			 */

			Object index = get(peers[1], Number160.createHash(dfs[3]));
			//System.out.println(index);

			// Put song in index to peer randomly
			String [] songs = index.toString().split("\n");
			for (int i = 1; i < songs.length; i++) {
				String [] songArray = songs[i].split(";");

				// add to metadata file
				meta.append(dfs[3], songArray);

				// get song wav file name
				String songName = songArray[0]+"_"+songArray[1]+"_"+songArray[2]+".wav";

				// get bytes of song from file
				byte [] songByte = getSong(songName);

				// random peer
				PeerDHT peer = peers[i%3];

				// args: random peer, guid from index file, song bytes
				put(peer,new Number160(songArray[3].trim()),songByte);
				peer.storageLayer().updateResponsibilities(new Number160(songArray[3].trim()), peer.peerID());
				
				// add duplicate songs into different peers
				PeerDHT peer2 = peers[(i+1)%3];
				put(peer2,new Number160(songArray[3].trim()),songByte);
				peer2.storageLayer().updateResponsibilities(new Number160(songArray[3].trim()), peer2.peerID());
			}
			
			MapReduce mr = new MapReduce();
			
			mr.runMapReduce(peers ,"METADATA.txt");
		} 
		catch (Exception e) {
			e.printStackTrace();
		}

		json = new Method();
		final ServerSocket serverSocket = new ServerSocket(serverSocketAddress);

		try	{ // setting up the listener thread
			//aSocket = new DatagramSocket(6733);
			System.out.println("Server started on port: " + serverSocketAddress);

			while (true) {
				Socket socket = serverSocket.accept();
				//System.out.println("Waiting for request...");
				ObjectInputStream request = new ObjectInputStream(socket.getInputStream());

				System.out.println("Request from Port: "+ socket.getPort());
				String stringRequest = (String) request.readObject();
				System.out.println("Request: "+stringRequest.toString());

				new Thread(new Runnable() {
					@Override public void run() {
						PeerDHT[] myPeers = peers;
						JSONObject JsonRequest=new JSONObject(stringRequest);

						//Request Data
						//@SuppressWarnings("unused")
						int ID = JsonRequest.getInt("id");
						String method = JsonRequest.get("method").toString();
						String param = JsonRequest.get("arguments").toString();
						String [] arguments = param.substring(2, param.length() - 2).split("\",\"");

						//Printing out request
						System.out.println("ID: "+ID);
						System.out.println("Method: "+method);
						System.out.println("Arguments: "+param);

						Class<?>[] argTypes = new Class<?>[arguments.length];
						for(int i = 0; i < arguments.length; i++) {
							argTypes[i] = String.class;
						}
						Object result = null;

						//Method call
						try {						
							//result = json.getClass().getMethod(method,argTypes).invoke(json, arguments);

							if (method.equals(song)) {
								// get song in bytes & transfer it over to UDP server
								String requestSong = arguments[0];
								Number160 guid = Number160.createHash(requestSong);
								byte [] songByte = (byte[]) get(myPeers[2],guid);
								result = songByte;
								//result = getSong(arguments[0]);

							} else if (method.equals(search)){
								String filter = arguments[0];
								String index = arguments[1];
								String[] result1 = meta.search(filter, index);
								result = result1;

							} else {
								//search here
								result = 0;
							}
							System.out.println(result.toString());
						}
						catch (Exception e) {
							e.printStackTrace();
						}

						byte[] rep = null;
						String[] rep1 = null;

						if (method.equals(song)) {
							rep = (byte[]) result;
						}

						if (method.equals(search)) {
							rep1 = (String[]) result;
						}

						//Reply
						try {
							Socket clientSocket  = new Socket("localhost", clientSocketAddress);

							if (method.equals(song)) {
								DataOutputStream reply = new DataOutputStream(clientSocket.getOutputStream());
								reply.writeInt(rep.length);
								reply.write(rep);
								reply.close();
								
								request.close();
								socket.close();
								clientSocket.close();
							}
							if (method.equals(search)) {
								ObjectOutputStream reply = new ObjectOutputStream(clientSocket.getOutputStream());
								reply.writeObject(rep1);								
								reply.close();
								
								request.close();
								socket.close();
								clientSocket.close();
							}	
						} 
						catch (JSONException e1) {
							e1.printStackTrace();
						}
						catch (IOException e) {
							e.printStackTrace();
						}
					}
				}).start(); 
			}
		}
		catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		}
		catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		}
		finally {
			if (serverSocket != null) 
				serverSocket.close();
		}
	}

	/**
	 * Print out a list containing the address of each peer.
	 * @param peers - array of peers
	 */
	public static void printPeers(PeerDHT[] peers) {
		for(int i = 0; i<peers.length; i++) {
			System.out.println("Peer " + peers[i].peerAddress());
		}
	}

	/**
	 * Create peers with a port and attach it to the first peer in the array.
	 * @param nr - The number of peers to be created
	 * @param port - The port that all the peer listens to. The multiplexing is done via the peer Id
	 * @return The created peers
	 * @throws IOException IOException
	 */
	public static PeerDHT[] createAndAttachPeersDHT( int nr, int port ) throws IOException {
		PeerDHT[] peers = new PeerDHT[nr];
		for (int i = 0; i < nr; i++) {
			if (i == 0) {
				peers[0] = new PeerBuilderDHT(new PeerBuilder( new Number160( i+1 ) ).ports( port ).start()).start();
			} else {
				peers[i] = new PeerBuilderDHT(new PeerBuilder( new Number160( i+1 ) ).masterPeer( peers[0].peer() ).ports(port+i).start()).start();
			}
		}
		return peers;
	}

	/**
	 * Bootstraps peers to the first peer in the array.
	 * @param peers The peers that should be bootstrapped
	 */
	public static void bootstrap( PeerDHT[] peers ) {
		//make perfect bootstrap, the regular can take a while
		for (int i = 0; i < peers.length; i++) {
			for (int j = 0; j < peers.length; j++) {
				peers[i].peerBean().peerMap().peerFound(peers[j].peerAddress(), null, null, null);
			}
		}
	}

	/**
	 * Puts the song byte into peers by mapping it with the song's guid.
	 * @param peer - peer to put song into
	 * @param guid - guid of song
	 * @param data - data of song
	 * @throws IOException
	 */
	private static void put(final PeerDHT peer, final Number160 guid, Object data) throws IOException {
		FuturePut futurePut = peer.put(guid).object(data).start();
		futurePut.awaitUninterruptibly();
		System.out.println("peer " + peer.peerID() + " stored [key: " + guid + ", value: "+ new Data (data));
	}

	/**
	 * Get data of song by using song guid get it in peer.
	 * @param peer - peer to get song from 
	 * @param guid - guid of song
	 * @return data of song
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private static Object get(final PeerDHT peer, final Number160 guid) throws ClassNotFoundException, IOException {
		FutureGet futureGet = peer.get(guid).start();
		futureGet.awaitUninterruptibly();
		System.out.println("peer " + peer.peerID() + " got: \"" + futureGet.data() + "\" for the key " + guid);
		return futureGet.data().object();
	}

	/**
	 * Get song in byte form.
	 * @param song - song to change into byte form
	 * @return song bytes
	 * @throws JSONException
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] getSong(String song) throws JSONException, UnsupportedEncodingException {
		File file = new File(song);
		int size = (int) file.length();
		byte[] bytes = new byte[size];

		try {
			BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
			buf.read(bytes, 0, bytes.length);
			buf.close();
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}

		return bytes;
	}

	/**
	 * Read the contents of a file.
	 * @param filename - file to be read
	 * @return string of content of file
	 * @throws IOException
	 */
	public static String readFile(String filename) throws IOException {
		String content = null;
		File file = new File(filename); 
		FileReader reader = null;

		try {
			reader = new FileReader(file);
			char[] chars = new char[(int) file.length()];
			reader.read(chars);
			content = new String(chars);
			reader.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
		finally {
			if (reader != null){
				reader.close();
			}
		}

		return content;
	}

	/**
	 * Change string to Hex.
	 * @param arg - string
	 * @return hex of arg string
	 */
	public static String toHex(String arg) {
		return String.format("%x", new BigInteger(1, arg.getBytes(/*YOUR_CHARSET?*/)));
	}

}
