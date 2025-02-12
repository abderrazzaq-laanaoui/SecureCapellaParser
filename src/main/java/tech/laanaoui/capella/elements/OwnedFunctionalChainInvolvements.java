package tech.laanaoui.capella.elements;

import lombok.Getter;
import lombok.Setter;

@Getter
public class OwnedFunctionalChainInvolvements {
    private final String id;
    @Setter
    private String summary;
    @Setter
    private String function;
    private final String chain; // parent<ownedFunctionalChains>.name

    public OwnedFunctionalChainInvolvements(String id, String summary, String function, String chain) {
        this.id = id;
        this.summary = summary;
        this.function = function;
        this.chain = chain;
    }

    @Override
    public String toString() {
        return "function [" + function + "] : " + summary;
    }
}
