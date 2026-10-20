package rw.brightcore.recruitment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import rw.brightcore.recruitment.common.FieldValidationException;
import rw.brightcore.recruitment.config.AppProperties;

class CvStorageServiceTest {

    private static final byte[] PDF = "%PDF-1.7 content".getBytes();

    @TempDir
    Path tempDir;

    private CvStorageService storage;

    @BeforeEach
    void setUp() {
        var props = new AppProperties("Africa/Kigali", tempDir.toString(), 100, null, null, null, false);
        storage = new CvStorageService(props);
    }

    @Test
    void acceptsARealPdf() {
        storage.validate(new MockMultipartFile("cv", "cv.pdf", "application/pdf", PDF));
    }

    @Test
    void rejectsAFileThatOnlyPretendsToBeAPdf() {
        var fake = new MockMultipartFile("cv", "cv.pdf", "application/pdf", "MZ-executable".getBytes());
        assertThatThrownBy(() -> storage.validate(fake))
                .isInstanceOf(FieldValidationException.class)
                .hasMessage("The CV must be a PDF file.");
    }

    @Test
    void rejectsWrongExtension() {
        var doc = new MockMultipartFile("cv", "cv.docx", "application/pdf", PDF);
        assertThatThrownBy(() -> storage.validate(doc)).hasMessage("The CV must be a PDF file.");
    }

    @Test
    void rejectsFilesOverTheSizeLimit() {
        var big = new MockMultipartFile("cv", "cv.pdf", "application/pdf", new byte[101]);
        assertThatThrownBy(() -> storage.validate(big)).hasMessage("The CV must be 5 MB or smaller.");
    }

    @Test
    void storesUnderARandomNameAndCanBeLoadedBack() throws Exception {
        var stored = storage.store(new MockMultipartFile("cv", "../../etc/passwd.pdf", "application/pdf", PDF));
        assertThat(stored.key()).matches("[0-9a-f-]{36}");
        assertThat(stored.originalName()).isEqualTo("passwd.pdf");
        assertThat(storage.load(stored.key()).getContentAsByteArray()).isEqualTo(PDF);
    }

    @Test
    void sanitisesFileNamesForDisplay() {
        assertThat(CvStorageService.sanitiseFileName("C:\\Users\\me\\CV \"final\".pdf")).isEqualTo("CV _final_.pdf");
        assertThat(CvStorageService.sanitiseFileName(null)).isEqualTo("cv.pdf");
    }
}
