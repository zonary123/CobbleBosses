package com.kingpixel.cobblebosses.api.model;

import lombok.Getter;

import java.util.Optional;
import java.util.UUID;

/**
 * Immutable view representing an active Boss entity currently spawned in the server.
 */
@Getter
public class ActiveBossView {

  private final UUID entityUuid;
  private final String bossId;
  private final String species;
  private final int level;
  private final float currentHealth;
  private final float maxHealth;
  private final String worldId;
  private final double x;
  private final double y;
  private final double z;
  private final UUID ownerUuid;
  private final String ownerName;
  private final long despawnTimestampMillis;
  private final long remainingSeconds;
  private final boolean inBattle;
  private final boolean catchable;

  public ActiveBossView(UUID entityUuid,
                        String bossId,
                        String species,
                        int level,
                        float currentHealth,
                        float maxHealth,
                        String worldId,
                        double x,
                        double y,
                        double z,
                        UUID ownerUuid,
                        String ownerName,
                        long despawnTimestampMillis,
                        long remainingSeconds,
                        boolean inBattle,
                        boolean catchable) {
    this.entityUuid = entityUuid;
    this.bossId = bossId != null ? bossId : "";
    this.species = species != null ? species : "";
    this.level = level;
    this.currentHealth = currentHealth;
    this.maxHealth = maxHealth;
    this.worldId = worldId != null ? worldId : "";
    this.x = x;
    this.y = y;
    this.z = z;
    this.ownerUuid = ownerUuid;
    this.ownerName = ownerName != null ? ownerName : "";
    this.despawnTimestampMillis = despawnTimestampMillis;
    this.remainingSeconds = Math.max(0L, remainingSeconds);
    this.inBattle = inBattle;
    this.catchable = catchable;
  }

  public boolean hasOwner() {
    return ownerUuid != null;
  }

  public Optional<UUID> getOwnerUuidOptional() {
    return Optional.ofNullable(ownerUuid);
  }
}
