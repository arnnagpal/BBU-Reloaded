package me.imoltres.bbu.utils.config;

import lombok.RequiredArgsConstructor;
import me.imoltres.bbu.utils.config.type.BasicConfigurationFile;

@RequiredArgsConstructor
public class ConfigGetter<T> {

    private final BasicConfigurationFile config;
    private final String path;

    @Override
    public String toString() {
        return config.getString(path);
    }

    public T get() {
        return (T) config.get(path);
    }

}
