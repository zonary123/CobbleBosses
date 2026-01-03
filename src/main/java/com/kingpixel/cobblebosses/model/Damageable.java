package com.kingpixel.cobblebosses.model;

import lombok.Data;

@Data
public class Damageable {

    private boolean enabled;
    private Double untilLifePercentage;

    public Damageable() {
        this.enabled = false;
        this.untilLifePercentage = 10.0D;
    }

}
