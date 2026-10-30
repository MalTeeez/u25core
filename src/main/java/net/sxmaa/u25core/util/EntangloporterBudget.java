package net.sxmaa.u25core.util;

/** Per-tick energy budgets of a Quantum Entangloporter, in J. */
public interface EntangloporterBudget {

    TransferBudget u25core$input();

    TransferBudget u25core$output();
}
