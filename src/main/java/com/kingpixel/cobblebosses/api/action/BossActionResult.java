package com.kingpixel.cobblebosses.api.action;

import lombok.Getter;

import java.util.UUID;

/**
 * Immutable result representation for CobbleBosses headless actions.
 */
@Getter
public class BossActionResult {

  public enum Status {
    SUCCESS,
    BOSS_NOT_FOUND,
    ENTITY_NOT_FOUND,
    WORLD_NOT_FOUND,
    WORLD_BLACKLISTED,
    NO_PERMISSION,
    PLAYER_OFFLINE,
    TARGET_NOT_FOUND,
    INVALID_DURATION,
    COOLDOWN,
    ERROR
  }

  private final boolean success;
  private final Status status;
  private final String message;
  private final String bossId;
  private final UUID entityUuid;

  private BossActionResult(boolean success,
                           Status status,
                           String message,
                           String bossId,
                           UUID entityUuid) {
    this.success = success;
    this.status = status != null ? status : Status.ERROR;
    this.message = message != null ? message : "";
    this.bossId = bossId != null ? bossId : "";
    this.entityUuid = entityUuid;
  }

  public static BossActionResult success(String message, String bossId) {
    return new BossActionResult(true, Status.SUCCESS, message, bossId, null);
  }

  public static BossActionResult success(String message, String bossId, UUID entityUuid) {
    return new BossActionResult(true, Status.SUCCESS, message, bossId, entityUuid);
  }

  public static BossActionResult failure(Status status, String message) {
    return new BossActionResult(false, status, message, "", null);
  }

  public static BossActionResult failure(Status status, String message, String bossId) {
    return new BossActionResult(false, status, message, bossId, null);
  }

  public static BossActionResult bossNotFound(String message, String bossId) {
    return failure(Status.BOSS_NOT_FOUND, message, bossId);
  }

  public static BossActionResult entityNotFound(String message) {
    return failure(Status.ENTITY_NOT_FOUND, message);
  }

  public static BossActionResult worldNotFound(String message) {
    return failure(Status.WORLD_NOT_FOUND, message);
  }

  public static BossActionResult offline(String message) {
    return failure(Status.PLAYER_OFFLINE, message);
  }

  public static BossActionResult noPermission(String message) {
    return failure(Status.NO_PERMISSION, message);
  }
}
