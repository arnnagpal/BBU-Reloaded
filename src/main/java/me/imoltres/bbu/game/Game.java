package me.imoltres.bbu.game;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import lombok.Getter;
import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.team.BBUCage;
import me.imoltres.bbu.data.team.BBUTeam;
import me.imoltres.bbu.utils.GsonFactory;
import me.imoltres.bbu.utils.world.*;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

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

        Bukkit.getScheduler().runTaskAsynchronously(BBU.getInstance(), () -> {
            try {
                placeCages(mainWorld, BBU.getInstance().getTeamController().getTeamsWithCages());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    //TODO: put in a command or config somewhere
    private void resetCages(World world) {
        deleteCages(world);
        try {
            placeCages(world, new ArrayList<>());
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void placeCages(World world, List<BBUTeam> teamsWithCages) throws ExecutionException, InterruptedException {
        List<Position2D> exclusions = new ArrayList<>();
        for (BBUTeam team : teamsWithCages) {
            exclusions.add(new Position2D(team.getCage().cuboid().getMin().getX(), team.getCage().cuboid().getMin().getZ()));
        }

        List<BBUTeam> teams = new ArrayList<>(BBU.getInstance().getTeamController().getAllTeams());
        teams.removeAll(teamsWithCages);
        for (BBUTeam team : teams) {
            System.out.printf("Exclusions: %s\n", Arrays.toString(exclusions.toArray()));
            Position2D position2D = getRandom2DPositionInsideWorldBorder(world, exclusions, 75);
            int y = world.getHighestBlockYAt((int) position2D.getX(), (int) position2D.getY());
            exclusions.add(position2D);
            placeCage(new Position(position2D.getX(), y, position2D.getY()), team, world);
        }
    }

    private void placeCage(Position position, BBUTeam bbuTeam, World world) {
        File schem = new File(BBU.getInstance().getSchemsFolder(), "cage.schem");
        Clipboard clipboard;
        try {
            clipboard = Objects.requireNonNull(ClipboardFormats.findByFile(schem)).load(schem);
        } catch (IOException e) {
            e.printStackTrace();
            Bukkit.shutdown();
            return;
        }
        BlockVector3 maxOffset = clipboard.getMaximumPoint().subtract(clipboard.getMinimumPoint());

        BlockVector3 to = BlockVector3.at(position.getX(), position.getY(), position.getZ());
        Cuboid cage = new Cuboid(
                new Position(to.getX(), to.getY(), to.getZ()),
                new Position(to.add(maxOffset).getX(), to.add(maxOffset).getY(), to.subtract(maxOffset).getZ())
        );

        CuboidRegion region = new CuboidRegion(
                BlockVector3.at(cage.getLowerX(), cage.getLowerY(), cage.getLowerY()),
                BlockVector3.at(cage.getUpperX(), cage.getUpperY(), cage.getUpperZ())
        );
        BlockArrayClipboard c = new BlockArrayClipboard(region);

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(new BukkitWorld(world))) {
            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                    editSession, region, c, region.getMinimumPoint()
            );
            Operations.complete(forwardExtentCopy);
        }

        try (ClipboardWriter writer = BuiltInClipboardFormat.FAST.getWriter(new FileOutputStream(new File(BBU.getInstance().getTempSchemsFolder(), bbuTeam.getColour().name() + "-CAGE-OVERRIDEN-BLOCKS.schem")))) {
            writer.write(c);
        } catch (IOException e) {
            e.printStackTrace();
        }

        clipboard.paste(new BukkitWorld(world), to).flushQueue();

        bbuTeam.setCage(new BBUCage(bbuTeam, cage, cage.getCenter().toWorldPosition(world.getName())));

        BBU.getInstance().println(
                "&aTeam '&" + bbuTeam.getColour().getChatColor().getChar() + bbuTeam.getColour().name() + "&a' cage spawned at &7" + bbuTeam.getCage().spawnPosition().toString() + "&a."
        );
        BBU.getInstance().getTeamSpawnsConfig().getConfiguration().set("team." + bbuTeam.getColour().name(), GsonFactory.getCompactGson().toJson(cage));
    }

    private void deleteCages(World world) {
        for (BBUTeam team : BBU.getInstance().getTeamController().getAllTeams()) {
            String cageStr = BBU.getInstance().getTeamSpawnsConfig().getString("team." + team.getColour().name());

            if (cageStr != null && !cageStr.isEmpty()) {
                Cuboid oldCage = GsonFactory.getCompactGson().fromJson(cageStr, Cuboid.class);
                File cageOverride = new File(BBU.getInstance().getTempSchemsFolder(), team.getColour().name() + "-CAGE-OVERRIDEN-BLOCKS.schem");

                if (cageOverride.exists()) {
                    try {
                        BuiltInClipboardFormat.FAST.load(cageOverride).paste(new BukkitWorld(world),
                                BlockVector3.at(oldCage.getLowerX(), oldCage.getLowerY(), oldCage.getLowerY())).flushQueue();

                        cageOverride.delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private Position2D getRandom2DPositionInsideWorldBorder(World world, List<Position2D> exclusions, int range) throws ExecutionException, InterruptedException {
        Rectangle world2D = new Rectangle(
                new Position2D(-(world.getWorldBorder().getSize() / 2), -(world.getWorldBorder().getSize() / 2)),
                new Position2D(world.getWorldBorder().getSize() / 2, world.getWorldBorder().getSize() / 2)
        );

        Position2D position2D = world2D.randomPosition().toIntPosition();
        double x = position2D.getX();
        double z = position2D.getY();
        if (!world.getChunkAt((int) x, (int) z).isLoaded())
            world.getChunkAt((int) x, (int) z).load();

        int y = world.getHighestBlockYAt((int) x, (int) z) + 3;
        WorldPosition worldPosition = new WorldPosition(position2D.getX(), world.getHighestBlockYAt((int) x, (int) z) + 3, position2D.getY(), world.getName());
        while (!worldPosition.isSafe()) {
            worldPosition.add(0, 1, 0);
        }

        x = worldPosition.getX();
        y = (int) worldPosition.getY();
        z = worldPosition.getZ();

        System.out.printf("Checking position: %s, %s, %s\n", x, y, z);
        for (Position2D p : exclusions) {
            while (p.distance(position2D) < range) {
                if (Bukkit.getScheduler().callSyncMethod(BBU.getInstance(), () -> BBU.getInstance().isDisabling()).get())
                    break;
                position2D = getRandom2DPositionInsideWorldBorder(world, exclusions, range);
            }
        }
        return position2D;
    }


}
