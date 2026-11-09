package com.fourtifys.gemmining.data;

import com.fourtifys.gemmining.GemMiningPlugin;
import com.fourtifys.gemmining.api.GemMiningBuilder;
import com.tonic.util.handler.StepHandler;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum State
{
    MINING(GemMiningBuilder::mining),
    BANKING(GemMiningBuilder::banking);

    private final Function<GemMiningPlugin, StepHandler> supplier;

    public StepHandler handler(GemMiningPlugin plugin)
    {
        return supplier.apply(plugin);
    }

    public State next()
    {
        return this == MINING ? BANKING : MINING;
    }
}
