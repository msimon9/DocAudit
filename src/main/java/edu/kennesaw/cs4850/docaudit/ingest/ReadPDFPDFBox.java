package edu.kennesaw.cs4850.docaudit.ingest;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.LoggerFactory;
import edu.kennesaw.cs4850.docaudit.convert.ImageToText;
import edu.kennesaw.cs4850.docaudit.convert.ImageToTextTesseract;

/**
 *
 * @author rwhitak3
 */
public class ReadPDFPDFBox implements ReadPDF {
    private org.slf4j.Logger logger = LoggerFactory.getLogger(ReadPDFPDFBox.class);
    private PDFParser parser;

   
    @Override
    public String readPDF(File inputFile) {
        
        try {
            this.parser = new PDFParser(new RandomAccessFile(inputFile,"r")); // update for PDFBox V 2.0
            
            parser.parse();
            String textOutput = readCosDoc(parser.getDocument());
            String imgOutput = "";
            if ( textOutput.length() < 20 ) {
                imgOutput = readImages(parser.getPDDocument());
            } else {
                logger.info("Found Mobile Text Document");
                return textOutput;
            }
            if (imgOutput.length() > 20) {
                textOutput = imgOutput;
                logger.info("Found Scanned image Document");
            } else {
                logger.info("Found Mobile Text Document");
            }
            return textOutput;
            
        } catch (FileNotFoundException ex) {
            logger.error("File Not found", ex);
            return null;
        } catch (IOException ex) {
            logger.error("IO Error:", ex);
            return null;
        }
    }
    
    private String readCosDoc(COSDocument cosDoc) throws IOException {
        PDFTextStripper pdfStripper = new PDFTextStripper();

        PDDocument pdDoc = new PDDocument(cosDoc);
        pdfStripper.setStartPage(1);
        pdfStripper.setEndPage(pdDoc.getNumberOfPages());

        String output = pdfStripper.getText(pdDoc);
        logger.debug("Output: " + output);
        return output;    
    }

    private String readImages(PDDocument pdDoc) {
        PDFRenderer r = new PDFRenderer(pdDoc);
        int pageNumber = pdDoc.getNumberOfPages();
        ImageToText conv = new ImageToTextTesseract();
        StringBuilder finalOutput = new StringBuilder();
        try {
            for ( int i = 0; i < pageNumber; i++ ) {
                logger.debug("Reading Page " + Integer.toString(i));
                BufferedImage image = r.renderImageWithDPI(i, 300);
                String out = conv.convert(image);
                logger.debug("Got : " + out);
                finalOutput.append(out);
                return finalOutput.toString();
            }
            logger.debug("Output: " + finalOutput.toString());
            return finalOutput.toString();
        }  catch (IOException ex) {
            logger.error("IO Exception while trying to view images in pdf", ex);
            return null;
        }
    }
}
