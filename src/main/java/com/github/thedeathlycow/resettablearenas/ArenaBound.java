package com.github.thedeathlycow.resettablearenas;

import com.google.gson.*;

import java.lang.reflect.Type;

public class ArenaBound implements JsonDeserializer<ArenaBound> {
    private int x;
    private int z;

    /**
     *
     * @param x ChunkX
     * @param z ChunkZ
     */
    public ArenaBound(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public ArenaBound() {
        this(0, 0);
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    @Override
    public ArenaBound deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        int x = object.get("x").getAsInt() / 16;
        int z = object.get("z").getAsInt() / 16;
        return new ArenaBound(x, z);
    }
}
