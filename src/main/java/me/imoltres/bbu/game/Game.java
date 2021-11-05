package me.imoltres.bbu.game;

import com.qrakn.phoenix.lang.file.type.BasicConfigurationFile;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.math.BlockVector3;
import lombok.Getter;
import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.team.BBUCage;
import me.imoltres.bbu.data.team.BBUTeam;
import me.imoltres.bbu.utils.GsonFactory;
import me.imoltres.bbu.utils.world.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Game {

    private final GameProgression progression = new GameProgression();
    private final GameThread gameThread = new GameThread(this);

    @Getter
    private boolean paused = false;

    @Getter
    private final int border;

    private Position fortressPosition;

    public Game() {
        border = BBU.getInstance().getMainConfig().getInteger("border");

        setupWorlds();
    }

    public void startGame() {

    }

    public void pauseGame() {
        paused = true;
    }

    public void stopGame() {

    }

    public GameState getGameState() {
        return progression.getGameState();
    }

    public void setGameState(GameState state) {
        System.out.printf("Changing game state from %s to %s\n", getGameState().name(), state.name());
        progression.setGameState(state);
    }

    private void setupWorlds() {
        //Get main world
        World mainWorld = Bukkit.getWorlds().get(0);
        //Get nether world
        World netherWorld = Bukkit.getWorlds().get(1);

        mainWorld.getWorldBorder().setCenter(0, 0);
        mainWorld.getWorldBorder().setSize(border);

        netherWorld.getWorldBorder().setCenter(0, 0);
        netherWorld.getWorldBorder().setSize(border);

        resetCages(mainWorld);
    }

    private void resetCages(World world) {
        BasicConfigurationFile teamSpawns = BBU.getInstance().getTeamSpawnsConfig();
        File schem = new File(BBU.getInstance().getSchemsFolder(), "cage.schem");
        Clipboard clipboard;
        try {
            clipboard = ClipboardFormats.findByFile(schem).load(schem);
        } catch (IOException e) {
            e.printStackTrace();
            Bukkit.shutdown();
            return;
        }

        int length = clipboard.getLength() - 1;
        int width = clipboard.getWidth() - 1;
        int height = clipboard.getHeight() - 1;

        List<Position2D> exclusions = new ArrayList<>();
        for (BBUTeam bbuTeam : BBU.getInstance().getTeamController().getAllTeams()) {
            String oldCageStr = teamSpawns.getString("team." + bbuTeam.getColour().name());
            if (oldCageStr != null && !oldCageStr.isEmpty()) {
                Cuboid oldCage = GsonFactory.getCompactGson().fromJson(oldCageStr, Cuboid.class);
                for (Position content : oldCage.contents()) {
                    WorldPosition worldPosition = new WorldPosition(content.getX(), content.getY(), content.getZ(), world.getName());
                    worldPosition.toBukkitLocation().getBlock().setType(Material.AIR);
                }
            }

            Position2D position2D = getRandom2DPositionInsideWorldBorder(world, exclusions, 75);
            int y = world.getHighestBlockYAt((int) position2D.getX(), (int) position2D.getY()) - 1;
            exclusions.add(position2D);

            System.out.println(position2D);

            Cuboid cage = new Cuboid(
                    new Position(position2D.getX(), y, position2D.getY()),
                    new Position(position2D.getX() + length, y + height, position2D.getY() + (width / 2.0))
            );

            bbuTeam.setCage(new BBUCage(bbuTeam, cage, cage.getMin().add((length / 2.0) - 0.5, 1, -(width / 2.0)).toWorldPosition(world.getName())));

            BBU.getInstance().println(
                    "&aTeam '&" + bbuTeam.getColour().getChatColor().getChar() + bbuTeam.getColour().name() + "&a' cage spawned at &7" + bbuTeam.getCage().spawnPosition().toString() + "&a."
            );
            EditSession ses = clipboard.paste(new BukkitWorld(world), BlockVector3.at(position2D.getX(), y, position2D.getY()));
            ses.close();
            for (Position content : cage.contents()) {
                WorldPosition worldPosition = new WorldPosition(content.getX(), content.getY(), content.getZ(), world.getName());
                worldPosition.toBukkitLocation().getBlock().setType(Material.RED_WOOL);
            }


            teamSpawns.getConfiguration().set("team." + bbuTeam.getColour().name(), GsonFactory.getCompactGson().toJson(cage));
        }
    }

    private Position2D getRandom2DPositionInsideWorldBorder(World world, List<Position2D> exclusions, int range) {
        Rectangle world2D = new Rectangle(
                new Position2D(-(world.getWorldBorder().getSize() / 2), -(world.getWorldBorder().getSize() / 2)),
                new Position2D(world.getWorldBorder().getSize() / 2, world.getWorldBorder().getSize() / 2)
        );

        Position2D position2D = world2D.randomPosition().toIntPosition();
        for (Position2D p : exclusions) {
            //TODO: make thread-safe
            while (p.distance(position2D) < range) {
                position2D = getRandom2DPositionInsideWorldBorder(world, exclusions, range);
            }
        }
        return position2D;
    }


}
