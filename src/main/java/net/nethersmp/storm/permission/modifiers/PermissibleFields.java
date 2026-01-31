package net.nethersmp.storm.permission.modifiers;

import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.permissions.PermissibleBase;

import java.lang.reflect.Field;

public class PermissibleFields {

    public static final Field PERMISSIONS;
    public static final Field ATTACHMENTS;
    public static final Field ENTITY_PERMISSIONS;

    static {
        try {
            PERMISSIONS = PermissibleBase.class.getDeclaredField("permissions");
            ATTACHMENTS = PermissibleBase.class.getDeclaredField("attachments");
            ENTITY_PERMISSIONS = CraftHumanEntity.class.getDeclaredField("perm");
            PERMISSIONS.setAccessible(true);
            ATTACHMENTS.setAccessible(true);
            ENTITY_PERMISSIONS.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

}
