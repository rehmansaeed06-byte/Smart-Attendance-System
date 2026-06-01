package database;

import models.Question;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;

public class QuestionDAO {

    public static void addQuestion(
            int lectureNo,
            String question,
            String option1,
            String option2,
            String option3,
            String option4,
            int correctOption
    ) {

        try {

            Connection con = DatabaseManager.connect();

            String checkSql =
                    "SELECT COUNT(*) FROM questions " +
                            "WHERE lecture_no = ? AND question = ?";

            PreparedStatement checkStmt =
                    con.prepareStatement(checkSql);

            checkStmt.setInt(1, lectureNo);
            checkStmt.setString(2, question);

            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {

                System.out.println("Duplicate skipped: " + question);

                con.close();
                return;
            }

            String sql =
                    "INSERT INTO questions " +
                            "(lecture_no, question, option1, option2, option3, option4, correct_option) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ps.setInt(1, lectureNo);
            ps.setString(2, question);
            ps.setString(3, option1);
            ps.setString(4, option2);
            ps.setString(5, option3);
            ps.setString(6, option4);
            ps.setInt(7, correctOption);

            ps.executeUpdate();

            con.close();

            System.out.println("Question added successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Question> getQuestionsByLecture(int lectureNo) {

        ArrayList<Question> questions = new ArrayList<>();

        try {

            Connection con = DatabaseManager.connect();

            String sql =
                    "SELECT question, option1, option2, option3, option4, correct_option " +
                            "FROM questions WHERE lecture_no = ?";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ps.setInt(1, lectureNo);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Question q = new Question(
                        rs.getString("question"),
                        rs.getString("option1"),
                        rs.getString("option2"),
                        rs.getString("option3"),
                        rs.getString("option4"),
                        rs.getInt("correct_option")
                );

                questions.add(q);
            }

            con.close();

            Collections.shuffle(questions);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return questions;
    }
}