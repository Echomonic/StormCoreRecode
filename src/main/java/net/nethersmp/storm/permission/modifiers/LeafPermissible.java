package net.nethersmp.storm.permission.modifiers;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static net.nethersmp.storm.permission.modifiers.PermissibleFields.ATTACHMENTS;
import static net.nethersmp.storm.permission.modifiers.PermissibleFields.PERMISSIONS;

public class LeafPermissible extends PermissibleBase {

    private PermissionHierarchy hierarchy;

    public LeafPermissible(Player player) {
        super(player);
        this.hierarchy = new PermissionHierarchy();
    }

    public Set<String> getPermissions() {
        try {
            Object rawPermissions = PERMISSIONS.get(this);
            if (rawPermissions == null) return Set.of();
            Map<String, PermissionAttachmentInfo> permissions = (Map<String, PermissionAttachmentInfo>) rawPermissions;
            return permissions.keySet();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasPermission(@NonNull String permission) {
        if (super.hasPermission(permission)) return true;
        return hierarchy.canReachEnd(permission);
    }

    @Override
    public boolean hasPermission(Permission perm) {
        return hasPermission(perm.getName());
    }

    @Override
    public void recalculatePermissions() {
        this.hierarchy = new PermissionHierarchy();
        super.recalculatePermissions();

        for (String perm : getPermissions()) {
            if (!perm.endsWith("*")) continue;
            hierarchy.addString(perm.substring(0, perm.length() - 1));
        }
    }

    public void copy(PermissibleBase from) {
        try {
            ATTACHMENTS.set(this, ATTACHMENTS.get(from));
            PERMISSIONS.set(this, PERMISSIONS.get(from));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static class PermissionHierarchy {
        private Node root = null;

        /**
         * Adds a string to the tree
         *
         * @param text
         */
        public void addString(String text) {
            if (root == null) {
                root = new Node();
            }
            Node currentNode = root;
            for (int i = 0; i < text.length(); i++) {
                char character = text.charAt(i);
                boolean created = false;
                if (currentNode.children == null) {
                    currentNode.children = new HashMap<>();
                    created = true;
                }
                if (!currentNode.children.containsKey(character)) {
                    if (!currentNode.isLeaf() || created) {
                        Node n = new Node();
                        currentNode.children.put(character, n);
                        currentNode = n;
                    } else {
                        return;
                    }
                } else {
                    currentNode = currentNode.children.get(character);
                }
                // if we're at the end, remove any remaining nodes as this is a more broad definition
                if (i + 1 == text.length()) {
                    currentNode.children = new HashMap<>();
                }
            }
            if (text.isEmpty()) {
                root.children = new HashMap<>();
            }
        }

        /**
         * Checks if a leaf can be reached from the tree with the given string
         *
         * @param text
         * @return if we can each the end of a node.
         */
        public boolean canReachEnd(String text) {
            if (root == null) return false;
            Node currentNode = root;
            for (char element : text.toCharArray()) {
                if (currentNode.isLeaf()) {
                    return true;
                } else if (currentNode.children == null || !currentNode.children.containsKey(element)) {
                    return false;
                }
                currentNode = currentNode.children.get(element);
            }
            return currentNode.isLeaf();
        }

        private class Node {
            HashMap<Character, Node> children = null;

            boolean isLeaf() {
                return children != null && children.isEmpty();
            }
        }
    }
}
