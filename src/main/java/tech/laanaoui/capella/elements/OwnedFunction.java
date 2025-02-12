package tech.laanaoui.capella.elements;


public record OwnedFunction(String id, String name) {

    @Override
    public String toString() {
        return "Function{id='" + id + "', name='" + name + "'}";
    }
}
