package com.template.contracts;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

public class YoContract implements Contract {
    public static final String ID = "com.template.contracts.YoContract";
    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
        //no checks, just a test
    }

    public interface Commands extends CommandData {
        class Send implements YoContract.Commands {
        }
    }
}
