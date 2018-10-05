/*
 * This file was created by Austin Tao on 9/20/2018.
 */

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Homepage 
{
	
	private String specials = "[!@#$%&*()+=|<>?{}\\[\\]~-]";
	
	private String 				userName;
	private JFrame 				frame;
	protected JTextField 		searchField;
	private JPanel 				HIGH_panel, 
								Playlist_Panel, 
								 PlaylistTitle, PlaylistOptions,
								Explore_Panel, 
								 TopPanel, _SearchPanel, _HistoryPanel, _ProfilePanel,
				  				LOW_panel, 
				   				 Description_Panel, 
				   				 Song_Panel, 
				   				  _SongBar, _SongButtons;
	private JButton 			createPlaylist_, removePlaylist_, 
								searchQuery_, logout_, 
								previousSong_, playPause_, nextSong_
								//, addSong_, removeSong_					// add these to _SongButtons
								;	// addSong_ adds to a chosen playlist, removeSong_ removes the song from that playlist ONLY
	private JLabel 				playlist_, username_, 
								title_, artist_, album_,
								currentTime_, songTime_;
	private DefaultListModel 	dm;
	private JList				playlist_List;
	private JScrollPane			UserSavedPanel, ShiftingPanel; //ShiftingPanel is the big one
	private JSlider				timedSlider;
	
	private CreatePlaylistDialog playlistCreation;
	
	private boolean isSongPlaying = false, // starts false
					isThereASong = false;
	
	public static void main(String[] args) 
	{ // test
		new Homepage("allan");
	}
	
	public Homepage(String user) 
	{								// main constructor
		userName = user; 										// takes the username from input
		initialize();
	}
	
	private void initialize() 
	{ 									// initialization starts here
		frame = new JFrame("MusicService -- " + userName);
		frame.setMinimumSize(new Dimension(675,400));		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		addHighComponentsToHome(frame.getContentPane()); 			// loads upper half of frame components into frame
		addLowComponentsToHome(frame.getContentPane()); 			// loads lower half of frame components into frame
		
		frame.pack(); 														// packs the frame together
		
		frame.setSize(new Dimension(1000,600)); 							// sets the size of the frame
		frame.setLocationRelativeTo(null); 									// sets frame location
		
		frame.setVisible(true); 											// make the frame visible
		
		playlistCreation = new CreatePlaylistDialog(frame, userName, dm); // create playlist creation dialog
		playlistCreation.pack(); 											// pack playlist creation dialog
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addHighComponentsToHome(Container pane) 
	{
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));		// set up pane layout
		pane.add(Box.createRigidArea(new Dimension(1,1)));			// add one rigid area to pane
		
		HIGH_panel = new JPanel(); 									// initialize HIGH_panel and set layout
		HIGH_panel.setLayout(new BoxLayout(HIGH_panel, BoxLayout.X_AXIS));
		
		Playlist_Panel = new JPanel(); 							// initialize Playlist_Panel and set layout and set max dimensions
		Playlist_Panel.setLayout(new BoxLayout(Playlist_Panel, BoxLayout.Y_AXIS));
		Playlist_Panel.setMaximumSize(new Dimension(200,10000));
		
		PlaylistTitle = new JPanel(); 								// initialize PlaylistTitle and set max dimensions
		PlaylistTitle.setMaximumSize(new Dimension(100,10));
		
		playlist_ = new JLabel("My Playlists"); 					// initialize playlist_ label
		playlist_.setFont(new Font("Tahoma", Font.PLAIN, 15)); 		// set font for playlist_
		PlaylistTitle.add(playlist_);  								// add playlist_ to PlaylistTitle
					
		dm = new DefaultListModel(); 								// initialize dm and playlist_List (using dm)
		playlist_List = new JList(dm);
		playlist_List.setFont(new Font("Tahoma", Font.PLAIN, 14));		// set font for playlist_List
		getPlaylists(dm);												// update list gui with playlist information from json
		
		UserSavedPanel = new JScrollPane(playlist_List); 				// initialize UserSavedPanel using playlist_List
		
	
		PlaylistOptions = new JPanel(); 								// initialize PlaylistOptions and set layout
		PlaylistOptions.setLayout(new BoxLayout(PlaylistOptions, BoxLayout.X_AXIS));

		createPlaylist_ = new JButton("Create"); 						// initialize createPlaylist_ and assign action listener
		createPlaylist_.addActionListener(new ActionListener() 
		{
			@Override public void actionPerformed(ActionEvent e) 
			{
				System.out.println("Opening subwindow for playlist naming...");				
				playlistCreation.setLocationRelativeTo(frame); 				// show playlist creation dialog
				playlistCreation.setVisible(true); 							// set dialog visibility to true
			}	
		});
		
		removePlaylist_ = new JButton("Remove"); 				// initialize removePlaylist_ and assign action listener
		removePlaylist_.addActionListener(new ActionListener() 
		{
			@Override public void actionPerformed(ActionEvent e) 
			{
				System.out.println("Displaying options for which playlist to delete...");	
				
				// the premise would be that the list of options would be based on the user's personal playlists				
				
				Object[] possibilities = dm.toArray(); 				// each playlist name would be listed here
				String rp = (String) JOptionPane.showInputDialog(frame, "Which playlist are you removing?\n"
						+ "(There's no going back once you do.)", "Playlist Removal", 
						JOptionPane.PLAIN_MESSAGE, null, possibilities, null);
				if (rp == null) 
				{									// the dialog was exited one way or another
					System.out.println("No playlist was removed in the end..."); 
				} 
				else 
				{
					removePlaylist(rp, userName);			// the variable rp would represent the playlist name to be deleted
					getPlaylists(dm); 						// update the model (and thus the gui) afterwards
					System.out.println("The playlist --" + rp + "-- was removed."); // system announcement
				}	
			}
		});
		
		PlaylistOptions.add(Box.createRigidArea(new Dimension(6,6))); 	// set the layout with rigid areas
		PlaylistOptions.add(createPlaylist_);							// and jbuttons for PlaylistOptions
		PlaylistOptions.add(Box.createRigidArea(new Dimension(5,5)));
		PlaylistOptions.add(removePlaylist_);
		PlaylistOptions.add(Box.createRigidArea(new Dimension(10,10)));
		
		Playlist_Panel.add(Box.createRigidArea(new Dimension(3,3))); 	// set the layout with rigid areas
		Playlist_Panel.add(PlaylistTitle);								// and jpanels to Playlist_Panel
		Playlist_Panel.add(Box.createRigidArea(new Dimension(4,4)));
		Playlist_Panel.add(UserSavedPanel);
		Playlist_Panel.add(Box.createRigidArea(new Dimension(3,3)));
		Playlist_Panel.add(PlaylistOptions);
		Playlist_Panel.add(Box.createRigidArea(new Dimension(3,3)));
		
		Playlist_Panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED)); 
		// set border type for Playlist_Panel
		
//		large song album cover (e_CoverPanel) can go here later
		
//		Explore_Panel
		Explore_Panel = new JPanel(); 									// initialize Explore_Panel and set layout
		Explore_Panel.setLayout(new BoxLayout(Explore_Panel, BoxLayout.Y_AXIS));
		
		TopPanel = new JPanel(); 										// initialize TopPanel and set layout
		TopPanel.setLayout(new BoxLayout(TopPanel, BoxLayout.X_AXIS));
		
		_SearchPanel = new JPanel(); 									// initialize _SearchPanel and set layout
		_SearchPanel.setLayout(new FlowLayout());
		searchField = new JTextField(); 								// initialize searchField and searchQuery_
		searchField.setText("Search for...	");
		searchField.setColumns(15);
		searchQuery_ = new JButton("Search");
		
		// searchQuery_ gets assigned an action listener. However...
		
		//CONTAINS CODE THAT IS DEBUG ONLY; DOES NOT REFLECT FINAL PRODUCT
		searchQuery_.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				// TODO Auto-generated method stub
				
				//DEBUG ONLY; DOES NOT REFLECT FINAL PRODUCT
				isThereASong = !isThereASong; 								//
				System.out.println("is there a song? " + isThereASong);		//
				if (isThereASong) 
				{											//
					// addSong_
					previousSong_.setText("\u2758" + "\u23F4");				//
					playPause_.setText("\u2758" + "\u2758");				//
					nextSong_.setText("\u23F5" + "\u2758");					//
					// removeSong_
				} 
				else 
				{													//
					// addSong_
					previousSong_.setText("\u274C");						//
					playPause_.setText("\u274C");							//
					nextSong_.setText("\u274C");							//
					// removeSong_
				}															//
				// addSong_
				previousSong_.setEnabled(isThereASong);						//
				playPause_.setEnabled(isThereASong);						//
				nextSong_.setEnabled(isThereASong);							//
				// removeSong_
				currentTime_.setVisible(isThereASong);						//
				timedSlider.setEnabled(isThereASong);						//
				songTime_.setVisible(isThereASong);							//
				//DEBUG ONLY; DOES NOT REFLECT FINAL PRODUCT
			}	
		});
		//CONTAINS CODE THAT IS DEBUG ONLY; DOES NOT REFLECT FINAL PRODUCT
		
		_SearchPanel.add(searchField);						// add searchField and searchQuery_ to _SearchPanel
		_SearchPanel.add(searchQuery_);							// and set max dimensions
		_SearchPanel.setMaximumSize(new Dimension(200,40));
		
//		_HistoryPanel (worry about this later)
		_HistoryPanel = new JPanel();						// initialize _HistoryPanel
		
		_ProfilePanel = new JPanel();						// initialize _ProfilePanel and set layout
		_ProfilePanel.setLayout(new FlowLayout());
		username_ = new JLabel("User: " + userName); 		// initialize username_ and logout_
		logout_ = new JButton("Logout");
		logout_.addActionListener(new ActionListener() 
		{ 	// assign action listener to logout_
			public void actionPerformed(ActionEvent e) 
			{
				frame.dispose();							// disposes of current frame
		        new Login().setVisible(true); 				// creates new Login() object
		        System.out.println("Logging out..."); 		// system announcement
			}	
		});
		
		_ProfilePanel.add(username_); 							// add username_ and logut_ to _Profile
		_ProfilePanel.add(logout_);	  							// and set max dimensions
		_ProfilePanel.setMaximumSize(new Dimension(100,40));
		
		TopPanel.add(_SearchPanel); 							// _SearchPanel and _ProfilePanel
//		TopPanel.add(Box.createHorizontalGlue());				// (and later _HistoryPanel) are
//		TopPanel.add(_HistoryPanel);							// added to TopPanel and gets set
		TopPanel.add(Box.createHorizontalGlue());				// a border
		TopPanel.add(_ProfilePanel);
		TopPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		
//		ShiftingPanel (the one that keeps changing)
		ShiftingPanel = new JScrollPane(); 						// initialize ShiftingPanel and set border
		ShiftingPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		
		Explore_Panel.add(TopPanel); 							// add TopPanel and ShiftingPanel to Explore_Panel
		Explore_Panel.add(ShiftingPanel);
		
		HIGH_panel.add(Box.createRigidArea(new Dimension(1,1))); 	// add rigid areas and panels
		HIGH_panel.add(Playlist_Panel);								// to HIGH_panel
		HIGH_panel.add(Box.createRigidArea(new Dimension(1,1)));
		HIGH_panel.add(Explore_Panel);
		
		pane.add(HIGH_panel); 								// add HIGH_panel to pane (the content pane for frame)
	}
	
	private void addLowComponentsToHome(Container pane) 
	{
		LOW_panel = new JPanel();							// initialize LOW_panel and set layout and max dimensions
		LOW_panel.setLayout(new BoxLayout(LOW_panel, BoxLayout.X_AXIS));
		LOW_panel.setMaximumSize(new Dimension(15000,100));
		LOW_panel.add(Box.createHorizontalStrut(4));	 	// add horizontal strut to LOW_panel
		
		// small song album cover (m_CoverPanel) can go here later
		
		Description_Panel = new JPanel(); 					// initialize Description_Panel and set layout
		Description_Panel.setLayout(new BoxLayout(Description_Panel, BoxLayout.Y_AXIS));
		
		title_ = new JLabel();								// initialize title_ and set max dimensions and text
		title_.setMaximumSize(new Dimension(150,20)); 
		title_.setText("Title: song title song title song title song title song title");
		
		artist_ = new JLabel(); 							// initialize artist_ and set max dimensions and text
		artist_.setMaximumSize(new Dimension(150,20)); 
		artist_.setText("Artist(s): list of artists");
		
		album_ = new JLabel(); 								// initialize album_ and set max dimensions and text
		album_.setMaximumSize(new Dimension(150,20)); 
		album_.setText("Album: song album");
		
		Description_Panel.add(Box.createRigidArea(new Dimension(1,1)));	// add rigid areas and labels
		Description_Panel.add(title_);									// to Description_Panel
		Description_Panel.add(Box.createRigidArea(new Dimension(1,1)));
		Description_Panel.add(artist_);
		Description_Panel.add(Box.createRigidArea(new Dimension(1,1)));
		Description_Panel.add(album_);
		Description_Panel.add(Box.createRigidArea(new Dimension(1,1)));
		
		Description_Panel.setMinimumSize(new Dimension(175,200));		// set minimum, preferred, and
		Description_Panel.setPreferredSize(new Dimension(175,60));		// maximum sizes for
		Description_Panel.setMaximumSize(new Dimension(175,200));		// Description_Panel
		
		Song_Panel = new JPanel();										// initialize Song_Panel and set layout
		Song_Panel.setLayout(new BoxLayout(Song_Panel, BoxLayout.Y_AXIS));

		_SongBar = new JPanel(); 										// initialize _SongBar and set layout
		_SongBar.setLayout(new FlowLayout());
		currentTime_ = new JLabel("current"); 							// initialize currentTime_, timedSlider, and songTime_
		timedSlider = new JSlider(0,100,0);
		songTime_ = new JLabel("total");
		// un-movable on initialization, movable when there is a song 'queued' (is that the word for it?)
		
		currentTime_.setVisible(isThereASong); 					// set visibilities of currentTime_ and songTime_ to off
		timedSlider.setEnabled(isThereASong);  						// timedSlider gets disabled
		songTime_.setVisible(isThereASong);
		
		_SongBar.add(currentTime_); 								// add labels and jslider to _SongBar
		_SongBar.add(timedSlider);
		_SongBar.add(songTime_);
							
		_SongButtons = new JPanel(); 								// initialize _SongButtons and set layout
		_SongButtons.setLayout(new FlowLayout());
		
//		addSong_ goes here
		
		previousSong_ = new JButton("\u2758" + "\u23F4"); 			// initialize previousSong_ and add an
		previousSong_.addActionListener(new ActionListener()
		{ 		// action listener to previousSong_
			@Override 
			public void actionPerformed(ActionEvent e) 
			{
				System.out.println("Moving to previous song...");
				// do things here
				
				playPause_.setText("\u2758" + "\u2758");
				isSongPlaying = true;								// the song is playing when it's all over
			}	
		});
		playPause_ = new JButton("\u274C");							// initialize playPause_ and add an 
		playPause_.addActionListener(new ActionListener() 
		{ 		// action listener to playPause_
			@Override 
			public void actionPerformed(ActionEvent arg0) 
			{
				isSongPlaying = !isSongPlaying;
				if (isSongPlaying) 
				{
					System.out.println("Song is playing");
					// do things here
					
					playPause_.setText("\u2758" + "\u2758");		
				} 
				else 
				{
					System.out.println("Song is paused");
					// do things here
					
					playPause_.setText("\u25B6");
				}	
			}
		});
		nextSong_ = new JButton("\u23F5" + "\u2758"); 				// initializes nextSong_ and add an
		nextSong_.addActionListener(new ActionListener() 
		{ 			// action listener to nextSong_
			@Override 
			public void actionPerformed(ActionEvent e) 
			{			
				System.out.println("Moving to next song...");
				//do things here
				
				playPause_.setText("\u2758" + "\u2758");
				isSongPlaying = true;							// the song will begin playing when it's all over			
			}	
		});
	
//		add removeSong_ here
		
		// all these buttons initialize as un-clickables
		// addSong_
		previousSong_.setEnabled(isThereASong);
		playPause_.setEnabled(isThereASong);
		nextSong_.setEnabled(isThereASong);
		// removeSong_
		// if there is a song, it will unlock and play automatically. but until then...
		
		_SongButtons.add(previousSong_); 						// add buttons to _SongButtons
		_SongButtons.add(playPause_); 	
		_SongButtons.add(nextSong_);
		
		Song_Panel.add(_SongBar); 								// add panels to Song_Panel
		Song_Panel.add(_SongButtons);
						
		LOW_panel.add(Box.createRigidArea(new Dimension(1,1))); // add rigid areas and panels
		LOW_panel.add(Description_Panel);						// to LOW_panel and set border
		LOW_panel.add(Box.createRigidArea(new Dimension(2,2)));
		LOW_panel.add(Song_Panel);
		LOW_panel.add(Box.createRigidArea(new Dimension(1,1)));
		
		LOW_panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));				
		
		pane.add(LOW_panel); // add LOW_panel to pane (the content pane of frame)
	}

		
	/** ORIGIN: Login.java
	 * A boolean method that abhors code injections.
	 * @param input: A string, either username or password
	 * @return false if any special characters are found in input; true otherwise
	 */
	private boolean codeDenial (String input) 
	{
		//denies if input has the following: "[!@#$%&*()_+=|<>?{}\\[\\]~-]" (code injection prevention)
		for (char c : input.toCharArray()) 
		{
			for (char s : specials.toCharArray())
				if (s == c) return false;
		} return true;
	}
	
	/**
	 * A void method that can make a frame visible.
	 * @param b boolean (true/false) that determines visibility of the frame (where true makes it visible, and false makes it not visible
	 */
	public void setVisible(boolean b) 
	{
		// TODO Auto-generated method stub
		frame.setVisible(b);
	}
	
	/** ORIGIN: Profile.java
	 * Read playlists array from json file and add to gui list
	 * @param dm defaultlistModel
	 */
	void getPlaylists(DefaultListModel dm) 
	{
		dm.clear(); //clear list 
		JSONObject obj1;
		String pathname = userName + ".json";
		try (InputStream input = new FileInputStream(pathname)) 
		{
			obj1 = new JSONObject(new JSONTokener(input));
		    //read playlists
		    String playlist = obj1.get("playlists").toString();		    
		    String [] playlistArray = playlist.substring(2, playlist.length() - 2).split("\",\"");
		    //add playlist to default list
		    for(int i = 0; i < playlistArray.length; i++) dm.addElement(playlistArray[i]);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	/** ORIGIN: Profile.java
	 * Remove playlist from JSON FiLE
	 * @param playlist playlist to be removed
	 * @param username current login user
	 */
	 void removePlaylist(String playlist, String username) 
	 {
		try (InputStream input = new FileInputStream(username+".json")) 
		{
		    JSONObject obj1 = new JSONObject(new JSONTokener(input));
		    
		    JSONArray currentList = obj1.getJSONArray("playlists");
		    currentList.remove(currentList.toList().indexOf(playlist)); 
		    obj1.remove(playlist); // also need to remove songs from playlist
		    
		    FileWriter fileWriter = new FileWriter(username+".json");
			fileWriter.write(obj1.toString());
			fileWriter.flush();
			fileWriter.close();
		    
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
}
