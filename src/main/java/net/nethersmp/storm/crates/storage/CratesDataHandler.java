package net.nethersmp.storm.crates.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.nethersmp.storm.crates.api.CrateData;
import org.bukkit.inventory.ItemStack;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RequiredArgsConstructor
public class CratesDataHandler {

    private final Path file;
    private final CratesStorage storage;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ObjectNode root;

    @SneakyThrows
    public void initialize() {
        if (!Files.exists(file)) {
            Files.createDirectories(file.getParent());
            Files.createFile(file);

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), "{}");

        }
        root = (ObjectNode) objectMapper.readTree(file.toFile());
    }

    public void read() {
        for (Map.Entry<String, JsonNode> crateEntry : root.properties()) {
            String crateId = crateEntry.getKey();
            JsonNode crateNode = crateEntry.getValue();

            JsonNode itemsNodeRaw = crateNode.get("items");
            if (!itemsNodeRaw.isArray()) continue;
            ArrayNode itemsNode = (ArrayNode) itemsNodeRaw;

            String crateColor = crateNode.get("color").asText();
            ConcurrentMap<Integer, ItemStack> crateItems = readItems(itemsNode);

            CrateData crateData = new CrateData(crateId, crateColor, crateItems);

            storage.add(crateId, crateData);
        }
    }

    @SneakyThrows
    public void write(CrateData crateData) {
        if (crateData == null)
            return;
        ObjectMapper objectMapper = new ObjectMapper();

        String crateName = crateData.name().toLowerCase();
        ObjectNode crateNode;
        if (root.has(crateName)) {
            crateNode = (ObjectNode) root.get(crateName);
        } else {
            crateNode = root.putObject(crateName);
        }

        crateNode.put("color", crateData.color());
        writeItems(crateData, crateNode);
        root.set(crateName, crateNode);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), root);

    }

    @SneakyThrows
    public boolean delete(String id) {
        id = id.toLowerCase();
        storage.remove(id);

        if (!root.has(id)) return false;
        root.remove(id);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), root);
        return true;
    }

    private void writeItems(CrateData crateData, ObjectNode crateNode) {
        if (crateData == null) return;
        ConcurrentMap<Integer, ItemStack> crateItems = crateData.items();

        ArrayNode itemNode;
        if (crateNode.has("items")) {
            itemNode = (ArrayNode) crateNode.get("items");
        } else {
            itemNode = crateNode.putArray("items");
        }
        if (!itemNode.isEmpty())
            itemNode.removeAll();

        //Converted from old core. Will most likely have to be optimized.
        long lastTicked = System.currentTimeMillis();
        for (Map.Entry<Integer, ItemStack> crateItem : crateItems.entrySet()) {
            int itemSlot = crateItem.getKey();
            ItemStack itemStack = crateItem.getValue();

            byte[] serializedBytes = itemStack.serializeAsBytes();
            byte[] combinedArray = new byte[serializedBytes.length + 1];
            System.arraycopy(serializedBytes, 0, combinedArray, 0, serializedBytes.length);
            combinedArray[serializedBytes.length] = (byte) itemSlot;

            // Reverse the array
            for (int i = 0; i < combinedArray.length / 2; i++) {
                byte temp = combinedArray[i];
                combinedArray[i] = combinedArray[combinedArray.length - 1 - i];
                combinedArray[combinedArray.length - 1 - i] = temp;
            }

            itemNode.add(Base64.getEncoder().encodeToString(combinedArray));
        }
        System.out.println("Wrote items in " + (System.currentTimeMillis() - lastTicked) + "ms");
    }

    private ConcurrentMap<Integer, ItemStack> readItems(ArrayNode itemsNode) {
        ConcurrentMap<Integer, ItemStack> items = new ConcurrentHashMap<>();

        itemsNode.iterator().forEachRemaining(itemNode -> {
            String serializedItemInformation = itemNode.asText();
            byte[] decodedBytes = Base64.getDecoder().decode(serializedItemInformation);
            // reverse the array
            for (int i = 0, j = decodedBytes.length - 1; i < j; i++, j--) {
                byte temp = decodedBytes[i];
                decodedBytes[i] = decodedBytes[j];
                decodedBytes[j] = temp;
            }
            int itemSlot = decodedBytes[decodedBytes.length - 1];
            byte[] itemInformation = new byte[decodedBytes.length];
            System.arraycopy(decodedBytes, 0, itemInformation, 0, decodedBytes.length);

            items.put(itemSlot, ItemStack.deserializeBytes(itemInformation));
        });

        return items;
    }

}
