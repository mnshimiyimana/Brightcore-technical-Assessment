package rw.brightcore.recruitment.application;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rw.brightcore.recruitment.common.FieldValidationException;
import rw.brightcore.recruitment.common.NotFoundException;
import rw.brightcore.recruitment.config.AppProperties;

/**
 * Stores CVs on local disk, outside anything the web server serves statically.
 * Files are only reachable through the admin-protected download endpoint.
 */
@Service
public class CvStorageService {

    private static final byte[] PDF_MAGIC = "%PDF-".getBytes();
    private static final Pattern KEY_FORMAT = Pattern.compile("^[0-9a-f-]{36}$");

    private final Path root;
    private final long maxBytes;

    public CvStorageService(AppProperties props) {
        this.root = Path.of(props.uploadsDir()).toAbsolutePath().normalize();
        this.maxBytes = props.cvMaxBytes();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create uploads directory " + root, e);
        }
    }

    public record StoredCv(String key, String originalName, long size) {}

    /**
     * Checks the upload is a real PDF of at most 5 MB. The browser-supplied content type and
     * file name are easy to fake, so the file's first bytes are checked as well.
     */
    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FieldValidationException("cv", "Please attach your CV as a PDF.");
        }
        if (file.getSize() > maxBytes) {
            throw new FieldValidationException("cv", "The CV must be 5 MB or smaller.");
        }
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        if (!name.endsWith(".pdf") || !"application/pdf".equalsIgnoreCase(file.getContentType())) {
            throw new FieldValidationException("cv", "The CV must be a PDF file.");
        }
        try (InputStream in = file.getInputStream()) {
            if (!Arrays.equals(in.readNBytes(PDF_MAGIC.length), PDF_MAGIC)) {
                throw new FieldValidationException("cv", "The CV must be a PDF file.");
            }
        } catch (IOException e) {
            throw new FieldValidationException("cv", "The CV could not be read. Please try again.");
        }
    }

    /** Saves an already-validated file under a random name. */
    public StoredCv store(MultipartFile file) {
        String key = UUID.randomUUID().toString();
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, pathFor(key), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store CV", e);
        }
        return new StoredCv(key, sanitiseFileName(file.getOriginalFilename()), file.getSize());
    }

    public Resource load(String key) {
        Path path = pathFor(key);
        if (!Files.isRegularFile(path)) {
            throw new NotFoundException("CV file not found.");
        }
        return new FileSystemResource(path);
    }

    public void delete(String key) {
        try {
            Files.deleteIfExists(pathFor(key));
        } catch (IOException ignored) {
            // Best effort clean-up of an orphaned file; nothing references it.
        }
    }

    /** Keys are UUIDs we generated; the format check makes path traversal impossible. */
    private Path pathFor(String key) {
        if (key == null || !KEY_FORMAT.matcher(key).matches()) {
            throw new IllegalArgumentException("Invalid storage key");
        }
        return root.resolve(key + ".pdf");
    }

    /** Keeps only a safe base name for display and the download header. */
    static String sanitiseFileName(String original) {
        String base = original == null ? "" : original.replaceAll(".*[\\\\/]", "");
        base = base.replaceAll("[^A-Za-z0-9._ -]", "_").trim();
        if (base.isEmpty() || base.equals(".pdf")) {
            base = "cv.pdf";
        }
        return base.length() > 100 ? base.substring(base.length() - 100) : base;
    }
}
