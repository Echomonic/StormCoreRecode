package net.nethersmp.storm.punishment.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.nethersmp.storm.module.api.Result;
import net.nethersmp.storm.punishment.UserPunishment;
import net.nethersmp.storm.punishment.api.storage.PunishmentDataStore;
import net.nethersmp.storm.user.data.UserPunishmentDataType;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class JsonFilePunishmentDataStore implements PunishmentDataStore {

    private final Path file;

    private ObjectNode root;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @SneakyThrows
    @Override
    public void initialize() {
        if (!Files.exists(file)) {
            Files.createDirectories(file.getParent());
            Files.createFile(file);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), objectMapper.createObjectNode());
        }
        this.root = (ObjectNode) objectMapper.readTree(file.toFile());
    }

    @Override
    public Optional<UserPunishment> load(UUID player) {
        String rawPunishmentId = UserPunishmentDataType.CURRENT_PUNISHMENT.get(player);

        if (rawPunishmentId.isBlank())
            return Optional.empty();

        JsonNode punishmentNode = root.get(rawPunishmentId);

        return Optional.ofNullable(objectMapper.convertValue(punishmentNode, UserPunishment.class));
    }

    public Result<String> write(UserPunishment punishment) {
        JsonNode convertedNode = objectMapper.valueToTree(punishment);
        root.set(punishment.id().text(), convertedNode);

        try {
            atomicWriteTmp();
        } catch (IOException e) {
            return Result.fail("[PERMISSIONS] IO_EXCEPTION", e.getMessage());
        }
        return Result.success("Successfully wrote permission!");
    }

    private void atomicWriteTmp() throws IOException {
        Path parent = file.getParent();
        if (parent != null) Files.createDirectories(parent);

        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        try (OutputStream out = Files.newOutputStream(tmp)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(out, root);
        }
        Files.move(tmp, file,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE);
    }

}
