// NeuralDesk — AI Chatbot Desktop App (by Michael Semera)
// Tech: Java, JavaFX, SQLite, lightweight NLP (lexicon + OpenNLP optional)
// Project is split by headers; copy each into separate files under src/main/java

/* === File: build.gradle === */
// Place this at the project root
plugins {
    id 'application'
    id 'java'
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.xerial:sqlite-jdbc:3.42.0.0'
    implementation 'org.apache.opennlp:opennlp-tools:2.3.0' // optional
    implementation 'org.openjfx:javafx-controls:20.0.1'
    implementation 'org.openjfx:javafx-fxml:20.0.1'
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.0'
}

application {
    mainClass = 'com.michaelsemera.neuraldesk.MainApp'
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

/* === File: settings.gradle === */
rootProject.name = 'NeuralDesk'

/* === File: src/main/java/com/michaelsemera/neuraldesk/MainApp.java === */
package com.michaelsemera.neuraldesk;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/chat.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("NeuralDesk — Smart Desktop Assistant");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        stage.setScene(scene);
        stage.setMinWidth(700);
        stage.setMinHeight(500);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

/* === File: src/main/resources/fxml/chat.fxml === */
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.geometry.Insets?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>
<BorderPane xmlns:fx="http://javafx.com/fxml/1" fx:controller="com.michaelsemera.neuraldesk.ui.ChatController">
    <top>
        <HBox spacing="10" style="-fx-padding:10; -fx-background-color: #1f2937;">
            <Label text="NeuralDesk" style="-fx-text-fill: white; -fx-font-size:16; -fx-font-weight:bold;"/>
            <Region HBox.hgrow="ALWAYS"/>
            <Button text="FAQ" onAction="#onOpenFAQ"/>
            <Button text="Train" onAction="#onOpenTrainer"/>
        </HBox>
    </top>

    <center>
        <VBox spacing="8" style="-fx-padding:12;">
            <ScrollPane fx:id="scrollPane" fitToWidth="true" vbarPolicy="AS_NEEDED">
                <VBox fx:id="messagesBox" spacing="6" style="-fx-padding:8;" />
            </ScrollPane>
        </VBox>
    </center>

    <bottom>
        <HBox spacing="8" style="-fx-padding:10; -fx-background-color:#f3f4f6;">
            <TextField fx:id="inputField" HBox.hgrow="ALWAYS" promptText="Type a message..." onAction="#onSendMessage" />
            <Button text="Send" onAction="#onSendMessage" />
        </HBox>
    </bottom>
</BorderPane>

/* === File: src/main/java/com/michaelsemera/neuraldesk/ui/ChatController.java === */
package com.michaelsemera.neuraldesk.ui;

import com.michaelsemera.neuraldesk.db.Database;
import com.michaelsemera.neuraldesk.model.Message;
import com.michaelsemera.neuraldesk.nlp.NLPService;
import com.michaelsemera.neuraldesk.util.FAQLoader;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChatController {
    @FXML private VBox messagesBox;
    @FXML private TextField inputField;
    @FXML private ScrollPane scrollPane;

    private final NLPService nlp = NLPService.getInstance();
    private final Database db = Database.getInstance();

    public void initialize() {
        // Load last 20 messages from DB
        List<Message> history = db.loadRecentMessages(20);
        history.forEach(m -> addMessageBubble(m));
        scrollToBottomLater();

        // Preload FAQs into NLP service (offline mode)
        FAQLoader.loadDefaultFaqs().forEach((q,a)-> nlp.addFAQ(q,a));
    }

    @FXML
    private void onSendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        inputField.clear();
        Message userMsg = new Message("user", text);
        db.saveMessage(userMsg);
        addMessageBubble(userMsg);

        // Process in background
        CompletableFuture.supplyAsync(() -> nlp.generateResponse(text))
                .thenAccept(resp -> Platform.runLater(() -> {
                    Message bot = new Message("bot", resp.getText(), resp.getSentiment());
                    db.saveMessage(bot);
                    addMessageBubble(bot);
                    scrollToBottomLater();
                }));
    }

    private void addMessageBubble(Message m) {
        VBox box = new VBox();
        box.setPadding(new Insets(8));
        Label who = new Label(m.getSender().equals("user")?"You":"NeuralDesk");
        who.setStyle("-fx-font-weight:bold;");
        Text txt = new Text(m.getText());
        Label sentiment = new Label(m.getSentiment() != null ? "Sentiment: " + m.getSentiment() : "");
        sentiment.setStyle("-fx-font-size:9; -fx-text-fill:#6b7280");
        box.getChildren().addAll(who, txt, sentiment);
        box.setBackground(new Background(new BackgroundFill(
                m.getSender().equals("user") ? Color.web("#e0f2fe") : Color.web("#eef2ff"), CornerRadii.EMPTY, Insets.EMPTY
        )));
        box.setMaxWidth(messagesBox.getWidth() - 20);
        messagesBox.getChildren().add(box);
    }

    private void scrollToBottomLater() {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    @FXML
    private void onOpenFAQ() {
        // Show small modal with FAQs — for brevity, print to console
        nlp.getFAQs().forEach((q,a)-> System.out.println("FAQ: " + q + " => " + a));
    }

    @FXML
    private void onOpenTrainer() {
        // For portfolio demo this opens a simple trainer in console
        System.out.println("Trainer opened — use CLI tools to add Q/A pairs or load CSV via util.Trainer");
    }
}

/* === File: src/main/java/com/michaelsemera/neuraldesk/nlp/NLPService.java === */
package com.michaelsemera.neuraldesk.nlp;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import java.io.InputStream;
import java.util.*;

/**
 * NLPService — singleton
 * Responsibilities:
 *  - lightweight sentiment analysis (lexicon based)
 *  - faq offline matching (exact/substring)
 *  - optional OpenNLP sentence detection for future extension
 */
public class NLPService {
    private static NLPService instance;
    private final Map<String,String> faqs = new LinkedHashMap<>();
    private final Set<String> positive = new HashSet<>();
    private final Set<String> negative = new HashSet<>();

    private NLPService() {
        buildLexicon();
        // load opennlp sentence model optionally
        try (InputStream in = getClass().getResourceAsStream("/models/en-sent.bin")){
            if (in != null) {
                SentenceModel sm = new SentenceModel(in);
                SentenceDetectorME s = new SentenceDetectorME(sm);
                // not used now, but reserved for later improvements
            }
        } catch (Exception ignored) {}
    }

    public static synchronized NLPService getInstance() {
        if (instance == null) instance = new NLPService();
        return instance;
    }

    private void buildLexicon() {
        String[] pos = {"good","great","love","happy","awesome","fantastic","nice","excellent","best","amazing"};
        String[] neg = {"bad","sad","hate","terrible","awful","worst","angry","disappointed","poor","problem"};
        positive.addAll(Arrays.asList(pos));
        negative.addAll(Arrays.asList(neg));
    }

    public synchronized void addFAQ(String q, String a) { faqs.put(q.toLowerCase(), a); }
    public synchronized Map<String,String> getFAQs() { return Collections.unmodifiableMap(faqs); }

    public Response generateResponse(String userText) {
        userText = userText.trim();
        // 1) Check FAQ exact match or substring
        String ql = userText.toLowerCase();
        for (String q : faqs.keySet()) {
            if (ql.equals(q) || ql.contains(q) || q.contains(ql)) {
                return new Response(faqs.get(q), analyzeSentiment(userText));
            }
        }
        // 2) Fallback: simple echo + sentiment
        String reply = generateFallbackReply(userText);
        return new Response(reply, analyzeSentiment(userText));
    }

    private String generateFallbackReply(String userText) {
        // Very simple heuristic — in a production app you'd call a model or backend API
        if (userText.endsWith("?")) return "Good question — I'll look into that for you.";
        if (userText.toLowerCase().contains("price") || userText.toLowerCase().contains("cost")) return "If you give me the product name I can check availability and price in the catalog.";
        return "Thanks for that. Can you tell me more or ask a specific question?";
    }

    private String analyzeSentiment(String text) {
        int score = 0;
        String[] tokens = text.toLowerCase().split("\\W+");
        for (String t : tokens) {
            if (positive.contains(t)) score++;
            if (negative.contains(t)) score--;
        }
        if (score > 0) return "positive";
        if (score < 0) return "negative";
        return "neutral";
    }

    public static class Response {
        private final String text;
        private final String sentiment;
        public Response(String text, String sentiment) { this.text = text; this.sentiment = sentiment; }
        public String getText() { return text; }
        public String getSentiment() { return sentiment; }
    }
}

/* === File: src/main/java/com/michaelsemera/neuraldesk/db/Database.java === */
package com.michaelsemera.neuraldesk.db;

import com.michaelsemera.neuraldesk.model.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal SQLite helper. Stores chat messages and allows app history persistence.
 */
public class Database {
    private static Database instance;
    private final Connection conn;

    private Database() throws SQLException {
        String url = "jdbc:sqlite:neuraldesk.db";
        conn = DriverManager.getConnection(url);
        createTables();
    }

    public static synchronized Database getInstance() {
        if (instance == null) {
            try { instance = new Database(); } catch (SQLException e) { throw new RuntimeException(e); }
        }
        return instance;
    }

    private void createTables() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS messages (id INTEGER PRIMARY KEY, sender TEXT, text TEXT, sentiment TEXT, ts DATETIME DEFAULT CURRENT_TIMESTAMP)";
        try (Statement s = conn.createStatement()) { s.execute(sql); }
    }

    public void saveMessage(Message m) {
        String sql = "INSERT INTO messages(sender,text,sentiment) VALUES(?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getSender());
            ps.setString(2, m.getText());
            ps.setString(3, m.getSentiment());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Message> loadRecentMessages(int limit) {
        List<Message> out = new ArrayList<>();
        String sql = "SELECT sender,text,sentiment FROM messages ORDER BY ts DESC LIMIT ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Message(rs.getString("sender"), rs.getString("text"), rs.getString("sentiment")));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        // reverse to chronological
        List<Message> rev = new ArrayList<>();
        for (int i = out.size()-1; i>=0; i--) rev.add(out.get(i));
        return rev;
    }
}

/* === File: src/main/java/com/michaelsemera/neuraldesk/model/Message.java === */
package com.michaelsemera.neuraldesk.model;

public class Message {
    private final String sender; // "user" or "bot"
    private final String text;
    private final String sentiment;

    public Message(String sender, String text) { this(sender, text, null); }
    public Message(String sender, String text, String sentiment) { this.sender = sender; this.text = text; this.sentiment = sentiment; }

    public String getSender() { return sender; }
    public String getText() { return text; }
    public String getSentiment() { return sentiment; }
}

/* === File: src/main/java/com/michaelsemera/neuraldesk/util/FAQLoader.java === */
package com.michaelsemera.neuraldesk.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

public class FAQLoader {
    public static Map<String,String> loadDefaultFaqs() {
        Map<String,String> m = new LinkedHashMap<>();
        // default built-in small FAQ set
        m.put("what can you do","I can answer FAQs, remember recent messages, and help with simple tasks.");
        m.put("how do i train you","Use the trainer to load CSV with question,answer rows or use the trainer CLI to add pairs.");
        m.put("are you offline","Yes — NeuralDesk works offline using built-in rules and a small FAQ database.");
        return m;
    }
}

/* === File: src/main/resources/images/README === */
// Add an application icon at src/main/resources/images/icon.png for a nicer window icon.

/* === File: README.md === */
# NeuralDesk — Desktop AI Chatbot (by Michael Semera)

NeuralDesk is a Java desktop assistant built with **JavaFX**, **SQLite**, and lightweight NLP. It demonstrates NLP integration, concurrency, and state persistence — perfect for a portfolio project.

## Key features
- Chat interface with message history
- Sentiment analysis (lexicon-based)
- Offline FAQ matching and trainer-ready design
- Persistent chat history using SQLite
- Clean, modular architecture ready for extension (OpenNLP, LLM API, GUI polish)

## Run (Gradle)
```
./gradlew run
```

## Project structure
- `src/main/java` — application code (ui, nlp, db, util)
- `src/main/resources/fxml` — UI layout
- `neuraldesk.db` — SQLite database (created on first run)

## Extensibility
- Add `en-sent.bin` OpenNLP model under `src/main/resources/models/` to enable sentence detection
- Integrate an LLM API for advanced responses
- Add a trainer UI to load CSV datasets and retrain FAQ pairs

---

© 2025 Michael Semera