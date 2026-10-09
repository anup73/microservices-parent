package com.ecommerce.springboot_mcp_server.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Read-only MCP tools for browsing files under a single configured root directory.
 * Any path that resolves outside the root is rejected.
 */
@Component
public class FileSystemTools {

    private static final long MAX_FILE_BYTES = 1024 * 1024;

    private final Path root;

    public FileSystemTools(@Value("${files.root-path:C:/RebindRise/spring_workspace/products_data}") String rootPath) {
        this.root = Path.of(rootPath).toAbsolutePath().normalize();
    }

    @Tool(description = "List files and folders in a directory under the products_data root. "
            + "Use an empty path for the root. Directories are suffixed with '/'.")
    public List<String> listFiles(
            @ToolParam(description = "Relative path of the directory, e.g. 'images' (empty for root)", required = false) String relativePath) throws IOException {
        Path dir = resolve(relativePath);
        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException("Not a directory: " + relativePath);
        }
        try (Stream<Path> s = Files.list(dir)) {
            return s.sorted()
                    .map(p -> root.relativize(p).toString().replace('\\', '/') + (Files.isDirectory(p) ? "/" : ""))
                    .toList();
        }
    }

    @Tool(description = "Read a UTF-8 text file under the products_data root (max 1 MB).")
    public String readFile(
            @ToolParam(description = "Relative path of the file, e.g. 'catalog/products.csv'") String relativePath) throws IOException {
        Path file = resolve(relativePath);
        if (!Files.isRegularFile(file)) {
            throw new IllegalArgumentException("Not a file: " + relativePath);
        }
        if (Files.size(file) > MAX_FILE_BYTES) {
            throw new IllegalArgumentException("File too large (limit 1 MB): " + relativePath);
        }
        return Files.readString(file, StandardCharsets.UTF_8);
    }

    private Path resolve(String relativePath) {
        String rel = relativePath == null ? "" : relativePath.trim();
        Path resolved = root.resolve(rel).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("Path is outside the allowed directory");
        }
        return resolved;
    }
}
