package net.maxbel.takeitout.compat;

import com.github.quiltservertools.ledger.callbacks.ItemInsertCallback;
import com.github.quiltservertools.ledger.callbacks.ItemRemoveCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LedgerCompat {
    private static final Logger LOGGER =
            LoggerFactory.getLogger("takeitout/ledger");

    private static final String SOURCE = "takeitout";

    private LedgerCompat() {
    }

    public static void logItemRemove(
            ServerPlayer player,
            ServerLevel world,
            BlockPos pos,
            ItemStack stack
    ) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        try {
            ItemRemoveCallback.EVENT
                    .invoker()
                    .remove(
                            stack.copy(),
                            pos,
                            world,
                            SOURCE,
                            player
                    );

            LOGGER.debug(
                    "Logged Ledger item removal: player={}, world={}, pos={}, stack={}",
                    player.getName().getString(),
                    world.dimension().identifier(),
                    pos,
                    stack
            );
        } catch (RuntimeException | LinkageError e) {
            LOGGER.warn(
                    "Failed to log Ledger item removal for {} at {}",
                    player.getName().getString(),
                    pos,
                    e
            );
        }
    }

    public static void logItemInsert(
            ServerPlayer player,
            ServerLevel world,
            BlockPos pos,
            ItemStack stack
    ) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        try {
            ItemInsertCallback.EVENT
                    .invoker()
                    .insert(
                            stack.copy(),
                            pos,
                            world,
                            SOURCE,
                            player
                    );

            LOGGER.debug(
                    "Logged Ledger item insertion: player={}, world={}, pos={}, stack={}",
                    player.getName().getString(),
                    world.dimension().identifier(),
                    pos,
                    stack
            );
        } catch (RuntimeException | LinkageError e) {
            LOGGER.warn(
                    "Failed to log Ledger item insertion for {} at {}",
                    player.getName().getString(),
                    pos,
                    e
            );
        }
    }
}
