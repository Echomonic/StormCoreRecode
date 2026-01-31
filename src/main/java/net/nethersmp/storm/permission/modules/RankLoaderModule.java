package net.nethersmp.storm.permission.modules;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.nethersmp.storm.module.api.Module;
import net.nethersmp.storm.module.api.Result;
import net.nethersmp.storm.permission.UserRank;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
public class RankLoaderModule implements Module<Void> {

    private final Path file;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentHashMap<String, UserRank> userRanks = new ConcurrentHashMap<>();

    private final ExecutorService loadingExecutor = Executors.newSingleThreadExecutor();

    private final Object lock = new Object();

    @Override
    public String id() {
        return "user_ranks_loader";
    }

    @Override
    public int priority() {
        return 980;
    }

    @Override
    public Result<Void> load() {
        AtomicReference<Result<Void>> result = new AtomicReference<>(Result.success());

        loadingExecutor.execute(() -> {
            try {
                if (!Files.exists(file)) {
                    Files.createDirectories(file.getParent());
                    Files.createFile(file);
                }

                JsonNode root = objectMapper.readTree(file.toFile());
                for (String rankId : Lists.newArrayList(root.fieldNames())) {
                    JsonNode rankNode = root.get(rankId);

                    UserRank userRank = objectMapper.convertValue(rankNode, UserRank.class);
                    synchronized (lock) {
                        userRanks.put(rankId, userRank);
                    }
                }
            } catch (IllegalArgumentException | IOException e) {
                result.set(Result.fail("[RANKS] (" + e.getClass().getSimpleName() + ")", e.getMessage()));
            }
        });
        loadingExecutor.shutdown();

        return result.get();
    }

    public Optional<UserRank> getUserRank(String id) {
        return Optional.ofNullable(userRanks.get(id));
    }

    @Override
    public void unload() {
        userRanks.clear();
    }
}
