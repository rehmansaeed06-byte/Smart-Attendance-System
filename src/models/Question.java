package models;

public class Question {

    private String questionText;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private int correctOption;

    public Question(String questionText, String option1, String option2,
                    String option3, String option4, int correctOption) {
        this.questionText = questionText;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.correctOption = correctOption;
    }

    public String formatQuestion() {
        return "\nQUESTION:\n" + questionText +
                "\n1. " + option1 +
                "\n2. " + option2 +
                "\n3. " + option3 +
                "\n4. " + option4 +
                "\nEnter answer number:";
    }

    public int getCorrectOption() {
        return correctOption;
    }

    public String getOptionText(int optionNumber) {

        if (optionNumber == 1) {
            return option1;
        } else if (optionNumber == 2) {
            return option2;
        } else if (optionNumber == 3) {
            return option3;
        } else if (optionNumber == 4) {
            return option4;
        } else {
            return "Invalid option";
        }
    }
}