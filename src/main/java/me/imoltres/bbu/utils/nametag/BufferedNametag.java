package me.imoltres.bbu.utils.nametag;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;

@Getter
@Setter
@AllArgsConstructor
public class BufferedNametag {

    private String groupName;
    private TextComponent prefix, suffix;
    private boolean friendlyInvis;
    private Player player;

}
