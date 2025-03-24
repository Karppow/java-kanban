package http.handler.typeAdapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;

public class DurationAdapter extends TypeAdapter<Duration> {
    @Override
    public void write(JsonWriter out, Duration duration) throws IOException {
        if (duration == null) {
            out.nullValue(); // Если duration равен null, записываем null в JSON
        } else {
            out.value(duration.toMillis());
        }
    }

    @Override
    public Duration read(JsonReader in) throws IOException {
        long millis = in.nextLong();
        return Duration.ofMillis(millis);
    }
}
