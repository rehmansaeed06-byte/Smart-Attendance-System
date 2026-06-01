package database;

import models.Question;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class QuestionLoader {

    public static ArrayList<Question> loadQuestions(String fileName) {

        ArrayList<Question> questions = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));

            String line;

            while ((line = reader.readLine()) != null) {

                String[] parts = line.split("\\|");

                if (parts.length == 6) {
                    Question q = new Question(
                            parts[0],
                            parts[1],
                            parts[2],
                            parts[3],
                            parts[4],
                            Integer.parseInt(parts[5])
                    );

                    questions.add(q);
                }
            }

            reader.close();

        } catch (Exception e) {
            System.out.println("Error loading questions: " + e.getMessage());
        }

        return questions;
    }
}