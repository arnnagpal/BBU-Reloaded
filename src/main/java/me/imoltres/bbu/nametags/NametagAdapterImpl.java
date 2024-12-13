package me.imoltres.bbu.nametags;

import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.team.BBUTeam;
import me.imoltres.bbu.utils.CC;
import me.imoltres.bbu.utils.nametag.BufferedNametag;
import me.imoltres.bbu.utils.nametag.NametagAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class NametagAdapterImpl implements NametagAdapter {
    @Override
    public List<BufferedNametag> getPlate() {
        List<BufferedNametag> tags = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            BBUTeam team = BBU.getInstance().getTeamController().getTeam(player);

            if (team != null) {
                tags.add(new BufferedNametag(
                        team.getName() + "_TAG",
                        CC.translate(team.getColour().getChatColor() + "[" + team.getName() + "] "),
                        CC.translate(""),
                        false,
                        player
                ));
            } else {
                if (player.isOp()) {
                    tags.add(new BufferedNametag(
                            "ADMINS_TAG",
                            CC.translate("&c[ADMIN] "),
                            CC.translate(""),
                            false,
                            player
                    ));
                } else {
                    tags.add(new BufferedNametag(
                            "DEFAULT_TAG",
                            CC.translate("&a"),
                            CC.translate(""),
                            false,
                            player
                    ));
                }
            }
        }

        return tags;
    }

    @Override
    public boolean showHealthBelowName() {
        return true;
    }
}
