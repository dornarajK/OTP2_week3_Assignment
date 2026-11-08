package org.example.model;




public class Unit {

    private int unitId;
    private String unitKey;
    private String category;
    private String symbol;
    private String name;
    private String description;

    public Unit(int unitId, String unitKey, String category,
                String symbol, String name, String description) {

        this.unitId = unitId;
        this.unitKey = unitKey;
        this.category = category;
        this.symbol = symbol;
        this.name = name;
        this.description = description;
    }

    public int getUnitId() {
        return unitId;
    }

    public String getUnitKey() {
        return unitKey;
    }

    public String getCategory() {
        return category;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name + " (" + symbol + ")";
    }
}

