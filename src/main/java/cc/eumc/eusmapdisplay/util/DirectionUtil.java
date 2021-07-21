package cc.eumc.eusmapdisplay.util;

import org.bukkit.block.BlockFace;

import java.util.HashMap;
import java.util.Map;

public class DirectionUtil {
    static Map<BlockFace, BlockFace> Opposite = new HashMap<>();

    static {
        Opposite.put(BlockFace.SOUTH, BlockFace.NORTH);
        Opposite.put(BlockFace.NORTH, BlockFace.SOUTH);

        Opposite.put(BlockFace.EAST, BlockFace.WEST);
        Opposite.put(BlockFace.WEST, BlockFace.EAST);
    }

    public static BlockFace getOpposite(BlockFace blockFace) {
        return Opposite.get(blockFace);
    }

    /**
     * For example: NORTH_NORTH_WEST -> NORTH
     * @param blockFace
     * @return
     */
    public static BlockFace standardize(BlockFace blockFace) {
        String facing = blockFace.toString();

        if (!facing.contains("_")) {
            return blockFace;
        }
        String[] split = facing.split("_");

        return BlockFace.valueOf(split[0]);
    }
}
