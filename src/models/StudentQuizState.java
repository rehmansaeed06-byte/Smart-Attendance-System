package models;

/**
 * FIX: Made answer-tracking fields volatile so changes on the timer thread
 * (resetAnswerStatus) are immediately visible to student handler threads
 * (hasAnsweredCurrentQuestion / addCorrectAnswer / addWrongAnswer).
 * Without volatile, a student could answer, the flag gets set to true,
 * but the timer thread resets it to false for the next question — and
 * the student thread may not see the updated value due to CPU cache.
 */
public class StudentQuizState {

    private volatile int correctAnswers = 0;
    private volatile int wrongAnswers = 0;
    private volatile int totalAnswered = 0;
    private volatile boolean answeredCurrentQuestion = false;
    private volatile boolean connected = false;

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public int getWrongAnswers() {
        return wrongAnswers;
    }

    public int getTotalAnswered() {
        return totalAnswered;
    }

    public boolean hasAnsweredCurrentQuestion() {
        return answeredCurrentQuestion;
    }

    public boolean isConnected() {
        return connected;
    }

    public void markConnected() {
        connected = true;
    }

    public void markDisconnected() {
        connected = false;
    }

    public void resetAnswerStatus() {
        answeredCurrentQuestion = false;
    }

    public synchronized void addCorrectAnswer() {
        correctAnswers++;
        totalAnswered++;
        answeredCurrentQuestion = true;
    }

    public synchronized void addWrongAnswer() {
        wrongAnswers++;
        totalAnswered++;
        answeredCurrentQuestion = true;
    }
}
