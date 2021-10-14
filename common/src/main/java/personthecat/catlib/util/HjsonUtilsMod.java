package personthecat.catlib.util;

import org.hjson.JsonObject;
import org.hjson.ParseException;
import personthecat.fresult.Result;

import java.io.*;
import java.util.Optional;

public class HjsonUtilsMod {
    public static Optional<JsonObject> readSuppressing(final InputStream is) {
        return Result.define(IOException.class, Result::WARN)
            .and(ParseException.class, Result::IGNORE)
            .suppress(() -> JsonObject.readHjson(new InputStreamReader(is), HjsonUtils.FORMATTER).asObject())
            .get();
    }
}
