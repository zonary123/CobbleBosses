package com.kingpixel.cobblebosses.api.model;

import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Immutable snapshot context representing a player's active reward bosses and boss system context.
 */
@Getter
public class PlayerBossContext {

  private final UUID playerUuid;
  private final String playerName;
  private final List<ActiveBossView> ownedActiveBosses;
  private final int totalConfiguredBosses;
  private final boolean hasAdminPermission;

  public PlayerBossContext(UUID playerUuid,
                           String playerName,
                           List<ActiveBossView> ownedActiveBosses,
                           int totalConfiguredBosses,
                           boolean hasAdminPermission) {
    this.playerUuid = playerUuid;
    this.playerName = playerName != null ? playerName : "";
    this.ownedActiveBosses = ownedActiveBosses != null
      ? Collections.unmodifiableList(List.copyOf(ownedActiveBosses))
      : Collections.emptyList();
    this.totalConfiguredBosses = totalConfiguredBosses;
    this.hasAdminPermission = hasAdminPermission;
  }

  public boolean hasActiveOwnedBosses() {
    return !ownedActiveBosses.isEmpty();
  }

  public int getActiveOwnedBossCount() {
    return ownedActiveBosses.size();
  }
}
