package com.cbcatfix.rocket;

/** Synced damage state, separate from the CBC penetration mass. */
public interface RocketEngineAccess {
    boolean cbcatfix$isEngineDisabled();

    void cbcatfix$setEngineDisabled(boolean disabled);
}
