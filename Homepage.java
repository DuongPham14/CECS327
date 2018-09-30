/*
 * This file was created by Austin Tao on 9/20/2018.
 */

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;


public class Homepage {
	private String 				userName;
	private JFrame 				frame;
	protected JTextField 		searchField;
	private JPanel 				HIGH_panel, 
								Playlist_Panel, 
								 PlaylistTitle, /*UserSavedPanel,*/ PlaylistOptions,
								Explore_Panel, 
								 TopPanel, _SearchPanel, _HistoryPanel, _ProfilePanel,
//								 /*
								  ShiftingPanel,
//								  * */ //maybe?
				  				LOW_panel, 
				   				 Description_Panel, 
				   				 Song_Panel, SongBar, SongOptions, _SongTime, _SongButtons;
	private JButton 			createPlaylist_, removePlaylist_, 
								searchQuery_, logout_, 
								previousSong_, playPause_, nextSong_; 
	private JLabel 				playlist_, username_, 
								title_, artist_, album_,
								currentTime_, divide_, songTime_;
	private DefaultListModel 	dm;
	private JList				playlist_List;
	private JScrollPane			UserSavedPanel 
//							    , ShiftingPanel /*maybe*/ 
							    ;
	
	private boolean isSongPlaying = false; //starts false
	
	public static void main(String[] args) {
		new Homepage("allan");
	}
	
	public Homepage(String user) {
		userName = user;
		initialize();
	}
	
	public void initialize() {
		frame = new JFrame("MusicService -- " + userName);
		frame.setMinimumSize(new Dimension(675,400));		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		addHighComponentsToHome(frame.getContentPane());
		addLowComponentsToHome(frame.getContentPane());
		
		frame.pack(); 
		
		frame.setSize(new Dimension(1000,600)); 
		frame.setLocationRelativeTo(null);
		
		frame.setVisible(true);
	}
	
	public void addHighComponentsToHome(Container pane) {
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		
		HIGH_panel = new JPanel();
		HIGH_panel.setLayout(new BoxLayout(HIGH_panel, BoxLayout.X_AXIS));
		
		Playlist_Panel = new JPanel();
		Playlist_Panel.setLayout(new BoxLayout(Playlist_Panel, BoxLayout.Y_AXIS));
		Playlist_Panel.setMaximumSize(new Dimension(150,10000));
		
		PlaylistTitle = new JPanel();
		PlaylistTitle.setMaximumSize(new Dimension(100,10));
		playlist_ = new JLabel("My Playlists");
		PlaylistTitle.add(playlist_);
//		PlaylistTitle.setBorder(BorderFactory.createLineBorder(Color.GREEN));
		
//		inspiration for borders/box.glue: http://www.java2s.com/Code/Java/Swing-JFC/BoxLayoutGlueSample.htm
		
		UserSavedPanel = new JScrollPane();
		dm = new DefaultListModel();
		playlist_List = new JList();
		playlist_List.setModel(dm);
//		getPlaylist(dm);
		UserSavedPanel.add(playlist_List);
	
		PlaylistOptions = new JPanel();
		PlaylistOptions.setLayout(new BoxLayout(PlaylistOptions, BoxLayout.X_AXIS));

		createPlaylist_ = new JButton("Create");
		createPlaylist_.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				System.out.print("Open subwindow displaying instructions for name of new playlist...");
			}
		});
		removePlaylist_ = new JButton("Remove");
		removePlaylist_.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				System.out.println("Displaying options for which one to delete...");	
			}
		});
		
		PlaylistOptions.add(Box.createRigidArea(new Dimension(10,10)));
		PlaylistOptions.add(createPlaylist_);
		PlaylistOptions.add(Box.createRigidArea(new Dimension(5,5)));
		PlaylistOptions.add(removePlaylist_);
		PlaylistOptions.add(Box.createRigidArea(new Dimension(10,10)));
		
		Playlist_Panel.add(Box.createRigidArea(new Dimension(5,5)));
		Playlist_Panel.add(PlaylistTitle);
		Playlist_Panel.add(Box.createRigidArea(new Dimension(5,5)));
		Playlist_Panel.add(UserSavedPanel);
		Playlist_Panel.add(Box.createRigidArea(new Dimension(5,5)));
		Playlist_Panel.add(PlaylistOptions);
		Playlist_Panel.add(Box.createRigidArea(new Dimension(5,5)));
		
//		Playlist_Panel.setBorder(BorderFactory.createLineBorder(Color.RED));
				
//		large song album cover (e_CoverPanel) can go here later
		
//		Explore_Panel
		Explore_Panel = new JPanel();
		Explore_Panel.setLayout(new BoxLayout(Explore_Panel, BoxLayout.Y_AXIS));
		
		TopPanel = new JPanel();
		TopPanel.setLayout(new BoxLayout(TopPanel, BoxLayout.X_AXIS));
		
		_SearchPanel = new JPanel();
		_SearchPanel.setLayout(new FlowLayout());
		searchField = new JTextField();
		searchField.setText("Search for...	");
		searchField.setSize(searchField.getPreferredSize()); 
		searchQuery_ = new JButton("Search");
		
		_SearchPanel.add(searchField);
		_SearchPanel.add(searchQuery_);
		_SearchPanel.setMaximumSize(new Dimension(200,40));
//		_SearchPanel.setBorder(BorderFactory.createLineBorder(Color.GREEN));
		
//		_HistoryPanel (worry about this later)
		_HistoryPanel = new JPanel();
		
		_ProfilePanel = new JPanel();
		_ProfilePanel.setLayout(new FlowLayout());
		username_ = new JLabel("User: " + userName);
		logout_ = new JButton("Logout");
		logout_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
		        new Login().setVisible(true); 
		        System.out.println("Logging out...");
			}
		});
		
		_ProfilePanel.add(username_);
		_ProfilePanel.add(logout_);
		_ProfilePanel.setMaximumSize(new Dimension(100,40));
//		_ProfilePanel.setBorder(BorderFactory.createLineBorder(Color.MAGENTA));
		
		TopPanel.add(_SearchPanel);
//		a horizontal box, then _HistoryPanel would go here
		TopPanel.add(Box.createHorizontalGlue());
		TopPanel.add(_ProfilePanel);
//		TopPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
//		ShiftingPanel (the one that keeps changing) //jscrollpane?
		ShiftingPanel = new JPanel();
		ShiftingPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		Explore_Panel.add(TopPanel); Explore_Panel.add(ShiftingPanel);
		
		HIGH_panel.add(Box.createRigidArea(new Dimension(5,5)));
		HIGH_panel.add(Playlist_Panel);
		HIGH_panel.add(Box.createRigidArea(new Dimension(5,5)));
		HIGH_panel.add(Explore_Panel);
//		HIGH_panel.add(Box.createRigidArea(new Dimension(5,5)));
		
		pane.add(HIGH_panel);
	}
	
	public void addLowComponentsToHome(Container pane) {
		LOW_panel = new JPanel();
		LOW_panel.setLayout(new BoxLayout(LOW_panel, BoxLayout.X_AXIS));
		LOW_panel.setMaximumSize(new Dimension(15000,300));
		LOW_panel.add(Box.createHorizontalStrut(4));
		
		// small song album cover (m_CoverPanel) can go here later
		
		Description_Panel = new JPanel();
		Description_Panel.setLayout(new BoxLayout(Description_Panel, BoxLayout.Y_AXIS));
		title_ = new JLabel(); title_.setMaximumSize(new Dimension(150,20)); title_.setText("Title: song title song title song title song title song title");
		artist_ = new JLabel(); artist_.setMaximumSize(new Dimension(150,20)); artist_.setText("Artist(s): list of artists");
		album_ = new JLabel(); album_.setMaximumSize(new Dimension(150,20));  album_.setText("Album: song album");
		
//		new Dimension(10,10)

		Description_Panel.add(Box.createRigidArea(new Dimension(1,1)));
		Description_Panel.add(title_);
		Description_Panel.add(Box.createRigidArea(new Dimension(1,1)));
		Description_Panel.add(artist_);
		Description_Panel.add(Box.createRigidArea(new Dimension(1,1)));
		Description_Panel.add(album_);
		Description_Panel.add(Box.createRigidArea(new Dimension(1,1)));
		
		Description_Panel.setMinimumSize(new Dimension(175,200));
		Description_Panel.setPreferredSize(new Dimension(175,60));
		Description_Panel.setMaximumSize(new Dimension(175,200));
		
//		Description_Panel.setBorder(BorderFactory.createLineBorder(Color.CYAN));
						
		
		Song_Panel = new JPanel();
		Song_Panel.setLayout(new BoxLayout(Song_Panel, BoxLayout.Y_AXIS));
		
//		SongBar goes here
		
		SongOptions = new JPanel();
		SongOptions.setLayout(new BoxLayout(SongOptions, BoxLayout.X_AXIS));
		
		_SongTime = new JPanel();
		_SongTime.setLayout(new FlowLayout());
		currentTime_ = new JLabel("current");
		divide_ = new JLabel(" /|/ ");
		songTime_ = new JLabel("Song");
//		currentTime_ responds to where the song is on SongBar, songTime_ responds to what the total time of the song is
		_SongTime.add(currentTime_);
		_SongTime.add(divide_);
		_SongTime.add(songTime_);
		
		_SongTime.setMaximumSize(new Dimension(20,50));
		
		_SongButtons = new JPanel();
		_SongButtons.setLayout(new FlowLayout());
		
		previousSong_ = new JButton("Previous Song");
		previousSong_.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				System.out.println("Moving to previous song...");
				// do things here
			}
		});
		playPause_ = new JButton("Paused");
		playPause_.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				isSongPlaying = !isSongPlaying;
				if (isSongPlaying) { 
					playPause_.setText("Playing");
					System.out.println("Song is playing");
					// do things here
				} else {
					playPause_.setText("Paused");
					System.out.println("Song is paused");
					// do things here
				}
			}
		});
		nextSong_ = new JButton("Next Song");
		nextSong_.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {			
				System.out.println("Moving to next song...");
				//do things here
			}
		});
	
		// all three buttons initialize as un-clickables 
		// 'if there is a song chosen,' it will unlock and play automatically
		_SongButtons.add(previousSong_); 
		_SongButtons.add(playPause_); 
		_SongButtons.add(nextSong_);
		
		SongOptions.add(_SongTime);
		SongOptions.add(_SongButtons);
		

		
//		SongBar gets added here
		Song_Panel.add(SongOptions);
		
				
		LOW_panel.add(Description_Panel);
		LOW_panel.add(Song_Panel);
		
		LOW_panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));				
		
		
		pane.add(LOW_panel);
	}

}
