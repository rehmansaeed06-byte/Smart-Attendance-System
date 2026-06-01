package launcher;

import database.DatabaseManager;
import database.LectureDAO;
import database.MCQImporter;
import gui.ServerGUI;

public class ServerLauncher {

    public static void main(String[] args) {

        DatabaseManager.initializeDatabase();

        LectureDAO.addLecture(1, "Introduction to Computers, Programs, and Java");
        LectureDAO.addLecture(2, "Elementary Programming");
        LectureDAO.addLecture(3, "Selections");
        LectureDAO.addLecture(4, "Mathematical Functions, Characters, and Strings");
        LectureDAO.addLecture(5, "Loops");
        LectureDAO.addLecture(6, "Methods");
        LectureDAO.addLecture(7, "Single-Dimensional Arrays");
        LectureDAO.addLecture(8, "Multidimensional Arrays");
        LectureDAO.addLecture(9, "Objects and Classes");
        LectureDAO.addLecture(10, "Object-Oriented Thinking");
        LectureDAO.addLecture(11, "Inheritance and Polymorphism");
        LectureDAO.addLecture(12, "Exception Handling and Text I/O");
        LectureDAO.addLecture(13, "Abstract Classes and Interfaces");
        LectureDAO.addLecture(14, "JavaFX Basics");
        LectureDAO.addLecture(15, "Event-Driven Programming and Animations");


        MCQImporter.importMCQs("mcqs.csv");

        ServerGUI.main(args);
    }
}