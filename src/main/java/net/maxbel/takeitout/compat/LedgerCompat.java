package net.maxbel.takeitout.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.UUID;

public final class LedgerCompat {
    private static final Logger LOGGER = LoggerFactory.getLogger("takeitout/ledger");

    private static final Object CONTAINER_EVENT_LOGGER;
    private static final Method LOG_ITEM_REMOVE;
    private static final Method LOG_ITEM_INSERT;
    private static final Constructor<?> ACTOR_CONSTRUCTOR;

    static {
        Object logger = null;
        Method logRemove = null;
        Method logInsert = null;
        Constructor<?> actorCtor = null;

        try {
            Class<?> actorClass = Class.forName("com.github.quiltservertools.ledger.actions.ActionActor");
            actorCtor = actorClass.getConstructor(String.class, UUID.class);

            Class<?> loggerClass = Class.forName("com.github.quiltservertools.ledger.events.ContainerEventLogger");
            Method getInstance = loggerClass.getMethod("getInstance");
            logger = getInstance.invoke(null);
            logRemove = loggerClass.getMethod("logItemRemove", ItemStack.class, BlockPos.class, actorClass);
            logInsert = loggerClass.getMethod("logItemInsert", ItemStack.class, BlockPos.class, actorClass);

            LOGGER.info("Ledger detected – compatibility initialized successfully");
        } catch (ClassNotFoundException e) {
            LOGGER.info("Ledger not detected – mod not present, container logging disabled");
        } catch (NoSuchMethodException e) {
            LOGGER.warn("Ledger detected but API mismatch – expected methods not found", e);
        } catch (ReflectiveOperationException e) {
            LOGGER.warn("Ledger detected but initialization failed", e);
        }

        CONTAINER_EVENT_LOGGER = logger;
        LOG_ITEM_REMOVE = logRemove;
        LOG_ITEM_INSERT = logInsert;
        ACTOR_CONSTRUCTOR = actorCtor;

        if (CONTAINER_EVENT_LOGGER != null) {
            LOGGER.info("Ledger compatibility: ACTIVE");
        } else {
            LOGGER.info("Ledger compatibility: INACTIVE (container logging disabled)");
        }
    }

    private LedgerCompat() {}

    public static void logItemRemove(ServerPlayer player, BlockPos pos, ItemStack stack) {
        LOGGER.debug("logItemRemove called for player {} at {}", player.getName().getString(), pos);
        logItemAction(LOG_ITEM_REMOVE, player, pos, stack);
    }

    public static void logItemInsert(ServerPlayer player, BlockPos pos, ItemStack stack) {
        LOGGER.debug("logItemInsert called for player {} at {}", player.getName().getString(), pos);
        logItemAction(LOG_ITEM_INSERT, player, pos, stack);
    }

    private static void logItemAction(Method method, ServerPlayer player, BlockPos pos, ItemStack stack) {
        if (method == null || ACTOR_CONSTRUCTOR == null) {
            LOGGER.debug("Ledger not available – skipping logItemAction");
            return;
        }
        try {
            Object actor = ACTOR_CONSTRUCTOR.newInstance(player.getName().getString(), player.getUUID());
            method.invoke(CONTAINER_EVENT_LOGGER, stack, pos, actor);
            LOGGER.debug("Ledger {} logged for player {} at {}: {}",
                    method.getName(), player.getName().getString(), pos, stack.getDisplayName().getString());
        } catch (ReflectiveOperationException e) {
            LOGGER.warn("Failed to invoke Ledger {} for player {} at {}",
                    method != null ? method.getName() : "null",
                    player.getName().getString(), pos, e);
        }
    }
}
