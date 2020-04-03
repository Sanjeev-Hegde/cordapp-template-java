package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.template.contracts.YoContract;
import com.template.states.YoState;
import net.corda.core.contracts.Command;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

public class YoFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<SignedTransaction> {
        private Party origin;
        private Party target;
        private String yo;

        public Initiator(Party target, String yo) {
            this.target = target;
            this.yo = yo;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
            origin = getOurIdentity();
            YoState yoState = new YoState(origin,target,yo);
            final Command<YoContract.Commands.Send> txCommand = new Command<>(
                    new YoContract.Commands.Send(),
                    ImmutableList.of(origin.getOwningKey(),target.getOwningKey()));

            final TransactionBuilder txBuilder = new TransactionBuilder(notary)
                    .addOutputState(yoState, YoContract.ID)
                    .addCommand(txCommand);
            txBuilder.verify(getServiceHub());
            final SignedTransaction partSignedTx = getServiceHub().signInitialTransaction(txBuilder);

            FlowSession otherPartySession = initiateFlow(target);
            final SignedTransaction fullySignedTx = subFlow(
                    new CollectSignaturesFlow(partSignedTx, ImmutableSet.of(otherPartySession), CollectSignaturesFlow.Companion.tracker()));

            // Notarise and record the transaction in both parties' vaults.
            return subFlow(new FinalityFlow(fullySignedTx, ImmutableSet.of(otherPartySession)));
        }
    }


    @InitiatedBy(Initiator.class)
    public static class Acceptor extends FlowLogic<SignedTransaction> {
        private final FlowSession otherPartySession;

        public Acceptor(FlowSession otherPartySession) {
            this.otherPartySession = otherPartySession;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            class SignTxFlow extends SignTransactionFlow {
                private SignTxFlow(FlowSession otherPartyFlow, ProgressTracker progressTracker) {
                    super(otherPartyFlow, progressTracker);
                }

                @Override
                protected void checkTransaction(SignedTransaction stx) {
                }
            }
            final SignTxFlow signTxFlow = new SignTxFlow(otherPartySession, SignTransactionFlow.Companion.tracker());
            final SecureHash txId = subFlow(signTxFlow).getId();
            return subFlow(new ReceiveFinalityFlow(otherPartySession, txId));
        }
    }
}
