package com.fourtifys.tormenteddemons.data;

import com.fourtifys.tormenteddemons.TormentedDemonsPlugin;
import com.fourtifys.tormenteddemons.api.BankBuilder;
import com.fourtifys.tormenteddemons.api.FightBuilder;
import com.fourtifys.tormenteddemons.api.TravelBuilder;
import com.tonic.util.handler.StepHandler;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum State
{
    BANKING(BankBuilder::banking),
    TRAVELLING(TravelBuilder::travelling),
    FIGHTING(FightBuilder::fighting);

    private final Function<TormentedDemonsPlugin, StepHandler> supplier;

    public StepHandler handler(TormentedDemonsPlugin plugin)
    {
        return supplier.apply(plugin);
    }

    public State next()
    {
        return values()[(ordinal() + 1) % values().length];
    }
}
