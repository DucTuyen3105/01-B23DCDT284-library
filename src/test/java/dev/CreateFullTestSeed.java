package dev;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import security.PasswordHash4;

/** Generates a reproducible SQL seed with salted hashes for user-requested local accounts. */
public final class CreateFullTestSeed {
    public static void main(String[] args) throws Exception {
        String template = new String(Files.readAllBytes(Paths.get("database/06-full-test.sql.template")), StandardCharsets.UTF_8);
        String sql = template.replace("{{ADMIN_PASSWORD_HASH}}", PasswordHash4.hash("1234"))
                .replace("{{READER_PASSWORD_HASH}}", PasswordHash4.hash("1234"));
        Files.write(Paths.get("database/06-full-test.sql"), sql.getBytes(StandardCharsets.UTF_8));
        System.out.println("Created database/06-full-test.sql for admin and tuyen.");
    }
}
