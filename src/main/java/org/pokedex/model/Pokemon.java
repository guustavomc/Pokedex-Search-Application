package org.pokedex.model;

import java.util.List;

public class Pokemon {

    private int id;
    private String name;
    private List<String> type;
    private String description;

    public Pokemon(int id, String name, List<String> type, String description){
        this.id=id;
        this.name=name;
        this.type=type;
        this.description=description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getType() {
        return type;
    }

    public void setType(List<String> type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String pokemonDescription(){
            return getName() + "(" + getId() + ") - Type" + getType() + ", " + getDescription();
    }
}
