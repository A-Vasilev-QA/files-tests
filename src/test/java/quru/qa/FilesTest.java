package quru.qa;

import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import org.apache.commons.io.IOUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import net.lingala.zip4j.core.ZipFile;

import static com.codeborne.selenide.Selectors.*;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Condition.*;
import static org.assertj.core.api.Assertions.assertThat;


public class FilesTest {

    @Test
    public void isTxtCorrectAndUploadableTest() throws Exception {
        File txt = new File(getClass().getClassLoader().getResource("QAGuru.txt").getFile());
        try (InputStream stream = new FileInputStream(txt)) {
            //String txtContent = new String(stream.readAllBytes(), "UTF-8"); - Не работает в Java8
            String txtContent = IOUtils.toString(stream, "UTF-8");
            assertThat(txtContent).startsWith("Hello");
        }
        open("https://cgi-lib.berkeley.edu/ex/fup.html");
        $(byName("upfile")).uploadFile(txt);
        $(byValue("Press")).click();
        $(byTagName("pre")).shouldHave(text("Hello QAGuru!"));
    }

    @Test
    public void isPdfCorrectTest() throws Exception {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("Certificate_251159_3CTALTAE-RU_Vasilev_10_12_2020_Remote.pdf")) {
            PDF parsed = new PDF(stream);
            assertThat(parsed.author).isEqualTo("Jelena Ples");
            assertThat(parsed.title).startsWith("Certificate");
            assertThat(parsed.text).contains("Aleksei Vasilev");
        }
    }

    @Test
    public void isXlsDownloadableAndCorrectTest() throws Exception {
        open("https://file-examples.com/index.php/sample-documents-download/sample-xls-download/");
        File download = $("tbody a").download();
        XLS parsed = new XLS(download);
        assertThat(parsed.name.endsWith("10.xls"));
        assertThat(parsed.excel.getSheetAt(0).getRow(2).getCell(2).getStringCellValue())
                .isEqualTo("Hashimoto");
    }

    @Test
    public void isZipOpenableAndCorrectTest() throws Exception {
        ZipFile zip = new ZipFile(getClass().getClassLoader().getResource("Certificates.zip").getFile());
        zip.setPassword("QAGuru");
        zip.extractAll("./src/test/resources/extracted");
        File extracted = new File("./src/test/resources/extracted");
        assertThat(extracted.listFiles().length).isEqualTo(3);
        for (File i:extracted.listFiles()) {
            assertThat(i.getName()).endsWith(".pdf");
        }
    }

    @Test
    public void isDocxDownloadableAndCorrectTest() throws Exception {
        open("https://file-examples.com/index.php/sample-documents-download/sample-doc-download/");
        File download = $(byText("DOCX")).sibling(0).$(byTagName("a")).download();
        InputStream stream = new FileInputStream(download);
        WordprocessingMLPackage docX = WordprocessingMLPackage.load(stream);
        assertThat(download.getName()).endsWith("100kB.docx");
        assertThat(docX.getMainDocumentPart().getContent().toString()).contains("Lorem ipsum");
    }
}
