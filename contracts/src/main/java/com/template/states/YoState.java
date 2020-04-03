package com.template.states;

import com.google.common.collect.ImmutableList;
import com.template.contracts.YoContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@BelongsToContract(YoContract.class)
public class YoState implements ContractState {
    private Party origin;
    private Party target;
    private String yo;
    public YoState(Party origin, Party target, String yo) {
        this.origin = origin;
        this.target = target;
        this.yo = yo;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(origin, target);
    }
}
