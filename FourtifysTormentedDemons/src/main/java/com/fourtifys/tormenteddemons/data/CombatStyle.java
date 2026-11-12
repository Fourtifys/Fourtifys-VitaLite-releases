package com.fourtifys.tormenteddemons.data;

import com.fourtifys.tormenteddemons.api.Combat;
import com.tonic.api.widgets.PrayerAPI;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CombatStyle
{
    MELEE("Melee", 0, Combat.MELEE_ATTACK),
    RANGED("Ranged", 1, Combat.RANGED_ATTACK),
    MAGIC("Magic", 2, Combat.MAGIC_ATTACK);

    private final String displayName;
    private final int headIcon;
    private final int attackAnimation;

    public PrayerAPI protection()
    {
        switch (this)
        {
            case MELEE:
                return PrayerAPI.PROTECT_FROM_MELEE;
            case RANGED:
                return PrayerAPI.PROTECT_FROM_MISSILES;
            default:
                return PrayerAPI.PROTECT_FROM_MAGIC;
        }
    }

    public PrayerAPI offence()
    {
        switch (this)
        {
            case MELEE:
                return PrayerAPI.getMeleePrayer();
            case RANGED:
                return PrayerAPI.getRangedPrayer();
            default:
                return PrayerAPI.getMagicPrayer();
        }
    }

    public static CombatStyle fromHeadIcon(int headIcon)
    {
        return Arrays.stream(values()).filter(style -> style.headIcon == headIcon).findFirst().orElse(null);
    }

    public static CombatStyle fromAnimation(int animation)
    {
        return Arrays.stream(values()).filter(style -> style.attackAnimation == animation).findFirst().orElse(null);
    }

    @Override
    public String toString()
    {
        return displayName;
    }
}
