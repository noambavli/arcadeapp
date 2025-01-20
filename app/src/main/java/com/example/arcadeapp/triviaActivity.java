package com.example.arcadeapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class triviaActivity extends AppCompatActivity {

    private TextView questionTextView, scoreTextView, timerTextView,title;
    private Button optionA, optionB, optionC, optionD;
    private ProgressBar progressBar;
    private int currentQuestionIndex = 0, score = 0;
    private List<Question> questionList;
    private CountDownTimer countDownTimer;
    private Button exitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trivia);
        title = findViewById(R.id.triviaTitle);
        questionTextView = findViewById(R.id.questionTextView);
        scoreTextView = findViewById(R.id.scoreTextView);
        timerTextView = findViewById(R.id.timerTextView);
        optionA = findViewById(R.id.optionA);
        optionB = findViewById(R.id.optionB);
        optionC = findViewById(R.id.optionC);
        optionD = findViewById(R.id.optionD);
        progressBar = findViewById(R.id.progressBar);
        exitButton = findViewById(R.id.exit_button);

        // Load questions and answers
        questionList = loadQuestions();
        Collections.shuffle(questionList); // Shuffle questions

        // Initialize score
        scoreTextView.setText("Score: 0");

        // Set up buttons to listen for user answers
        optionA.setOnClickListener(v -> checkAnswer(optionA.getText().toString()));
        optionB.setOnClickListener(v -> checkAnswer(optionB.getText().toString()));
        optionC.setOnClickListener(v -> checkAnswer(optionC.getText().toString()));
        optionD.setOnClickListener(v -> checkAnswer(optionD.getText().toString()));

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Start the first question
        showNextQuestion();
    }

    private List<Question> loadQuestions() {
        List<Question> questions = new ArrayList<>();
        // Format: "question|A_answer|B_answer|C_answer|D_answer|correct_answer"
        String[] data = {
                "What is the capital of France?|Paris|London|Berlin|Madrid|Paris",
                "Who developed Java?|Dennis Ritchie|Bjarne Stroustrup|James Gosling|Ken Thompson|James Gosling",
                "Which planet is known as the Red Planet?|Earth|Venus|Mars|Jupiter|Mars",
                "What is the square root of 144?|10|11|12|13|12",
                "What is the largest ocean on Earth?|Atlantic|Indian|Arctic|Pacific|Pacific",
                "Who painted the Mona Lisa?|Vincent van Gogh|Pablo Picasso|Leonardo da Vinci|Claude Monet|Leonardo da Vinci",
                "What is the smallest prime number?|0|1|2|3|2",
                "What is the freezing point of water in Celsius?|0|32|100|212|0",
                "Which element has the chemical symbol 'O'?|Oxygen|Gold|Osmium|Iron|Oxygen",
                "Who wrote 'Romeo and Juliet'?|Charles Dickens|William Shakespeare|Mark Twain|Homer|William Shakespeare",
                "What is the capital of Japan?|Seoul|Tokyo|Beijing|Bangkok|Tokyo",
                "Which gas do plants absorb from the atmosphere?|Nitrogen|Oxygen|Carbon Dioxide|Helium|Carbon Dioxide",
                "What is the currency of the United States?|Euro|Dollar|Pound|Yen|Dollar",
                "Which organ is responsible for pumping blood in the human body?|Lungs|Kidneys|Heart|Liver|Heart",
                "What is 7 multiplied by 8?|48|54|56|64|56",
                "What is the speed of light?|300,000 km/s|150,000 km/s|250,000 km/s|350,000 km/s|300,000 km/s",
                "Which language is used to write Android apps?|C++|Java|Python|Swift|Java",
                "What is the largest planet in our solar system?|Earth|Mars|Jupiter|Saturn|Jupiter",
                "What is the national animal of India?|Elephant|Tiger|Peacock|Lion|Tiger",
                "Who discovered penicillin?|Marie Curie|Alexander Fleming|Louis Pasteur|Albert Einstein|Alexander Fleming",
                "Which continent is known as the 'Dark Continent'?|Asia|Africa|Australia|South America|Africa",
                "What is the chemical symbol for gold?|Au|Ag|Fe|Hg|Au",
                "Who is known as the 'Father of Computers'?|Alan Turing|Charles Babbage|Steve Jobs|Bill Gates|Charles Babbage",
                "What is the tallest mountain in the world?|K2|Mount Everest|Kangchenjunga|Lhotse|Mount Everest",
                "What is the most abundant gas in Earth's atmosphere?|Oxygen|Carbon Dioxide|Nitrogen|Helium|Nitrogen",
                "Who was the first President of the United States?|Thomas Jefferson|George Washington|Abraham Lincoln|John Adams|George Washington",
                "Which is the longest river in the world?|Amazon|Nile|Yangtze|Mississippi|Nile",
                "What is the main ingredient in guacamole?|Tomato|Avocado|Cucumber|Spinach|Avocado",
                "What is the hardest natural substance on Earth?|Gold|Iron|Diamond|Quartz|Diamond",
                "Which country is known as the Land of the Rising Sun?|China|Japan|Thailand|South Korea|Japan",
                "What is the capital of Australia?|Sydney|Melbourne|Canberra|Brisbane|Canberra",
                "Who invented the telephone?|Thomas Edison|Alexander Graham Bell|Nikola Tesla|Michael Faraday|Alexander Graham Bell",
                "Which year did World War II end?|1942|1945|1948|1950|1945",
                "What is the smallest country in the world?|Vatican City|Monaco|Malta|San Marino|Vatican City",
                "Which planet has the most moons?|Earth|Mars|Saturn|Jupiter|Saturn",
                "What is the capital of Italy?|Rome|Venice|Florence|Milan|Rome",
                "Who wrote 'The Odyssey'?|Homer|Virgil|Sophocles|Plato|Homer",
                "What is the boiling point of water in Celsius?|90|95|100|105|100",
                "Which metal is liquid at room temperature?|Mercury|Iron|Aluminum|Zinc|Mercury",
                "What is the main gas found in the sun?|Hydrogen|Helium|Nitrogen|Oxygen|Hydrogen",
                "Who is known as the Iron Man of India?|Mahatma Gandhi|Subhas Chandra Bose|Vallabhbhai Patel|Jawaharlal Nehru|Vallabhbhai Patel",
                "Which blood type is known as the universal donor?|A|B|AB|O|O",
                "What is the capital of Canada?|Toronto|Vancouver|Ottawa|Montreal|Ottawa",
                "What is the study of the stars and planets called?|Geology|Biology|Astronomy|Chemistry|Astronomy",
                "Who wrote 'To Kill a Mockingbird'?|Harper Lee|Mark Twain|Ernest Hemingway|F. Scott Fitzgerald|Harper Lee",
                "What is the chemical formula for water?|H2O|CO2|O2|NaCl|H2O",
                "What is the capital of Germany?|Munich|Berlin|Frankfurt|Hamburg|Berlin",
                "What is the largest desert in the world?|Gobi|Sahara|Antarctic|Kalahari|Antarctic",
                "Which part of the plant conducts photosynthesis?|Root|Stem|Leaf|Flower|Leaf",
                "What is the currency of Japan?|Dollar|Euro|Yen|Won|Yen",
                "Who painted the ceiling of the Sistine Chapel?|Leonardo da Vinci|Raphael|Michelangelo|Donatello|Michelangelo"
        };

        for (String entry : data) {
            String[] parts = entry.split("\\|");
            questions.add(new Question(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]));
        }
        return questions;
    }

    private void showNextQuestion() {
        if (currentQuestionIndex < questionList.size() && currentQuestionIndex <= 10) {
            Question currentQuestion = questionList.get(currentQuestionIndex);

            questionTextView.setText(currentQuestion.getQuestion());
            optionA.setText(currentQuestion.getOptionA());
            optionB.setText(currentQuestion.getOptionB());
            optionC.setText(currentQuestion.getOptionC());
            optionD.setText(currentQuestion.getOptionD());

            // Reset Progress Bar and Start Timer
            progressBar.setProgress(0);
            startTimer();

            currentQuestionIndex++;
        } else {
            title.setText("Final Score: "+score);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("score", score);  // Pass the final score
            setResult(RESULT_OK, resultIntent);
        }
    }

    private void checkAnswer(String selectedAnswer) {
        Question currentQuestion = questionList.get(currentQuestionIndex - 1);
        if (selectedAnswer.equals(currentQuestion.getCorrectAnswer())) {
            score+=20;
            scoreTextView.setText("Score: " + score);
        }
        // Stop the timer and move to the next question
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        showNextQuestion();
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(10000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                int progress = (int) (100 - (millisUntilFinished / 100));
                progressBar.setProgress(progress);
                timerTextView.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                showNextQuestion();
            }
        }.start();
    }

    static class Question {
        private final String question;
        private final String optionA, optionB, optionC, optionD, correctAnswer;

        public Question(String question, String optionA, String optionB, String optionC, String optionD, String correctAnswer) {
            this.question = question;
            this.optionA = optionA;
            this.optionB = optionB;
            this.optionC = optionC;
            this.optionD = optionD;
            this.correctAnswer = correctAnswer;
        }

        public String getQuestion() {
            return question;
        }

        public String getOptionA() {
            return optionA;
        }

        public String getOptionB() {
            return optionB;
        }

        public String getOptionC() {
            return optionC;
        }

        public String getOptionD() {
            return optionD;
        }

        public String getCorrectAnswer() {
            return correctAnswer;
        }
    }
}