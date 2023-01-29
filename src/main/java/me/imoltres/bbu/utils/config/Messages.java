package me.imoltres.bbu.utils.config;

import lombok.RequiredArgsConstructor;
import me.imoltres.bbu.BBU;
import me.imoltres.bbu.utils.config.type.BasicConfigurationFile;

/**
 * Messages imported from the messages.yml config
 */
public class Messages<T> extends ConfigGetter<T> {

    public static String SCOREBOARD_TITLE = new Messages<String>("scoreboard-title").get();
    public static String BEACON_DESTROYED = new Messages<String>("beacon-destroyed").get();
    public static String FINAL_DEATH = new Messages<String>("final-death").get();

    public static String ALL_CHAT_FORMAT = new Messages<String>("chat-format.all").get();
    public static String NO_TEAM_CHAT_FORMAT = new Messages<String>("chat-format.no-team").get();
    public static String TEAM_CHAT_FORMAT = new Messages<String>("chat-format.team").get();

    Messages(String path) {
        super(BBU.getInstance().getMessagesConfig(), path);
    }
}
