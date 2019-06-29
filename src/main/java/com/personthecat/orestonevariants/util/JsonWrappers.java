package com.personthecat.orestonevariants.util;

import org.hjson.JsonObject;
import org.hjson.JsonValue;

import static com.personthecat.orestonevariants.util.HjsonTools.*;

public class JsonWrappers {
    public static class JsonInt {
        private final JsonObject obj;
        private final String field;
        public JsonValue value;
        public int raw;

        public JsonInt(JsonObject obj, String field, int defaultVal) {
            this.obj = obj;
            this.field = field;
            set(defaultVal);
        }

        public JsonInt comment(String comment) {
            value.setComment(comment);
            return this;
        }

        public JsonInt set(int val) {
            this.value = JsonValue.valueOf(val);
            this.raw = val;
            setOrAdd(obj, field, value);
            return this;
        }
    }
}