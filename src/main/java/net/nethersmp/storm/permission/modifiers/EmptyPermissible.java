package net.nethersmp.storm.permission.modifiers;

import lombok.SneakyThrows;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.HashMap;

public class EmptyPermissible extends PermissibleBase {


    public static EmptyPermissible INSTANCE = new EmptyPermissible();

    @SneakyThrows
    public EmptyPermissible() {
        super(null);
        PermissibleFields.PERMISSIONS.set(this, new HashMap<String, PermissionAttachmentInfo>());
        PermissibleFields.ATTACHMENTS.set(this, new ArrayList<PermissionAttachment>());
    }
}
