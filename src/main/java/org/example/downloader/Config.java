package org.example.downloader;

import com.google.gson.Gson;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class Config {
    public Set<String> urls;
    public int maxTime;
    public String outputDir;
    public int maxConcurrentDownloads;

    public static Config fromJsonFile(File configFile) throws IOException {
        Gson gson = new Gson();
        String content = new String(Files.readAllBytes(configFile.toPath()));
        return gson.fromJson(content, Config.class);
    }
}
