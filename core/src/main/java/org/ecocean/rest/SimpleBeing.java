package org.ecocean.rest;

import org.ecocean.Species;

public interface SimpleBeing {

    public abstract String getDisplayName();

    public abstract String getAvatar();

    public abstract Species getSpecies();
}