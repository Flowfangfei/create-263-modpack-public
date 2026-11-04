package com.railwayteam.railways.registry;

/** Adapted schedule entries implement IScheduleInput directly, including their client widgets. */
public final class CRScheduleClient {
    private CRScheduleClient() { }
    public static void register() {
        if (new com.railwayteam.railways.content.schedule.RedstoneLinkInstruction(CRSchedule.REDSTONE_LINK).slotsTargeted()!=2)
            throw new IllegalStateException("Railway redstone schedule editor has lost its frequency slots");
    }
}
