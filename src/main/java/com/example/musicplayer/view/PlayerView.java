package com.example.musicplayer.view;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.example.musicplayer.controller.PlayerController;
import com.example.musicplayer.model.Playlist;
import com.example.musicplayer.model.Track;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;

public class PlayerView {
    private Playlist playlist;
    private PlayerController controller;
    private MediaPlayer mediaPlayer;

    // UI Components
    private ListView<Track> playlistView;
    private Label currentTrackLabel;
    private Label timeLabel;
    private Slider volumeSlider;
    private Slider progressSlider;
    private Slider speedSlider;
    private Button playButton;
    private Button pauseButton;
    private Button nextButton;
    private Button prevButton;
    private Button addTrackButton;
    private Button removeTrackButton;
    private Button savePlaylistButton;
    private Button loadPlaylistButton;
    private Button changeAlbumArtButton;
    private ToggleButton repeatButton;
    private ToggleButton shuffleButton;
    private ToggleButton favoriteButton;
    private Label nowPlayingLabel;
    private ProgressBar songProgressBar;
    private ImageView albumArt;
    private StackPane albumArtContainer;

    private boolean isRepeating = false;
    private boolean isShuffling = false;
    private boolean isPlaying = false;
    private Random random = new Random();
    private String customAlbumArtPath = null;

    public void show(Stage stage) {
        playlist = new Playlist();
        controller = new PlayerController(playlist);

        initializeUI();
        BorderPane mainLayout = createMainLayout();

        Scene scene = new Scene(mainLayout, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Modern Music Player");
        stage.setResizable(false);
        stage.show();
    }

    private void initializeUI() {
        // Control Buttons
        playButton = createIconButton("", "/icons/play.png", this::playTrack);
        pauseButton = createIconButton("", "/icons/pause.png", this::pauseTrack);
        nextButton = createIconButton("", "/icons/next.png", this::nextTrack);
        prevButton = createIconButton("", "/icons/prev.png", this::prevTrack);

        // Feature Buttons
        addTrackButton = createIconButton("Add", "/icons/add.png", () -> addTracks());
        removeTrackButton = createIconButton("Remove", "/icons/remove.png", () -> removeSelectedTrack());
        savePlaylistButton = createIconButton("Save", "/icons/save.png", () -> savePlaylist());
        loadPlaylistButton = createIconButton("Load", "/icons/load.png", () -> loadPlaylist());
        changeAlbumArtButton = createIconButton("Change Art", "/icons/art.png", () -> changeAlbumArt());

        // Mode Buttons
        repeatButton = new ToggleButton("🔂");
        repeatButton.setOnAction(e -> toggleRepeat());
        shuffleButton = new ToggleButton("🎲");
        shuffleButton.setOnAction(e -> toggleShuffle());
        favoriteButton = new ToggleButton("❤");
        favoriteButton.setOnAction(e -> toggleFavorite());

        // Labels
        currentTrackLabel = new Label("No track selected");
        currentTrackLabel.getStyleClass().add("current-track");
        timeLabel = new Label("00:00 / 00:00");
        timeLabel.getStyleClass().add("time-label");
        nowPlayingLabel = new Label("Now Playing");
        nowPlayingLabel.getStyleClass().add("now-playing");

        // Sliders
        volumeSlider = new Slider(0, 1, 0.7);
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                controller.setVolume(newVal.doubleValue()));

        speedSlider = new Slider(0.5, 2.0, 1.0);
        speedSlider.setMajorTickUnit(0.5);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                controller.setPlaybackRate(newVal.doubleValue()));

        progressSlider = new Slider();
        progressSlider.setDisable(true);
        songProgressBar = new ProgressBar(0);
        songProgressBar.setPrefWidth(Double.MAX_VALUE);

        // Album Art
        albumArt = new ImageView();
        albumArt.setFitWidth(300);
        albumArt.setFitHeight(300);
        albumArt.setPreserveRatio(true);
        albumArt.setSmooth(true);

        albumArtContainer = new StackPane(albumArt);
        albumArtContainer.setAlignment(Pos.CENTER);
        albumArtContainer.setPrefSize(320, 320);
        albumArtContainer.setStyle("-fx-background-color: #181818; -fx-background-radius: 10;");

        // Playlist
        playlistView = new ListView<>();
        playlistView.setPrefHeight(300);
        playlistView.setCellFactory(lv -> new ListCell<Track>() {
            @Override
            protected void updateItem(Track track, boolean empty) {
                super.updateItem(track, empty);
                if (empty || track == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(track.toString());
                    if (track == playlist.getCurrentTrack() && isPlaying) {
                        setStyle("-fx-background-color: #1DB954; -fx-text-fill: white;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        playlistView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        playSelectedTrack(newVal);
                    }
                });

        setupMediaPlayer();
    }

    private BorderPane createMainLayout() {
        // Left Panel - Album Art and Controls
        VBox leftPanel = new VBox(20);
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setPadding(new Insets(20));
        leftPanel.setPrefWidth(350);

        // Album Art Section
        VBox albumSection = new VBox(10, albumArtContainer, changeAlbumArtButton);
        albumSection.setAlignment(Pos.CENTER);

        // Control Panel
        HBox controlPanel = new HBox(15,
                prevButton, playButton, pauseButton, nextButton);
        controlPanel.setAlignment(Pos.CENTER);

        // Mode Panel
        HBox modePanel = new HBox(10,
                shuffleButton, repeatButton, favoriteButton);
        modePanel.setAlignment(Pos.CENTER);

        leftPanel.getChildren().addAll(
                albumSection,
                nowPlayingLabel,
                currentTrackLabel,
                controlPanel,
                modePanel
        );

        // Right Panel - Playlist and Settings
        VBox rightPanel = new VBox(15);
        rightPanel.setAlignment(Pos.TOP_CENTER);
        rightPanel.setPadding(new Insets(20));
        rightPanel.setPrefWidth(600);

        // Playlist Controls
        HBox playlistControls = new HBox(10,
                addTrackButton, removeTrackButton, savePlaylistButton, loadPlaylistButton);
        playlistControls.setAlignment(Pos.CENTER);

        // Volume and Speed Controls
        HBox settingsPanel = new HBox(20,
                new VBox(5, new Label("Volume:"), volumeSlider),
                new VBox(5, new Label("Speed:"), speedSlider));
        settingsPanel.setAlignment(Pos.CENTER);

        // Progress Section
        VBox progressSection = new VBox(10,
                new HBox(10, new Label("0:00"), progressSlider, timeLabel),
                songProgressBar);
        progressSection.setAlignment(Pos.CENTER);

        rightPanel.getChildren().addAll(
                new Label("Playlist"),
                playlistView,
                playlistControls,
                progressSection,
                settingsPanel
        );

        // Main Layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setLeft(leftPanel);
        mainLayout.setRight(rightPanel);
        mainLayout.setStyle("-fx-background-color: #121212;");

        return mainLayout;
    }

    private Button createIconButton(String text, String iconPath, Runnable action) {
        try {
            Image image = new Image(getClass().getResourceAsStream(iconPath));
            ImageView imageView = new ImageView(image);
            imageView.setFitHeight(20);
            imageView.setFitWidth(20);

            Button button = new Button(text, imageView);
            button.setOnAction(e -> action.run());
            button.getStyleClass().add("icon-button");
            return button;
        } catch (Exception ex) {
            Button button = new Button(text);
            button.setOnAction(e -> action.run());
            button.getStyleClass().add("icon-button");
            return button;
        }
    }

    private void setupMediaPlayer() {
        progressSlider.valueChangingProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && mediaPlayer != null) {
                mediaPlayer.seek(Duration.seconds(progressSlider.getValue()));
            }
        });

        progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null && !progressSlider.isValueChanging()) {
                songProgressBar.setProgress(newVal.doubleValue() / mediaPlayer.getTotalDuration().toSeconds());
            }
        });
    }

    // Остальные методы остаются без изменений
    private void playTrack() {
        if (playlist.getCurrentTrack() == null && !playlist.getTracks().isEmpty()) {
            playlist.setCurrentTrackIndex(0);
        }

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        Track currentTrack = playlist.getCurrentTrack();
        if (currentTrack != null) {
            Media media = new Media(currentTrack.getFilePath());
            mediaPlayer = new MediaPlayer(media);
            controller.setMediaPlayer(mediaPlayer);

            currentTrackLabel.setText(currentTrack.getTitle() + " - " + currentTrack.getArtist());
            loadAlbumArt(currentTrack);

            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (!progressSlider.isValueChanging()) {
                    progressSlider.setValue(newTime.toSeconds());
                }
                timeLabel.setText(
                        formatTime(newTime.toSeconds()) + " / " + formatTime(media.getDuration().toSeconds())
                );
                songProgressBar.setProgress(newTime.toSeconds() / media.getDuration().toSeconds());
            });

            mediaPlayer.setOnReady(() -> {
                progressSlider.setMax(media.getDuration().toSeconds());
                progressSlider.setDisable(false);
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                if (isRepeating) {
                    mediaPlayer.seek(Duration.ZERO);
                    mediaPlayer.play();
                } else {
                    nextTrack();
                }
            });

            mediaPlayer.setOnError(() -> {
                showAlert("Error", "Cannot play the selected track: " + mediaPlayer.getError().getMessage());
                currentTrackLabel.setText("Error playing track");
            });

            mediaPlayer.play();
            isPlaying = true;
            playlistView.refresh();
        }
    }

    // Остальные методы остаются без изменений
    private void pauseTrack() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            isPlaying = false;
            playlistView.refresh();
        }
    }

    private void nextTrack() {
        if (isShuffling) {
            int randomIndex = random.nextInt(playlist.getTracks().size());
            playlist.setCurrentTrackIndex(randomIndex);
        } else {
            playlist.nextTrack();
        }

        if (playlist.getCurrentTrack() != null) {
            playTrack();
            playlistView.getSelectionModel().select(playlist.getCurrentTrack());
            playlistView.scrollTo(playlist.getCurrentTrack());
        }
    }

    private void prevTrack() {
        if (isShuffling) {
            int randomIndex = random.nextInt(playlist.getTracks().size());
            playlist.setCurrentTrackIndex(randomIndex);
        } else {
            playlist.prevTrack();
        }

        if (playlist.getCurrentTrack() != null) {
            playTrack();
            playlistView.getSelectionModel().select(playlist.getCurrentTrack());
            playlistView.scrollTo(playlist.getCurrentTrack());
        }
    }

    private void playSelectedTrack(Track track) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        playlist.setCurrentTrackIndex(playlist.getTracks().indexOf(track));
        playTrack();
        playlistView.getSelectionModel().select(track);
        playlistView.scrollTo(track);
    }

    private void toggleRepeat() {
        isRepeating = !isRepeating;
        repeatButton.setSelected(isRepeating);
        repeatButton.setText(isRepeating ? "🔁" : "🔂");
    }

    private void toggleShuffle() {
        isShuffling = !isShuffling;
        shuffleButton.setSelected(isShuffling);
        if (isShuffling) {
            shufflePlaylist();
        }
    }

    private void toggleFavorite() {
        Track currentTrack = playlist.getCurrentTrack();
        if (currentTrack != null) {
            boolean isFavorite = favoriteButton.isSelected();
            favoriteButton.setText(isFavorite ? "❤" : "♡");
        }
    }

    private void changeAlbumArt() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Album Art");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            customAlbumArtPath = selectedFile.toURI().toString();
            albumArt.setImage(new Image(customAlbumArtPath));
            animateAlbumArt();
        }
    }

    private void addTracks() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Audio Files");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.flac"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);
        if (selectedFiles != null) {
            for (File file : selectedFiles) {
                try {
                    AudioFile audioFile = AudioFileIO.read(file);
                    Tag tag = audioFile.getTag();

                    String title = tag.getFirst(FieldKey.TITLE);
                    String artist = tag.getFirst(FieldKey.ARTIST);
                    int duration = audioFile.getAudioHeader().getTrackLength();

                    if (title == null || title.isEmpty()) {
                        title = file.getName().replaceFirst("[.][^.]+$", "");
                    }

                    if (artist == null || artist.isEmpty()) {
                        artist = "Unknown Artist";
                    }

                    Track track = new Track(title, artist, file.toURI().toString(), duration);
                    playlist.addTrack(track);
                } catch (Exception e) {
                    Track track = new Track(
                            file.getName(),
                            "Unknown Artist",
                            file.toURI().toString(),
                            0
                    );
                    playlist.addTrack(track);
                }
            }
            updatePlaylistView();
        }
    }

    private void removeSelectedTrack() {
        Track selectedTrack = playlistView.getSelectionModel().getSelectedItem();
        if (selectedTrack != null) {
            playlist.removeTrack(selectedTrack);
            updatePlaylistView();

            if (playlist.getTracks().isEmpty()) {
                currentTrackLabel.setText("No track selected");
                albumArt.setImage(null);
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                }
            } else {
                if (playlist.getCurrentTrack() == null && !playlist.getTracks().isEmpty()) {
                    playlist.setCurrentTrackIndex(0);
                    playSelectedTrack(playlist.getCurrentTrack());
                }
            }
        }
    }

    private void loadAlbumArt(Track track) {
        try {
            if (customAlbumArtPath != null) {
                albumArt.setImage(new Image(customAlbumArtPath));
                return;
            }

            File file = new File(new java.net.URI(track.getFilePath()));
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();

            if (tag.getFirstArtwork() != null) {
                Image albumImage = new Image(new java.io.ByteArrayInputStream(tag.getFirstArtwork().getBinaryData()));
                albumArt.setImage(albumImage);
                animateAlbumArt();
            } else {
                albumArt.setImage(new Image(getClass().getResourceAsStream("/icons/default_album.png")));
            }
        } catch (Exception e) {
            albumArt.setImage(new Image(getClass().getResourceAsStream("/icons/default_album.png")));
        }
    }

    private void animateAlbumArt() {
        FadeTransition fade = new FadeTransition(Duration.seconds(0.5), albumArt);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.seconds(0.5), albumArt);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1);
        scale.setToY(1);

        ParallelTransition transition = new ParallelTransition(fade, scale);
        transition.play();
    }

    private void shufflePlaylist() {
        List<Track> tracks = playlist.getTracks();
        for (int i = tracks.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Track temp = tracks.get(i);
            tracks.set(i, tracks.get(j));
            tracks.set(j, temp);
        }
        updatePlaylistView();
    }

    private String formatTime(double seconds) {
        int hours = (int) (seconds / 3600);
        int minutes = (int) ((seconds % 3600) / 60);
        int secs = (int) (seconds % 60);

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%02d:%02d", minutes, secs);
        }
    }

    private void updatePlaylistView() {
        playlistView.getItems().setAll(playlist.getTracks());
    }

    private void savePlaylist() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Directory to Save Playlist");
        File selectedDirectory = directoryChooser.showDialog(null);

        if (selectedDirectory != null) {
            try {
                String playlistName = "MyPlaylist_" + System.currentTimeMillis() + ".m3u";
                File playlistFile = new File(selectedDirectory, playlistName);

                try (PrintWriter writer = new PrintWriter(playlistFile)) {
                    for (Track track : playlist.getTracks()) {
                        writer.println(track.getFilePath());
                    }
                }

                showAlert("Success", "Playlist saved to:\n" + playlistFile.getAbsolutePath());
            } catch (Exception e) {
                showAlert("Error", "Failed to save playlist: " + e.getMessage());
            }
        }
    }

    private void loadPlaylist() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Playlist File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Playlist Files", "*.m3u", "*.pls"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                playlist.getTracks().clear();
                List<String> lines = Files.readAllLines(selectedFile.toPath());

                for (String line : lines) {
                    if (!line.trim().isEmpty() && !line.startsWith("#")) {
                        File audioFile = new File(line);
                        if (audioFile.exists()) {
                            try {
                                AudioFile audio = AudioFileIO.read(audioFile);
                                Tag tag = audio.getTag();

                                String title = tag.getFirst(FieldKey.TITLE);
                                String artist = tag.getFirst(FieldKey.ARTIST);
                                int duration = audio.getAudioHeader().getTrackLength();

                                if (title == null || title.isEmpty()) {
                                    title = audioFile.getName().replaceFirst("[.][^.]+$", "");
                                }

                                if (artist == null || artist.isEmpty()) {
                                    artist = "Unknown Artist";
                                }

                                Track track = new Track(title, artist, audioFile.toURI().toString(), duration);
                                playlist.addTrack(track);
                            } catch (Exception e) {
                                Track track = new Track(
                                        audioFile.getName(),
                                        "Unknown Artist",
                                        audioFile.toURI().toString(),
                                        0
                                );
                                playlist.addTrack(track);
                            }
                        }
                    }
                }

                updatePlaylistView();
                showAlert("Success", "Playlist loaded successfully!");
            } catch (Exception e) {
                showAlert("Error", "Failed to load playlist: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}