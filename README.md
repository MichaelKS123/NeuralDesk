# NeuralDesk — AI Chatbot Desktop App by Michael Semera

**NeuralDesk** is a smart, offline-capable JavaFX desktop chatbot built with **Java**, **SQLite**, and a lightweight **NLP engine**. Designed as a showcase for portfolio projects, it demonstrates AI logic, user interface design, concurrency, and data persistence — all written in clean, original Java code.

---

## 🧠 Features

* **AI Chat Interface** — sleek JavaFX chat window with smooth UI.
* **Sentiment Analysis** — detects positive, neutral, and negative emotions.
* **Context Memory** — maintains recent messages to produce relevant replies.
* **Offline FAQ Mode** — instantly answers pre-trained common questions.
* **Trainable Dataset** — extendable FAQ file for custom responses.
* **SQLite Chat History** — stores user conversations locally for review.
* **Threaded NLP Engine** — responses processed asynchronously for a smooth experience.

---

## 🧩 Project Structure

```
NeuralDesk/
├── src/main/java/com/michaelsemera/neuraldesk/
│   ├── MainApp.java              # JavaFX app launcher
│   ├── ChatController.java       # UI logic
│   ├── NLPService.java           # AI/NLP engine
│   ├── Database.java             # SQLite integration
│   ├── Message.java              # Data model
│   ├── FAQLoader.java            # Loads predefined Q/A
│   └── utils/
│       └── TextUtils.java        # Sentiment word analysis
├── src/main/resources/com/michaelsemera/neuraldesk/
│   ├── chat.fxml                 # Chat UI layout
│   └── faq.txt                   # Default FAQ data
├── data/neuraldesk.db            # SQLite database (auto-created)
├── build.gradle                  # Gradle build file
├── settings.gradle               # Gradle settings
└── README.md                     # This file
```

---

## ⚙️ Setup & Run

### Requirements

* Java 17 or newer
* Gradle (or use Gradle Wrapper)
* SQLite (auto-handled in code)

### Run locally

```bash
gradle run
```

Or compile manually:

```bash
javac -d out $(find src/main/java -name "*.java")
java -cp out com.michaelsemera.neuraldesk.MainApp
```

---

## 💬 How It Works

1. **User Input** — You type a message into the chat box.
2. **Processing Layer (NLPService)** —

   * Checks FAQ matches (using fuzzy string similarity).
   * Performs basic sentiment detection via lexicon lookup.
   * If unknown, returns a neutral fallback message.
3. **Persistence (Database)** — Each message pair (user/bot) is stored in SQLite.
4. **Async Response** — Bot replies after a short simulated delay (threaded).

---

## 🧪 Extend & Train

You can add new FAQs in `src/main/resources/com/michaelsemera/neuraldesk/faq.txt`:

```
Q: What is NeuralDesk?
A: NeuralDesk is an intelligent chatbot built in JavaFX by Michael Semera.

Q: Who created you?
A: I was designed by Michael Semera as a portfolio AI demo.
```

Future enhancements:

* Integrate OpenAI API or NLP libraries (e.g., OpenNLP, Stanford CoreNLP).
* Add GUI-based FAQ training.
* Implement context-based memory beyond the current session.
* Introduce voice input/output using Java Speech API.

---

## 🧰 Technologies Used

* **Java 17+** — core logic and structure
* **JavaFX** — UI framework
* **SQLite (JDBC)** — lightweight database
* **Gradle** — build and dependency management

---

## 🏆 Portfolio Value

This project demonstrates:

* JavaFX UI/UX design
* Multithreading and concurrency
* Basic NLP and sentiment analysis
* Database integration with JDBC
* Clean, modular, testable Java code

Perfect for a **portfolio or GitHub showcase** highlighting full-stack Java capabilities in AI, UX, and data persistence.

---

## 📞 Contact & Support

For questions, suggestions, or collaboration opportunities:
- Open an issue on GitHub
- Email: michaelsemera15@gmail.com
- LinkedIn: [Michael Semera](https://www.linkedin.com/in/michael-semera-586737295/)

For issues or questions:
- Review this documentation
- Check the troubleshooting section
- Ensure proper privileges and setup
- Verify libpcap installation

---

---

## ⚠️ Disclaimer

This project is for **educational and portfolio purposes**. The NLP logic is simplified and does not represent full AI understanding. For production, integrate robust NLP frameworks or APIs.

---

© 2025 Michael Semera. All Rights Reserved.  Licensed under MIT.

