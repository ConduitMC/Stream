package systems.conduit.stream;

import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Mojang2Tsrg {

    public Map<String, String> classMap;

    public Mojang2Tsrg(Path map, Path out) {
        classMap = new HashMap<>();
        try {
            loadClasses(map.toFile());
            writeTsrg(map.toFile(), out.toFile());
            map.toFile().delete();
        } catch (IOException e) {
            Logger.exception("Error converting Minecraft server mappings", e);
            System.exit(0);
        }
    }

    public String typeToDescriptor(String type) {
        if (type.endsWith("[]")) return "[" + typeToDescriptor(type.substring(0, type.length() - 2));
        if (type.contains(".")) return "L" + classMap.getOrDefault(type.replaceAll("\\.", "/"), type.replaceAll("\\.", "/")) + ";";
        switch (type) {
            case "void": return "V";
            case "int": return "I";
            case "float": return "F";
            case "char": return "C";
            case "byte": return "B";
            case "boolean": return "Z";
            case "double": return "D";
            case "long": return "J";
            case "short": return "S";
            default: return "";
        }
    }

    public void loadClasses(File map) throws IOException {
        try (InputStreamReader reader = new FileReader(map); BufferedReader buf = new BufferedReader(reader)) {
            boolean loop = true;
            while (loop) {
                String s = buf.readLine();
                if (s != null && !s.isEmpty()) {
                    if (s.startsWith("#")) continue;
                    if (!s.startsWith(" ")) { // We only care about lines mapping classes. Read the class name into the map.
                        String[] parts = getObfAndClassName(s.split(" "));
                        classMap.put(parts[1], parts[0]);
                    }
                } else {
                    loop = false;
                }
            }
        }
    }

    public void writeTsrg(File map, File out) throws IOException {
        try (BufferedReader txt = new BufferedReader(new FileReader(map)); BufferedWriter buf = new BufferedWriter(new FileWriter(out))) {
            String s;
            while ((s = txt.readLine()) != null) {
                if (s.startsWith("#")) continue;
                if (s.startsWith(" ")) { // This is a field or a method.
                    s = s.substring(4);
                    String[] parts = s.split(" ");
                    if (parts.length != 4)continue;
                    if (parts[1].endsWith(")")) { // This is a method.
                        String returnType = parts[0].contains(":") ? parts[0].split(":")[2] : parts[0]; // Split line numbers.
                        String obfName = parts[3];
                        String methodName = parts[1].split("\\(")[0]; // Separate params from name.
                        String params = parts[1].split("\\(")[1];
                        params = params.substring(0, params.length() - 1);

                        returnType = typeToDescriptor(returnType);
                        params = Arrays.stream(params.split(",")).map(this::typeToDescriptor).collect(Collectors.joining());

                        buf.write("\t" + obfName + " (" + params + ")" + returnType + " " + methodName + "\n");
                    } else { // This is a field.
                        String fieldName = parts[1];
                        String obfName = parts[3];
                        buf.write("\t" + obfName + " " + fieldName + "\n");
                    }
                } else { // Classes have no dependencies.
                    String[] parts = getObfAndClassName(s.split(" "));
                    if (parts[0].contains(".")) parts[0] = parts[0].replaceAll("\\.", "/");
                    buf.write(parts[0] + " " + parts[1] + "\n"); // Write class entry.
                }
            }
        }
    }

    private String[] getObfAndClassName(String[] parts) {
        if (parts.length != 3) return new String[]{};
        String className = parts[0].replaceAll("\\.", "/");
        String obfName = parts[2].substring(0, parts[2].length() - 1);
        if (obfName.contains(".")) obfName = obfName.replaceAll("\\.", "/");
        return new String[]{obfName, className};
    }
}
