package systems.conduit.stream.json.minecraft;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class MinecraftVersion {

    private MinecraftDownload downloads = new MinecraftDownload();
    private List<MinecraftLibrary> libraries = new ArrayList<>();
}
