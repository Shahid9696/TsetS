import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;

public class PDFPlaceholderUtility {

    public static void main(String[] args) {
        try {
            // Example data
            File docFile = new File("C:/input/sample.pdf");
            String destinationFolderpath = "C:/output/";
            String reqRefNo = "CASE-12345";
            List<String> WiNosList = List.of("WI001", "WI002");
            String reqType = "CourtOrder";
            String sessionID = "SESSION123";

            boolean isDocPresent = docFile.exists();

            if (isDocPresent && docFile != null) {
                // Replace placeholders before attaching
                File replacedPdf = new File(destinationFolderpath, docFile.getName());
                try {
                    Map<String, String> replacements = new HashMap<>();
                    replacements.put("${CUSTOMER_NAME}", "Vedant Agarwal");
                    replacements.put("${DATE}", "09-Oct-2025");
                    replacements.put("${AMOUNT}", "₹5000");
                    replacements.put("${CASE_NO}", reqRefNo);

                    replacePlaceholdersInPdf(docFile, replacedPdf, replacements);
                    System.out.println("PDF placeholder replaced successfully");
                } catch (Exception e) {
                    System.err.println("Error replacing placeholders: " + e.getMessage());
                    return;
                }

                // Now attach the replaced file (output PDF)
                for (int i = 0; i < WiNosList.size(); i++) {
                    String attachDocWiStatus = "";
                    String wiNo = WiNosList.get(i);
                    boolean isLastWi = (i == WiNosList.size() - 1);

                    attachDocWiStatus = attachDocwithWI(replacedPdf, wiNo, reqType, isLastWi, sessionID);

                    System.out.println("attachDocWiStatus is: " + attachDocWiStatus);
                    if ("Success".equalsIgnoreCase(attachDocWiStatus)) {
                        System.out.println("Document attached to WI successfully");
                    } else {
                        System.out.println("Error while attaching doc with WI");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- REPLACE PLACEHOLDERS IN PDF ---
    public static void replacePlaceholdersInPdf(File inputFile, File outputFile, Map<String, String> replacements)
            throws IOException {
        try (PDDocument document = PDDocument.load(inputFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            // Replace placeholders
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                text = text.replace(entry.getKey(), entry.getValue());
            }

            // Create a new PDF
            PDDocument newDoc = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            newDoc.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(newDoc, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 11);
                contentStream.newLineAtOffset(50, 750);

                // Write line by line
                for (String line : text.split("\n")) {
                    contentStream.showText(line);
                    contentStream.newLineAtOffset(0, -15);
                }

                contentStream.endText();
            }

            newDoc.save(outputFile);
            newDoc.close();
        }
    }

    // --- ATTACH FILE TO WORKITEM ---
    public static String attachDocwithWI(File file, String wiNo, String reqType, boolean isLastWi, String sessionID) {
        // Placeholder for your actual work item attachment logic
        // Example: API call or workflow service logic
        System.out.println("Attaching " + file.getName() + " to WI: " + wiNo);
        return "Success"; // or "Failure"
    }
}
