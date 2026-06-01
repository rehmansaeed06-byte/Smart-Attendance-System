package database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class MCQImporter {

    public static void importMCQs(String filePath) {

        int totalImported = 0;
        int totalSkipped = 0;
        int lineNumber = 1;

        try {

            BufferedReader br =
                    new BufferedReader(
                            new FileReader(filePath)
                    );

            String line;

            br.readLine(); // skip header

            while ((line = br.readLine()) != null) {

                lineNumber++;

                ArrayList<String> data =
                        parseCSVLine(line);

                if (data.size() != 7) {

                    System.out.println(
                            "Invalid row skipped at line "
                                    + lineNumber
                                    + " | Columns found: "
                                    + data.size()
                    );

                    System.out.println(line);

                    totalSkipped++;

                    continue;
                }

                int lectureNo =
                        Integer.parseInt(data.get(0).trim());

                String question =
                        data.get(1).trim();

                String option1 =
                        data.get(2).trim();

                String option2 =
                        data.get(3).trim();

                String option3 =
                        data.get(4).trim();

                String option4 =
                        data.get(5).trim();

                int correctOption =
                        Integer.parseInt(data.get(6).trim());

                if (correctOption < 1 || correctOption > 4) {

                    System.out.println(
                            "Invalid correct option skipped at line "
                                    + lineNumber
                    );

                    totalSkipped++;

                    continue;
                }

                System.out.println(
                        "Importing Lecture "
                                + lectureNo
                                + " | "
                                + question
                );

                QuestionDAO.addQuestion(
                        lectureNo,
                        question,
                        option1,
                        option2,
                        option3,
                        option4,
                        correctOption
                );

                totalImported++;
            }

            br.close();

            System.out.println("----------------------------");
            System.out.println("MCQs import finished.");
            System.out.println("Imported/Processed: " + totalImported);
            System.out.println("Skipped rows: " + totalSkipped);
            System.out.println("----------------------------");

        } catch (Exception e) {

            System.out.println("MCQ import failed.");
            e.printStackTrace();
        }
    }

    private static ArrayList<String> parseCSVLine(String line) {

        ArrayList<String> values =
                new ArrayList<>();

        StringBuilder current =
                new StringBuilder();

        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {

            char ch = line.charAt(i);

            if (ch == '"') {

                insideQuotes = !insideQuotes;

            } else if (ch == ',' && !insideQuotes) {

                values.add(
                        current.toString().trim()
                );

                current.setLength(0);

            } else {

                current.append(ch);
            }
        }

        values.add(
                current.toString().trim()
        );

        return values;
    }
}