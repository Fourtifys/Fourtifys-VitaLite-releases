package com.fourtifys.gemmining.data;

import com.tonic.data.locatables.BankLocations;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.gameval.ObjectID;

@Getter
@RequiredArgsConstructor
public enum Mine
{
    SURFACE("Surface", new WorldArea(2820, 2998, 6, 6, 0), BankLocations.SHILO_VILLAGE_BANK.getArea(),
            ObjectID.BANK_DEPOSIT_BOX, false),
    UNDERGROUND("Underground", new WorldArea(2826, 9377, 32, 23, 0), new WorldArea(2826, 9377, 32, 23, 0),
            ObjectID.BANK_DEPOSIT_CHEST, true);

    private final String name;
    private final WorldArea rocks;
    private final WorldArea bank;
    private final int depositObject;
    private final boolean underground;

    @Override
    public String toString()
    {
        return name;
    }
}
