import java.io.*;
import java.nio.file.*;
import java.util.regex.*;

public class Parser {

    public static void main(String[] args) {
        File inputDir = new File("input_articles");
        File outputDir = new File("output_resumes");

        if (!inputDir.exists() || !inputDir.isDirectory()) {
            System.out.println("Le dossier 'input_articles' est introuvable.");
            return;
        }

        if (outputDir.exists()) {
            deleteDirectory(outputDir);
        }
        outputDir.mkdir();

        File globalOutputFile = new File(outputDir, "resume.txt");

        try (BufferedWriter globalWriter = new BufferedWriter(new FileWriter(globalOutputFile))) {

            for (File file : inputDir.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".txt")) {
                    long start = System.nanoTime();

                    try {
                        String content = Files.readString(file.toPath());
                        String filename = file.getName().replace(" ", "_");
                        String title = extractTitle(content);
                        String abstractText = extractAbstract(content);
                        int totalLines = content.split("\n").length;
                        int abstractLines = abstractText.split("\n").length;

                        long end = System.nanoTime();
                        long durationMs = (end - start) / 1_000_000;

                        globalWriter.write("Nom : " + filename + "\n");
                        globalWriter.write("Titre : " + title + "\n");
                        globalWriter.write("Résumé :\n" + abstractText.trim() + "\n");
                        globalWriter.write("Nombre total de lignes : " + totalLines + "\n");
                        globalWriter.write("Nombre de lignes du résumé : " + abstractLines + "\n");
                        globalWriter.write("Temps d’analyse : " + durationMs + " ms\n");
                        globalWriter.write("=====================\n\n");

                    } catch (IOException e) {
                        System.err.println("Erreur de lecture : " + file.getName());
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Erreur d'écriture dans le fichier global.");
        }
    }

    private static String extractTitle(String content) {
        for (String line : content.split("\n")) {
            if (!line.trim().isEmpty() && line.trim().length() > 10 && !line.trim().toLowerCase().contains("abstract")) {
                return line.trim();
            }
        }
        return "Titre inconnu";
    }

    private static String extractAbstract(String content) {
        Pattern pattern = Pattern.compile("(?i)abstract\\s*[:\\-\\n]*", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            int start = matcher.end();
            String sub = content.substring(start);
            String[] lines = sub.split("\n");
            StringBuilder sb = new StringBuilder();
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.toLowerCase().startsWith("keywords") || line.toLowerCase().startsWith("introduction"))
                    break;
                sb.append(line).append(" ");
            }
            return sb.toString().replaceAll("-\\s+", "").replaceAll("\\s+", " ");
        }
        return "Résumé non trouvé.";
    }

    private static void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                deleteDirectory(f);
            }
        }
        dir.delete();
    }
}