package com.fourtifys.tormenteddemons;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tonic.util.handler.StepContext;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class StepContextTest
{
    private static final Pattern STORES_NULL = Pattern.compile("context\\.(getOrDefault|put|putIfAbsent)\\([^;]*,\\s*null\\)");

    @Test
    void aNullValueBreaksTheHandlerReset()
    {
        StepContext context = new StepContext();
        context.getOrDefault("style", null);
        assertThrows(NullPointerException.class, context::values);
        StepContext read = new StepContext();
        read.get("style");
        assertDoesNotThrow(read::values);
    }

    @Test
    void theBuilderNeverStoresNull() throws Exception
    {
        try (Stream<Path> builders = Files.list(Path.of("src/main/java/com/fourtifys/tormenteddemons/api")))
        {
            for (Path builder : builders.filter(path -> path.toString().endsWith("Builder.java")).collect(Collectors.toList()))
            {
                String source = Files.readString(builder);
                assertFalse(STORES_NULL.matcher(source).find(), builder + ": " + STORES_NULL.matcher(source).results().map(match -> match.group()).findFirst().orElse(""));
            }
        }
    }
}
