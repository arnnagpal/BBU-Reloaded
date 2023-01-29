package me.imoltres.bbu.menus;

import lombok.RequiredArgsConstructor;
import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.team.BBUTeam;
import me.imoltres.bbu.utils.CC;
import me.imoltres.bbu.utils.general.PlayerUtils;
import me.imoltres.bbu.utils.item.ItemBuilder;
import me.imoltres.bbu.utils.menu.Button;
import me.imoltres.bbu.utils.menu.Menu;
import me.imoltres.bbu.utils.menu.button.Filler;
import me.imoltres.bbu.utils.world.WorldPosition;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrackingCompassMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return "&cChoose a beacon to track";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        for (int i = 0; i < 45; ++i) {
            if (!((i >= 33 && i <= 38) || (i >= 42) || (i >= 27 && i <= 29))) {
                buttons.put(i, new Filler(true));
            }
        }

        List<Button> teams = new ArrayList<>();
        for (BBUTeam team : BBU.getInstance().getTeamController().getAllTeams()) {
            teams.add(new TeamButton(team));
        }

        int x = 0;
        for (int i = 10; i < 17; i++) {
            if (x > (teams.size() - 1)) {
                buttons.put(i, new Filler(true));
                continue;
            }

            buttons.put(i, teams.get(x));
            x++;
        }

        buttons.put(31, new TeamButton(BBU.getInstance().getTeamController().getTeam(player)));

        return buttons;
    }

    @RequiredArgsConstructor
    private static class TeamButton extends Button {
        @Nullable
        private final BBUTeam team;

        @Override
        public ItemStack getButtonItem(Player player) {
            if (team != null) {
                ItemBuilder builder = new ItemBuilder(Material.getMaterial((team.getColour().getMaterial() == null ? team.getColour().name() : team.getColour().getMaterial()) + "_WOOL"));
                if (!BBU.getInstance().getGame().getGameState().isPvp()) {
                    builder = builder.name(team.getRawDisplayName()).lore("&cCan't track teams on grace period.");
                } else {
                    builder.localizedName(team.getColour().name());
                    builder = builder.name(team.getRawDisplayName())
                            .lore(
                                    (!team.hasBeacon() ? "&cThis team's beacon is destroyed." :
                                            (team.getBeacon() != null ? "&aClick to track" : "&cThis team hasn't placed down their beacon yet."))
                            );
                }

                return builder.build();
            }

            return new ItemBuilder(Material.BLACK_WOOL).name("&cYou're not in a team!").build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            player.closeInventory();
            if (!BBU.getInstance().getGame().getGameState().isPvp())
                return;

            if (team != null) {
                if (team.getBeacon() == null) {
                    player.sendMessage(CC.translate("&cNo beacon to track..."));
                    return;
                }

                WorldPosition worldPosition = team.getBeacon()
                        .toWorldPosition(BBU.getInstance().getGame().getOverworld().getName());

                if (!PlayerUtils.trackPosition(player, worldPosition)) {
                    System.out.println("Failed to track " + worldPosition + " due to a lack of a compass.");
                }
            }
        }
    }
}
