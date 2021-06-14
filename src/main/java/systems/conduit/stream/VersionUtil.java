package systems.conduit.stream;

import jdk.internal.joptsimple.internal.Strings;

import java.util.List;
import java.util.Optional;

/**
 * @author Innectic
 * @since 2/6/2021
 */
public class VersionUtil {

    public static Optional<String> findMatchingVersion(String requested, List<String> available) {
        String[] pieces = requested.split("\\.");
        String whole = Strings.join(pieces, "");

        if (available.contains(whole)) return Optional.of(whole);

        for (int i = 0; i < 2; i++) {
            StringBuilder current = new StringBuilder();
            if (i == 0) current = new StringBuilder(Strings.join(pieces, ""));
            else for (int y = 0; y < i; y++) current.append(pieces[y]);

            System.out.println(current);

            if (available.contains(current.toString())) return Optional.of(current.toString());
        }

        return Optional.empty();
    }
}
